from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import yaml
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class RateLimitConfig(BaseModel):
    max_attempts: int = 8
    window_seconds: int = 60
    block_seconds: int = 300


class DatabaseConfig(BaseModel):
    dsn: str = Field(
        default="postgresql://postgres:postgres@127.0.0.1:5432/bouteagle",
        description="Asyncpg-compatible PostgreSQL DSN",
    )
    min_size: int = 2
    max_size: int = 20
    command_timeout: float = 5.0


class ObservabilityConfig(BaseModel):
    json_logs: bool = True
    log_level: str = "INFO"
    prometheus_host: str = "0.0.0.0"
    prometheus_port: int = 9102
    enable_tracing: bool = False


class ProtocolConfig(BaseModel):
    listen_host: str = "0.0.0.0"
    listen_port: int = 53699
    read_timeout_seconds: float = 10.0
    write_timeout_seconds: float = 10.0
    use_crypto_table: bool = False


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_prefix="LOGIN_",
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    environment: str = "dev"
    protocol: ProtocolConfig = ProtocolConfig()
    database: DatabaseConfig = DatabaseConfig()
    rate_limit: RateLimitConfig = RateLimitConfig()
    observability: ObservabilityConfig = ObservabilityConfig()


@dataclass(slots=True)
class LoadedConfig:
    settings: Settings
    raw: dict[str, Any]
    path: Path | None


def _deep_merge(base: dict[str, Any], override: dict[str, Any]) -> dict[str, Any]:
    result = dict(base)
    for key, value in override.items():
        if isinstance(value, dict) and isinstance(result.get(key), dict):
            result[key] = _deep_merge(result[key], value)
        else:
            result[key] = value
    return result


def load_yaml_config(path: str | os.PathLike[str] | None) -> LoadedConfig:
    raw: dict[str, Any] = {}
    config_path: Path | None = None
    if path:
        config_path = Path(path)
        if config_path.exists():
            raw = yaml.safe_load(config_path.read_text(encoding="utf-8")) or {}
    env_settings = Settings()
    if raw:
        merged = _deep_merge(env_settings.model_dump(), raw)
        settings = Settings.model_validate(merged)
    else:
        settings = env_settings
    return LoadedConfig(settings=settings, raw=raw, path=config_path)
