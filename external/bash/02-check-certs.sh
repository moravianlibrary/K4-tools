#!/bin/bash
#----------------------------------------------------------------
# Name:     02-check-certs.sh
# Args:     $1 = url or filename with url list e.g. example.com
# Desc:     Prints one or multiple lines with information about
#           certificate expiration.
#           In file mode blank lines are ignored; line comments
#           available using '#'.
#           Please note that file must end with blank line.
#
# Deps:     openssl, sed, date
#
# Author:   Frantisek Stepanovsky
# Email:    stepanovsky.frantisek@gmail.com
# Created:  02/10/2020
# License:  MIT
#
# Last update:  05/10/2020
# ---------------------------------------------------------------

set -euf
set -o pipefail

readonly script_name=$(basename "$0")
readonly default_file="services.txt"

readonly RED='\e[31m'
readonly GREEN='\e[32m'
readonly ORANGE='\e[33m'
readonly NC='\e[0m'
readonly BLINK='\e[5m'
readonly RBLINK='\e[25m'

function print_help {
    cat <<- EOF
$script_name [URL | file.txt]

Tries to get certificate expiration information.

Input options:
    o  single url specified on command line
    o  text file containing url list
EOF
}

# Check if required packages are avaiable
# othrewise stop execution and report missing util
function check_deps {
    # FIXME: resolve and print errs all at once
    hash openssl 2>/dev/null || { echo "openssl not found"; exit 1; }
    hash sed 2>/dev/null || { echo "sed not found"; exit 1; }
    hash date 2>/dev/null || { echo "date not found"; exit 1; }

}

function get_timestamp () {
    date +%s
}


function date_to_timestamp {
    local date=${1:-}
    [[ -z "${date}" ]] && return 1

    true
    local res
    res=$(date -d "${date}" +"%s" 2>/dev/null)
    [[ $? -eq 0 ]] || return 1
    echo "${res}"
}

function get_domain_cert_expiration {
    local host="${1:-}"
    [[ -z "$host" ]] && return 1

    local res # avoid overriding return value from command with local $?
    res=$(echo | openssl s_client -servername "${host}" -connect "${host}":443 2>/dev/null | openssl x509 -noout -dates 2>/dev/null)
    [[ $? -eq 0 ]] || return 1                 # error while loading cert
    [[ "${res}" =~ "notBefore".* && "${res}" =~ .*"notAfter".* ]] || return 1

    res=$(echo "${res}" | sed 1d)              # remove notBefore line (first line)
    [[ "${res}" =~ "notAfter".* ]] || return 1 # test if output is according to plan
    res="${res//notAfter=/}"
    
    local timestamp
    timestamp=$(date_to_timestamp "$res")
    [[ $? -eq 0 ]] || return 1
    echo "$timestamp"
}

function is_file {
    if [[ -f ${1:-} ]]; then
        true
    else
        false
    fi
}

# Days remaining
#
# Accepts two timestamps and
# subtracts them ($2 - $1)
function days_remaining {
    local expiration
    local timestamp
    local remain
    timestamp=${1:-}
    expiration=${2:-}
    [[ -z "$expiration" || -z "$timestamp" ]] && return 1

    remain=$(((expiration-timestamp)/(3600*24)))
    echo "$remain"
}

function get_expiry_formated_line {
    [[ -z ${1:-} ]] && return 1
    local res
    res=$(get_domain_cert_expiration "$1")
    if [[ $? -ne 0 ]]; then
        echo -e "${1}\t\tcert probably not found"
        return 0
    fi
    res=$(days_remaining "$(get_timestamp)" "${res}")

    [[ $? -eq 0 ]] || { echo -e "${1}\t\tdate error"; return 0; }
    [[ ${res} -lt 0 ]] && { printf "%-40s ${RED}${BLINK}expired${NC}${RBLINK} %s days ago - please renew now!\n" "${1}" "$((res*(-1)))"; return 0; }
    [[ ${res} -eq 0 ]] && { printf "%-40s ${RED}${BLINK}expired${NC}${RBLINK} today - please renew now!\n" "${1}"; return 0; }
    # TODO: warning if 10 or less days remaining
    [[ ${res} -lt 20 ]] && { printf "%-40s ${ORANGE}warning${NC} %s days remaining\n" "${1}" "${res}"; return 0; }
    [[ ${res} -gt 0 ]] && { printf "%-40s ${GREEN}valid${NC} %4s days remaining\n" "${1}" "${res}"; return 0; }
    echo -e "${1}\t\tprocessing error"
    return 1
}

if [[ "${1:-}" == "-h" ]] || [[ "${1:-}" == "--help" ]]; then
    print_help
    exit 0
fi

check_deps

ARG="${1:-$default_file}"


if [[ -r "$ARG" ]]; then
    while IFS= read -r line; do
        [[ "$line" =~ "#".* || -z "$line" ]] && continue
        res=$(get_expiry_formated_line "$line")
        echo "${res}"
    done < "${ARG}"
elif [[ "${ARG}" = "${default_file}" ]]; then
    echo "Invalid input parameter."
    exit 1
else
    res=$(get_expiry_formated_line "$ARG")
    echo "${res}"
fi
