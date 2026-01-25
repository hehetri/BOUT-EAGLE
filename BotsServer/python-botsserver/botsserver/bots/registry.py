from __future__ import annotations

import asyncio
from dataclasses import dataclass, field
from typing import Dict


@dataclass(slots=True)
class BotState:
    bot_id: int
    bot_name: str
    online: bool = True
    room_id: int | None = None
    relay_id: int | None = None
    metadata: dict[str, str] = field(default_factory=dict)


class BotRegistry:
    def __init__(self) -> None:
        self._bots: Dict[int, BotState] = {}
        self._lock = asyncio.Lock()

    async def register(self, bot_id: int, bot_name: str) -> BotState:
        async with self._lock:
            state = BotState(bot_id=bot_id, bot_name=bot_name, online=True)
            self._bots[bot_id] = state
            return state

    async def set_offline(self, bot_id: int) -> None:
        async with self._lock:
            state = self._bots.get(bot_id)
            if state:
                state.online = False

    async def update_location(self, bot_id: int, room_id: int | None, relay_id: int | None) -> None:
        async with self._lock:
            state = self._bots.get(bot_id)
            if not state:
                return
            state.room_id = room_id
            state.relay_id = relay_id

    async def get(self, bot_id: int) -> BotState | None:
        async with self._lock:
            return self._bots.get(bot_id)

    async def snapshot(self) -> dict[int, BotState]:
        async with self._lock:
            return dict(self._bots)
