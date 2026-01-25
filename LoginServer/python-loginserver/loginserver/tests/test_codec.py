from __future__ import annotations

import asyncio

import pytest

from loginserver.protocol.codec import LoginPacketCodec


@pytest.mark.asyncio
async def test_codec_reads_packet_with_mixed_endianness() -> None:
    codec = LoginPacketCodec(read_timeout=1.0)
    reader = asyncio.StreamReader()
    payload = b"a" * 10
    header = (0xF82A).to_bytes(2, "big") + len(payload).to_bytes(2, "little")
    reader.feed_data(header + payload)
    reader.feed_eof()

    packet = await codec.read_packet(reader)
    assert packet is not None
    assert packet.command == 0xF82A
    assert packet.length == 10
    assert packet.payload == payload
