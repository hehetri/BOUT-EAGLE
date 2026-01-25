from __future__ import annotations

from dataclasses import dataclass
from enum import IntEnum
from typing import Final

from .codec import ByteReader, LITTLE_ENDIAN

LOGIN_HEADER: Final[bytes] = bytes.fromhex("EC 2C 4A 00")
LOGIN_RESPONSE_SIZE: Final[int] = 74


class LoginCommand(IntEnum):
    LOGIN_REQUEST = 0xF82A


class LoginResult(IntEnum):
    SUCCESS = 0
    INCORRECT_USERNAME = 1
    INCORRECT_PASSWORD = 2
    BANNED = 3
    ALREADY_LOGGED_IN = 4


LOGIN_SUCCESS_PAYLOAD: Final[bytes] = bytes.fromhex(
    "01 00 00 00 00 01 FF " + "00 " * (LOGIN_RESPONSE_SIZE - 7)
)
LOGIN_INCORRECT_USERNAME_PAYLOAD: Final[bytes] = bytes.fromhex(
    "01 00 02 00 00 00 FF " + "00 " * (LOGIN_RESPONSE_SIZE - 7)
)
LOGIN_INCORRECT_PASSWORD_PAYLOAD: Final[bytes] = bytes.fromhex(
    "01 00 01 00 00 00 FF " + "00 " * (LOGIN_RESPONSE_SIZE - 7)
)
LOGIN_BANNED_PAYLOAD: Final[bytes] = bytes.fromhex(
    "01 00 03 00 00 00 FF " + "00 " * (LOGIN_RESPONSE_SIZE - 7)
)
LOGIN_ALREADY_LOGGED_IN_PAYLOAD: Final[bytes] = bytes.fromhex(
    "01 00 06 00 00 00 FF " + "00 " * (LOGIN_RESPONSE_SIZE - 7)
)

LOGIN_RESULT_TO_PAYLOAD: Final[dict[LoginResult, bytes]] = {
    LoginResult.SUCCESS: LOGIN_SUCCESS_PAYLOAD,
    LoginResult.INCORRECT_USERNAME: LOGIN_INCORRECT_USERNAME_PAYLOAD,
    LoginResult.INCORRECT_PASSWORD: LOGIN_INCORRECT_PASSWORD_PAYLOAD,
    LoginResult.BANNED: LOGIN_BANNED_PAYLOAD,
    LoginResult.ALREADY_LOGGED_IN: LOGIN_ALREADY_LOGGED_IN_PAYLOAD,
}


@dataclass(slots=True)
class LoginRequest:
    username: str
    password: str
    username_raw: bytes
    password_raw: bytes


@dataclass(slots=True)
class LoginResponse:
    result: LoginResult
    payload: bytes

    def to_wire(self) -> bytes:
        return LOGIN_HEADER + self.payload


def _strip_h_prefix(username: str) -> str:
    return username[1:] if username.startswith("H") else username


def parse_login_request(packet_raw: bytes) -> LoginRequest:
    """
    Mirrors the Java parsing logic:
    - username occupies bytes [4:27) of the raw packet.
    - password occupies bytes [27:27+32).
    Both fields are null-terminated ISO-8859-1 strings.
    """

    if len(packet_raw) < 27:
        raise ValueError(f"packet too small for login request: {len(packet_raw)}")

    username_raw = packet_raw[4:27]
    password_raw = packet_raw[27 : 27 + 32]

    username_reader = ByteReader(username_raw, endian=LITTLE_ENDIAN)
    password_reader = ByteReader(password_raw, endian=LITTLE_ENDIAN)

    username = username_reader.read_cstring(len(username_raw))
    password = password_reader.read_cstring(len(password_raw))

    username = _strip_h_prefix(username)

    return LoginRequest(
        username=username,
        password=password,
        username_raw=username_raw,
        password_raw=password_raw,
    )


def build_login_response(result: LoginResult) -> LoginResponse:
    payload = LOGIN_RESULT_TO_PAYLOAD[result]
    if len(payload) != LOGIN_RESPONSE_SIZE:
        raise ValueError(f"login payload has unexpected size: {len(payload)}")
    return LoginResponse(result=result, payload=payload)
