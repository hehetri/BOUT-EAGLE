# BotsServer protocol (inferred + compatibility plan)

Este documento separa duas coisas:

1) o que já foi inferido do código Java em `BotsServer/src/botsserver`
2) como validar e convergir para 100% de compatibilidade byte-a-byte

## Framing inferido (cliente ↔ botsserver)

Padrão observado em outros módulos do repo:

```text
0..1   command   u16 big-endian
2..3   length    u16 little-endian (payload length)
4..N   payload   `length` bytes
```

O codec Python implementa exatamente esse framing e limita o tamanho máximo por configuração (`protocol.max_packet_size`).

## Comandos (protocolo inferido e versionável)

Como não há, neste momento, uma tabela oficial de opcodes do BotsServer Java exposta no repo, adotamos:

- um conjunto mínimo de opcodes explícitos
- versionamento e validação por sniff/replay

### Cliente → BotsServer

- `0xE02E` `BOT_HELLO`
- `0xE030` `BOT_COMMAND`
- `0xE031` `BOT_TASK`

### BotsServer → Cliente

- `0xE12E` `BOT_HELLO_ACK`
- `0xE140` `BOT_EVENT`
- `0xE1FF` `BOT_ERROR`

## Server-to-server (s2s) proposto

Quando o protocolo interno não está documentado, a proposta é:

- manter framing binário simples (mesmo framing)
- autenticar com shared secret + HMAC
- versionar explicitamente

### Handshake

- command `0x9001` (`AUTH`) com payload JSON contendo `proof` (hex)
- command `0x9002` (`AUTH_OK`) responde `ok`

O `proof` é:

```text
HMAC_SHA256(shared_secret, f"{S2S_MAGIC}:{version}")[:16]
```

## Plano para confirmar compatibilidade com o Java

### 1) Pontos para inspecionar no Java

Priorize estes arquivos/classes:

- `botsserver/ChannelServerConnection.java`
- `botsserver/Packet.java`
- `botsserver/BotClass.java`
- handlers/opcodes dentro de `parsecmd(...)`

### 2) Sniff / captura

Use tcpdump:

```bash
tcpdump -i any -s 0 -w botsserver.pcap tcp port 54100
```

Depois no Wireshark:

- Follow TCP stream
- Export bytes
- Compare contra a saída Python

### 3) Golden logs (sem binário no repo)

Em vez de `.bin`, salve hexdumps textuais por cenário e valide nos testes.

### 4) Replay

Construa um replay harness que:

- lê requests reais
- envia para Java e Python
- compara respostas byte-a-byte
