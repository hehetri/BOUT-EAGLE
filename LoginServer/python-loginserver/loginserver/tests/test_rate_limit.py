from __future__ import annotations

from loginserver.config import RateLimitConfig
from loginserver.security.rate_limit import LoginRateLimiter


def test_rate_limiter_blocks_after_threshold() -> None:
    limiter = LoginRateLimiter(RateLimitConfig(max_attempts=3, window_seconds=60, block_seconds=120))
    ip = "127.0.0.1"
    user = "tester"

    assert limiter.check(ip, user)[0]
    limiter.record_failure(ip, user)
    limiter.record_failure(ip, user)
    limiter.record_failure(ip, user)

    allowed, retry = limiter.check(ip, user)
    assert not allowed
    assert retry is not None and retry > 0


def test_rate_limiter_resets_on_success() -> None:
    limiter = LoginRateLimiter(RateLimitConfig(max_attempts=2, window_seconds=60, block_seconds=30))
    ip = "127.0.0.1"
    user = "tester"

    limiter.record_failure(ip, user)
    limiter.record_success(ip, user)
    assert limiter.check(ip, user)[0]
