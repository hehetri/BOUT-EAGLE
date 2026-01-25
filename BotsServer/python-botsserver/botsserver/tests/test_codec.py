from __future__ import annotations

import asyncio

import pytest

from botsserver.protocol.codec import BotsPacketCodec


@pytest.mark.asyncio
async def test_codec_reads_packet() -> None:
    codec = BotsPacketCodec(read_timeout=1.0)
    reader = asyncio.StreamReader()
    payload = b"abc"
    header = (0xE02E).to_bytes(2, "big") + len(payload).to_bytes(2, "little")
    reader.feed_data(header + payload)
    reader.feed_eof()

    packet = await codec.read_packet(reader, max_packet_size=1024)
    assert packet is not None
    assert packet.command == 0xE02E
    assert packet.payload == payload
