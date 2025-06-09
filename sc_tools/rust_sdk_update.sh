#!/bin/bash

set -e

if [ ! -z "$1" ]; then
    rust_ver="$1"
else
    rust_ver=
fi

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

if [ -z "$rust_ver" ]; then
    upstream_tag=`upstream_latest_tag`

    rust_ver="$(git show "$upstream_tag:gradle/libs.versions.toml" | grep "org.matrix.rustcomponents:sdk-android:" | sed 's|.*:\(.*\)"|\1|' | sed 's|\([0-9]*\)\.\([1-9]\)\.\([0-9]\)|\1.0\2.0\3|')"
fi

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

echo "Trying to detect appropriate Rust SDK from recent commits"
sdk_revision=`git log "$components_tag" --oneline|grep "Bump SDK version to"|head -n 1|sed 's|.*(matrix-rust-sdk to \(.*\))|\1|'`

echo "Merging Rust SDK version: $sdk_revision"
cd "$SDK_DIR"
require_clean_git
git fetch upstream
git merge "$sdk_revision" || echo "Please resolve and commit SDK conflicts manually!"


#git merge "$sdk_revision" || read -p "Enter once conflicts are solved"

#echo "Build SDK..."
#"$GIT_ROOT/sc_tools/rust_sdk_build.sh"
