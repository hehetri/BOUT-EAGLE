from __future__ import annotations

from dataclasses import dataclass
from datetime import UTC, datetime

import asyncpg

from botsserver.config import DatabaseConfig


@dataclass(slots=True)
class BotRecord:
    bot_id: int
    bot_name: str
    script: str | None
    updated_at: datetime | None


class BotsRepository:
    def __init__(self, cfg: DatabaseConfig) -> None:
        self._cfg = cfg
        self._pool: asyncpg.Pool | None = None

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

    async def fetch_bot(self, bot_id: int) -> BotRecord | None:
        row = await self.pool.fetchrow(
            """
            SELECT id, botname, script, updated_at
            FROM bots
            WHERE id = $1
            LIMIT 1
            """,
            bot_id,
        )
        if not row:
            return None
        updated_at: datetime | None = row.get("updated_at")
        if updated_at and updated_at.tzinfo is None:
            updated_at = updated_at.replace(tzinfo=UTC)
        return BotRecord(
            bot_id=row["id"],
            bot_name=row["botname"],
            script=row.get("script"),
            updated_at=updated_at,
        )

    async def record_bot_event(self, bot_id: int, event_name: str, payload: bytes) -> None:
        await self.pool.execute(
            """
            INSERT INTO bot_events(bot_id, event_name, payload, created_at)
            VALUES ($1, $2, $3, $4)
            """,
            bot_id,
            event_name,
            payload,
            datetime.now(tz=UTC),
        )
