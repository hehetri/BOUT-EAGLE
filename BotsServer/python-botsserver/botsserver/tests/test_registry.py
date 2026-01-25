from __future__ import annotations

import pytest

from botsserver.bots.registry import BotRegistry


@pytest.mark.asyncio
async def test_registry_register_and_snapshot() -> None:
    registry = BotRegistry()
    state = await registry.register(1, "alpha")
    assert state.bot_name == "alpha"
    snap = await registry.snapshot()
    assert 1 in snap
    assert snap[1].online
