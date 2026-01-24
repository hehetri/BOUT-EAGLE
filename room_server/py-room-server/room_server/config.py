from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import yaml
from pydantic import BaseModel
from pydantic_settings import BaseSettings, SettingsConfigDict


class ProtocolConfig(BaseModel):
    listen_host: str = "0.0.0.0"
    listen_port: int = 55300
    read_timeout_seconds: float = 10.0
    max_packet_size: int = 16384
    use_crypto_table: bool = False


class RoomConfig(BaseModel):
    max_rooms: int = 4096
    max_users_per_room: int = 8
    room_queue_size: int = 1024


class LimitsConfig(BaseModel):
    per_connection_queue: int = 256
    per_connection_bytes: int = 524288


class ObservabilityConfig(BaseModel):
    json_logs: bool = True
    log_level: str = "INFO"
    prometheus_host: str = "0.0.0.0"
    prometheus_port: int = 9132
    enable_tracing: bool = False


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_prefix="ROOM_",
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    environment: str = "dev"
    protocol: ProtocolConfig = ProtocolConfig()
    room: RoomConfig = RoomConfig()
    limits: LimitsConfig = LimitsConfig()
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
