from __future__ import annotations

from loginserver.protocol.messages import (
    LOGIN_ALREADY_LOGGED_IN_PAYLOAD,
    LOGIN_BANNED_PAYLOAD,
    LOGIN_HEADER,
    LOGIN_INCORRECT_PASSWORD_PAYLOAD,
    LOGIN_INCORRECT_USERNAME_PAYLOAD,
    LOGIN_RESPONSE_SIZE,
    LOGIN_SUCCESS_PAYLOAD,
)


def test_golden_success_payload() -> None:
    assert len(LOGIN_HEADER) == 4
    assert len(LOGIN_SUCCESS_PAYLOAD) == LOGIN_RESPONSE_SIZE
    assert LOGIN_SUCCESS_PAYLOAD[:7] == bytes.fromhex("01 00 00 00 00 01 FF")


def test_golden_incorrect_username_payload() -> None:
    assert len(LOGIN_INCORRECT_USERNAME_PAYLOAD) == LOGIN_RESPONSE_SIZE
    assert LOGIN_INCORRECT_USERNAME_PAYLOAD[:7] == bytes.fromhex("01 00 02 00 00 00 FF")


def test_golden_incorrect_password_payload() -> None:
    assert len(LOGIN_INCORRECT_PASSWORD_PAYLOAD) == LOGIN_RESPONSE_SIZE
    assert LOGIN_INCORRECT_PASSWORD_PAYLOAD[:7] == bytes.fromhex("01 00 01 00 00 00 FF")


def test_golden_banned_payload() -> None:
    assert len(LOGIN_BANNED_PAYLOAD) == LOGIN_RESPONSE_SIZE
    assert LOGIN_BANNED_PAYLOAD[:7] == bytes.fromhex("01 00 03 00 00 00 FF")


def test_golden_already_logged_in_payload() -> None:
    assert len(LOGIN_ALREADY_LOGGED_IN_PAYLOAD) == LOGIN_RESPONSE_SIZE
    assert LOGIN_ALREADY_LOGGED_IN_PAYLOAD[:7] == bytes.fromhex("01 00 06 00 00 00 FF")
