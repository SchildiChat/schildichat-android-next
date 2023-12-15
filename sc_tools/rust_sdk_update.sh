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

if [ -z "$JAVA_HOME" ]; then
    JAVA_HOME=/opt/android-studio/jbr
    if [ -d "$JAVA_HOME" ]; then
        export JAVA_HOME
    else
        unset JAVA_HOME
        echo "Warn: JAVA_HOME not set"
    fi
fi
if [ -z "$ANDROID_NDK_HOME" ]; then
    ANDROID_NDK_HOME="$HOME/AndroidSdk/ndk/25.2.9519653"
    if [ -d "$ANDROID_NDK_HOME" ]; then
        export ANDROID_NDK_HOME="$ANDROID_NDK_HOME"
    else
        unset ANDROID_NDK_HOME
        echo "Warn: ANDROID_NDK_HOME not set"
    fi
fi
echo "JAVA_HOME=$JAVA_HOME"
echo "ANDROID_NDK_HOME=$ANDROID_NDK_HOME"

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
sdk_revision=`curl https://github.com/matrix-org/matrix-rust-components-kotlin/releases/tag/sdk-v0.1.68 | grep ">https://github.com/matrix-org/matrix-rust-sdk/tree/" | sed "s|.*https://github.com/matrix-org/matrix-rust-sdk/tree/\\(.*\\)</a>.*|\\1|g"`

echo "Merging upstream SDK at $sdk_revision..."
cd "$SDK_DIR"
require_clean_git
git fetch upstream
git merge "$sdk_revision" || read -p "Enter once conflicts are solved"

echo "Build SDK..."
cd "$COMPONENTS_DIR"
./scripts/build.sh -p "$SDK_DIR" -r -m sdk -o "$GIT_ROOT"/libraries/rustsdk/matrix-rust-sdk.aar
