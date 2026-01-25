from __future__ import annotations

import asyncio
from dataclasses import dataclass
from typing import Awaitable, Callable

TaskCallable = Callable[[], Awaitable[None]]


@dataclass(slots=True)
class ScheduledTask:
    name: str
    bot_id: int
    callable: TaskCallable


class BotScheduler:
    def __init__(self, tick_interval_ms: int, max_queue_size: int, task_timeout_seconds: float) -> None:
        self._tick_interval = tick_interval_ms / 1000.0
        self._queue: asyncio.Queue[ScheduledTask] = asyncio.Queue(maxsize=max_queue_size)
        self._task_timeout = task_timeout_seconds
        self._worker: asyncio.Task[None] | None = None
        self._stopping = asyncio.Event()

    async def start(self) -> None:
        if self._worker:
            return
        self._worker = asyncio.create_task(self._run(), name="bot-scheduler")

    async def stop(self) -> None:
        self._stopping.set()
        if self._worker:
            await self._worker
            self._worker = None

    async def enqueue(self, task: ScheduledTask) -> None:
        await self._queue.put(task)

    async def _run(self) -> None:
        while not self._stopping.is_set():
            try:
                task = await asyncio.wait_for(self._queue.get(), timeout=self._tick_interval)
            except asyncio.TimeoutError:
                continue
            try:
                await asyncio.wait_for(task.callable(), timeout=self._task_timeout)
            except asyncio.TimeoutError:
                # timeout acts as sandbox safety; task is dropped.
                pass
            finally:
                self._queue.task_done()
