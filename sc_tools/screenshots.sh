#!/bin/bash

mydir="$(dirname "$(realpath "$0")")"
outdir="$(realpath "$mydir/../metadata/en-US/images/phoneScreenshots")"
sdir="$(realpath "$mydir/../schildi/screenshots/src/main/kotlin/chat/schildi/screenshots/")"

s_locale="en-US"

handle_file() {
    local f="$1"
    local s_count="$2"
    local count=0
    read -p "Enter to open $(basename "$f")"
    android-studio "$f"
    echo "Please make sure compose previews are up-to-date!"
    while ((s_count > 0)); do
        ((count++)) || true
        local outfile="$outdir/$(basename "$f" ".kt")_$count.png"
        read -p "Enter after clicking \"Copy Image\" for $(basename "$outfile")"
        wl-paste -t "image/png" > "$outfile"
        ((s_count--)) || true
    done
}

handle_file "$sdir/ScOverviewScreenshots.kt" 1
handle_file "$sdir/ScConversationScreenshots.kt" 2

# Final permutation
count=0
order_screenshot() {
    local f="$1"
    ((count++)) || true
    mv "$outdir/$f" "$outdir/${count}_${s_locale}.png"
}

order_screenshot "ScOverviewScreenshots_1.png"
order_screenshot "ScConversationScreenshots_1.png"
order_screenshot "ScConversationScreenshots_2.png"
