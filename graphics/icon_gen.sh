#!/bin/bash

# Dependencies:
# inkscape for svg -> png conversions
# imagemagick for additional png operations

mydir="$(dirname "$(realpath "$0")")"

extension=".png"

export_files() {
    newfile="$(basename "$file" .svg)$extension"
    mkdir -p $base_folder-mdpi
    mkdir -p $base_folder-hdpi
    mkdir -p $base_folder-xhdpi
    mkdir -p $base_folder-xxhdpi
    mkdir -p $base_folder-xxxhdpi
    inkscape "$file" --export-filename="$base_folder-mdpi/$newfile" -C --export-dpi=$dpi
    inkscape "$file" --export-filename="$base_folder-hdpi/$newfile" -C --export-dpi=$(($dpi*3/2))
    inkscape "$file" --export-filename="$base_folder-xhdpi/$newfile" -C --export-dpi=$(($dpi*2))
    inkscape "$file" --export-filename="$base_folder-xxhdpi/$newfile" -C --export-dpi=$(($dpi*3))
    inkscape "$file" --export-filename="$base_folder-xxxhdpi/$newfile" -C --export-dpi=$(($dpi*4))
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
base_folder="$mydir/../app/src/scRelease/res/mipmap"
export_files
base_folder="$mydir/../app/src/scDebug/res/mipmap"
export_files
file="$mydir/ic_launcher_monochrome.svg"
base_folder="$mydir/../app/src/sc/res/mipmap"
export_files


# Store icon:
# - remove adaptive-icon extra-padding by reducing export area by a factor of 2/3
# - fix size to 512x512
# - apply background manually with imagemagick
file="$mydir/ic_launcher_foreground.svg"
store_icon="$mydir/../.fastlane/metadata/android/en-US/images/icon.png"
inkscape "$file" --export-filename="$store_icon.tmp.png" --export-area=36:36:180:180 -w 512 -h 512
# Read gradient from actual vector drawable
get_bg_prop() {
    local prop="$1"
    cat "$mydir/../app/src/scRelease/res/drawable/ic_launcher_background.xml"|grep "$prop"=|sed 's|.*'"$prop"'=\"\([^\"]*\)".*|\1|'
}
bg_angle=`get_bg_prop angle`
bg_startColor=`get_bg_prop startColor`
bg_endColor=`get_bg_prop endColor`
# Exclude chunks for reproducible generation (no update to git if re-running the script on same sources)
magick -define png:exclude-chunks=date,time -size 512x512 -define gradient:angle=$bg_angle gradient:$bg_startColor-$bg_endColor "$store_icon.bg.png"
magick composite -define png:exclude-chunks=date,time -gravity center "$store_icon.tmp.png" "$store_icon.bg.png" "$store_icon"
rm "$store_icon.tmp.png"
rm "$store_icon.bg.png"
