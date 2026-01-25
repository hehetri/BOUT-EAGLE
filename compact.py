#!/usr/bin/env python3
import argparse
import json
import struct
from pathlib import Path

ENTRY_SIZE_DEFAULT = 260
DEFAULT_VERSION = 1
DEFAULT_KEY = 1188951748


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Pack .dun files into dungeon.bin")
    parser.add_argument("manifest", nargs="?", default="dungeon_extracted/manifest.json", help="Manifest produced by extract.py")
    parser.add_argument("output", nargs="?", default="dungeon.bin", help="Output .bin file")
    parser.add_argument(
        "--scan-dir",
        help="Directory containing .dun files to include (adds missing entries to the manifest list).",
    )
    parser.add_argument(
        "--update-manifest",
        action="store_true",
        help="Write back the manifest with any newly discovered files.",
    )
    parser.add_argument("--default-version", type=int, default=DEFAULT_VERSION, help="Default version for new entries.")
    parser.add_argument("--default-key", type=int, default=DEFAULT_KEY, help="Default key for new entries.")
    return parser.parse_args()


def encrypt_payload(payload: bytes) -> bytes:
    return bytes((~b) & 0xFF for b in payload)


def add_missing_entries(file_entries: list[dict], scan_dir: Path, default_version: int, default_key: int) -> list[dict]:
    known = {entry["name"] for entry in file_entries}
    new_entries = []
    for path in sorted(scan_dir.glob("*.dun")):
        if path.name in known:
            continue
        new_entries.append(
            {
                "name": path.name,
                "version": default_version,
                "payload_size": path.stat().st_size,
                "key": default_key,
            }
        )
    return file_entries + new_entries


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

    if args.scan_dir:
        scan_dir = Path(args.scan_dir)
        if not scan_dir.is_dir():
            raise SystemExit(f"Scan directory not found: {scan_dir}")
        fallback_version = file_entries[0]["version"] if file_entries else args.default_version
        fallback_key = file_entries[0]["key"] if file_entries else args.default_key
        file_entries = add_missing_entries(file_entries, scan_dir, fallback_version, fallback_key)
        file_count = len(file_entries)
        manifest["files"] = file_entries
        manifest["file_count"] = file_count

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

    if data_start is None or data_start < len(output):
        data_start = len(output)
        manifest["data_start"] = data_start
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

    if args.update_manifest:
        manifest_path.write_text(json.dumps(manifest, indent=2, sort_keys=True), encoding="utf-8")


if __name__ == "__main__":
    main()
