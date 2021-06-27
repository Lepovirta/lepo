#!/usr/bin/env sh
set -eux
cd "$(dirname "$0")/.."

echo "Lint checks shell scripts" >&2
shellcheck bin/*.sh
