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


base_folder="$mydir/../app/src/release_sc/res/mipmap"
dpi=48 # 96/2
file="$mydir/ic_launcher_foreground.svg"
#extension=".webp"
export_files
base_folder="$mydir/../app/src/debug_sc/res/mipmap"
export_files
