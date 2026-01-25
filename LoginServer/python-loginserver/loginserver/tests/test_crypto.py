from __future__ import annotations

from loginserver.security import crypto


def test_crypto_tables_are_inverses() -> None:
    data = bytes(range(256))
    encrypted = crypto.encrypt(data)
    decrypted = crypto.decrypt(encrypted)
    assert decrypted == data
