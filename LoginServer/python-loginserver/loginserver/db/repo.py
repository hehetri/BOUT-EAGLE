from __future__ import annotations

import base64
import hashlib
import hmac
import os
import secrets
from dataclasses import dataclass
from datetime import UTC, datetime, timedelta
import asyncpg

from loginserver.config import DatabaseConfig


@dataclass(slots=True)
class AccountRecord:
    account_id: int
    username: str
    password_hash: str | None
    banned: int
    bantime: int | None
    ban_start_time: datetime | None
    online: int
    logincount: int


@dataclass(slots=True)
class BanEvaluation:
    is_banned: bool
    remaining_seconds: int | None


class LoginRepository:
    def __init__(self, cfg: DatabaseConfig) -> None:
        self._cfg = cfg
        self._pool: asyncpg.Pool | None = None
        self._session_secret = os.environ.get("LOGIN_SESSION_SECRET", "change-me-session-secret").encode(
            "utf-8"
        )

    @property
    def pool(self) -> asyncpg.Pool:
        if not self._pool:
            raise RuntimeError("database pool not initialized")
        return self._pool

    async def start(self) -> None:
        self._pool = await asyncpg.create_pool(
            dsn=self._cfg.dsn,
            min_size=self._cfg.min_size,
            max_size=self._cfg.max_size,
            command_timeout=self._cfg.command_timeout,
        )

    async def stop(self) -> None:
        if self._pool:
            await self._pool.close()
            self._pool = None

    async def clear_current_ip_for_ip(self, ip: str) -> None:
        await self.pool.execute("UPDATE bout_users SET current_ip='' WHERE last_ip=$1", ip)

    async def get_account_by_username(self, username: str) -> AccountRecord | None:
        row = await self.pool.fetchrow(
            """
            SELECT id, username, password, banned, bantime, "banStime", online, logincount
            FROM bout_users
            WHERE username = $1
            LIMIT 1
            """,
            username,
        )
        if not row:
            return None
        ban_start_time = row["banStime"]
        if isinstance(ban_start_time, str):
            try:
                ban_start_time = datetime.fromisoformat(ban_start_time)
            except ValueError:
                ban_start_time = None
        if ban_start_time and ban_start_time.tzinfo is None:
            ban_start_time = ban_start_time.replace(tzinfo=UTC)
        return AccountRecord(
            account_id=row["id"],
            username=row["username"],
            password_hash=row["password"],
            banned=row["banned"] or 0,
            bantime=row["bantime"],
            ban_start_time=ban_start_time,
            online=row["online"] or 0,
            logincount=row["logincount"] or 0,
        )

    async def evaluate_and_lift_ban_if_expired(self, account: AccountRecord) -> BanEvaluation:
        if not account.banned:
            return BanEvaluation(is_banned=False, remaining_seconds=None)
        if account.bantime is None or account.ban_start_time is None:
            return BanEvaluation(is_banned=True, remaining_seconds=None)

        elapsed = int((datetime.now(tz=UTC) - account.ban_start_time).total_seconds())
        if elapsed > account.bantime:
            await self.pool.execute(
                """
                UPDATE bout_users
                SET banned = 0, bantime = 0, "banStime" = NULL
                WHERE username = $1
                """,
                account.username,
            )
            return BanEvaluation(is_banned=False, remaining_seconds=0)
        return BanEvaluation(is_banned=True, remaining_seconds=max(account.bantime - elapsed, 0))

    async def update_login_success(self, username: str, ip: str) -> None:
        row = await self.pool.fetchrow(
            "SELECT logincount FROM bout_users WHERE username = $1 LIMIT 1",
            username,
        )
        logincount = (row["logincount"] if row else 0) or 0
        logincount += 1
        now = datetime.now(tz=UTC)
        await self.pool.execute(
            """
            UPDATE bout_users
            SET current_ip = $1,
                logincount = $2,
                last_ip = $1,
                lastlogin = $3
            WHERE username = $4
            """,
            ip,
            logincount,
            now,
            username,
        )
        await self._record_ip_history(username=username, ip=ip, at=now)

    async def _record_ip_history(self, username: str, ip: str, at: datetime) -> None:
        await self.pool.execute(
            """
            INSERT INTO ip_history(username, ip_address, observed_at)
            VALUES ($1, $2, $3)
            ON CONFLICT DO NOTHING
            """,
            username,
            ip,
            at,
        )

    async def create_session_ticket(
        self, username: str, ip: str, ttl_seconds: int = 900
    ) -> bytes:
        issued_at = datetime.now(tz=UTC)
        expires_at = issued_at + timedelta(seconds=ttl_seconds)
        nonce = secrets.token_bytes(16)
        payload = (
            username.encode("utf-8")
            + b"|"
            + ip.encode("utf-8")
            + b"|"
            + int(issued_at.timestamp()).to_bytes(8, "big", signed=False)
            + nonce
        )
        mac = hmac.new(self._session_secret, payload, hashlib.sha256).digest()
        ticket = payload + mac[:16]
        ticket_id = base64.urlsafe_b64encode(hashlib.sha256(ticket).digest()[:12]).decode("ascii")
        await self.pool.execute(
            """
            INSERT INTO sessions(session_id, username, ip_address, issued_at, expires_at, ticket)
            VALUES ($1, $2, $3, $4, $5, $6)
            """,
            ticket_id,
            username,
            ip,
            issued_at,
            expires_at,
            ticket,
        )
        return ticket

    async def validate_session_ticket(self, ticket: bytes) -> bool:
        if len(ticket) < 16:
            return False
        payload, sig = ticket[:-16], ticket[-16:]
        expected = hmac.new(self._session_secret, payload, hashlib.sha256).digest()[:16]
        if not hmac.compare_digest(expected, sig):
            return False
        # best-effort DB validation
        row = await self.pool.fetchrow(
            "SELECT expires_at FROM sessions WHERE ticket = $1 LIMIT 1",
            ticket,
        )
        if not row:
            return False
        expires_at: datetime = row["expires_at"]
        if expires_at.tzinfo is None:
            expires_at = expires_at.replace(tzinfo=UTC)
        return expires_at >= datetime.now(tz=UTC)

    async def is_ip_banned(self, ip: str) -> bool:
        direct = await self.pool.fetchrow(
            "SELECT banned FROM ipbanned WHERE ip = $1 LIMIT 1",
            ip,
        )
        if direct and (direct["banned"] == 1):
            return True

        rows = await self.pool.fetch(
            "SELECT ip, banned FROM ipbanned WHERE banned = 1",
        )
        for row in rows:
            banned_ip: str = row["ip"]
            if banned_ip.endswith("*") and ip.startswith(banned_ip[:-1]):
                return True
        return False

    async def mark_account_online(self, username: str, online: int) -> None:
        await self.pool.execute(
            "UPDATE bout_users SET online = $1 WHERE username = $2",
            online,
            username,
        )
