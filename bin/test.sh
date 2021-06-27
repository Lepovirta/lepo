#!/usr/bin/env sh
set -eux
cd "$(dirname "$0")/.."

wait_test_server() {
    retry_times="${1:-10}"
    retry_delay="${2:-2}"
    curl -sfSL -4 \
        --retry "$retry_times" \
        --retry-connrefused \
        --retry-delay "$retry_delay" \
        "http://localhost:1313/" >/dev/null
}

echo "Check if test server is available..." >&2
if ! wait_test_server 0; then
    trap "exit" INT TERM
    trap "kill 0" EXIT
    echo "Starting test server" >&2
    ./bin/serve.sh 2>/dev/null >&2 &
    echo "Waiting for test server to become available..." >&2
    wait_test_server 10 2
fi

echo "Run website tester" >&2
echo '{
    "urls": ["http://localhost:1313/"],
    "ignoreRobotsTxt": true
}' | lukki
