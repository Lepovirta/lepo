#!/usr/bin/env bash
set -euox pipefail
cd "$(dirname "$0")/.."
trap "kill 0" EXIT

echo "Lint checks shell scripts" >&2
shellcheck bin/*.sh

echo "Starting test server" >&2
./bin/serve.sh 2>/dev/null >&2 &

echo "Waiting for test server to become available..." >&2
curl -sfSL -4 --retry 10 --retry-connrefused --retry-delay 2 "http://localhost:1313/" >/dev/null

echo "Run website tester" >&2
echo '{
    "urls": ["http://localhost:1313/"],
    "ignoreRobotsTxt": true
}' | lukki
