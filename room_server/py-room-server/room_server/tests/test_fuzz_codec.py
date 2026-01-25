from __future__ import annotations

import pytest

from pycommon.protocol.codec import MixedEndianCodec
from pycommon.testing.fuzz import fuzz_codec_once


@pytest.mark.asyncio
async def test_fuzz_codec_roundtrip() -> None:
    codec = MixedEndianCodec(read_timeout=0.5)
    await fuzz_codec_once(codec, iterations=50)
