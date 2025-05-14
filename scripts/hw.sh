#!/bin/bash
lshwParse () {
    type="${1}"
    str=$(lshw -c "$type" -numeric)
    numLines=$(printf "%s" "$str" | wc -l)
    items=()
    busitems=()
    
    
    parsing=""
    lineItr=0
    while read -r line; do
        lineItr=$((lineItr+1))
        lastLine=""
        if ((lineItr > numLines)); then
            parsing+="$line"$'\n'
            lastLine="1"
        fi
        if (printf "%s" "$line" | grep --quiet -e "^[[:space:]]*\*-.*[[:space:]]*$") || [ -n "$lastLine" ]; then
            if [ -n "$parsing" ]; then
                items+=("${parsing}")
            fi
            parsing=""
        fi
        parsing+="$line"$'\n'
    done <<< "$str"
    
    numItemsDec=$(printf "%d" "0x"${#items[@]})
    for ((i = 0 ; i < numItemsDec ; i++ )); do
        matchesType=""
        if (printf "%s" "${items[$i]}" | grep --quiet -e "^\*-$type:\?[0-9A-Fa-f]*[[:space:]]*\(DISABLED\)\?$"); then
            matchesType="1"
        fi
        isPhysical=""
        if (printf "%s" "${items[$i]}" | grep --quiet -e "^bus info:.*$"); then
            isPhysical="1"
        fi

        if [ -n "$matchesType" ] && [ -n "$isPhysical" ]; then
            busitems+=("${items[$i]}")
        fi        
    done
}
lshwDisk () {
    lshwParse "disk"
}
lshwDisplay () {
    lshwParse "display"
}
lshwNetwork () {
    lshwParse "network"
}
lshwNumBusItems () {
    printf "%s" "${#busitems[@]}"
}
lshwGetVendorIDFromBusItem () {
    itemNumber="${1}"
    result=""
    str=$(echo "${busitems[$itemNumber]}" | grep -e "^vendor:.*[^\[]\[.\+$" | sed 's/^vendor:.*[^\[]\[\([0-9A-Fa-f]\+\)\]$/\1/')
    if [ -n "$str" ]; then
        result="0000$str"
        result="${result: -4}"
    fi
    printf "%s" "$result"
}
lshwGetProductIDFromBusItem () {
    itemNumber="${1}"
    result=""
    str=$(echo "${busitems[$itemNumber]}" | grep -e "^product:.*[^\[]\[.\+$" | sed 's/^product:.*[^\[]\[[0-9A-Fa-f]\+:\([0-9A-Fa-f]\+\)\]$/\1/')
    if [ -n "$str" ]; then
        result="0000$str"
        result="${result: -4}"
    fi
    printf "%s" "$result"
}
lshwGetVersionFromBusItem () {
    itemNumber="${1}"
    result=""
    str=$(echo "${busitems[$itemNumber]}" | grep -e "^version:.*$" | sed 's/^version: \([0-9A-Za-z]\+\)$/\1/')
    if [ -n "$str" ]; then
        result=$str
    fi
    printf "%s" "$result"
}
lshwGetSerialFromBusItem () {
    itemNumber="${1}"
    result=""
    str=$(echo "${busitems[$itemNumber]}" | grep -e "^serial:.*$" | sed 's/^serial: \([0-9A-Za-z:-]\+\)$/\1/')
    if [ -n "$str" ]; then
        result=$str
    fi
    printf "%s" "$result"
}
lshwGetLogicalNameFromBusItem () {
    itemNumber="${1}"
    result=""
    str=$(echo "${busitems[$itemNumber]}" | grep -e "^logical name:.*$" | sed 's/^logical name: \(.\+\)$/\1/')
    if [ -n "$str" ]; then
        result=$str
    fi
    printf "%s" "$result"
}
lshwGetVendorNameFromBusItem () {
    itemNumber="${1}"
    result=""
    str=$(echo "${busitems[$itemNumber]}" | grep -e "^vendor:.*$" | sed 's/^vendor: \([0-9A-Za-z -]\+\) \?\[\?.*$/\1/')
    if [ -n "$str" ]; then
        result=$str
    fi
    printf "%s" "$result"
}
lshwGetProductNameFromBusItem () {
    itemNumber="${1}"
    result=""
    str=$(echo "${busitems[$itemNumber]}" | grep -e "^product:.*$" | sed 's/^product: \([0-9A-Za-z\(\) -]\+\) \?\[\?.*$/\1/')
    if [ -n "$str" ]; then
        result=$str
    fi
    printf "%s" "$result"
}
lshwBusItemBluetoothCap () {
    itemNumber="${1}"
    result=""
    if (echo "${busitems[$itemNumber]}" | grep --quiet "capabilities.*bluetooth"); then
        result="1"
    fi
    printf "%s" "$result"
}
lshwBusItemEthernetCap () {
    itemNumber="${1}"
    result=""
    if (echo "${busitems[$itemNumber]}" | grep --quiet "capabilities.*ethernet"); then
        result="1"
    fi
    printf "%s" "$result"
}
lshwBusItemWirelessCap () {
    itemNumber="${1}"
    result=""
    if (echo "${busitems[$itemNumber]}" | grep --quiet "capabilities.*wireless"); then
        result="1"
    fi
    printf "%s" "$result"
}
ethtoolPermAddr () {
    iface="${1}"
    str=$(ethtool -P "$iface" 2> /dev/null | grep -e "^Perm.*$" | sed 's/^Permanent address: \([0-9a-f:]\+\)$/\1/')
    printf "%s" "$str"
}
standardizeMACAddr () {
    mac=$(printf "%s" "${1}" | tr -d "[[:space:]]:-" | awk '{ print toupper($0) }')
    printf "%s" "$mac"
}
#lshwParse "disk"
#lshwNetwork
#echo ${items[0]}
#echo ${#busitems[@]}
#echo ${busitems[*]}
#ven=$(lshwGetVendorIDFromBusItem "0")
#prod=$(lshwGetProductIDFromBusItem "0")
#rev=$(lshwGetVersionFromBusItem "0")
#serial=$(lshwGetSerialFromBusItem "0")
#venName=$(lshwGetVendorNameFromBusItem "0")
#prodName=$(lshwGetProductNameFromBusItem "0")
#bluetoothCap=$(lshwBusItemBluetoothCap)
#ethernetCap=$(lshwBusItemEthernetCap)
#wirelessCap=$(lshwBusItemWirelessCap)
#echo $ven
#echo $prod
#echo $rev
#echo $serial
#echo $venName
#echo $prodName
#echo $bluetoothCap
#echo $ethernetCap
#echo $wirelessCap
