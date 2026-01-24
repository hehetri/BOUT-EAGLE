from __future__ import annotations

import asyncio

from room_server.service.room_actor import RoomActor, RoomState


class RoomManager:
    def __init__(self, max_users: int, queue_size: int) -> None:
        self._max_users = max_users
        self._queue_size = queue_size
        self._rooms: dict[int, RoomActor] = {}
        self._lock = asyncio.Lock()

    async def get_or_create(self, room_id: int) -> RoomActor:
        async with self._lock:
            actor = self._rooms.get(room_id)
            if actor:
                return actor
            actor = RoomActor(room_id=room_id, max_users=self._max_users, queue_size=self._queue_size)
            await actor.start()
            self._rooms[room_id] = actor
            return actor

    async def join(self, room_id: int, user_id: int, slot: int) -> tuple[bool, str, RoomState]:
        actor = await self.get_or_create(room_id)
        ok, reason = await actor.call("join", user_id, slot)
        snap = await actor.call("snapshot")
        return ok, reason, snap

    async def leave(self, room_id: int, user_id: int) -> RoomState:
        actor = await self.get_or_create(room_id)
        await actor.call("leave", user_id)
        snap = await actor.call("snapshot")
        return snap

    async def snapshot(self, room_id: int) -> RoomState:
        actor = await self.get_or_create(room_id)
        return await actor.call("snapshot")
