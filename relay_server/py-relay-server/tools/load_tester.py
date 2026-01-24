from __future__ import annotations

import argparse
import asyncio
import os
import time

from pycommon.protocol.codec import MixedEndianCodec

from relay_server.protocol.messages import RelayCommand


def build_route(room_id: int, channel_id: int, payload: bytes) -> bytes:
    body = (
        room_id.to_bytes(4, "little")
        + channel_id.to_bytes(2, "little")
        + (0).to_bytes(1, "little")
        + len(payload).to_bytes(2, "little")
        + payload
    )
    return MixedEndianCodec.frame(RelayCommand.ROUTE, body)


async def worker(host: str, port: int, room_id: int, channel_id: int, messages: int) -> None:
    _reader, writer = await asyncio.open_connection(host, port)
    try:
        sub_payload = room_id.to_bytes(4, "little") + channel_id.to_bytes(2, "little")
        writer.write(MixedEndianCodec.frame(RelayCommand.SUBSCRIBE, sub_payload))
        await writer.drain()
        for _ in range(messages):
            payload = os.urandom(32)
            writer.write(build_route(room_id, channel_id, payload))
            await writer.drain()
        await asyncio.sleep(0.1)
    finally:
        writer.close()
        await writer.wait_closed()


async def main_async(args: argparse.Namespace) -> None:
    start = time.perf_counter()
    tasks = []
    for i in range(args.clients):
        room_id = args.room + (i % args.rooms)
        channel_id = args.channel
        tasks.append(asyncio.create_task(worker(args.host, args.port, room_id, channel_id, args.messages)))
    await asyncio.gather(*tasks)
    elapsed = time.perf_counter() - start
    total = args.clients * args.messages
    rps = total / elapsed if elapsed else 0.0
    print(f"sent={total} elapsed={elapsed:.3f}s rps={rps:.1f}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="RelayServer asyncio load tester")
    parser.add_argument("--host", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=55200)
    parser.add_argument("--clients", type=int, default=50)
    parser.add_argument("--messages", type=int, default=20)
    parser.add_argument("--room", type=int, default=1000)
    parser.add_argument("--rooms", type=int, default=5)
    parser.add_argument("--channel", type=int, default=1)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    asyncio.run(main_async(args))


if __name__ == "__main__":
    main()
