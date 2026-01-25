from __future__ import annotations

import argparse
import asyncio
import os
import time

from pycommon.protocol.codec import MixedEndianCodec

from room_server.protocol.messages import RoomCommand


def build_join(room_id: int, user_id: int, slot: int) -> bytes:
    payload = room_id.to_bytes(4, "little") + user_id.to_bytes(4, "little") + slot.to_bytes(1, "little")
    return MixedEndianCodec.frame(RoomCommand.JOIN, payload)


def build_chat(room_id: int, user_id: int, message: str) -> bytes:
    msg = message.encode("utf-8")
    payload = (
        room_id.to_bytes(4, "little")
        + user_id.to_bytes(4, "little")
        + len(msg).to_bytes(2, "little")
        + msg
    )
    return MixedEndianCodec.frame(RoomCommand.CHAT, payload)


async def worker(host: str, port: int, room_id: int, user_id: int, messages: int) -> None:
    reader, writer = await asyncio.open_connection(host, port)
    try:
        writer.write(build_join(room_id, user_id, slot=user_id % 8))
        await writer.drain()
        for _ in range(messages):
            writer.write(build_chat(room_id, user_id, os.urandom(8).hex()))
            await writer.drain()
        await asyncio.sleep(0.1)
        # drain a few responses to keep buffers bounded
        for _ in range(5):
            await reader.read(1024)
    finally:
        writer.close()
        await writer.wait_closed()


async def main_async(args: argparse.Namespace) -> None:
    start = time.perf_counter()
    tasks = []
    for i in range(args.clients):
        room_id = args.room + (i % args.rooms)
        user_id = args.user_base + i
        tasks.append(asyncio.create_task(worker(args.host, args.port, room_id, user_id, args.messages)))
    await asyncio.gather(*tasks)
    elapsed = time.perf_counter() - start
    total = args.clients * args.messages
    rps = total / elapsed if elapsed else 0.0
    print(f"sent={total} elapsed={elapsed:.3f}s rps={rps:.1f}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="RoomServer asyncio load tester")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=55300)
    parser.add_argument("--clients", type=int, default=50)
    parser.add_argument("--messages", type=int, default=20)
    parser.add_argument("--room", type=int, default=2000)
    parser.add_argument("--rooms", type=int, default=5)
    parser.add_argument("--user-base", type=int, default=10_000)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    asyncio.run(main_async(args))


if __name__ == "__main__":
    main()
