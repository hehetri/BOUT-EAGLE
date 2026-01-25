from __future__ import annotations

import argparse
import asyncio
import logging
import signal
from contextlib import suppress
from typing import Final

from prometheus_client import Counter, Gauge, Histogram, start_http_server

from pycommon.observability.logging import configure_logging
from pycommon.protocol.codec import MixedEndianCodec, hexdump

from relay_server.config import LoadedConfig, load_yaml_config
from relay_server.protocol.messages import (
    RelayCommand,
    RelayEvent,
    build_error,
    build_routed,
    parse_route,
    parse_subscription,
)
from relay_server.service.router import RelayRouter

LOGGER: Final[logging.Logger] = logging.getLogger("relay_server")

RELAY_CONNECTIONS = Gauge("relay_connections", "Active relay connections")
RELAY_PACKETS = Counter("relay_packets_total", "Relay packets handled", labelnames=("command",))
RELAY_FANOUT = Counter("relay_fanout_total", "Fanout deliveries", labelnames=("room", "channel"))
RELAY_LATENCY = Histogram("relay_latency_seconds", "Relay command latency")


def _peer_ip(writer: asyncio.StreamWriter) -> str:
    peer = writer.get_extra_info("peername")
    if not peer:
        return "0.0.0.0"
    host, *_rest = peer
    return str(host)


class RelayTCPServer:
    def __init__(self, config: LoadedConfig) -> None:
        self._config = config
        proto = config.settings.protocol
        self._codec = MixedEndianCodec(read_timeout=proto.read_timeout_seconds, use_crypto_table=proto.use_crypto_table)
        self._router = RelayRouter(
            per_connection_queue=config.settings.limits.per_connection_queue,
            per_connection_bytes=config.settings.limits.per_connection_bytes,
            fanout_batch_size=config.settings.routing.fanout_batch_size,
        )
        self._server: asyncio.base_events.Server | None = None
        self._stopping = asyncio.Event()

    async def start(self) -> None:
        obs = self._config.settings.observability
        start_http_server(obs.prometheus_port, addr=obs.prometheus_host)
        proto = self._config.settings.protocol
        self._server = await asyncio.start_server(self._handle_client, host=proto.listen_host, port=proto.listen_port)
        sockets = ", ".join(str(sock.getsockname()) for sock in (self._server.sockets or []))
        LOGGER.info("relay_server_started", extra={"sockets": sockets})

    async def stop(self) -> None:
        if self._server:
            self._server.close()
            await self._server.wait_closed()
        self._stopping.set()
        LOGGER.info("relay_server_stopped")

    async def wait_stopped(self) -> None:
        await self._stopping.wait()

    async def _handle_client(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter) -> None:
        RELAY_CONNECTIONS.inc()
        ip = _peer_ip(writer)
        conn_id = await self._router.register(writer)
        queue = await self._router.get_queue(conn_id)
        writer_task = asyncio.create_task(self._drain_queue(conn_id, writer, queue), name=f"relay-writer-{conn_id}")
        LOGGER.info("relay_client_connected", extra={"ip": ip, "conn_id": conn_id})
        try:
            while True:
                packet = await self._codec.read_packet(reader, self._config.settings.protocol.max_packet_size)
                if not packet:
                    break
                RELAY_PACKETS.labels(command=f"0x{packet.command:04X}").inc()
                LOGGER.debug(
                    "relay_packet_received",
                    extra={
                        "ip": ip,
                        "conn_id": conn_id,
                        "command": f"0x{packet.command:04X}",
                        "length": packet.length,
                        "hexdump": hexdump(packet.raw[: min(len(packet.raw), 128)]),
                    },
                )
                await self._dispatch(conn_id, packet.command, packet.payload, writer)
        except Exception:
            LOGGER.exception("relay_client_error", extra={"ip": ip, "conn_id": conn_id})
        finally:
            writer_task.cancel()
            with suppress(Exception):
                await writer_task
            await self._router.unregister(conn_id)
            writer.close()
            with suppress(Exception):
                await writer.wait_closed()
            RELAY_CONNECTIONS.dec()
            LOGGER.info("relay_client_disconnected", extra={"ip": ip, "conn_id": conn_id})

    async def _drain_queue(
        self, conn_id: int, writer: asyncio.StreamWriter, queue: asyncio.Queue[bytes] | None
    ) -> None:
        if not queue:
            return
        while True:
            payload = await queue.get()
            try:
                writer.write(payload)
                await writer.drain()
            finally:
                queue.task_done()
                await self._router.mark_sent(conn_id, len(payload))

    async def _dispatch(self, conn_id: int, command: int, payload: bytes, writer: asyncio.StreamWriter) -> None:
        with RELAY_LATENCY.time():
            if command == RelayCommand.SUBSCRIBE:
                room_id, channel_id = parse_subscription(payload)
                await self._router.subscribe(conn_id, room_id, channel_id)
                return
            if command == RelayCommand.UNSUBSCRIBE:
                room_id, channel_id = parse_subscription(payload)
                await self._router.unsubscribe(conn_id, room_id, channel_id)
                return
            if command == RelayCommand.ROUTE:
                msg = parse_route(payload)
                framed = MixedEndianCodec.frame(
                    RelayEvent.ROUTED,
                    build_routed(msg.room_id, msg.channel_id, msg.payload),
                    use_crypto_table=self._config.settings.protocol.use_crypto_table,
                )
                delivered = await self._router.fanout(msg.room_id, msg.channel_id, framed)
                RELAY_FANOUT.labels(room=str(msg.room_id), channel=str(msg.channel_id)).inc(delivered)
                return
            if command == RelayCommand.HEARTBEAT:
                return

            err = MixedEndianCodec.frame(
                RelayEvent.ERROR,
                build_error(404, "unknown relay command"),
                use_crypto_table=self._config.settings.protocol.use_crypto_table,
            )
            writer.write(err)
            await writer.drain()


def _install_uvloop() -> None:
    try:
        import uvloop  # type: ignore

        uvloop.install()
    except Exception:
        LOGGER.warning("uvloop_unavailable")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="BOUT-EAGLE RelayServer (Python)")
    parser.add_argument(
        "--config",
        default="config/relay_server.yaml",
        help="Path to YAML configuration (defaults to config/relay_server.yaml)",
    )
    return parser.parse_args()


async def _run_server(config: LoadedConfig) -> None:
    server = RelayTCPServer(config)
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
