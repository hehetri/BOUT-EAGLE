# RelayServer (Python 3.12)

RelayServer é responsável por:
- conexões persistentes
- roteamento por room/channel
- fanout eficiente com backpressure por conexão

## Rodando localmente

```bash
cd /workspace/BOUT-EAGLE/relay_server/py-relay-server
python3.12 -m venv .venv
source .venv/bin/activate
pip install -e .[dev]
cp .env.example .env
pytest
python -m relay_server.server --config config/relay_server.yaml
```

## Load tester asyncio

```bash
python tools/load_tester.py --clients 100 --messages 50
```
