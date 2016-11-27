#!/bin/sh

set -e

if ! hash livereloadx 2>/dev/null; then
    cat <<EOF 1>&2
WARNING: Could not find LiveReloadX. Auto-reloading is disabled.

In order to enable auto-reloading, install LiveReloadX:

    $ npm install -g livereloadx

...and install one of the LiveReload browser extensions:

    http://livereload.com/extensions/

EOF
else
    livereloadx \
        --include '*.scss' \
        --include '*.svg' \
        --include '*.clj' \
        --include '*.edn' \
        --exclude 'target/*' &
fi

exec lein live
