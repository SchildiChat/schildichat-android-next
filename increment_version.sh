#!/bin/bash

set -e

mydir="$(dirname "$(realpath "$0")")"

source "$mydir/merge_helpers.sh"

version_kt="$mydir/plugins/src/main/kotlin/Versions.kt"
reference_fdroid_metadata="$HOME/fdroid/sm/data/metadata/chat.schildi.next.internal.yml"

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

last_tag=

if [ "$1" = "test" ]; then
    release_type="test"
    bump_type="auto"
    previousTestVersionCode="$2"
    last_tag="$3"
    if echo "$previousTestVersionCode" | grep -q 0\$; then
        previousTestVersionCode="${previousTestVersionCode::-1}"
    else
        echo "Previous version code not ending with 0, not supported"
        exit 1
    fi
else
    release_type="normal"
    if [ "$1" = "major" ]; then
        bump_type="major"
    elif [ "$1" = "minor" ]; then
        bump_type="minor"
    else
        bump_type="auto"
    fi
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

if [ -z "$last_tag" ]; then
    last_tag=`downstream_latest_tag`
fi


#
# Increment version
#

elVersionYear=`get_prop versionYear "$version_kt"`
elVersionMonth=`get_prop versionMonth "$version_kt"`
elVersionRelNumber=`get_prop versionReleaseNumber "$version_kt"`
scVersionMajor=`get_prop scVersionMajor`
scVersionMinor=`get_prop scVersionMinor`
previousVersionCode=`grep '^            versionCode = ' "$build_gradle" | sed 's|^            versionCode = ||'`

sc_el_version_append="-ex_${elVersionYear}_${elVersionMonth}_${elVersionRelNumber}"

if [ "$bump_type" = "major" ]; then
    ((scVersionMajor++)) || true
    scVersionMinor=0
elif [ "$bump_type" = "minor" ]; then
    ((scVersionMinor++)) || true
else
    previousVersionName=`grep '^            versionName = "' "$build_gradle" | sed 's|^            versionName = "||;s|"$||'`
    previousElVersionAppend=`echo "$previousVersionName"|sed 's|^[^-]*||'`
    if [ "$previousElVersionAppend" = "$sc_el_version_append" ]; then
        ((scVersionMinor++)) || true
    else
        ((scVersionMajor++)) || true
        scVersionMinor=0
    fi
fi

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
        lastTestVersionCode="$(echo "$lastTestVersionCode / 10" | bc)"
    else
        read -p "Enter versionCode of last test version: " lastTestVersionCode
    fi
    while [ "$lastTestVersionCode" -ge "$versionCode" ]; do
        versionCode=$((versionCode + 10))
    done
fi

# Hardcoded "0" to be bumped to "2" once SCNext gets encouraged over legacy
sc_base_version="0.$scVersionMajor.$scVersionMinor"

if [ "$release_type" = "test" ]; then
    version="$sc_base_version-test${testVersionCount}${sc_el_version_append}"
else
    version="${sc_base_version}${sc_el_version_append}"
fi

new_tag="sc_v$version"
abiExtra=0

if ((preview)); then
    echo "versionCode $versionCode$abiExtra"
    echo "versionName $version"
    exit 0
fi

set_prop "scVersionMajor" "$scVersionMajor"
set_prop "scVersionMinor" "$scVersionMinor"
set_prop "versionCode" "$versionCode"
set_prop "versionName" "\"$version\""



#
# Generate changelog
#

git_changelog() {
    local git_args="$1"

    git log $git_args --pretty=format:"- %s" "$last_tag".. --committer="$(git config user.name)" \
        | sed "s|Merge tag '\\(.*\\)'.*|Update codebase to Element X \1|" \
        | grep -v "Merge .*branch" \
        | grep -v "Automatic" \
        | grep -v 'merge_helpers\|README\|increment_version' \
        | grep -v 'Increment version' \
        | grep -v "\\.sh" \
        | grep -v "\\.md" \
        | grep -v "Added translation using Weblate" \
        | grep -v "Translated using Weblate" \
        | grep -v "weblate/main" \
        | grep -v "\\[.*merge.*\\]" \
        | grep -v "Disable Android Auto supports" \
        | grep -v "Switch to alternative Schil" \
        | grep -vi "fastlane" \
        | grep -vi "gitignore" \
        | grep -v "\\[gplay-release\\]" \
        || echo "No significant changes since the last release"
}

changelog_dir=.fastlane/metadata/android/en-US/changelogs
changelog_file="$changelog_dir/$versionCode$abiExtra.txt"
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

for changelogAbi in 1 2 3 4; do
    changelog_copy="$changelog_dir/$versionCode$changelogAbi.txt"
    cp "$changelog_file" "$changelog_copy"
done

git add -A
if [ "$release_type" = "test" ]; then
    git commit -m "Test version $versionCode"
else
    git commit -m "Increment version"
    git tag "$new_tag" -m "Version $version (${versionCode}0)

$(cat "$changelog_file")"
fi

if ((do_translation_pull)); then
    echo "Updating weblate repo..."
    translation pull
fi

popd > /dev/null
