from __future__ import annotations

from dataclasses import dataclass
from enum import IntEnum
from typing import Final

from .codec import ByteReader, ByteWriter, LITTLE_ENDIAN


class ClientCommand(IntEnum):
    BOT_HELLO = 0xE02E
    BOT_COMMAND = 0xE030
    BOT_TASK = 0xE031


class ServerCommand(IntEnum):
    BOT_HELLO_ACK = 0xE12E
    BOT_EVENT = 0xE140
    BOT_ERROR = 0xE1FF


class S2SCommand(IntEnum):
    AUTH = 0x9001
    AUTH_OK = 0x9002
    EVENT = 0x9010
    TASK = 0x9011


S2S_MAGIC: Final[int] = 0xB07A


@dataclass(slots=True)
class BotHello:
    bot_id: int
    bot_name: str
    session_ticket: bytes


@dataclass(slots=True)
class BotCommandRequest:
    bot_id: int
    command: str
    argument: str


@dataclass(slots=True)
class BotTaskRequest:
    bot_id: int
    task_name: str
    payload: bytes


def parse_bot_hello(payload: bytes) -> BotHello:
    reader = ByteReader(payload, endian=LITTLE_ENDIAN)
    bot_id = reader.read_u32()
    name_len = reader.read_u16()
    bot_name = reader.read_cstring(name_len)
    ticket_len = reader.read_u16()
    session_ticket = reader.read_bytes(ticket_len)
    return BotHello(bot_id=bot_id, bot_name=bot_name, session_ticket=session_ticket)


def parse_bot_command(payload: bytes) -> BotCommandRequest:
    reader = ByteReader(payload, endian=LITTLE_ENDIAN)
    bot_id = reader.read_u32()
    command_len = reader.read_u16()
    command = reader.read_cstring(command_len)
    argument_len = reader.read_u16()
    argument = reader.read_cstring(argument_len)
    return BotCommandRequest(bot_id=bot_id, command=command, argument=argument)


def parse_bot_task(payload: bytes) -> BotTaskRequest:
    reader = ByteReader(payload, endian=LITTLE_ENDIAN)
    bot_id = reader.read_u32()
    task_len = reader.read_u16()
    task_name = reader.read_cstring(task_len)
    payload_len = reader.read_u16()
    task_payload = reader.read_bytes(payload_len)
    return BotTaskRequest(bot_id=bot_id, task_name=task_name, payload=task_payload)


def build_hello_ack(bot_id: int, accepted: bool) -> bytes:
    writer = ByteWriter(endian=LITTLE_ENDIAN)
    writer.write_u32(bot_id)
    writer.write_u8(1 if accepted else 0)
    return writer.to_bytes()


def build_bot_event(bot_id: int, event_name: str, event_payload: bytes) -> bytes:
    writer = ByteWriter(endian=LITTLE_ENDIAN)
    writer.write_u32(bot_id)
    name_bytes = event_name.encode("utf-8")
    writer.write_u16(len(name_bytes))
    writer.write_bytes(name_bytes)
    writer.write_u16(len(event_payload))
    writer.write_bytes(event_payload)
    return writer.to_bytes()


def build_error(code: int, message: str) -> bytes:
    writer = ByteWriter(endian=LITTLE_ENDIAN)
    writer.write_u16(code)
    msg_bytes = message.encode("utf-8")[:255]
    writer.write_u8(len(msg_bytes))
    writer.write_bytes(msg_bytes)
    return writer.to_bytes()
