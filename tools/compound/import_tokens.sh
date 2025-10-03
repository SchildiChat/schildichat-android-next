#!/bin/bash

set -e

SHORT=b,:
LONG=branch,:
BRANCH='main'

while getopts b: flag
do
    case "${flag}" in
        b) BRANCH=${OPTARG};;
    esac
done

echo "Branch used: $BRANCH"

echo "Cloning the compound-design-tokens repository..."
if [ -d tmp ]; then
    echo "Deleting tmp folder..."
    rm -rf tmp
fi
mkdir tmp
pushd tmp
git clone --branch $BRANCH https://github.com/vector-im/compound-design-tokens

echo "Copying files from tokens repository..."
cp -R compound-design-tokens/assets/android/res/drawable ../compound/src/main/res/
cp -R compound-design-tokens/assets/android/src/* ../compound/src/main/kotlin/io/element/android/compound/tokens/generated/
popd

echo "Adding autoMirrored attribute..."
python3 ./scripts/addAutoMirrored.py

echo "Removing temporary files..."
rm -rf tmp

echo "Done!"
