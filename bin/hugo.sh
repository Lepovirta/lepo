#!/usr/bin/env bash
set -euo pipefail

case "$OSTYPE" in
linux*) DETECTED_OS="Linux" ;;
darwin*) DETECTED_OS="macOS" ;;
*) echo "unsupported OS: $OSTYPE" >&2; exit 1 ;;
esac

case "$(uname -m)" in
i386|i686) DETECTED_ARCH="32bit" ;;
x86_64) DETECTED_ARCH="64bit" ;;
arm) DETECTED_ARCH="arm64" ;;
*) echo "usupported arch: $(uname -m)" >&2; exit 1 ;;
esac

HUGO_VERSION="${HUGO_VERSION:-0.70.0}"
SCRIPT_DIR="$(dirname "$0")"
HUGO_PATH="$SCRIPT_DIR/hugo"
RELEASE_BASE_URL="https://github.com/gohugoio/hugo/releases/download/v${HUGO_VERSION}"
CHECKSUMS_URL="${RELEASE_BASE_URL}/hugo_${HUGO_VERSION}_checksums.txt"
RELEASE_FILENAME="hugo_${HUGO_VERSION}_${DETECTED_OS}-${DETECTED_ARCH}.tar.gz"
RELEASE_URL="${RELEASE_BASE_URL}/${RELEASE_FILENAME}"

safe_curl() {
    curl -fSL --retry 3 --retry-connrefused --retry-delay 2 "$@"
}

run_hugo() {
    if [ -x "$HUGO_PATH" ]; then
        "$HUGO_PATH" "$@"
    elif hash hugo 2>/dev/null; then
        hugo "$@"
    else
        return 1
    fi
}

check_hugo() {
    run_hugo version | grep -q "v${HUGO_VERSION}"
}

check_hugo_download() {
    safe_curl "$CHECKSUMS_URL" \
        | grep "$RELEASE_FILENAME" \
        | shasum -c \
        >&2
}

download_hugo() {
    if [ ! -f "$RELEASE_FILENAME" ]; then
        safe_curl -o "$RELEASE_FILENAME" "$RELEASE_URL"
    fi
}

extract_hugo() {
    tar xvfz "$RELEASE_FILENAME" -C "$SCRIPT_DIR" hugo >&2
}

delete_temp_files() {
    if [ -f "$RELEASE_FILENAME" ]; then
        rm "$RELEASE_FILENAME"
    fi
}

trap delete_temp_files EXIT

main() {
    if ! check_hugo; then
        echo "Hugo not found. Downloading..." >&2
        download_hugo
        check_hugo_download
        extract_hugo
    fi

    run_hugo "$@"
}

main "$@"
