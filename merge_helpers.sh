#!/bin/bash

build_gradle="app/build.gradle.kts"

get_prop() {
    local prop="$1"
    local file="$2"
    if [ -z "$file" ]; then
      file="$build_gradle"
    fi
    cat "$file" | grep "$prop = " | sed "s|.*$prop = ||"
}
set_prop() {
    local prop="$1"
    local value="$2"
    if grep -q "$prop =" "$build_gradle"; then
        local equals="= "
        local not_equals=""
    else
        local equals=""
        # Don't touch lines that have an equals in it, but not for this prop
        local not_equals="/=/! "
    fi
    sed -i "$not_equals""s|\($prop $equals\).*|\1$value|g" "$build_gradle"
}

find_last_commit_for_title() {
    local title="$1"
    git log --oneline --author=SpiritCroc | grep "$title" | head -n 1 | sed 's| .*||'
}

revert_last() {
    local title="$1"
    shift
    git revert --no-edit `find_last_commit_for_title "$title"` $@
}

require_clean_git() {
    if [ "$NO_REQUIRE_CLEAN_GIT" = "y" ]; then
        return
    fi
    uncommitted=`git status --porcelain`
    if [ ! -z "$uncommitted" ]; then
        echo "Uncommitted changes are present, please commit first!"
        exit 1
    fi
}

upstream_common_base() {
    local base="$1"
    if [ -z "$base" ]; then
        local base="HEAD"
    fi
    local merged_develop=`git merge-base "$base" upstream/develop`
    local merged_main=`git merge-base "$base" upstream/main`
    >&2 echo "Last merged main: $(git log -1 --oneline "$merged_main")"
    >&2 echo "Last merged develop: $(git log -1 --oneline "$merged_develop")"
    # Is latest main or develop merge more up-to-date?
    if [ "$merged_develop" = "$merged_main" ]; then
        >&2 echo "Last merged: upstream/main = upstream/develop"
        commit="$merged_main"
    elif git merge-base --is-ancestor "$merged_develop" "$merged_main"; then
        >&2 echo "Last merged: upstream/main > upstream/develop"
        commit="$merged_main"
    elif git merge-base --is-ancestor "$merged_main" "$merged_develop"; then
        >&2 echo "Last merged: upstream/develop > upstream/main"
        commit="$merged_develop"
    else
        # Commented out: code works (I think), but may be safer to not assume such things
        #develop_timestamp=`git show -s --format=%ct "$merged_develop"`
        #main_timestamp=`git show -s --format=%ct "$merged_main"`
        #if ((develop_timestamp > main_timestamp)); then
        #    >&2 echo "Last merged: upstream/develop (later ts)"
        #    commit="$merged_develop"
        #elif ((develop_timestamp < main_timestamp)); then
        #    >&2 echo "Last merged: upstream/main (later ts)"
        #    commit="$merged_main"
        #else
            >&2 echo "ERROR: don't know how to compare main and develop upstream branches"
            exit 1
        #fi
    fi
    echo "$commit"
}

upstream_previous_common_base() {
    local base="$(upstream_common_base 2>/dev/null)"
    if [ -z "$base" ]; then
        >&2 echo "ERROR: don't know how to compare main and develop upstream branches"
        exit 1
    fi
    upstream_common_base "$base~1"
}

upstream_latest_tag() {
    git describe --abbrev=0 upstream/main --tags
}
upstream_previous_tag() {
    #git describe --abbrev=0 `upstream_latest_tag`~1 --tags
    #downstream_latest_tag | sed 's|sc_\(v.*\).sc.*|\1|'
    git log | grep "Merge tag 'v.*' into " | head -n 1 |sed "s|.*Merge tag '\\(v.*\\)' into .*|\1|"
}
downstream_latest_tag() {
    local commit="HEAD"
    while true; do
        local tag=`git tag --points-at "$commit" | grep "^sc_v" | head -n 1`
        if [ ! -z "$tag" ]; then
            echo "$tag"
            break
        else
            commit="$commit^1"
        fi
    done
}

upstream_diff() {
    #local latest_tag=`upstream_latest_tag`
    #local previous_tag=`upstream_previous_tag`
    if git rev-parse MERGE_HEAD > /dev/null; then
        local latest_tag=`git rev-parse MERGE_HEAD`
        local previous_tag=`upstream_common_base`
    else
        local latest_tag=`upstream_common_base`
        local previous_tag=`upstream_previous_common_base`
    fi
    git diff "$previous_tag".."$latest_tag" "$@"
}
upstream_log() {
    #local latest_tag=`upstream_latest_tag`
    #local previous_tag=`upstream_previous_tag`
    if git rev-parse MERGE_HEAD > /dev/null; then
        local latest_tag=`git rev-parse MERGE_HEAD`
        local previous_tag=`upstream_common_base`
    else
        local latest_tag=`upstream_common_base`
        local previous_tag=`upstream_previous_common_base`
    fi
    git log "$previous_tag".."$latest_tag" "$@"
}

downstream_upstream_diff() {
    local previous_tag=`upstream_previous_tag`
    local downstream_tag=`downstream_latest_tag`
    git diff "$previous_tag".."$downstream_latest_tag" "$@"
}


# Opposite to restore_sc
restore_upstream() {
    local f="$(basename "$1")"
    local path="$(dirname "$1")"
    local sc_f="tmp_sc_$f"
    local upstream_f="upstream_$f"
    if [ -e "$path/$f" ]; then
        mv "$path/$f" "$path/$sc_f"
    fi
    if [ -e "$path/$upstream_f" ]; then
        mv "$path/$upstream_f" "$path/$f"
    fi
}

# Oposite to restore_upstream
restore_sc() {
    local f="$(basename "$1")"
    local path="$(dirname "$1")"
    local sc_f="tmp_sc_$f"
    local upstream_f="upstream_$f"
    if [ -e "$path/$f" ]; then
        mv "$path/$f" "$path/$upstream_f"
    fi
    if [ -e "$path/$sc_f" ]; then
        mv "$path/$sc_f" "$path/$f"
    fi
}

await_edit() {
    local f="$1"
    shift 1
    if [ ! -z "$GRAPHICAL_EDITOR" ]; then
        $GRAPHICAL_EDITOR "$f"
        read -p "Press enter when done"
    elif [ ! -z "$VISUAL" ]; then
        $VISUAL "$f"
    elif [ ! -z "$EDITOR" ]; then
        $EDITOR "$f"
    else
        read -p "No editor set, please edit $f manually, and press enter when done"
    fi
}
