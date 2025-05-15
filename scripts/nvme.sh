#!/bin/bash
# Gather descriptors for NVMe devices  
nvmeParse () {
    str=$(nvme list -o json)
    count=$(echo "$str" | jq '.Devices | length')
    nvmeDevices=()

    for ((i = 0 ; i < count ; i++ )); do
        elementJson=$(echo "$str" | jq .Devices["$i"])
        nvmeDevices+=("$elementJson")        
    done
}
nvmeNumDevices () {
    printf "${#nvmeDevices[@]}"
}
nvmeGetModelNumberForDevice () {
    dev="${1}"
    str=$(echo "${nvmeDevices[$dev]}" | jq -r .ModelNumber)
    printf "%s" "$str"
}
nvmeGetSerialNumberForDevice () {
    dev="${1}"
    str=$(echo "${nvmeDevices[$dev]}" | jq -r .SerialNumber)
    printf "%s" "$str"
}
nvmeGetEuiForDevice () {
    dev="${1}"
    devNode=$(echo "${nvmeDevices[$dev]}" | jq -r .DevicePath)
    str=$(nvme id-ns "$devNode" -o json | jq -r .eui64)
    printf "%s" "$str"
}
nvmeGetNguidForDevice () {
    dev="${1}"
    devNode=$(echo "${nvmeDevices[$dev]}" | jq -r .DevicePath)
    str=$(nvme id-ns "$devNode" -o json | jq -r .nguid)
    printf "%s" "$str"
}
