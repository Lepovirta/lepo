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
    local bucket="$1"

    lein build-site "$OUTPUT_DIR"
    aws s3 sync "$OUTPUT_DIR" "s3://$bucket/"
}

deploy_staging() {
    local bucket="$1"
    local branch_name=$(git rev-parse --abbrev-ref HEAD)

    if [ ! "$branch_name" ]; then
        log "Empty branch name!"
        exit 1
    fi

    lein build-site "$OUTPUT_DIR" "$branch_name"
    aws s3 cp "$OUTPUT_DIR/$branch_name/" "s3://$bucket/$branch_name/" --recursive
}

main() {
    local target="$1"
    local bucket="$2"

    if [ ! "$bucket" ]; then
        log "Bucket not specified!"
        exit 1
    fi

    case "$target" in
        "production") deploy_production "$bucket" ;;
        "staging") deploy_staging "$bucket" ;;
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
