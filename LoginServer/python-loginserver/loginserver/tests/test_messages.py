from __future__ import annotations

from loginserver.protocol.messages import (
    LOGIN_HEADER,
    LOGIN_SUCCESS_PAYLOAD,
    LoginResult,
    build_login_response,
    parse_login_request,
)


def _build_login_packet(username: str, password: str) -> bytes:
    user_bytes = username.encode("iso-8859-1")[:23]
    pass_bytes = password.encode("iso-8859-1")[:32]
    user_field = user_bytes + b"\x00" * (23 - len(user_bytes))
    pass_field = pass_bytes + b"\x00" * (32 - len(pass_bytes))
    payload = user_field + pass_field
    length = len(payload).to_bytes(2, "little")
    command = (0xF82A).to_bytes(2, "big")
    return command + length + payload


def test_parse_login_request_strips_h_prefix() -> None:
    packet = _build_login_packet("Halice", "secret")
    req = parse_login_request(packet)
    assert req.username == "alice"
    assert req.password == "secret"


def test_build_login_response_wire_format() -> None:
    resp = build_login_response(LoginResult.SUCCESS)
    assert resp.to_wire().startswith(LOGIN_HEADER)
    assert resp.payload == LOGIN_SUCCESS_PAYLOAD
    assert len(resp.to_wire()) == 4 + 74
