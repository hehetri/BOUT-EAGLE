-- Core tables for the Python LoginServer.
-- These coexist with the legacy `bout_users` table used by the Java server.

CREATE TABLE IF NOT EXISTS accounts (
    account_id BIGSERIAL PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    flags INTEGER NOT NULL DEFAULT 0,
    banned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS bans (
    ban_id BIGSERIAL PRIMARY KEY,
    account_id BIGINT REFERENCES accounts(account_id) ON DELETE CASCADE,
    reason TEXT,
    banned_until TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS sessions (
    session_id TEXT PRIMARY KEY,
    username TEXT NOT NULL,
    ip_address INET NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    ticket BYTEA NOT NULL
);

CREATE INDEX IF NOT EXISTS sessions_username_idx ON sessions (username);
CREATE INDEX IF NOT EXISTS sessions_expires_at_idx ON sessions (expires_at);

CREATE TABLE IF NOT EXISTS ip_history (
    username TEXT NOT NULL,
    ip_address INET NOT NULL,
    observed_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (username, ip_address, observed_at)
);

CREATE TABLE IF NOT EXISTS ipbanned (
    ip TEXT PRIMARY KEY,
    banned INTEGER NOT NULL DEFAULT 0
);
