#!/bin/bash
# note: this requires inkscape to be on the path, for svg -> png conversions

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

base_folder="$mydir/../app/src/scRelease/res/mipmap"
dpi=48 # 96/2
file="$mydir/ic_launcher_foreground.svg"
#extension=".webp"
export_files
base_folder="$mydir/../app/src/scDebug/res/mipmap"
export_files
