# Python LoginServer (incremental migration)

Status: **o servidor sobe e os testes passam**, mas a compatibilidade 100% depende de validar com o seu ambiente (DB real + cliente real + captura de tráfego).【F:LoginServer/python-loginserver/loginserver/server.py†L78-L91】【F:LoginServer/python-loginserver/loginserver/tests/test_golden.py†L13-L38】

Este módulo espelha o protocolo do `LoginServer` Java e adiciona preocupações de produção (async I/O, logs estruturados, métricas e configuração).

## Por que asyncio + uvloop

- O workload é I/O bound (TCP + banco).
- `asyncio.start_server` mantém o servidor pequeno e explícito.
- `uvloop` é drop-in e melhora latência em Linux quando disponível.【F:LoginServer/python-loginserver/loginserver/server.py†L172-L179】

## Como usar (local / dev rápido)

```bash
cd /workspace/BOUT-EAGLE/LoginServer/python-loginserver
python3.12 -m venv .venv
source .venv/bin/activate
pip install -e .[dev]
cp .env.example .env
pytest
python -m loginserver.server --config config/loginserver.yaml
```

O servidor TCP escuta por padrão em `0.0.0.0:53699` e expõe métricas Prometheus em `0.0.0.0:9102`.【F:LoginServer/python-loginserver/config/loginserver.yaml†L1-L18】【F:LoginServer/python-loginserver/loginserver/server.py†L78-L87】

## Como apontar para o seu banco

O código atual lê/escreve na tabela legada `bout_users` (compatibilidade com o Java).【F:LoginServer/python-loginserver/loginserver/db/repo.py†L63-L72】【F:LoginServer/python-loginserver/loginserver/db/repo.py†L121-L134】

1) Edite o `.env`:

```bash
LOGIN_DATABASE__DSN=postgresql://USER:PASS@HOST:5432/DB
```

2) (Opcional) aplique as migrações auxiliares:

```bash
psql "$LOGIN_DATABASE__DSN" -f loginserver/migrations/001_init.sql
```

> Observação: a migração cria tabelas novas (`accounts`, `sessions`, etc.), mas o login compatível com o legado depende principalmente da `bout_users`.【F:LoginServer/python-loginserver/loginserver/migrations/001_init.sql†L1-L12】【F:LoginServer/python-loginserver/loginserver/db/repo.py†L63-L72】

## Criptografia por tabela (compatibilidade com `Main.encrypt/decrypt`)

O Java tem tabelas de substituição de bytes. Aqui isso é **configurável** por flag:

- YAML: `protocol.use_crypto_table: true`
- ou `.env`: `LOGIN_PROTOCOL__USE_CRYPTO_TABLE=true`【F:LoginServer/python-loginserver/loginserver/config.py†L37-L43】【F:LoginServer/python-loginserver/config/loginserver.yaml†L2-L7】【F:LoginServer/python-loginserver/.env.example†L3-L6】

Quando habilitado, o servidor aplica a mesma substituição no inbound/outbound.【F:LoginServer/python-loginserver/loginserver/protocol/codec.py†L111-L131】【F:LoginServer/python-loginserver/loginserver/server.py†L150-L156】

## Como rodar “em paralelo” com o Java

Estratégia recomendada:

1) Suba o Python em outra porta (ex.: `53698`).
2) Faça replay/captura e compare bytes.
3) Só depois troque rota/DNS/LB.

Exemplo de override rápido via `.env`:

```bash
LOGIN_PROTOCOL__LISTEN_PORT=53698
```

Campos e defaults estão em `config.py` e `config/loginserver.yaml`.【F:LoginServer/python-loginserver/loginserver/config.py†L37-L43】【F:LoginServer/python-loginserver/config/loginserver.yaml†L1-L7】

## Como validar se “está funcionando” de verdade

O que já está validado aqui:

- framing + parsing + códigos fixos + tamanhos fixos via testes unitários.【F:LoginServer/python-loginserver/loginserver/tests/test_codec.py†L10-L21】【F:LoginServer/python-loginserver/loginserver/tests/test_messages.py†L22-L33】【F:LoginServer/python-loginserver/loginserver/tests/test_golden.py†L13-L38】

O que você ainda deve validar no seu ambiente:

- login com cliente real
- ban / already logged in
- se precisa `use_crypto_table=true`
- comparação byte-a-byte contra o Java com tcpdump/wireshark

Veja também: `docs/protocol.md`.【F:LoginServer/python-loginserver/docs/protocol.md†L76-L88】【F:LoginServer/python-loginserver/docs/protocol.md†L104-L124】
