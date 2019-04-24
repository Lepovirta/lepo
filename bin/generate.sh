#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

# Config
PROD_HOST=${PROD_HOST:-"lepo.group"}
STAGING_HOST=${STAGING_HOST:-"staging.lepo.group"}
PROD_BUCKET=${PROD_BUCKET:-}
STAGING_BUCKET=${STAGING_BUCKET:-}

# Facts
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
STAGING_DIR=${STAGING_DIR:-"$GIT_BRANCH"}
PROD_BASE_URL="https://${PROD_HOST}"
STAGING_BASE_URL="https://${STAGING_HOST}/${STAGING_DIR}"
PRINT_HELP=""
TARGET_ENV=""
PUBLISH_ENABLED=""

parse_args() {
    local key
    while [[ "$#" -gt 0 ]]; do
        key=$1
        case "$key" in
            -t|--target)
                TARGET_ENV=$2
                shift
                shift
                ;;
            -p|--publish)
                PUBLISH_ENABLED=yes
                shift
                ;;
            -h|--help)
                PRINT_HELP=yes
                shift
                ;;
        esac
    done
}

delete_old_files() {
    if [ -d public ]; then
        rm -r public
    fi
}

build_site() {
    ./bin/hugo.sh --baseURL "$1" --minify
}

publish_site() {
    local target_bucket target_dir
    
    if [ ! -d public ]; then
        echo "No directory found for publishing" >&2
    fi

    case "${TARGET_ENV}" in
    prod|production)
        target_bucket=$PROD_BUCKET
        target_dir="/"
        ;;
    stg|staging)
        target_bucket=$STAGING_BUCKET
        target_dir="/$STAGING_DIR"
        ;;
    esac

    aws s3 sync public/ "s3://${target_bucket}${target_dir}" --delete
}

print_help() {
    cat <<EOF
usage: $0 OPTIONS

OPTIONS:
    -t, --target        Target to generate: production, staging
    -p, --publish       If enabled, publish the site.
    -h, --help          Print this help
EOF
    exit 1
}

main() {
    local base_url
    parse_args "$@"

    if [ -n "${PRINT_HELP:-}" ]; then
        print_help
        exit 1
    fi

    case "${TARGET_ENV:-}" in
    prod|production) base_url="$PROD_BASE_URL" ;;
    stg|staging) base_url="$STAGING_BASE_URL" ;;
    *) echo "Invalid target: ${TARGET_ENV:-}" >&2; exit 1;
    esac

    delete_old_files
    build_site "$base_url"

    if [ -n "${PUBLISH_ENABLED}" ]; then
        publish_site
        echo "----------------------------" >&2
        echo "Site available @ $base_url" >&2
    fi
}

main "$@"
