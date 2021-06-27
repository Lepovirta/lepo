#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
./bin/hugo.sh server --buildFuture --buildDrafts "$@"
