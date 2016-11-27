#!/bin/bash

set -e

OUTPUT_DIR="target/website/"

is_installed() {
    hash "$1" 2>/dev/null
}

log() {
    echo "$@" 1>&2
}

deploy_production() {
    if [ ! "$PRODUCTION_S3_BUCKET" ]; then
        log "Production S3 bucket is not specified: PRODUCTION_S3_BUCKET"
        exit 1
    fi

    lein build-site "$OUTPUT_DIR"
    aws s3 sync "$OUTPUT_DIR" "s3://$PRODUCTION_S3_BUCKET/"
}

deploy_staging() {
    if [ ! "$STAGING_S3_BUCKET" ]; then
        log "Staging S3 bucket is not specified: STAGING_S3_BUCKET"
        exit 1
    fi

    local branch_name=$(git rev-parse --abbrev-ref HEAD)

    if [ ! "$branch_name" ]; then
        log "Empty branch name!"
        exit 1
    fi

    lein build-site "$OUTPUT_DIR" "$branch_name"
    aws s3 cp "$OUTPUT_DIR/$branch_name/" "s3://$STAGING_S3_BUCKET/$branch_name/" --recursive
}

main() {
    local target="$1"
    case "$target" in
        "production") deploy_production ;;
        "staging") deploy_staging ;;
        *) log "usage: $0 production|staging"
    esac
}

# Preconditions

if ! is_installed "aws"; then
    log "AWS CLI is not installed"
    exit 1
fi

if ! is_installed "lein"; then
    log "Leiningen is not installed"
fi

# Everything good? Deploy!

main "$@"
