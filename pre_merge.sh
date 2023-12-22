#!/bin/bash

set -e

mydir="$(dirname "$(realpath "$0")")"
source "$mydir/merge_helpers.sh"

# Require clean git state
require_clean_git

# Tag this version for easier git diff-ing
version_kt="$mydir/plugins/src/main/kotlin/Versions.kt"
versionMajor=`get_prop versionMajor "$version_kt"`
versionMinor=`get_prop versionMinor "$version_kt"`
versionPatch=`get_prop versionPatch "$version_kt"`
tag="sc_last_v$versionMajor.$versionMinor.$versionPatch"
git tag "$tag" || true

# Make sure LFS is happy
"$mydir/sc_tools/lfs_mirror.sh"
