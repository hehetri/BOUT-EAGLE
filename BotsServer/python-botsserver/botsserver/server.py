from __future__ import annotations

import argparse
import asyncio
import json
import logging
import signal
from contextlib import suppress
from typing import Final

from prometheus_client import Counter, Gauge, Histogram, start_http_server
from pythonjsonlogger import jsonlogger

from botsserver.bots.plugins import PluginManager
from botsserver.bots.registry import BotRegistry
from botsserver.bots.scheduler import BotScheduler, ScheduledTask
from botsserver.config import LoadedConfig, load_yaml_config
from botsserver.db.repo import BotsRepository
from botsserver.protocol.codec import BotsPacketCodec, hexdump
from botsserver.protocol.messages import (
    ClientCommand,
    ServerCommand,
    S2SCommand,
    build_bot_event,
    build_error,
    build_hello_ack,
    parse_bot_command,
    parse_bot_hello,
    parse_bot_task,
)
from botsserver.s2s.server import S2SServer, S2SServerConfig
from botsserver.security.session import SessionValidator

LOGGER: Final[logging.Logger] = logging.getLogger("botsserver")

BOT_CONNECTIONS = Gauge("bots_connections", "Active bot connections")
BOT_PACKETS = Counter("bots_packets_total", "Packets handled", labelnames=("command",))
BOT_LATENCY = Histogram("bots_command_latency_seconds", "Latency of bot command handling")


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


class BotsTCPServer:
    def __init__(self, config: LoadedConfig) -> None:
        self._config = config
        protocol_cfg = config.settings.protocol
        self._codec = BotsPacketCodec(
            read_timeout=protocol_cfg.read_timeout_seconds,
            use_crypto_table=protocol_cfg.use_crypto_table,
        )
        self._repo = BotsRepository(config.settings.database)
        self._registry = BotRegistry()
        self._scheduler = BotScheduler(
            tick_interval_ms=config.settings.scheduler.tick_interval_ms,
            max_queue_size=config.settings.scheduler.max_queue_size,
            task_timeout_seconds=config.settings.scheduler.task_timeout_seconds,
        )
        self._plugins = PluginManager(
            directory=config.settings.plugins.directory,
            hot_reload=config.settings.plugins.hot_reload,
            default_timeout_seconds=config.settings.plugins.default_timeout_seconds,
        )
        self._session_validator = SessionValidator()
        self._server: asyncio.base_events.Server | None = None
        self._s2s_server: S2SServer | None = None
        self._stopping = asyncio.Event()

    async def start(self) -> None:
        await self._repo.start()
        await self._scheduler.start()
        await self._plugins.start()

        obs = self._config.settings.observability
        start_http_server(obs.prometheus_port, addr=obs.prometheus_host)

        protocol_cfg = self._config.settings.protocol
        self._server = await asyncio.start_server(
            self._handle_client,
            host=protocol_cfg.listen_host,
            port=protocol_cfg.listen_port,
        )

        if self._config.settings.s2s.enabled:
            s2s_cfg = self._config.settings.s2s
            self._s2s_server = S2SServer(
                S2SServerConfig(
                    host=s2s_cfg.listen_host,
                    port=s2s_cfg.listen_port,
                    shared_secret=s2s_cfg.shared_secret,
                    version=s2s_cfg.version,
                    read_timeout=protocol_cfg.read_timeout_seconds,
                    max_packet_size=protocol_cfg.max_packet_size,
                ),
                handler=self._handle_s2s_packet,
            )
            await self._s2s_server.start()

        sockets = ", ".join(str(sock.getsockname()) for sock in (self._server.sockets or []))
        LOGGER.info("botsserver_started", extra={"sockets": sockets})

    async def stop(self) -> None:
        if self._server:
            self._server.close()
            await self._server.wait_closed()
        if self._s2s_server:
            await self._s2s_server.stop()
        await self._scheduler.stop()
        await self._repo.stop()
        self._stopping.set()
        LOGGER.info("botsserver_stopped")

    async def wait_stopped(self) -> None:
        await self._stopping.wait()

    async def _handle_client(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter) -> None:
        BOT_CONNECTIONS.inc()
        ip = _peer_ip(writer)
        LOGGER.info("bot_client_connected", extra={"ip": ip})
        try:
            while True:
                packet = await self._codec.read_packet(reader, self._config.settings.protocol.max_packet_size)
                if not packet:
                    break
                BOT_PACKETS.labels(command=f"0x{packet.command:04X}").inc()
                LOGGER.debug(
                    "bot_packet_received",
                    extra={
                        "ip": ip,
                        "command": f"0x{packet.command:04X}",
                        "length": packet.length,
                        "hexdump": hexdump(packet.raw[: min(len(packet.raw), 128)]),
                    },
                )
                await self._dispatch(packet, writer)
        except Exception:
            LOGGER.exception("bot_client_error", extra={"ip": ip})
        finally:
            writer.close()
            with suppress(Exception):
                await writer.wait_closed()
            BOT_CONNECTIONS.dec()
            LOGGER.info("bot_client_disconnected", extra={"ip": ip})

    async def _dispatch(self, packet, writer: asyncio.StreamWriter) -> None:
        with BOT_LATENCY.time():
            if packet.command == ClientCommand.BOT_HELLO:
                hello = parse_bot_hello(packet.payload)
                if not self._session_validator.validate(hello.session_ticket):
                    await self._send(writer, ServerCommand.BOT_ERROR, build_error(401, "invalid session"))
                    return
                await self._registry.register(hello.bot_id, hello.bot_name)
                await self._send(writer, ServerCommand.BOT_HELLO_ACK, build_hello_ack(hello.bot_id, True))
                return

            if packet.command == ClientCommand.BOT_COMMAND:
                req = parse_bot_command(packet.payload)
                await self._handle_command(req.bot_id, req.command, req.argument, writer)
                return

            if packet.command == ClientCommand.BOT_TASK:
                req = parse_bot_task(packet.payload)
                await self._handle_task(req.bot_id, req.task_name, req.payload, writer)
                return

            await self._send(writer, ServerCommand.BOT_ERROR, build_error(404, "unknown command"))

    async def _handle_command(self, bot_id: int, command: str, argument: str, writer: asyncio.StreamWriter) -> None:
        plugin_name, _, plugin_command = command.partition(":")
        plugin_name = plugin_name or "default"
        plugin_command = plugin_command or command

        async def _task() -> None:
            try:
                payload = await self._plugins.dispatch(plugin_name, bot_id, plugin_command, argument)
                await self._repo.record_bot_event(bot_id, plugin_command, payload)
                await self._send(writer, ServerCommand.BOT_EVENT, build_bot_event(bot_id, plugin_command, payload))
            except KeyError:
                await self._send(writer, ServerCommand.BOT_ERROR, build_error(404, "plugin not found"))
            except asyncio.TimeoutError:
                await self._send(writer, ServerCommand.BOT_ERROR, build_error(408, "plugin timeout"))

        await self._scheduler.enqueue(ScheduledTask(name=plugin_command, bot_id=bot_id, callable=_task))

    async def _handle_task(self, bot_id: int, task_name: str, payload: bytes, writer: asyncio.StreamWriter) -> None:
        async def _task() -> None:
            await self._repo.record_bot_event(bot_id, task_name, payload)
            await self._send(writer, ServerCommand.BOT_EVENT, build_bot_event(bot_id, task_name, payload))

        await self._scheduler.enqueue(ScheduledTask(name=task_name, bot_id=bot_id, callable=_task))

    async def _send(self, writer: asyncio.StreamWriter, command: int, payload: bytes) -> None:
        data = BotsPacketCodec.frame(command, payload, use_crypto_table=self._config.settings.protocol.use_crypto_table)
        if self._config.settings.protocol.use_crypto_table:
            # frame() already encrypts the full packet; keep behaviour explicit.
            pass
        writer.write(data)
        await writer.drain()

    async def _handle_s2s_packet(self, packet) -> bytes | None:
        if packet.command == S2SCommand.EVENT:
            try:
                data = json.loads(packet.payload.decode("utf-8"))
            except json.JSONDecodeError:
                return build_error(400, "invalid json")
            bot_id = int(data.get("bot_id", 0))
            event_name = str(data.get("event", "event"))
            payload = bytes.fromhex(str(data.get("payload_hex", ""))) if data.get("payload_hex") else b""
            await self._repo.record_bot_event(bot_id, event_name, payload)
            return b"ok"
        return build_error(404, "unknown s2s command")


def _install_uvloop() -> None:
    try:
        import uvloop  # type: ignore

        uvloop.install()
    except Exception:
        LOGGER.warning("uvloop_unavailable")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="BOUT-EAGLE BotsServer (Python)")
    parser.add_argument(
        "--config",
        default="config/botsserver.yaml",
        help="Path to YAML configuration (defaults to config/botsserver.yaml)",
    )
    return parser.parse_args()


async def _run_server(config: LoadedConfig) -> None:
    server = BotsTCPServer(config)
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
