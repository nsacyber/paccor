#!/bin/bash
dmidecodeHandles () {
    type="${1}"
    str=$(dmidecode -t "$type" | grep -e '^Handle.*' | sed 's/Handle \(0x[0-9A-F^,]*\),.*/\1/' | tr "\n\r\t" ' ' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
    local IFS=' '
    read -r -a tableHandles <<< "$str"

    echo "${tableHandles[*]}"
}
dmidecodeData () {
    handle="${1}"
    if  [[ $handle =~ ^0x[0-9A-Fa-f]+$ ]]; then
        str=$(dmidecode -H "$handle" -u | awk '/Header and Data:/{f=1;next} /Strings/{f=0} f' | tr "\n\r\t" ' ' | tr -s ' ' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        local IFS=' '
        read -r -a tableData <<< "$str"

        echo "${tableData[*]}"
    fi
}
dmidecodeStrings () {
    handle="${1}"
    if  [[ $handle =~ ^0x[0-9A-Fa-f]+$ ]]; then
        str=$(dmidecode -H "$handle" -u | awk '/Strings/{f=1;next} /^\w+$/{f=0} f' | sed 's/\t//g' | sed ':a;N;$!ba;s/\n/\t/g' | sed 's/[[:space:]]00[[:space:]]/&"/g' | sed 's/"[^\t]*/&"/g' | sed 's/\t/\n/g' | sed 's/^[^"]*$//g' | sed 's/^\w+//g' | sed 's/""/"/g' | sed '/^[[:space:]]*$/d')
        mapfile -t tableStrings <<< "$str"

        local IFS=$'\n'
        echo "${tableStrings[*]}"
    fi
}
dmidecodeNumHandles () {
    local tableHandles=("$@")
    printf "%s" "${#tableHandles[@]}"
}
dmidecodeGetByte () {
    local index="${1}"
    shift
    local tableData=("$@")
    index=$(printf "%d" "$index")
    printf "%s" "${tableData[$index]}"
}
dmidecodeGetString () {
    local strref="${1}"
    shift
    local tableStrings=("$@")
    str=""
    if [[ $strref =~ ^[0-9A-Fa-f]+$ ]]; then
        strrefDec=$(printf "%d" "0x""$strref")
        lenDec=$(printf "%d" "0x"${#tableStrings[@]})
        if [ "$strrefDec" -le "$lenDec" ] && [ "$strrefDec" -gt 0 ]; then
            str="${tableStrings[$strrefDec-1]}"
            str=$(printf "%s" "$str" | sed 's/^[ \t]*"\?//;s/"\?[ \t]*$//')
        fi
    fi
    printf "%s" "$str"
}


# Examples:
#IFS=' ' read -r -a tableHandles <<< "$(dmidecodeHandles "1")"
#numHandles=$(dmidecodeNumHandles "${tableHandles[@]}")
#echo "numHandles: $numHandles"
#printf "tableHandles: %s\n" "${tableHandles[@]}"
#
#IFS=' ' read -r -a tableData <<< "$(dmidecodeData "${tableHandles[0]}")"
#printf "tableData: %s\n" "${tableData[@]}"
#
#mapfile -t tableStrings <<< "$(dmidecodeStrings "${tableHandles[0]}")"
#printf "tableStrings: %s\n" "${tableStrings[@]}"
#
#manufacturer="$(dmidecodeGetString "$(dmidecodeGetByte "0x4" "${tableData[@]}")" "${tableStrings[@]}")"
#model=$(dmidecodeGetString "$(dmidecodeGetByte "0x5" "${tableData[@]}")" "${tableStrings[@]}")
#serial=$(dmidecodeGetString "$(dmidecodeGetByte "0x7" "${tableData[@]}")" "${tableStrings[@]}")
#revision=$(dmidecodeGetString "$(dmidecodeGetByte "0x6" "${tableData[@]}")" "${tableStrings[@]}")
#printf "manufacturer: %s\n" "$manufacturer"
#printf "model: %s\n" "$model"
#printf "serial: %s\n" "$serial"
#printf "revision: %s\n" "$revision"
