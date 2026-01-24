from __future__ import annotations

import asyncio
import os
import random

from pycommon.protocol.codec import MixedEndianCodec


def random_packet(max_payload: int = 64) -> bytes:
    command = random.randint(0, 0xFFFE)
    size = random.randint(0, max_payload)
    payload = os.urandom(size)
    return MixedEndianCodec.frame(command, payload)


async def fuzz_codec_once(codec: MixedEndianCodec, iterations: int = 100) -> None:
    for _ in range(iterations):
        reader = asyncio.StreamReader()
        packet = random_packet()
        reader.feed_data(packet)
        reader.feed_eof()
        parsed = await codec.read_packet(reader, max_packet_size=4096)
        if parsed is None:
            raise AssertionError("codec returned None for fuzz packet")
