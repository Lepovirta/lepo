#!/bin/bash

set -e

SCRIPT_DIR=$(dirname "$0")
DEPLOY="$SCRIPT_DIR/deploy.sh"

cd "$TRAVIS_BUILD_DIR"

slack_notify() {
    curl -X POST -H 'Content-type: application/json' \
        --data "{\"text\": \"$1\"}" \
        "https://hooks.slack.com/services/$SLACK_HOOK" || return 0
}

build_url() {
    echo "https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
}

build_header() {
    echo "Build <$(build_url)|#${TRAVIS_BUILD_NUMBER}>" \
        "of ${TRAVIS_REPO_SLUG}@${TRAVIS_BRANCH}:\n" \
        "$1"
}

master_build_success() {
    build_header ":+1: Deploy to production succeeded. https://lepo.group/"
}

master_build_failure() {
    build_header ":-1: Deploy to production failed."
}

staging_build_success() {
    build_header ":+1: Deploy to staging succeeded. https://staging.lepo.group/$TRAVIS_BRANCH"
}

staging_build_failure() {
    build_header ":-1: Deploy to staging failed."
}

main() {
    if [ "$TRAVIS_BRANCH" = "master" ]; then
        if "$DEPLOY" production "$PRODUCTION_S3_BUCKET"; then
            slack_notify "$(master_build_success)"
            return 0
        else
            slack_notify "$(master_build_failure)"
            return 1
        fi
    else
        if "$DEPLOY" staging "$STAGING_S3_BUCKET" "$TRAVIS_BRANCH"; then
            slack_notify "$(staging_build_success)"
            return 0
        else
            slack_notify "$(staging_build_failure)"
            return 1
        fi
    fi
}

main