#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."

hugo() {
    ./bin/hugo.sh "$@"
}

main() {
    _env="${1:-stg}"
    case "$_env" in
        prod|production) hugo --gc --minify ;;
        stg|staging) hugo --gc --minify --buildFuture ;;
        *) echo "Invalid environment $_env. Expected prod/production/stg/staging." >&2; return 1 ;;
    esac
}

main "$@"
