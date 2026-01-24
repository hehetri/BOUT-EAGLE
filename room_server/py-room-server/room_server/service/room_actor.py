from __future__ import annotations

import asyncio
from dataclasses import dataclass, field


@dataclass(slots=True)
class RoomState:
    room_id: int
    host_id: int | None = None
    users: dict[int, int] = field(default_factory=dict)  # user_id -> slot
    ready: set[int] = field(default_factory=set)
    properties: dict[str, str] = field(default_factory=dict)


class RoomActor:
    def __init__(self, room_id: int, max_users: int, queue_size: int) -> None:
        self._state = RoomState(room_id=room_id)
        self._max_users = max_users
        self._queue: asyncio.Queue[tuple[str, tuple, asyncio.Future]] = asyncio.Queue(maxsize=queue_size)
        self._task: asyncio.Task[None] | None = None
        self._stopping = asyncio.Event()

    async def start(self) -> None:
        if not self._task:
            self._task = asyncio.create_task(self._run(), name=f"room-actor-{self._state.room_id}")

    async def stop(self) -> None:
        self._stopping.set()
        if self._task:
            await self._task
            self._task = None

    async def call(self, op: str, *args):
        fut: asyncio.Future = asyncio.get_running_loop().create_future()
        await self._queue.put((op, args, fut))
        return await fut

    async def _run(self) -> None:
        while not self._stopping.is_set():
            op, args, fut = await self._queue.get()
            try:
                result = self._dispatch(op, *args)
                if not fut.done():
                    fut.set_result(result)
            except Exception as exc:  # pragma: no cover - defensive
                if not fut.done():
                    fut.set_exception(exc)
            finally:
                self._queue.task_done()

    def _dispatch(self, op: str, *args):
        if op == "join":
            return self._join(*args)
        if op == "leave":
            return self._leave(*args)
        if op == "snapshot":
            return self.snapshot()
        raise ValueError(f"unknown room op: {op}")

    def _join(self, user_id: int, slot: int) -> tuple[bool, str]:
        if user_id in self._state.users:
            return True, "already joined"
        if len(self._state.users) >= self._max_users:
            return False, "room full"
        self._state.users[user_id] = slot
        if self._state.host_id is None:
            self._state.host_id = user_id
        return True, "joined"

    def _leave(self, user_id: int) -> None:
        self._state.users.pop(user_id, None)
        self._state.ready.discard(user_id)
        if self._state.host_id == user_id:
            self._state.host_id = next(iter(self._state.users), None)

    def snapshot(self) -> RoomState:
        return RoomState(
            room_id=self._state.room_id,
            host_id=self._state.host_id,
            users=dict(self._state.users),
            ready=set(self._state.ready),
            properties=dict(self._state.properties),
        )
