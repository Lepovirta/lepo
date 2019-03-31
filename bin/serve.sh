#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

if [ ! -x bin/hugo ]; then
    echo "Hugo not available. Downloading." >&2
    ./bin/download_hugo.sh
fi
./bin/hugo server "$@"
