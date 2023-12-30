#!/bin/bash

set -e

GIT_ROOT="$(git rev-parse --show-toplevel)"
SDK_DIR="$GIT_ROOT/../matrix-rust-sdk"
COMPONENTS_DIR="$GIT_ROOT/../matrix-rust-components-kotlin"

source "$GIT_ROOT/merge_helpers.sh"

if [ ! -d "$SDK_DIR" ]; then
    echo "SDK not found at $SDK_DIR"
    exit 1
fi
if [ ! -d "$COMPONENTS_DIR" ]; then
    echo "SDK components not found at $COMPONENTS_DIR"
    exit 1
fi

cd "$GIT_ROOT"

SDK_DIR="$(realpath "$SDK_DIR")"
COMPONENTS_DIR="$(realpath "$COMPONENTS_DIR")"

echo "Using SDK: $SDK_DIR"
echo "           $COMPONENTS_DIR"

upstream_tag=`upstream_latest_tag`

rust_ver="$(git show "$upstream_tag:gradle/libs.versions.toml" | grep "org.matrix.rustcomponents:sdk-android:" | sed 's|.*:\(.*\)"|\1|')"

if [ -z "$rust_ver" ]; then
    echo "Unable to detect currently prefered components version"
    exit 1
fi

echo "Detected Rust components version: $rust_ver"
components_tag="sdk-v$rust_ver"
echo "Merging Rust components for upstream $components_tag"
cd "$COMPONENTS_DIR"
require_clean_git
git fetch upstream
git merge "$components_tag" || read -p "Enter once conflicts are solved"

echo "Trying to detect appropriate Rust SDK from GitHub releases"
sdk_revision=`curl https://github.com/matrix-org/matrix-rust-components-kotlin/releases/tag/sdk-v"$rust_ver" | grep ">https://github.com/matrix-org/matrix-rust-sdk/tree/" | sed "s|.*https://github.com/matrix-org/matrix-rust-sdk/tree/\\(.*\\)</a>.*|\\1|g"`

echo "Merging upstream SDK at $sdk_revision..."
cd "$SDK_DIR"
require_clean_git
git fetch upstream
git merge "$sdk_revision" || read -p "Enter once conflicts are solved"

echo "Build SDK..."
"$GIT_ROOT/sc_tools/rust_sdk_build.sh"
