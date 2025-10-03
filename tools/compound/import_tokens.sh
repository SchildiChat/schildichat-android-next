#!/bin/bash

set -e

BRANCH='main'

while getopts b: flag
do
    case "${flag}" in
        b) BRANCH=${OPTARG};;
        *) echo "usage: $0 [-b branch]" >&2
           exit 1 ;;
    esac
done

echo "Branch used: $BRANCH"

echo "Cloning the compound-design-tokens repository..."
if [ -d tmpCompound ]; then
    echo "Deleting tmpCompound folder..."
    rm -rf tmpCompound
fi
mkdir tmpCompound
pushd tmpCompound
git clone --branch "${BRANCH}" https://github.com/vector-im/compound-design-tokens

echo "Copying files from tokens repository..."
rm -R ../libraries/compound/src/main/res/drawable
cp -R compound-design-tokens/assets/android/res/drawable ../libraries/compound/src/main/res/
cp -R compound-design-tokens/assets/android/src/* ../libraries/compound/src/main/kotlin/io/element/android/compound/tokens/generated/
popd

echo "Adding autoMirrored attribute..."
python3 ./tools/compound/addAutoMirrored.py

echo "Removing temporary files..."
rm -rf tmpCompound

echo "Done!"
