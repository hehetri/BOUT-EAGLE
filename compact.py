#!/usr/bin/env python3
import argparse
import json
import struct
from pathlib import Path

ENTRY_SIZE_DEFAULT = 260


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Pack .dun files into dungeon.bin")
    parser.add_argument("manifest", nargs="?", default="dungeon_extracted/manifest.json", help="Manifest produced by extract.py")
    parser.add_argument("output", nargs="?", default="dungeon.bin", help="Output .bin file")
    return parser.parse_args()


def encrypt_payload(payload: bytes) -> bytes:
    return bytes((~b) & 0xFF for b in payload)


def main() -> None:
    args = parse_args()
    manifest_path = Path(args.manifest)
    output_path = Path(args.output)

    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    header = manifest["container_header"]
    file_entries = manifest["files"]
    file_count = manifest["file_count"]
    entry_size = manifest.get("entry_size", ENTRY_SIZE_DEFAULT)
    data_start = manifest.get("data_start")

    if file_count != len(file_entries):
        raise SystemExit("Manifest file count does not match entry list length.")

    if len(header) != 4:
        raise SystemExit("Manifest header must contain exactly 4 integers.")

    base_dir = manifest_path.parent

    output = bytearray()
    output.extend(struct.pack("<4I", *header))

    for entry in file_entries:
        name_bytes = entry["name"].encode("utf-8")
        if len(name_bytes) >= entry_size:
            raise SystemExit(f"Filename too long for entry table: {entry['name']}")
        padded = name_bytes + b"\x00" * (entry_size - len(name_bytes))
        output.extend(padded)

    offsets_position = len(output)
    output.extend(b"\x00" * (file_count * 4))

    if data_start is None:
        data_start = len(output)
    if len(output) > data_start:
        raise SystemExit("Data start from manifest is smaller than header size.")
    output.extend(b"\x00" * (data_start - len(output)))

    offsets = []
    for entry in file_entries:
        payload_path = base_dir / entry["name"]
        payload = payload_path.read_bytes()
        encrypted = encrypt_payload(payload)
        offsets.append(len(output))
        output.extend(struct.pack("<III", entry["version"], len(encrypted), entry["key"]))
        output.extend(encrypted)

    for idx, offset in enumerate(offsets):
        struct.pack_into("<I", output, offsets_position + (idx * 4), offset)

    output_path.write_bytes(output)


if __name__ == "__main__":
    main()
