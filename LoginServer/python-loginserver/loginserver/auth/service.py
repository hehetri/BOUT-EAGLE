from __future__ import annotations

from dataclasses import dataclass
from typing import Protocol

from loginserver.db.repo import AccountRecord, LoginRepository
from loginserver.protocol.messages import LoginRequest, LoginResult
from loginserver.security.rate_limit import LoginRateLimiter


class PasswordVerifier(Protocol):
    def verify(self, provided: str, stored: str | None) -> bool: ...


class PlaintextPasswordVerifier:
    def verify(self, provided: str, stored: str | None) -> bool:
        if stored is None:
            return False
        return stored == provided


@dataclass(slots=True)
class AuthDecision:
    result: LoginResult
    account: AccountRecord | None
    session_ticket: bytes | None = None
    rate_limited_seconds: float | None = None


class AuthService:
    def __init__(
        self,
        repo: LoginRepository,
        rate_limiter: LoginRateLimiter,
        password_verifier: PasswordVerifier | None = None,
    ) -> None:
        self._repo = repo
        self._rate_limiter = rate_limiter
        self._password_verifier = password_verifier or PlaintextPasswordVerifier()

    async def authenticate(self, request: LoginRequest, ip: str) -> AuthDecision:
        allowed, retry_after = self._rate_limiter.check(ip=ip, username=request.username)
        if not allowed:
            return AuthDecision(
                result=LoginResult.INCORRECT_PASSWORD,
                account=None,
                rate_limited_seconds=retry_after,
            )

        await self._repo.clear_current_ip_for_ip(ip)
        account = await self._repo.get_account_by_username(request.username)
        if not account:
            self._rate_limiter.record_failure(ip=ip, username=request.username)
            return AuthDecision(result=LoginResult.INCORRECT_USERNAME, account=None)

        if not self._password_verifier.verify(request.password, account.password_hash):
            self._rate_limiter.record_failure(ip=ip, username=request.username)
            return AuthDecision(result=LoginResult.INCORRECT_PASSWORD, account=account)

        ban_status = await self._repo.evaluate_and_lift_ban_if_expired(account)
        if ban_status.is_banned:
            self._rate_limiter.record_failure(ip=ip, username=request.username)
            return AuthDecision(result=LoginResult.BANNED, account=account)

        if account.online == 1:
            self._rate_limiter.record_failure(ip=ip, username=request.username)
            return AuthDecision(result=LoginResult.ALREADY_LOGGED_IN, account=account)

        await self._repo.update_login_success(username=account.username, ip=ip)
        session_ticket = await self._repo.create_session_ticket(username=account.username, ip=ip)
        self._rate_limiter.record_success(ip=ip, username=request.username)
        return AuthDecision(result=LoginResult.SUCCESS, account=account, session_ticket=session_ticket)
