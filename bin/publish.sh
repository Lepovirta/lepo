#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

STAGING_URL_FILE=staging_url.txt
DEPLOY_LOG=deploy_log.txt

delete_logs() {
    if [ -f "$DEPLOY_LOG" ]; then
        rm "$DEPLOY_LOG"
    fi
}
trap delete_logs EXIT

extract_staging_link() {
    awk -F ': ' '/Live Draft URL/ { printf $2; exit }' "$DEPLOY_LOG" > "$STAGING_URL_FILE"
}

deploy_message() {
    local git_branch="${CIRCLE_BRANCH:-}"
    if [ -z "$git_branch" ]; then
        git_branch=$(git rev-parse --abbrev-ref HEAD)
    fi
    echo "branch: $git_branch"
}

main() {
    local env="${1:-stg}"
    case "$env" in
        prod|production)
            netlify deploy --prod
            ;;
        stg|staging)
            netlify deploy -m "$(deploy_message)" | tee "$DEPLOY_LOG"
            extract_staging_link
            ;;
        *)
            echo "Invalid environment $env. Expected prod/production/stg/staging." >&2
            return 1
            ;;
    esac
}

main "$@"
