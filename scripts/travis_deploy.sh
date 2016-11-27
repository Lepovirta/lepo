#!/bin/bash

set -e

SCRIPT_DIR=$(dirname "$0")
DEPLOY="$SCRIPT_DIR/deploy.sh"

cd "$TRAVIS_BUILD_DIR"

if [ "$TRAVIS_BRANCH" = "master" ]; then
    "$DEPLOY" production "$PRODUCTION_S3_BUCKET"
else
    "$DEPLOY" staging "$STAGING_S3_BUCKET"
fi
