# RoomServer (Python 3.12)

RoomServer gerencia estado por sala com modelo actor (fila por sala) para consistência e baixa latência.

## Rodando localmente

```bash
cd /workspace/BOUT-EAGLE/room_server/py-room-server
python3.12 -m venv .venv
source .venv/bin/activate
pip install -e .[dev]
cp .env.example .env
pytest
python -m room_server.server --config config/room_server.yaml
```

## Load tester asyncio

```bash
python tools/load_tester.py --clients 100 --messages 50
```
