# Python BotsServer (migração incremental)

Este serviço é um **scaffold compatível e versionável** para migrar o `BotsServer` Java para Python 3.12, mantendo o ecossistema existente funcionando enquanto convergimos o protocolo byte-a-byte.

> Status: sobe, tem testes e define interfaces claras, mas você ainda deve validar contra o Java real via sniff/replay.【F:BotsServer/python-botsserver/docs/protocol.md†L55-L87】

## Como rodar localmente

```bash
cd /workspace/BOUT-EAGLE/BotsServer/python-botsserver
python3.12 -m venv .venv
source .venv/bin/activate
pip install -e .[dev]
cp .env.example .env
pytest
python -m botsserver.server --config config/botsserver.yaml
```

## Portas padrão

- cliente/bot TCP: `54100`
- server-to-server TCP: `54110`
- métricas Prometheus: `9112`【F:BotsServer/python-botsserver/config/botsserver.yaml†L2-L10】【F:BotsServer/python-botsserver/config/botsserver.yaml†L24-L30】

## Interfaces explícitas (contratos)

- Cliente → BotsServer: framing binário + opcodes definidos em `protocol/messages.py`.【F:BotsServer/python-botsserver/botsserver/protocol/messages.py†L9-L27】
- BotsServer → DB: repositório assíncrono em `db/repo.py`.【F:BotsServer/python-botsserver/botsserver/db/repo.py†L16-L66】
- BotsServer → Login/session: `SessionValidator` validando session ticket binário (HMAC tail).【F:BotsServer/python-botsserver/botsserver/security/session.py†L8-L23】
- BotsServer ↔ Relay/Room (server-to-server): `s2s/server.py` e `s2s/client.py` com handshake HMAC versionado.【F:BotsServer/python-botsserver/botsserver/s2s/server.py†L27-L86】【F:BotsServer/python-botsserver/botsserver/s2s/client.py†L25-L57】

## Plugins / IA

Plugins são módulos Python em `bots/plugins/*.py` com uma função `handle(bot_id, command, argument)` async. O carregamento e hot reload são controlados por config.【F:BotsServer/python-botsserver/botsserver/bots/plugins.py†L12-L68】

## Próximo passo essencial

Me envie os handlers/opcodes do Java (principalmente `parsecmd(...)` e o `Packet.java`) para eu travar o protocolo exatamente igual ao legado.
