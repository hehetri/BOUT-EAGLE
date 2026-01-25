from __future__ import annotations

import logging

from pythonjsonlogger import jsonlogger


def configure_logging(json_logs: bool, level: str) -> None:
    root = logging.getLogger()
    root.handlers.clear()
    root.setLevel(level.upper())
    handler = logging.StreamHandler()
    if json_logs:
        formatter = jsonlogger.JsonFormatter("%(asctime)s %(levelname)s %(name)s %(message)s")
    else:
        formatter = logging.Formatter("%(asctime)s %(levelname)s %(name)s %(message)s")
    handler.setFormatter(formatter)
    root.addHandler(handler)
