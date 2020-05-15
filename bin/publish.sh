#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

deploy_message() {
    local git_branch="${CI_COMMIT_REF_NAME:-}"
    if [ -z "$git_branch" ]; then
        git_branch=$(git rev-parse --abbrev-ref HEAD)
    fi
    echo "branch: $git_branch"
}

main() {
    local draft="${NETLIFY_DRAFT:-"true"}"
    local message=${NETLIFY_DEPLOYMESSAGE:-}
    local dir=${NETLIFY_DIRECTORY:-"public"}

    if [ -z "${message:-}" ]; then
        message=$(deploy_message)
    fi

    case "${1:-}" in
        prod|production) draft="false" ;;
    esac

    NETLIFY_DEPLOYMESSAGE=${message} \
    NETLIFY_DRAFT=${draft} \
    NETLIFY_DIRECTORY=${dir} \
    netlify-deployer
}

main "$@"
