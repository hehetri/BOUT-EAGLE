from __future__ import annotations

import asyncio
from collections import defaultdict
from dataclasses import dataclass, field


@dataclass(slots=True)
class ConnectionState:
    writer: asyncio.StreamWriter
    queue: asyncio.Queue[bytes]
    queued_bytes: int = 0
    rooms: set[tuple[int, int]] = field(default_factory=set)


class RelayRouter:
    def __init__(self, per_connection_queue: int, per_connection_bytes: int, fanout_batch_size: int) -> None:
        self._per_queue = per_connection_queue
        self._per_bytes = per_connection_bytes
        self._fanout_batch = fanout_batch_size
        self._connections: dict[int, ConnectionState] = {}
        self._subs: dict[tuple[int, int], set[int]] = defaultdict(set)
        self._conn_seq = 0
        self._lock = asyncio.Lock()

    async def register(self, writer: asyncio.StreamWriter) -> int:
        async with self._lock:
            self._conn_seq += 1
            conn_id = self._conn_seq
            self._connections[conn_id] = ConnectionState(
                writer=writer,
                queue=asyncio.Queue(maxsize=self._per_queue),
            )
            return conn_id

    async def unregister(self, conn_id: int) -> None:
        async with self._lock:
            state = self._connections.pop(conn_id, None)
            if not state:
                return
            for key in list(state.rooms):
                self._subs[key].discard(conn_id)
            state.rooms.clear()

    async def subscribe(self, conn_id: int, room_id: int, channel_id: int) -> None:
        key = (room_id, channel_id)
        async with self._lock:
            state = self._connections.get(conn_id)
            if not state:
                return
            state.rooms.add(key)
            self._subs[key].add(conn_id)

    async def unsubscribe(self, conn_id: int, room_id: int, channel_id: int) -> None:
        key = (room_id, channel_id)
        async with self._lock:
            state = self._connections.get(conn_id)
            if not state:
                return
            state.rooms.discard(key)
            self._subs[key].discard(conn_id)

    async def fanout(self, room_id: int, channel_id: int, payload: bytes) -> int:
        key = (room_id, channel_id)
        async with self._lock:
            targets = list(self._subs.get(key, set()))
            states = [(cid, self._connections.get(cid)) for cid in targets]

        delivered = 0
        for start in range(0, len(states), self._fanout_batch):
            batch = states[start : start + self._fanout_batch]
            for cid, state in batch:
                if not state:
                    continue
                if state.queue.full():
                    continue
                projected = state.queued_bytes + len(payload)
                if projected > self._per_bytes:
                    continue
                state.queue.put_nowait(payload)
                state.queued_bytes = projected
                delivered += 1
        return delivered

    async def get_queue(self, conn_id: int) -> asyncio.Queue[bytes] | None:
        async with self._lock:
            state = self._connections.get(conn_id)
            return state.queue if state else None

    async def mark_sent(self, conn_id: int, size: int) -> None:
        async with self._lock:
            state = self._connections.get(conn_id)
            if not state:
                return
            state.queued_bytes = max(0, state.queued_bytes - size)
