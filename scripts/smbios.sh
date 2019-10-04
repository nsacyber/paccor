#!/bin/bash
dmidecodeHandles () {
    type="${1}"
    str=$(dmidecode -t "$type" | grep -e '^Handle.*' | sed 's/Handle \(0x[0-9A-F^,]*\),.*/\1/' | tr "\n\r\t" ' ' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
    old="$IFS"
    IFS=' '
    tableHandles=($str)
    IFS="$old"
}
dmidecodeData () {
    handle="${1}"
    str=$(dmidecode -H "$handle" -u | awk '/Header and Data:/{f=1;next} /Strings/{f=0} f' | tr "\n\r\t" ' ' | tr -s ' ' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
    old="$IFS"
    IFS=' '
    tableData=($str)
    IFS="$old"
}
dmidecodeStrings () {
    handle="${1}"
    str=$(dmidecode -H "$handle" -u | awk '/Strings/{f=1;next} /^\w+$/{f=0} f' | sed 's/^[^"]*$//g' | sed 's/.*"\(.*\)".*/\1/g' | sed 's/^\w+//g' | sed '/^[[:space:]]*$/d')
    old="$IFS"
    IFS=$'\n'
    tableStrings=($str)
    IFS="$old"
}
dmidecodeParseHandle () {
    handle="${1}"
    dmidecodeData "$handle"
    dmidecodeStrings "$handle"
}
dmidecodeNumHandles () {
    printf "${#tableHandles[@]}"
}
dmidecodeParseTypeAssumeOneHandle () {
    type="${1}"
    dmidecodeHandles "$type" > /dev/null
    dmidecodeParseHandle "${tableHandles[0]}"
}
dmidecodeGetByte () {
    index="${1}"
    index=$(printf "%d" $index)
    printf "${tableData[$index]}"
}
dmidecodeGetString () {
    strref="${1}"
    str=""
    strrefDec=$(printf "%d" "0x""$strref")
    lenDec=$(printf "%d" "0x"${#tableStrings[@]})
    if [ $strrefDec -le $lenDec ] && [ $strrefDec -gt 0 ]; then
        str="${tableStrings[$strrefDec-1]}"
    fi
    printf "$str"
}



#dmidecodeHandles "4"
#numHandles=$(dmidecodeNumHandles)
#echo $numHandles
#echo ${tableHandles[*]}

#dmidecodeStrings "${tableHandles[0]}"

#echo ${tableStrings[0]}

#dmidecodeData "${tableHandles[0]}"

#manufacturer=${tableData[4]}

#echo "${tableStrings[$manufacturer]}"
#dmidecodeHandles "2"
#dmidecodeParseHandle "${tableHandles[0]}"
#result=$(dmidecodeGetByte "9")
#result2=$(dmidecodeGetString $result)
#echo $result2
#result3=$(dmidecodeGetString $(dmidecodeGetByte "7"))
#echo $result3
