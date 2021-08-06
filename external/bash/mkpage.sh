#!/bin/bash
#----------------------------------------------------------------
# Name:     mkpage.sh
# Args:     $1 = command [dcd, download, convert]
#           $2 = uuid or path to imageserver
#           $3 = output path (directory) 
# Desc:     Downloads images from imageserver server,
#           additionaly can covert to tiff from jp2
#           using kdu_expand utility.
#
# Deps:     curl, xmllint, sed, rm, scp, kdu_expand
#
# Author:   Frantisek Stepanovsky
# Email:    stepanovsky.frantisek@gmail.com
# Created:  18/02/2019
# License:  MIT
#
# Last update:  06/08/2021
# ---------------------------------------------------------------

set -euo pipefail

# ---------------------------------------------------------------
# Setup variables

SSH_USER=user
SSH_KEY=~/.ssh/id_rsa

FCUSER=fedoraAdmin
FCPASS=fedoraPassword
FCBASEURL=fedora.example.com
IMAGESERVER_SERVER_FRONT=is.example.com

# ---------------------------------------------------------------

GREEN="\e[32m"
RED="\e[31m"
DEFAULT="\e[39m"

UUID="${2- }"
MZK_PREFIX=/mnt/imageserver
NDK_PREFIX=/storage

function create_ndk_path {
    cesta=$1
    cesta=$(echo "$cesta" | sed -e 's/\/NDK\///')
    year=(${cesta//\// })
    
    case ${year[0]} in
        2021)
        NDK_PREFIX="$NDK_PREFIX/ndk2021" ;;
        2020)
        NDK_PREFIX="$NDK_PREFIX/ndk2020" ;;
        2019)
        NDK_PREFIX="$NDK_PREFIX/ndk2019" ;;
        2018 | 2017)
        NDK_PREFIX="$NDK_PREFIX/ndk04" ;;
        2016 | 2015)
        NDK_PREFIX="$NDK_PREFIX/ndk03" ;;
        2014)
        NDK_PREFIX="$NDK_PREFIX/ndk02" ;;
        2013 | 2012)
        NDK_PREFIX="$NDK_PREFIX/ndk01" ;;
    esac
}

# accepts: $1 filename_path, $2 output_path
function convert {
    $(kdu_expand -i "$1" -o "$2" -quiet)
    local ret_code
    ret_code=$?
    if [[ $ret_code -ne 0 ]]; then
        echo -e "    Convert $(basename $1)  [$RED FAIL $DEFAULT]" >&2
        return 1
    fi
    echo -e "    Convert $(basename $1)  [$GREEN  OK  $DEFAULT]"
    return 0
}

# accepts only $1 output_path
function download {

    if [[ $UUID == "uuid:"* ]]; then
    # get image path
        cesta=$(curl -s -u "$FCUSER":"$FCPASS" http://"$FCBASEURL"/fedora/objects/"$UUID"/datastreams/IMG_FULL?format=xml | sed -e 's/ xmlns.*=".*"//g' | xmllint --xpath 'string(/datastreamProfile/dsLocation)' - | sed -e 's/http:\/\/imageserver.mzk.cz//' | sed -e 's/\/big.jpg//')
        if [[ "$cesta" == "/NDK"* ]]; then
            create_ndk_path "$cesta"
            $(scp -i "$SSH_KEY" "$SSH_USER"@"$IMAGESERVER_SERVER_FRONT":"$NDK_PREFIX/$cesta".jp2 "$1")
            if [[ $? -eq 0 ]]; then
                echo -e "NDK $UUID\t      [$GREEN  OK  $DEFAULT]"
            else
                echo -e "NDK $UUID\t      [$RED FAIL $DEFAULT]"
                return 1
            fi
        else
            $(scp -i "$SSH_KEY" "$SSH_USER"@"$IMAGESERVER_SERVER_FRONT":"$MZK_PREFIX$cesta".jp2 "$1")
            if [[ $? -eq 0 ]]; then
                echo -e "MZK $UUID\t      [$GREEN  OK  $DEFAULT]"
            else
                echo -e "MZK $UUID\t      [$RED FAIL $DEFAULT]"
                return 1
            fi
        fi
    elif [[ "$UUID" == *"imageserver.mzk.cz"* ]]; then
        cesta=""
        if [[ "$UUID" == "https"* ]]; then
            cesta=$(echo "$UUID" | sed -e 's/https:\/\/imageserver.mzk.cz//')
        elif [[ $UUID == "http"* ]]; then
            cesta=$(echo "$UUID" | sed -e 's/http:\/\/imageserver.mzk.cz//')
        fi
    
        if [[ "$cesta" == "/NDK"* ]]; then
            create_ndk_path "$cesta"
            $(scp -i "$SSH_KEY" "$SSH_USER"@"$IMAGESERVER_SERVER_FRONT":"$NDK_PREFIX/$cesta".jp2 "$1")
            if [[ $? -eq 0 ]]; then
                echo -e "NDK $UUID\t      [$GREEN  OK  $DEFAULT]"
            else
                echo -e "NDK $UUID\t      [$RED FAIL $DEFAULT]"
                return 1
            fi
        else
            $(scp -i "$SSH_KEY" "$SSH_USER"@"$IMAGESERVER_SERVER_FRONT":"$MZK_PREFIX$cesta".jp2 "$1")
            if [[ $? -eq 0 ]]; then
                echo -e "MZK $UUID\t      [$GREEN  OK  $DEFAULT]"
            else
                echo -e "MZK $UUID\t      [$RED FAIL $DEFAULT]"
                return 1
            fi
        fi
    else
        echo "Unknown image source" >&2
        return 1
    fi
    
    return 0
}

# download-convert-delete
# accepts $1 $2
# $1  source
# $2  target dir
function dcd {
    download "$2"
    if [[ $? -eq 0 ]]; then
        # get filename
        local filename="$2/$(basename "$cesta")"
        if [[ -f "$filename.jp2" ]]; then
            filename="$filename.jp2"
            local newfilename=$(echo "$filename" | sed -e 's/\.jp2/\.tif/')
            convert "$filename" "$newfilename"
            if [[ $? -eq 0 ]]; then exit $(rm "$filename"); fi
        fi
    else
        exit 1
    fi
}

check_utils() {
    local err=0
    declare -a missing
    hash curl 2>/dev/null       || { err=1; missing+=('curl'); }
    hash xmllint 2>/dev/null    || { err=1; missing+=('xmllint'); }
    hash kdu_expand 2>/dev/null || { err=1; missing+=('kdu_expand'); }
    hash sed 2>/dev/null        || { err=1; missing+=('sed'); }
    hash rm 2>/dev/null         || { err=1; missing+=('rm'); }
    hash scp 2>/dev/null        || { err=1; missing+=('scp'); }
    if [[ $err -eq 1 ]]; then
	echo >&2 "Error missing tools, please install:"
	echo >&2
	for item in ${missing[@]}; do
	    echo >&2 -e "\t$item"
	done
	exit 1;
    fi
    
}

function phelp {
    echo -e "$(basename $0) [download|convert|dcd] image_id output_path\n"
    echo -e " download    only download image"
    echo -e " convert     convert using kdu_expand"
    echo -e " dcd         download->convert->delete"
    
    exit 0
}

check_utils

# handle commandline
case "${1-help}" in
    download)
        download "$3";;
    convert)
        convert "$2" "$3";;
    dcd)
        dcd "$2" "$3";;
    *)
        phelp;;
esac

