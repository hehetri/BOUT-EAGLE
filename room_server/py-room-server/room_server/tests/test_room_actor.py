from __future__ import annotations

import pytest

from room_server.service.manager import RoomManager


@pytest.mark.asyncio
async def test_join_leave_flow() -> None:
    manager = RoomManager(max_users=2, queue_size=16)
    ok, _reason, snap = await manager.join(room_id=1, user_id=10, slot=0)
    assert ok
    assert snap.host_id == 10
    ok2, reason2, _snap2 = await manager.join(room_id=1, user_id=11, slot=1)
    assert ok2
    ok3, reason3, _snap3 = await manager.join(room_id=1, user_id=12, slot=2)
    assert not ok3
    assert reason3 == "room full"
    snap_after = await manager.leave(room_id=1, user_id=10)
    assert snap_after.host_id in {11, None}
