#!/bin/bash

set -e

SCRIPT_DIR=$(dirname "$0")
"$SCRIPT_DIR/deploy.sh" staging "$STAGING_S3_BUCKET"
