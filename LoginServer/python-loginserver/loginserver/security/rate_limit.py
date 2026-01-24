from __future__ import annotations

import time
from collections import deque
from dataclasses import dataclass
from typing import Deque, Dict, Tuple

from loginserver.config import RateLimitConfig


@dataclass(slots=True)
class AttemptWindow:
    attempts: Deque[float]
    blocked_until: float | None = None


class LoginRateLimiter:
    """Simple sliding-window limiter with temporary blocking."""

    def __init__(self, cfg: RateLimitConfig) -> None:
        self._cfg = cfg
        self._store: Dict[Tuple[str, str], AttemptWindow] = {}

    def _now(self) -> float:
        return time.monotonic()

    def _prune(self, window: AttemptWindow, now: float) -> None:
        cutoff = now - self._cfg.window_seconds
        while window.attempts and window.attempts[0] < cutoff:
            window.attempts.popleft()
        if window.blocked_until and window.blocked_until <= now:
            window.blocked_until = None

    def check(self, ip: str, username: str) -> tuple[bool, float | None]:
        key = (ip, username.lower())
        now = self._now()
        window = self._store.setdefault(key, AttemptWindow(attempts=deque()))
        self._prune(window, now)
        if window.blocked_until:
            return False, max(window.blocked_until - now, 0.0)
        if len(window.attempts) >= self._cfg.max_attempts:
            window.blocked_until = now + self._cfg.block_seconds
            return False, float(self._cfg.block_seconds)
        return True, None

    def record_failure(self, ip: str, username: str) -> None:
        key = (ip, username.lower())
        now = self._now()
        window = self._store.setdefault(key, AttemptWindow(attempts=deque()))
        self._prune(window, now)
        window.attempts.append(now)
        if len(window.attempts) >= self._cfg.max_attempts:
            window.blocked_until = now + self._cfg.block_seconds

    def record_success(self, ip: str, username: str) -> None:
        key = (ip, username.lower())
        self._store.pop(key, None)
