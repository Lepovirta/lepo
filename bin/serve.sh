#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
./bin/hugo.sh server --buildFuture --buildDrafts "$@"
