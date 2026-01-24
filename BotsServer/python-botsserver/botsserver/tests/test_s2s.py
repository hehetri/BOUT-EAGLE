from __future__ import annotations

import asyncio

import pytest

from botsserver.s2s.client import S2SClient, S2SClientConfig
from botsserver.s2s.server import S2SServer, S2SServerConfig
from botsserver.protocol.messages import S2SCommand


@pytest.mark.asyncio
async def test_s2s_auth_roundtrip() -> None:
    async def handler(packet):
        if packet.command == S2SCommand.EVENT:
            return b"ok"
        return b"noop"

    server = S2SServer(
        S2SServerConfig(
            host="127.0.0.1",
            port=54119,
            shared_secret="secret",
            version=1,
            read_timeout=1.0,
            max_packet_size=1024,
        ),
        handler=handler,
    )
    await server.start()

    client = S2SClient(
        S2SClientConfig(
            host="127.0.0.1",
            port=54119,
            shared_secret="secret",
            version=1,
            read_timeout=1.0,
            max_packet_size=1024,
        )
    )
    await client.connect()
    packet = await client.send(S2SCommand.EVENT, b"{}")
    assert packet is not None
    assert packet.payload == b"ok"
    await client.close()
    await server.stop()
