from __future__ import annotations

import asyncio
import hashlib
import hmac
import json
from dataclasses import dataclass
from typing import Awaitable, Callable

from botsserver.protocol.codec import BotsPacketCodec, Packet
from botsserver.protocol.messages import S2SCommand, S2S_MAGIC

S2SHandler = Callable[[Packet], Awaitable[bytes | None]]


@dataclass(slots=True)
class S2SServerConfig:
    host: str
    port: int
    shared_secret: str
    version: int
    read_timeout: float
    max_packet_size: int


class S2SServer:
    def __init__(self, cfg: S2SServerConfig, handler: S2SHandler) -> None:
        self._cfg = cfg
        self._handler = handler
        self._codec = BotsPacketCodec(read_timeout=cfg.read_timeout)
        self._server: asyncio.base_events.Server | None = None

    async def start(self) -> None:
        self._server = await asyncio.start_server(self._handle_client, host=self._cfg.host, port=self._cfg.port)

    async def stop(self) -> None:
        if self._server:
            self._server.close()
            await self._server.wait_closed()
            self._server = None

    def _expected_proof(self) -> bytes:
        payload = f"{S2S_MAGIC}:{self._cfg.version}".encode("utf-8")
        return hmac.new(self._cfg.shared_secret.encode("utf-8"), payload, hashlib.sha256).digest()[:16]

    async def _handle_client(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter) -> None:
        authed = False
        proof = self._expected_proof()
        try:
            while True:
                packet = await self._codec.read_packet(reader, self._cfg.max_packet_size)
                if not packet:
                    break
                if not authed:
                    authed = self._handle_auth(packet, proof, writer)
                    if not authed:
                        break
                    continue
                response = await self._handler(packet)
                if response is not None:
                    writer.write(BotsPacketCodec.frame(packet.command, response))
                    await writer.drain()
        finally:
            writer.close()
            await writer.wait_closed()

    def _handle_auth(self, packet: Packet, proof: bytes, writer: asyncio.StreamWriter) -> bool:
        if packet.command != S2SCommand.AUTH:
            writer.close()
            return False
        try:
            data = json.loads(packet.payload.decode("utf-8"))
            received = bytes.fromhex(data.get("proof", ""))
        except Exception:
            writer.close()
            return False
        if not hmac.compare_digest(received, proof):
            writer.close()
            return False
        writer.write(BotsPacketCodec.frame(S2SCommand.AUTH_OK, b"ok"))
        return True
