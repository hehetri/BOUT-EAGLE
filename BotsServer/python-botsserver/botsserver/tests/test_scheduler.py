from __future__ import annotations

import asyncio

import pytest

from botsserver.bots.scheduler import BotScheduler, ScheduledTask


@pytest.mark.asyncio
async def test_scheduler_runs_task() -> None:
    scheduler = BotScheduler(tick_interval_ms=50, max_queue_size=10, task_timeout_seconds=1)
    await scheduler.start()
    ran = asyncio.Event()

    async def _work() -> None:
        ran.set()

    await scheduler.enqueue(ScheduledTask(name="t", bot_id=1, callable=_work))
    await asyncio.wait_for(ran.wait(), timeout=1)
    await scheduler.stop()
