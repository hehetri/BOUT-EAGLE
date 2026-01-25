from __future__ import annotations

import argparse
import asyncio
import logging
import signal
from contextlib import suppress
from typing import Final

from prometheus_client import Counter, Histogram, start_http_server
from pythonjsonlogger import jsonlogger

from loginserver.auth.service import AuthService
from loginserver.config import LoadedConfig, load_yaml_config
from loginserver.db.repo import LoginRepository
from loginserver.protocol.codec import LoginPacketCodec, hexdump
from loginserver.protocol.messages import (
    LOGIN_HEADER,
    LoginCommand,
    LoginResult,
    build_login_response,
    parse_login_request,
)
from loginserver.security import crypto
from loginserver.security.rate_limit import LoginRateLimiter

LOGGER: Final[logging.Logger] = logging.getLogger("loginserver")

LOGIN_ATTEMPTS = Counter(
    "login_attempts_total",
    "Total number of login attempts processed",
    labelnames=("result",),
)
LOGIN_LATENCY = Histogram(
    "login_attempt_latency_seconds",
    "Latency of login handling in seconds",
)
ACTIVE_CONNECTIONS = Counter(
    "login_active_connections_total",
    "Number of accepted connections",
)


def configure_logging(json_logs: bool, level: str) -> None:
    root = logging.getLogger()
    root.handlers.clear()
    root.setLevel(level.upper())
    handler = logging.StreamHandler()
    if json_logs:
        formatter = jsonlogger.JsonFormatter("%(asctime)s %(levelname)s %(name)s %(message)s")
    else:
        formatter = logging.Formatter("%(asctime)s %(levelname)s %(name)s %(message)s")
    handler.setFormatter(formatter)
    root.addHandler(handler)


def _peer_ip(writer: asyncio.StreamWriter) -> str:
    peer = writer.get_extra_info("peername")
    if not peer:
        return "0.0.0.0"
    host, *_rest = peer
    return str(host)


class LoginTCPServer:
    def __init__(self, config: LoadedConfig) -> None:
        self._config = config
        self._codec = LoginPacketCodec(
            read_timeout=config.settings.protocol.read_timeout_seconds,
            use_crypto_table=config.settings.protocol.use_crypto_table,
        )
        self._repo = LoginRepository(config.settings.database)
        self._rate_limiter = LoginRateLimiter(config.settings.rate_limit)
        self._auth = AuthService(repo=self._repo, rate_limiter=self._rate_limiter)
        self._server: asyncio.base_events.Server | None = None
        self._stopping = asyncio.Event()

    async def start(self) -> None:
        await self._repo.start()
        obs = self._config.settings.observability
        start_http_server(obs.prometheus_port, addr=obs.prometheus_host)
        protocol = self._config.settings.protocol
        self._server = await asyncio.start_server(
            self._handle_client,
            host=protocol.listen_host,
            port=protocol.listen_port,
            start_serving=True,
        )
        sockets = ", ".join(str(sock.getsockname()) for sock in (self._server.sockets or []))
        LOGGER.info("loginserver_started", extra={"sockets": sockets})

    async def stop(self) -> None:
        if self._server:
            self._server.close()
            await self._server.wait_closed()
        await self._repo.stop()
        self._stopping.set()
        LOGGER.info("loginserver_stopped")

    async def wait_stopped(self) -> None:
        await self._stopping.wait()

    async def _handle_client(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter) -> None:
        ACTIVE_CONNECTIONS.inc()
        ip = _peer_ip(writer)
        LOGGER.info("client_connected", extra={"ip": ip})

        if await self._repo.is_ip_banned(ip):
            LOGGER.warning("ip_banned", extra={"ip": ip})
            writer.close()
            with suppress(Exception):
                await writer.wait_closed()
            return

        try:
            while True:
                packet = await self._codec.read_packet(reader)
                if not packet:
                    break
                LOGGER.debug(
                    "packet_received",
                    extra={
                        "ip": ip,
                        "command": f"0x{packet.command:04X}",
                        "length": packet.length,
                        "hexdump": hexdump(packet.raw[: min(len(packet.raw), 128)]),
                    },
                )
                if packet.command != LoginCommand.LOGIN_REQUEST:
                    continue
                await self._handle_login(packet.raw, ip, writer)
                # Java closes on success; we mirror that behaviour.
                if writer.is_closing():
                    break
        except Exception:
            LOGGER.exception("client_handler_error", extra={"ip": ip})
        finally:
            writer.close()
            with suppress(Exception):
                await writer.wait_closed()
            LOGGER.info("client_disconnected", extra={"ip": ip})

    async def _handle_login(self, packet_raw: bytes, ip: str, writer: asyncio.StreamWriter) -> None:
        with LOGIN_LATENCY.time():
            request = parse_login_request(packet_raw)
            decision = await self._auth.authenticate(request=request, ip=ip)
            response = build_login_response(decision.result)
            header = LOGIN_HEADER
            payload = response.payload
            if self._config.settings.protocol.use_crypto_table:
                header = crypto.encrypt(header)
                payload = crypto.encrypt(payload)
            writer.write(header)
            await writer.drain()
            writer.write(payload)
            await writer.drain()
            LOGIN_ATTEMPTS.labels(result=decision.result.name.lower()).inc()
            LOGGER.info(
                "login_result",
                extra={
                    "ip": ip,
                    "username": request.username,
                    "result": decision.result.name,
                    "rate_limited_seconds": decision.rate_limited_seconds,
                    "session_ticket_issued": bool(decision.session_ticket),
                },
            )
            if decision.result == LoginResult.SUCCESS:
                writer.close()


def _install_uvloop() -> None:
    try:
        import uvloop  # type: ignore

        uvloop.install()
    except Exception:
        LOGGER.warning("uvloop_unavailable")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="BOUT-EAGLE LoginServer (Python)")
    parser.add_argument(
        "--config",
        default="config/loginserver.yaml",
        help="Path to YAML configuration (defaults to config/loginserver.yaml)",
    )
    return parser.parse_args()


async def _run_server(config: LoadedConfig) -> None:
    server = LoginTCPServer(config)
    await server.start()

    loop = asyncio.get_running_loop()
    stop_event = asyncio.Event()

    def _signal_handler() -> None:
        stop_event.set()

    for sig in (signal.SIGINT, signal.SIGTERM):
        with suppress(NotImplementedError):
            loop.add_signal_handler(sig, _signal_handler)

    await stop_event.wait()
    await server.stop()


def main() -> None:
    args = parse_args()
    config = load_yaml_config(args.config)
    configure_logging(config.settings.observability.json_logs, config.settings.observability.log_level)
    _install_uvloop()
    asyncio.run(_run_server(config))


if __name__ == "__main__":
    main()
