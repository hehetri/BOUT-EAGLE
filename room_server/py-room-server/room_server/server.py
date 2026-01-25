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

from room_server.config import LoadedConfig, load_yaml_config
from room_server.protocol.messages import (
    RoomCommand,
    RoomEvent,
    build_broadcast,
    build_error,
    build_joined,
    build_left,
    build_state,
    parse_chat,
    parse_join,
    parse_leave,
)
from room_server.service.manager import RoomManager

LOGGER: Final[logging.Logger] = logging.getLogger("room_server")

ROOM_CONNECTIONS = Gauge("room_connections", "Active room connections")
ROOM_PACKETS = Counter("room_packets_total", "Room packets handled", labelnames=("command",))
ROOM_LATENCY = Histogram("room_latency_seconds", "Room command latency")
ROOM_BROADCAST = Counter("room_broadcast_total", "Room broadcasts", labelnames=("room",))


def _peer_ip(writer: asyncio.StreamWriter) -> str:
    peer = writer.get_extra_info("peername")
    if not peer:
        return "0.0.0.0"
    host, *_rest = peer
    return str(host)


class RoomTCPServer:
    def __init__(self, config: LoadedConfig) -> None:
        self._config = config
        proto = config.settings.protocol
        self._codec = MixedEndianCodec(read_timeout=proto.read_timeout_seconds, use_crypto_table=proto.use_crypto_table)
        self._manager = RoomManager(max_users=config.settings.room.max_users_per_room, queue_size=config.settings.room.room_queue_size)
        self._server: asyncio.base_events.Server | None = None
        self._stopping = asyncio.Event()
        self._connections: dict[int, asyncio.StreamWriter] = {}
        self._conn_seq = 0
        self._lock = asyncio.Lock()

    async def start(self) -> None:
        obs = self._config.settings.observability
        start_http_server(obs.prometheus_port, addr=obs.prometheus_host)
        proto = self._config.settings.protocol
        self._server = await asyncio.start_server(self._handle_client, host=proto.listen_host, port=proto.listen_port)
        sockets = ", ".join(str(sock.getsockname()) for sock in (self._server.sockets or []))
        LOGGER.info("room_server_started", extra={"sockets": sockets})

    async def stop(self) -> None:
        if self._server:
            self._server.close()
            await self._server.wait_closed()
        self._stopping.set()
        LOGGER.info("room_server_stopped")

    async def wait_stopped(self) -> None:
        await self._stopping.wait()

    async def _register(self, writer: asyncio.StreamWriter) -> int:
        async with self._lock:
            self._conn_seq += 1
            cid = self._conn_seq
            self._connections[cid] = writer
            return cid

    async def _unregister(self, cid: int) -> None:
        async with self._lock:
            self._connections.pop(cid, None)

    async def _handle_client(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter) -> None:
        ROOM_CONNECTIONS.inc()
        ip = _peer_ip(writer)
        cid = await self._register(writer)
        LOGGER.info("room_client_connected", extra={"ip": ip, "cid": cid})
        try:
            while True:
                packet = await self._codec.read_packet(reader, self._config.settings.protocol.max_packet_size)
                if not packet:
                    break
                ROOM_PACKETS.labels(command=f"0x{packet.command:04X}").inc()
                LOGGER.debug(
                    "room_packet_received",
                    extra={
                        "ip": ip,
                        "cid": cid,
                        "command": f"0x{packet.command:04X}",
                        "length": packet.length,
                        "hexdump": hexdump(packet.raw[: min(len(packet.raw), 128)]),
                    },
                )
                await self._dispatch(packet.command, packet.payload, writer)
        except Exception:
            LOGGER.exception("room_client_error", extra={"ip": ip, "cid": cid})
        finally:
            await self._unregister(cid)
            writer.close()
            with suppress(Exception):
                await writer.wait_closed()
            ROOM_CONNECTIONS.dec()
            LOGGER.info("room_client_disconnected", extra={"ip": ip, "cid": cid})

    async def _dispatch(self, command: int, payload: bytes, writer: asyncio.StreamWriter) -> None:
        with ROOM_LATENCY.time():
            if command == RoomCommand.JOIN:
                req = parse_join(payload)
                ok, reason, snap = await self._manager.join(req.room_id, req.user_id, req.slot)
                if not ok:
                    await self._send(writer, RoomEvent.ERROR, build_error(409, reason))
                    return
                await self._broadcast(req.room_id, RoomEvent.JOINED, build_joined(req.room_id, req.user_id, req.slot))
                await self._broadcast(req.room_id, RoomEvent.STATE, build_state(req.room_id, list(snap.users.keys())))
                return
            if command == RoomCommand.LEAVE:
                room_id, user_id = parse_leave(payload)
                snap = await self._manager.leave(room_id, user_id)
                await self._broadcast(room_id, RoomEvent.LEFT, build_left(room_id, user_id))
                await self._broadcast(room_id, RoomEvent.STATE, build_state(room_id, list(snap.users.keys())))
                return
            if command == RoomCommand.CHAT:
                msg = parse_chat(payload)
                await self._broadcast(msg.room_id, RoomEvent.BROADCAST, build_broadcast(msg.room_id, msg.user_id, msg.message))
                return
            if command == RoomCommand.HEARTBEAT:
                return

            await self._send(writer, RoomEvent.ERROR, build_error(404, "unknown room command"))

    async def _send(self, writer: asyncio.StreamWriter, command: int, payload: bytes) -> None:
        writer.write(
            MixedEndianCodec.frame(command, payload, use_crypto_table=self._config.settings.protocol.use_crypto_table)
        )
        await writer.drain()

    async def _broadcast(self, room_id: int, command: int, payload: bytes) -> None:
        framed = MixedEndianCodec.frame(command, payload, use_crypto_table=self._config.settings.protocol.use_crypto_table)
        async with self._lock:
            writers = list(self._connections.values())
        for writer in writers:
            writer.write(framed)
        await asyncio.gather(*(w.drain() for w in writers), return_exceptions=True)
        ROOM_BROADCAST.labels(room=str(room_id)).inc()


def _install_uvloop() -> None:
    try:
        import uvloop  # type: ignore

        uvloop.install()
    except Exception:
        LOGGER.warning("uvloop_unavailable")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="BOUT-EAGLE RoomServer (Python)")
    parser.add_argument(
        "--config",
        default="config/room_server.yaml",
        help="Path to YAML configuration (defaults to config/room_server.yaml)",
    )
    return parser.parse_args()


async def _run_server(config: LoadedConfig) -> None:
    server = RoomTCPServer(config)
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
