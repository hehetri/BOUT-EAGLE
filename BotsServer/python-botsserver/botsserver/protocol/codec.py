from __future__ import annotations

import asyncio
import binascii
from dataclasses import dataclass
from typing import Final

from botsserver.security import crypto

LITTLE_ENDIAN: Final[str] = "little"
BIG_ENDIAN: Final[str] = "big"


class BufferUnderflowError(ValueError):
    """Raised when attempting to read beyond the available buffer."""


class ByteReader:
    def __init__(self, data: bytes, endian: str = LITTLE_ENDIAN) -> None:
        self._data = memoryview(data)
        self._offset = 0
        self._endian = endian

    @property
    def remaining(self) -> int:
        return len(self._data) - self._offset

    def _read(self, size: int) -> memoryview:
        if self._offset + size > len(self._data):
            raise BufferUnderflowError(
                f"requested={size} remaining={self.remaining} offset={self._offset}"
            )
        start = self._offset
        self._offset += size
        return self._data[start : start + size]

    def read_u8(self) -> int:
        return int.from_bytes(self._read(1), self._endian, signed=False)

    def read_u16(self) -> int:
        return int.from_bytes(self._read(2), self._endian, signed=False)

    def read_u32(self) -> int:
        return int.from_bytes(self._read(4), self._endian, signed=False)

    def read_bytes(self, size: int) -> bytes:
        return bytes(self._read(size))

    def read_cstring(self, size: int, encoding: str = "iso-8859-1") -> str:
        raw = self.read_bytes(size)
        terminator = raw.find(b"\x00")
        if terminator != -1:
            raw = raw[:terminator]
        return raw.decode(encoding, errors="ignore")


class ByteWriter:
    def __init__(self, endian: str = LITTLE_ENDIAN) -> None:
        self._buffer = bytearray()
        self._endian = endian

    def write_u8(self, value: int) -> None:
        self._buffer += int(value).to_bytes(1, self._endian, signed=False)

    def write_u16(self, value: int) -> None:
        self._buffer += int(value).to_bytes(2, self._endian, signed=False)

    def write_u32(self, value: int) -> None:
        self._buffer += int(value).to_bytes(4, self._endian, signed=False)

    def write_bytes(self, data: bytes) -> None:
        self._buffer += data

    def to_bytes(self) -> bytes:
        return bytes(self._buffer)


def hexdump(data: bytes) -> str:
    return binascii.hexlify(data, sep=b" ").decode("ascii")


@dataclass(slots=True)
class Packet:
    command: int
    length: int
    payload: bytes
    raw: bytes


class BotsPacketCodec:
    """
    Default framing inferred from other modules in the repo:
    - command: 2 bytes big-endian
    - length:  2 bytes little-endian payload length
    - payload: `length` bytes
    """

    HEADER_SIZE: Final[int] = 4
    TERMINATOR_COMMAND: Final[int] = 0xFFFF

    def __init__(self, read_timeout: float = 10.0, use_crypto_table: bool = False) -> None:
        self._read_timeout = read_timeout
        self._use_crypto_table = use_crypto_table

    async def read_packet(self, reader: asyncio.StreamReader, max_packet_size: int) -> Packet | None:
        try:
            header = await asyncio.wait_for(reader.readexactly(self.HEADER_SIZE), self._read_timeout)
        except (asyncio.IncompleteReadError, TimeoutError, asyncio.TimeoutError):
            return None

        if self._use_crypto_table:
            header = crypto.decrypt(header)

        command = int.from_bytes(header[:2], BIG_ENDIAN, signed=False)
        length = int.from_bytes(header[2:4], LITTLE_ENDIAN, signed=False)
        if command == self.TERMINATOR_COMMAND:
            return None
        if length > max_packet_size:
            raise ValueError(f"packet too large: {length} > {max_packet_size}")

        if length:
            try:
                payload = await asyncio.wait_for(reader.readexactly(length), self._read_timeout)
            except (asyncio.IncompleteReadError, TimeoutError, asyncio.TimeoutError):
                return None
        else:
            payload = b""

        if self._use_crypto_table and payload:
            payload = crypto.decrypt(payload)

        raw = header + payload
        return Packet(command=command, length=length, payload=payload, raw=raw)

    @staticmethod
    def frame(command: int, payload: bytes, use_crypto_table: bool = False) -> bytes:
        length = len(payload).to_bytes(2, LITTLE_ENDIAN, signed=False)
        framed = command.to_bytes(2, BIG_ENDIAN, signed=False) + length + payload
        if use_crypto_table:
            return crypto.encrypt(framed)
        return framed
