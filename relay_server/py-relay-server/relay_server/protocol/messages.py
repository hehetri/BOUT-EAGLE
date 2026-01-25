from __future__ import annotations

from dataclasses import dataclass
from enum import IntEnum

from pycommon.protocol.codec import ByteReader, ByteWriter, LITTLE_ENDIAN


class RelayCommand(IntEnum):
    ROUTE = 0x7001
    SUBSCRIBE = 0x7002
    UNSUBSCRIBE = 0x7003
    HEARTBEAT = 0x70FF


class RelayEvent(IntEnum):
    ROUTED = 0x7101
    ERROR = 0x71FF


@dataclass(slots=True)
class RouteMessage:
    room_id: int
    channel_id: int
    target: int
    payload: bytes


def parse_route(payload: bytes) -> RouteMessage:
    reader = ByteReader(payload, endian=LITTLE_ENDIAN)
    room_id = reader.read_u32()
    channel_id = reader.read_u16()
    target = reader.read_u8()
    size = reader.read_u16()
    body = reader.read_bytes(size)
    return RouteMessage(room_id=room_id, channel_id=channel_id, target=target, payload=body)


def parse_subscription(payload: bytes) -> tuple[int, int]:
    reader = ByteReader(payload, endian=LITTLE_ENDIAN)
    room_id = reader.read_u32()
    channel_id = reader.read_u16()
    return room_id, channel_id


def build_routed(room_id: int, channel_id: int, payload: bytes) -> bytes:
    writer = ByteWriter(endian=LITTLE_ENDIAN)
    writer.write_u32(room_id)
    writer.write_u16(channel_id)
    writer.write_u16(len(payload))
    writer.write_bytes(payload)
    return writer.to_bytes()


def build_error(code: int, message: str) -> bytes:
    writer = ByteWriter(endian=LITTLE_ENDIAN)
    writer.write_u16(code)
    msg = message.encode("utf-8")[:255]
    writer.write_u8(len(msg))
    writer.write_bytes(msg)
    return writer.to_bytes()
