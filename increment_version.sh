#!/bin/bash

set -e

mydir="$(dirname "$(realpath "$0")")"

source "$mydir/merge_helpers.sh"

version_kt="$mydir/plugins/src/main/kotlin/Versions.kt"
reference_fdroid_metadata="$HOME/fdroid/sm/data/metadata/chat.schildi.next.yml"

# https://f-droid.org/en/docs/All_About_Descriptions_Graphics_and_Screenshots/
max_changelog_len=500

should_merge_translations_for_release=0

if [ "$1" = "preview" ]; then
    preview=1
    shift
else
    preview=0
    require_clean_git
fi


if [ "$1" = "test" ]; then
    release_type="test"
    previousTestVersionCode="$2"
    if echo "$previousTestVersionCode" | grep -q 0\$; then
        previousTestVersionCode="${previousTestVersionCode::-1}"
    else
        echo "Previous version code not ending with 0, not supported"
        exit 1
    fi
else
    release_type="normal"
fi


pushd "$mydir" > /dev/null

do_translation_pull=0

if ((should_merge_translations_for_release)); then
    if [ "$release_type" = "normal" ] && [ "$preview" != 1 ]; then
        if git remote get-url weblate > /dev/null; then
            echo "Pulling translations..."
            translation commit && do_translation_pull=1 || echo "translation tool not found, skipping forced commit"
            git fetch weblate
            git merge weblate/sc --no-edit
        else
            echo "WARN: remote weblate not found, not updating translations"
        fi
    fi
fi

last_tag=`downstream_latest_tag`


# Legacy versioning, based on Element's version codes
#calculate_version_code() {
#    echo "(($versionMajor * 10000 + $versionMinor * 100 + $versionPatch + $scVersion) + 4000000) * 10" | bc
#}


#
# Increase version
#

versionMajor=`get_prop versionMajor "$version_kt"`
versionMinor=`get_prop versionMinor "$version_kt"`
versionPatch=`get_prop versionPatch "$version_kt"`
scVersion=`get_prop scVersion`

previousVersionCode=`grep '^            versionCode = ' "$build_gradle" | sed 's|^            versionCode = ||'`
# versionCode incremented independently of versionName, and always increment scVersion
((scVersion++)) || true
if [ "$release_type" = "test" ]; then
    if [ ! -z "$previousTestVersionCode" ]; then
        testVersionCount=$((previousVersionCode > previousTestVersionCode ? 1 : (previousTestVersionCode - previousVersionCode + 1)))
        previousVersionCode=$((previousVersionCode > previousTestVersionCode ? previousVersionCode : previousTestVersionCode))
    else
        testVersionCount=1
    fi
    versionCode=$((previousVersionCode + 1))
else
    versionCode=$((previousVersionCode + 10))
    # Ensure the new version code is higher than the one of the last test version
    if [ -f "$reference_fdroid_metadata" ]; then
        lastTestVersionCode="$(cat "$reference_fdroid_metadata"|grep versionCode|tail -n 1|sed 's|.*: ||' || echo 0)"
    else
        read -p "Enter versionCode of last test version: " lastTestVersionCode
    fi
    while [ "$lastTestVersionCode" -ge "$versionCode" ]; do
        versionCode=$((versionCode + 10))
    done
fi


version="$versionMajor.$versionMinor.$versionPatch.sc$scVersion"

if [ "$release_type" = "test" ]; then
    version="$version-test$testVersionCount"
fi

new_tag="sc_v$version"

if ((preview)); then
    abiExtra=0
    echo "versionCode $versionCode$abiExtra"
    echo "versionName $version"
    exit 0
fi

set_prop "ext.scVersion" "$scVersion"
set_prop "versionCode" "$versionCode"
set_prop "versionName" "\"$version\""



#
# Generate changelog
#

git_changelog() {
    git_args="$1"

    git log $git_args --pretty=format:"- %s" "$last_tag".. --committer="$(git config user.name)" \
        | sed "s|Merge tag '\\(.*\\)' into .*|Update codebase to Element \1|" \
        | grep -v "Merge .*branch" \
        | grep -v "Automatic" \
        | grep -v 'merge_helpers\|README\|increment_version' \
        | grep -v "\\.sh" \
        | grep -v "\\.md" \
        | grep -v "Added translation using Weblate" \
        | grep -v "Translated using Weblate" \
        | grep -v "weblate/main" \
        | grep -v "\\[.*merge.*\\]" \
        | grep -v "Disable Android Auto supports" \
        | grep -v "Switch to alternative Schil" \
        | grep -v "\\[gplay-release\\]" \
        || echo "No significant changes since the last stable release"
}

changelog_dir=.fastlane/metadata/android/en-US/changelogs
changelog_file="$changelog_dir/$versionCode.txt"
mkdir -p "$changelog_dir"
if [ "$release_type" = "test" ]; then
    git_changelog > "$changelog_file"
    # Automated changelog is usually too long for F-Droid changelog
    if [ "$(wc -m "$changelog_file"|sed 's| .*||')" -gt "$max_changelog_len" ]; then
        current_commit="$(git rev-parse HEAD)"
        changelog_add="$(echo -e "- ...\n\nAll changes: https://github.com/SchildiChat/schildichat-android-next/commits/$current_commit")"
        addlen="$(expr length "$changelog_add")"
        # - 3: probably not necessary, but I don't want to risk a broken link because of some miscalculation
        allow_len=$((max_changelog_len - addlen - 3))
        while [ "$(wc -m "$changelog_file"|sed 's| .*||')" -gt "$allow_len" ]; do
            content_shortened="$(head -n -1 "$changelog_file")"
            echo "$content_shortened" > "$changelog_file"
        done
        echo "$changelog_add" >> "$changelog_file"
    fi
else
    git_changelog --reverse > "$changelog_file"
fi
if [ "$release_type" != "test" ]; then
    echo "Opening changelog for manual revision..."
    await_edit "$changelog_file" || true
fi

while [ "$(wc -m "$changelog_file"|sed 's| .*||')" -gt "$max_changelog_len" ]; do
    echo "Your changelog is too long, only $max_changelog_len characters allowed!"
    echo "Currently: $(wc -m "$changelog_file")"
    read -p "Press enter when changelog is done"
done

git add -A
if [ "$release_type" = "test" ]; then
    git commit -m "Test version $versionCode"
else
    git commit -m "Increment version"
    git tag "$new_tag"
fi

if ((do_translation_pull)); then
    echo "Updating weblate repo..."
    translation pull
fi

popd > /dev/null
