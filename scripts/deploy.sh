#!/bin/bash

set -e

OUTPUT_DIR="target/website"

is_installed() {
    hash "$1" 2>/dev/null
}

log() {
    echo "$@" 1>&2
}

current_branch() {
    git symbolic-ref --short HEAD
}

deploy_production() {
    local bucket=$1

    lein build-site "$OUTPUT_DIR"
    aws s3 sync "$OUTPUT_DIR/" "s3://$bucket/" --delete
}

deploy_staging() {
    local bucket=$1
    local root_dir=${2:-$(current_branch)}

    if [ ! "$root_dir" ]; then
        log "No root directory defined!"
        exit 1
    fi

    lein build-site "$OUTPUT_DIR" "$root_dir"
    aws s3 cp "$OUTPUT_DIR/$root_dir/" "s3://$bucket/$root_dir/" --recursive
}

main() {
    local target=$1
    local bucket=$2
    local root_dir=$3

    if [ ! "$bucket" ]; then
        log "Bucket not specified!"
        exit 1
    fi

    case "$target" in
        "production") deploy_production "$bucket" ;;
        "staging") deploy_staging "$bucket" "$root_dir" ;;
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
