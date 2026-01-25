# LoginServer protocol (inferred from Java)

This document describes the currently observed wire contract from `src/LoginServer/LoginServerConnection.java` and `src/LoginServer/LoginServer.java`.

## Transport

- TCP server, default port `53699`.
- Client sends length-prefixed packets.
- Server replies with two raw writes:
  1. 4-byte fixed header: `EC 2C 4A 00`
  2. 74-byte fixed payload, depends on the result.
- The legacy codebase includes byte-substitution tables (`Main.encrypt` / `Main.decrypt`).
  The Python server can apply the same substitution by setting `protocol.use_crypto_table=true`.

## Client → Server framing

Packet layout:

```text
0..1   command      u16 big-endian
2..3   length       u16 little-endian (payload length)
4..N   payload      `length` bytes
```

Special case:

- If `command == 0xFFFF`, the server stops processing.

## Login request (`command = 0xF82A`)

Parsing is offset-based on the **raw** packet (including the 4-byte header):

```text
4..26   username   23 bytes, ISO-8859-1, null-terminated
27..58  password   32 bytes, ISO-8859-1, null-terminated
```

Additional behaviour:

- If the username starts with `"H"`, that prefix is removed before lookup.

### Example login request (hex)

This is a synthetic example to validate tooling. Replace credentials as needed.

```text
F8 2A 37 00
48 75 73 65 72 00 ... (pad to 23 bytes)
70 61 73 73 77 6F 72 64 00 ... (pad to 32 bytes)
```

Notes:

- `0x0037 == 55` payload bytes.
- Total packet size would be `4 + 55 = 59` bytes.

## Server → Client responses

The server always writes:

1. Header: `EC 2C 4A 00`
2. Payload: 74 bytes.

The first bytes of the payload encode the result code.

| Result | Payload prefix (hex) |
|--------|-----------------------|
| Success | `01 00 00 00 00 01 FF` |
| Incorrect username | `01 00 02 00 00 00 FF` |
| Incorrect password | `01 00 01 00 00 00 FF` |
| Banned | `01 00 03 00 00 00 FF` |
| Already logged in | `01 00 06 00 00 00 FF` |

All remaining bytes are zero-padding up to 74 bytes.

## Protocol confirmation plan

Because the Java implementation contains commented cryptography and external PHP auth, confirm the inference with the steps below.

### 1) Add hexdump logging in Java

Instrument `LoginServerConnection.read()` to log:

- raw header bytes
- parsed command
- payload length
- first 64 bytes of payload

### 2) Generate golden files

Capture the exact response bytes for each outcome:

- valid login
- wrong user
- wrong password
- banned
- already online

Persist as binary fixtures:

- `tests/golden/login_success.bin`
- etc.

### 3) Capture live traffic

Use tcpdump:

```bash
tcpdump -i any -s 0 -w loginserver.pcap tcp port 53699
```

Then inspect in Wireshark:

- Follow TCP stream
- Export packet bytes
- Confirm the framing and byte order

### 4) Replay captured traffic

Create a replay harness that:

- reads client requests from pcap/golden files
- sends them to both Java and Python servers
- asserts byte-identical responses

### 5) Resolve unknowns

If any of the following exist outside the current repo, we need the exact Java sources:

- encryption table usage (`Main.encrypt` / `Main.decrypt`)
- password hashing function
- session ticket format used by downstream servers
