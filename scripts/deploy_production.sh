#!/bin/bash

set -e

SCRIPT_DIR=$(basename "$0")
"$SCRIPT_DIR/deploy.sh" production "$PRODUCTION_S3_BUCKET"
