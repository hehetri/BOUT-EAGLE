# RelayServer protocol (inferido + plano)

## Framing

O RelayServer Python usa o codec compartilhado `MixedEndianCodec`:

```text
0..1   command   u16 big-endian
2..3   length    u16 little-endian
4..N   payload
```

## Estratégia para compatibilidade 100%

1) Instrumentar o Java para logar:
- opcode
- tamanho
- hexdump
- campos decodificados

2) Capturar tráfego real:

```bash
tcpdump -i any -s 0 -w relay.pcap tcp port 55200
```

3) Replay e comparação byte-a-byte entre Java e Python.
