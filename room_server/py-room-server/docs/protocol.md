# RoomServer protocol (inferido + plano)

## Modelo de consistência

Cada sala é um actor single-threaded (`RoomActor`) com fila própria, evitando race conditions por sala.

## Framing

Mesmo codec compartilhado (`MixedEndianCodec`):

```text
command: u16 BE
length:  u16 LE
payload
```

## Confirmação com Java

- instrumentar `Packet.java` + handlers
- capturar com tcpdump
- replay byte-a-byte
