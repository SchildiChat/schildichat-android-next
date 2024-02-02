#!/bin/bash

# Dependencies:
# inkscape for svg -> png conversions
# imagemagick for additional png operations

mydir="$(dirname "$(realpath "$0")")"

extension=".png"

# In order to convert adaptive icon foreground to regular icon,
# reduce export area by 2/3
non_adaptive_area="36:36:180:180"

export_files() {
    newfile="$(basename "$file" .svg)$extension"
    export_files_custom "$newfile" -C "$@"
}
export_files_custom() {
    newfile="$1"
    shift
    mkdir -p $base_folder-mdpi
    mkdir -p $base_folder-hdpi
    mkdir -p $base_folder-xhdpi
    mkdir -p $base_folder-xxhdpi
    mkdir -p $base_folder-xxxhdpi
    inkscape "$file" --export-filename="$base_folder-mdpi/$newfile" --export-dpi=$dpi "$@"
    inkscape "$file" --export-filename="$base_folder-hdpi/$newfile" --export-dpi=$(($dpi*3/2)) "$@"
    inkscape "$file" --export-filename="$base_folder-xhdpi/$newfile" --export-dpi=$(($dpi*2)) "$@"
    inkscape "$file" --export-filename="$base_folder-xxhdpi/$newfile" --export-dpi=$(($dpi*3)) "$@"
    inkscape "$file" --export-filename="$base_folder-xxxhdpi/$newfile" --export-dpi=$(($dpi*4)) "$@"
}


# Notification icon
# UPDATE: use xml directly just like upstream

#base_folder="$mydir/../libraries/designsystem/src/sc/res/drawable"
#dpi=96
#file="$mydir/materialdesignicons/ic_notification_small.svg"
#export_files


# Launcher icon

# Upstream uses webp for launcher icons but inkscape doesn't like that, so we don't
#extension=".webp"
dpi=48 # 96/2

file="$mydir/ic_launcher_foreground.svg"
for variant in fdroidScBetaDebug fdroidScDefaultDebug fdroidScInternalRelease fdroidScBetaRelease fdroidScDefaultRelease; do
    base_folder="$mydir/../app/src/$variant/res/mipmap"
    export_files
done
# Monochrome and non-adaptive icons aren't handled per release type upstream, so suffices to overwrite once here
base_folder="$mydir/../app/src/sc/res/mipmap"
file="$mydir/ic_launcher_monochrome.svg"
export_files
file="$mydir/ic_launcher_foreground.svg"
dpi=32
export_files_custom "ic_launcher.png" --export-area="$non_adaptive_area"
dpi=96
base_folder="$mydir/../schildi/lib/src/main/res/drawable"
export_files_custom "sc_logo_atom.png" --export-area="$non_adaptive_area"


# Store icon:
# - remove adaptive-icon extra-padding by reducing export area by a factor of 2/3
# - fix size to 512x512
# - apply background manually with imagemagick
file="$mydir/ic_launcher_foreground.svg"
store_icon="$mydir/../.fastlane/metadata/android/en-US/images/icon.png"
inkscape "$file" --export-filename="$store_icon.tmp.png" --export-area="$non_adaptive_area" -w 512 -h 512
# Read gradient from actual vector drawable
get_bg_prop() {
    local prop="$1"
    cat "$mydir/../app/src/fdroidScDefaultRelease/res/drawable/ic_launcher_background.xml"|grep "$prop"=|sed 's|.*'"$prop"'=\"\([^\"]*\)".*|\1|'
}
bg_angle=`get_bg_prop angle`
bg_angle=`echo "90 - $bg_angle" | bc`
bg_startColor=`get_bg_prop startColor`
bg_endColor=`get_bg_prop endColor`
# Exclude chunks for reproducible generation (no update to git if re-running the script on same sources)
magick -define png:exclude-chunks=date,time -size 512x512 -define gradient:angle=$bg_angle gradient:$bg_startColor-$bg_endColor "$store_icon.bg.png"
magick composite -define png:exclude-chunks=date,time -gravity center "$store_icon.tmp.png" "$store_icon.bg.png" "$store_icon"
rm "$store_icon.tmp.png"
rm "$store_icon.bg.png"
