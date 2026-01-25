#!/usr/bin/env python3
import argparse
import json
import struct
from pathlib import Path

ENTRY_SIZE = 260


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Extract and decrypt .dun files from dungeon.bin")
    parser.add_argument("input", nargs="?", default="dungeon.bin", help="Input .bin file")
    parser.add_argument("output_dir", nargs="?", default="dungeon_extracted", help="Directory to write extracted files")
    return parser.parse_args()


def read_null_terminated(entry: bytes) -> str:
    raw = entry.split(b"\x00", 1)[0]
    return raw.decode("utf-8", errors="replace")


def decrypt_payload(payload: bytes) -> bytes:
    return bytes((~b) & 0xFF for b in payload)


def main() -> None:
    args = parse_args()
    input_path = Path(args.input)
    output_dir = Path(args.output_dir)
    data = input_path.read_bytes()

    if len(data) < 16:
        raise SystemExit("Input file is too small to be a valid container.")

    header = struct.unpack_from("<4I", data, 0)
    file_count = header[3]

    offset = 16
    names = []
    for _ in range(file_count):
        entry = data[offset:offset + ENTRY_SIZE]
        if len(entry) < ENTRY_SIZE:
            raise SystemExit("Unexpected end of file while reading name table.")
        names.append(read_null_terminated(entry))
        offset += ENTRY_SIZE

    offsets = list(struct.unpack_from(f"<{file_count}I", data, offset))
    data_start = offsets[0] if offsets else offset

    output_dir.mkdir(parents=True, exist_ok=True)

    manifest = {
        "container_header": list(header),
        "entry_size": ENTRY_SIZE,
        "file_count": file_count,
        "data_start": data_start,
        "files": [],
    }

    for index, name in enumerate(names):
        start = offsets[index]
        end = offsets[index + 1] if index + 1 < file_count else len(data)
        chunk = data[start:end]
        if len(chunk) < 12:
            raise SystemExit(f"Entry {name} is too small to contain a header.")
        version, payload_size, key = struct.unpack_from("<III", chunk, 0)
        available_payload = len(chunk) - 12
        if payload_size != available_payload:
            payload_size = available_payload
        payload = chunk[12:12 + payload_size]
        decrypted = decrypt_payload(payload)

        output_path = output_dir / name
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_bytes(decrypted)

        manifest["files"].append(
            {
                "name": name,
                "version": version,
                "payload_size": payload_size,
                "key": key,
            }
        )

    manifest_path = output_dir / "manifest.json"
    manifest_path.write_text(json.dumps(manifest, indent=2, sort_keys=True), encoding="utf-8")


if __name__ == "__main__":
    main()
