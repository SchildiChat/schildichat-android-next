#!/bin/bash

set -e

mydir="$(dirname "$(realpath "$0")")"
source "$mydir/merge_helpers.sh"

# Require clean git state
require_clean_git

# Tag this version for easier git diff-ing
version_kt="$mydir/plugins/src/main/kotlin/Versions.kt"
elVersionYear=`get_prop versionYear "$version_kt"`
elVersionMonth=`get_prop versionMonth "$version_kt"`
elVersionRelNumber=`get_prop versionReleaseNumber "$version_kt"`
tag="sc_last_ex_${elVersionYear}_${elVersionMonth}_${elVersionRelNumber}"
git tag "$tag" || true
