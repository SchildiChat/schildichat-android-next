#!/bin/bash

set -x

git lfs fetch --all upstream || exit $?

for remote in gerrit sm origin github; do
    git lfs push --all "$remote"
done
