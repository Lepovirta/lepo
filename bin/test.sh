#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
trap "kill 0" EXIT

# Start live server
./bin/serve.sh 2>/dev/null >&2 &

# Wait until server has started
curl -sfSL -4 --retry 10 --retry-connrefused --retry-delay 2 "http://localhost:1313/" >/dev/null

# Test the site
echo '{
    "urls": ["http://localhost:1313/"],
    "ignoreRobotsTxt": true
}' | lukki
