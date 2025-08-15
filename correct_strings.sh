#!/bin/bash

set -e

mydir="$(dirname "$(realpath "$0")")"
source "$mydir/merge_helpers.sh"

pushd "$mydir" > /dev/null

mydir="."

# Element -> SchildiChat, but restore Element where it makes sense
# After resoring some "Element Call" things, redo those call-related stringt pointing to "Element X" anyway
find "$mydir" -not -path "./schildi/**" \
    -name translations.xml -exec \
    sed -i 's|Element|SchildiChat|g;
            s|SchildiChat X|SchildiChat Next|g;
            s/SchildiChat \(Web\|iOS\|Desktop\)/Element \1/g;
            s|SchildiChat Matrix Services|Element Matrix Services|g;
            s|\(name="screen_onboarding_welcome_title".*\)SchildiChat|\1Element|g;
            s|\(name=".*_call_.*".*\)SchildiChat|\1Element|g;
            s|\(name=".*element_dot_io.*".*\)SchildiChat|\1Element|g;
            s|Element X|SchildiChat Next|g;
            s/SchildiChat\( \|-\)Pro/Element\1Pro/g;
            s/SchildiChat\( \|-\)Call/Element\1Call/g' '{}' \;

git --no-pager diff \*/translations.xml

popd > /dev/null
