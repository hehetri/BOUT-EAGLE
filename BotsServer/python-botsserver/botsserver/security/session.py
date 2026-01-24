from __future__ import annotations

import hashlib
import hmac
import os


class SessionValidator:
    """
    Minimal session-ticket validator placeholder.

    This does NOT assume JWT. It validates an HMAC tail compatible with the
    ticket shape used in the Python LoginServer scaffold.
    """

    def __init__(self, secret_env: str = "LOGIN_SESSION_SECRET") -> None:
        self._secret = os.environ.get(secret_env, "change-me-session-secret").encode("utf-8")

    def validate(self, ticket: bytes) -> bool:
        if len(ticket) < 16:
            return False
        payload, sig = ticket[:-16], ticket[-16:]
        expected = hmac.new(self._secret, payload, hashlib.sha256).digest()[:16]
        return hmac.compare_digest(expected, sig)
