#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

PROD_HOST=${PROD_HOST:-"lepo.group"}
STAGING_HOST=${STAGING_HOST:-"staging.lepo.group"}
PROD_BASE_URL="https://${PROD_HOST}"
STAGING_BASE_URL="https://${STAGING_HOST}"

hugo() {
    ./bin/hugo "$@"
}

check_and_download_hugo() {
    if [ ! -x bin/hugo ]; then
        echo "Hugo not available. Downloading." >&2
        ./bin/download_hugo.sh
    fi
}

delete_old_files() {
    if [ -d public ]; then
        rm -r public
    fi
}

build_prod() {
    hugo --baseURL "$PROD_BASE_URL" --minify
}

build_staging() {
    hugo --baseURL "$STAGING_BASE_URL/$(git_branch)" --minify
}

git_branch() {
    git rev-parse --abbrev-ref HEAD
}

print_help() {
    cat <<EOF
usage: $0 COMMAND

COMMAND:
    prod, production        Generate production site
    stg, staging            Generate staging site
EOF
    exit 1
}

main() {
    check_and_download_hugo
    delete_old_files
    
    case "${1:-}" in
    prod|production) build_prod ;;
    stg|staging) build_staging ;;
    *) print_help ;;
    esac
}

main "$@"
