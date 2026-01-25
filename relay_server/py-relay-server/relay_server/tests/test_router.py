from __future__ import annotations

import asyncio

import pytest

from relay_server.service.router import RelayRouter


class DummyWriter:
    def __init__(self) -> None:
        self.buffer = bytearray()

    def write(self, data: bytes) -> None:
        self.buffer.extend(data)

    async def drain(self) -> None:
        return None


@pytest.mark.asyncio
async def test_router_fanout_respects_subscriptions() -> None:
    router = RelayRouter(per_connection_queue=4, per_connection_bytes=1024, fanout_batch_size=8)
    writer = DummyWriter()
    conn_id = await router.register(writer)
    await router.subscribe(conn_id, room_id=1, channel_id=2)
    payload = b"hello"
    delivered = await router.fanout(1, 2, payload)
    assert delivered == 1
    queue = await router.get_queue(conn_id)
    assert queue is not None
    queued = await queue.get()
    assert queued == payload
    await router.mark_sent(conn_id, len(payload))
    await router.unregister(conn_id)
