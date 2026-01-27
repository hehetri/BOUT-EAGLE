#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BIN_DIR="${ROOT_DIR}/tests/bin"

mkdir -p "${BIN_DIR}"

javac -d "${BIN_DIR}" \
  "${ROOT_DIR}/LoginServer/src/LoginServer/PasswordUtil.java" \
  "${ROOT_DIR}/tests/PasswordUtilTest.java"

javac -d "${BIN_DIR}" \
  "${ROOT_DIR}/BotsServer/src/botsserver/CommandRules.java" \
  "${ROOT_DIR}/BotsServer/src/botsserver/ChatCommandParser.java" \
  "${ROOT_DIR}/tests/CommandRulesTest.java" \
  "${ROOT_DIR}/tests/ChatCommandParserTest.java"

java -cp "${BIN_DIR}" LoginServer.PasswordUtilTest
java -cp "${BIN_DIR}" botsserver.CommandRulesTest
java -cp "${BIN_DIR}" botsserver.ChatCommandParserTest
