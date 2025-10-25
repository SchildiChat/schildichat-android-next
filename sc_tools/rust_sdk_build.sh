#!/bin/bash

set -e

GIT_ROOT="$(git rev-parse --show-toplevel)"
SDK_DIR="$GIT_ROOT/../matrix-rust-sdk"
COMPONENTS_DIR="$GIT_ROOT/../matrix-rust-components-kotlin"

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
    ANDROID_NDK_HOME="$HOME/AndroidSdk/ndk/29.0.14206865"
    if [ -d "$ANDROID_NDK_HOME" ]; then
        export ANDROID_NDK_HOME="$ANDROID_NDK_HOME"
    else
        unset ANDROID_NDK_HOME
        echo "Warn: ANDROID_NDK_HOME not set"
    fi
fi
echo "JAVA_HOME=$JAVA_HOME"
echo "ANDROID_NDK_HOME=$ANDROID_NDK_HOME"

RUSTFLAGS="$RUSTFLAGS --remap-path-prefix=$HOME/.cargo/=.cargo/"
RUSTFLAGS="$RUSTFLAGS --remap-path-prefix=$(realpath "$SDK_DIR")/=."
RUSTFLAGS="$RUSTFLAGS --remap-path-prefix=$HOME/.rustup/=.rustup/"
export RUSTUP_TOOLCHAIN=1.90.0
export RUSTFLAGS
echo "RUSTFLAGS=$RUSTFLAGS"

cd "$COMPONENTS_DIR"

./scripts/build.sh -p "$SDK_DIR" -m sdk -o "$GIT_ROOT"/libraries/rustsdk/matrix-rust-sdk.aar "$@"
