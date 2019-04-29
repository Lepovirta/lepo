#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

hugo() {
    ./bin/hugo.sh "$@"
}

main() {
    local env="${1:-stg}"
    case "$env" in
        prod|production) hugo --gc --minify ;;
        stg|staging) hugo --gc --minify --buildFuture ;;
        *) echo "Invalid environment $env. Expected prod/production/stg/staging." >&2; return 1 ;;
    esac
}

main "$@"
