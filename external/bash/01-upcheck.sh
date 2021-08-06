#!/bin/bash
#----------------------------------------------------------------
# Name:     01-upcheck.sh
# Args:     $1 = url or filename with url list e.g. example.com
# Desc:     Prints one or multiple lines with information if
#           server is up or not.
#           In file mode blank lines are ignored; line comments
#           available using '#'.
#           Please note that file must end with blank line.
#
# Deps:     ping
#
# Author:   Frantisek Stepanovsky
# Email:    stepanovsky.frantisek@gmail.com
# Created:  01/10/2020
# License:  MIT
#
# Last update:  01/10/2020
# ---------------------------------------------------------------

set -eu
set -o pipefail

readonly script_name=$(basename "$0")
readonly default_file="servers.txt"

readonly RED='\e[31m'
readonly GREEN='\e[32m'
readonly NC='\e[0m'


function print_help {
    cat <<- EOF
$script_name [server_addres | file.txt]
EOF
}

function check_deps {
    hash ping 2>/dev/null || { echo "ping not found"; exit 1; }
}

function ping_server_status_line {
    local res
    local rc
    [[ -z "${1:-}" ]] && return 1
    res=$(ping -c1 "${1}" 2>/dev/null)
    rc=$?

    [[ $rc -eq 2 ]] && { printf "%-40s ${RED}no response${NC}" "${1}"; return 0; }
    [[ $rc -eq 0 ]] && { printf "%-40s ${GREEN}OK${NC}" "${1}"; return 0; }
    # other ret codes are system depended
    printf "%-40 ${RED}error${NC} code: %-3s (consult with <sysexits.h>)" "${1}" "${rc}"
}

check_deps

ARG="${1:-$default_file}"


if [[ -r "$ARG" ]]; then
    while IFS= read -r line; do
        [[ "$line" =~ "#".* || -z "$line" ]] && continue
        res=$(ping_server_status_line "$line")
        echo "${res}"
    done < "${ARG}"
elif [[ "${ARG}" = "${default_file}" ]]; then
    echo "Invalid input parameter."
    exit 1
else
    res=$(ping_server_status_line "$ARG")
    echo "${res}"
fi