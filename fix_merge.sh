#!/bin/bash

# After running `git merge` against upstream,
# this script is probably the first thing you will want to run,
# to solve some avoidable merge conflicts for you.

set -e

mydir="$(dirname "$(realpath "$0")")"
source "$mydir/merge_helpers.sh"

shopt -s globstar

pushd "$mydir"

gitattributes="upstream_infra/gitattributes"
if [ ! -f "$gitattributes" ] || ! git diff --quiet -- "$gitattributes"; then
    echo "Fix merge conflicts in $gitattributes first!"
    exit 1
fi

echo "Checking for new LFS files..."
for lfs_regex in `grep "filter=lfs" "$gitattributes" | sed 's| .*||'`; do
    if [ -f "$lfs_regex" ]; then
        git rm -f "$lfs_regex"
    else
        # Double-check in case bash didn't properly expand the globstar?
        echo "Processing new $lfs_regex"
        find -wholename $lfs_regex -exec git rm -f {} \; || true
    fi
done

lfs_regex=`grep "filter=lfs" "$gitattributes" | sed 's| .*||' | tr '\n' '|' | sed 's/|$//;s|\*\*|\*|g;s|\*|.*|g'`
echo "LFS regex: $lfs_regex"

echo "Checking for modified LFS files..."
del_prefix="	deleted by us:   "
for deleted_by_us in `git status | grep "^$del_prefix" | sed "s|$del_prefix||"`; do
    if [[ "$deleted_by_us" =~ $lfs_regex ]]; then
        echo "Processing modified $deleted_by_us"
        git rm "$deleted_by_us"
    fi
done

echo "Checking for added infra files..."
add_prefix="	added by them:   "
for added_by_them in `git status | grep "^$add_prefix" | sed "s|$add_prefix||"`; do
    if [[ "$added_by_them" =~ upstream_infra ]]; then
        echo "Processing added $added_by_them"
        git add "$added_by_them"
    fi
done

echo "Checking for translation conflicts"
mod_prefix="	both modified:   "
for modified in `git status | grep "^$mod_prefix" | sed "s|$mod_prefix||"`; do
    if [[ "$modified" =~ translations.xml ]]; then
        echo "Processing conflicting $modified"
        git checkout MERGE_HEAD -- "$modified"
    fi
done

echo "Re-applying string corrections"
./correct_strings.sh
git add \*/translations.xml

popd
