from __future__ import annotations

import asyncio
import hashlib
import hmac
import json
from dataclasses import dataclass

from botsserver.protocol.codec import BotsPacketCodec, Packet
from botsserver.protocol.messages import S2SCommand, S2S_MAGIC


@dataclass(slots=True)
class S2SClientConfig:
    host: str
    port: int
    shared_secret: str
    version: int
    read_timeout: float
    max_packet_size: int


class S2SClient:
    def __init__(self, cfg: S2SClientConfig) -> None:
        self._cfg = cfg
        self._codec = BotsPacketCodec(read_timeout=cfg.read_timeout)
        self._reader: asyncio.StreamReader | None = None
        self._writer: asyncio.StreamWriter | None = None

    async def connect(self) -> None:
        self._reader, self._writer = await asyncio.open_connection(self._cfg.host, self._cfg.port)
        await self._authenticate()

    async def close(self) -> None:
        if self._writer:
            self._writer.close()
            await self._writer.wait_closed()
        self._reader = None
        self._writer = None

    async def send(self, command: int, payload: bytes) -> Packet | None:
        if not self._reader or not self._writer:
            raise RuntimeError("s2s client not connected")
        self._writer.write(BotsPacketCodec.frame(command, payload))
        await self._writer.drain()
        return await self._codec.read_packet(self._reader, self._cfg.max_packet_size)

    async def _authenticate(self) -> None:
        if not self._writer or not self._reader:
            raise RuntimeError("connection not established")
        payload = f"{S2S_MAGIC}:{self._cfg.version}".encode("utf-8")
        proof = hmac.new(self._cfg.shared_secret.encode("utf-8"), payload, hashlib.sha256).digest()[:16]
        auth_payload = json.dumps({"proof": proof.hex(), "version": self._cfg.version}).encode("utf-8")
        self._writer.write(BotsPacketCodec.frame(S2SCommand.AUTH, auth_payload))
        await self._writer.drain()
        packet = await self._codec.read_packet(self._reader, self._cfg.max_packet_size)
        if not packet or packet.command != S2SCommand.AUTH_OK:
            raise RuntimeError("s2s authentication failed")
