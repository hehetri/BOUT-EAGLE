from __future__ import annotations

import asyncio
import importlib.util
from dataclasses import dataclass
from pathlib import Path
from types import ModuleType
from typing import Awaitable, Callable

PluginHandler = Callable[[int, str, str], Awaitable[bytes]]


@dataclass(slots=True)
class LoadedPlugin:
    name: str
    module: ModuleType
    handler: PluginHandler
    mtime: float


class PluginManager:
    def __init__(self, directory: str, hot_reload: bool, default_timeout_seconds: float) -> None:
        self._dir = Path(directory)
        self._hot_reload = hot_reload
        self._timeout = default_timeout_seconds
        self._plugins: dict[str, LoadedPlugin] = {}
        self._lock = asyncio.Lock()

    async def start(self) -> None:
        self._dir.mkdir(parents=True, exist_ok=True)
        await self._load_all()

    async def _load_all(self) -> None:
        async with self._lock:
            for path in self._dir.glob("*.py"):
                plugin = self._load_plugin(path)
                if plugin:
                    self._plugins[plugin.name] = plugin

    def _load_plugin(self, path: Path) -> LoadedPlugin | None:
        spec = importlib.util.spec_from_file_location(path.stem, path)
        if not spec or not spec.loader:
            return None
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        handler = getattr(module, "handle", None)
        if not handler:
            return None
        mtime = path.stat().st_mtime
        return LoadedPlugin(name=path.stem, module=module, handler=handler, mtime=mtime)

    async def maybe_reload(self) -> None:
        if not self._hot_reload:
            return
        async with self._lock:
            for path in self._dir.glob("*.py"):
                current = self._plugins.get(path.stem)
                mtime = path.stat().st_mtime
                if not current or mtime > current.mtime:
                    plugin = self._load_plugin(path)
                    if plugin:
                        self._plugins[path.stem] = plugin

    async def dispatch(self, plugin_name: str, bot_id: int, command: str, argument: str) -> bytes:
        await self.maybe_reload()
        plugin = self._plugins.get(plugin_name)
        if not plugin:
            raise KeyError(f"plugin not found: {plugin_name}")
        return await asyncio.wait_for(plugin.handler(bot_id, command, argument), timeout=self._timeout)
