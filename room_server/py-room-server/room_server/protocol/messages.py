from __future__ import annotations

from dataclasses import dataclass
from enum import IntEnum

from pycommon.protocol.codec import ByteReader, ByteWriter, LITTLE_ENDIAN


class RoomCommand(IntEnum):
    JOIN = 0x8001
    LEAVE = 0x8002
    CHAT = 0x8003
    UPDATE = 0x8004
    START = 0x8005
    END = 0x8006
    HEARTBEAT = 0x80FF


class RoomEvent(IntEnum):
    JOINED = 0x8101
    LEFT = 0x8102
    BROADCAST = 0x8103
    STATE = 0x8104
    ERROR = 0x81FF


@dataclass(slots=True)
class JoinRequest:
    room_id: int
    user_id: int
    slot: int


@dataclass(slots=True)
class ChatMessage:
    room_id: int
    user_id: int
    message: str


def parse_join(payload: bytes) -> JoinRequest:
    reader = ByteReader(payload, endian=LITTLE_ENDIAN)
    room_id = reader.read_u32()
    user_id = reader.read_u32()
    slot = reader.read_u8()
    return JoinRequest(room_id=room_id, user_id=user_id, slot=slot)


def parse_leave(payload: bytes) -> tuple[int, int]:
    reader = ByteReader(payload, endian=LITTLE_ENDIAN)
    room_id = reader.read_u32()
    user_id = reader.read_u32()
    return room_id, user_id


def parse_chat(payload: bytes) -> ChatMessage:
    reader = ByteReader(payload, endian=LITTLE_ENDIAN)
    room_id = reader.read_u32()
    user_id = reader.read_u32()
    msg_len = reader.read_u16()
    msg = reader.read_cstring(msg_len, encoding="utf-8")
    return ChatMessage(room_id=room_id, user_id=user_id, message=msg)


def build_joined(room_id: int, user_id: int, slot: int) -> bytes:
    writer = ByteWriter(endian=LITTLE_ENDIAN)
    writer.write_u32(room_id)
    writer.write_u32(user_id)
    writer.write_u8(slot)
    return writer.to_bytes()


def build_left(room_id: int, user_id: int) -> bytes:
    writer = ByteWriter(endian=LITTLE_ENDIAN)
    writer.write_u32(room_id)
    writer.write_u32(user_id)
    return writer.to_bytes()


def build_broadcast(room_id: int, user_id: int, message: str) -> bytes:
    writer = ByteWriter(endian=LITTLE_ENDIAN)
    writer.write_u32(room_id)
    writer.write_u32(user_id)
    msg_bytes = message.encode("utf-8")[:512]
    writer.write_u16(len(msg_bytes))
    writer.write_bytes(msg_bytes)
    return writer.to_bytes()


def build_state(room_id: int, users: list[int]) -> bytes:
    writer = ByteWriter(endian=LITTLE_ENDIAN)
    writer.write_u32(room_id)
    writer.write_u8(len(users))
    for uid in users:
        writer.write_u32(uid)
    return writer.to_bytes()


def build_error(code: int, message: str) -> bytes:
    writer = ByteWriter(endian=LITTLE_ENDIAN)
    writer.write_u16(code)
    msg = message.encode("utf-8")[:255]
    writer.write_u8(len(msg))
    writer.write_bytes(msg)
    return writer.to_bytes()
