#!/bin/bash
# Gather descriptors for NVMe devices  
nvmeParse () {
    str=$(nvme list -o json -v)
    count=$(echo "$str" | jq '.Devices | length')
    nvmeDevices=()

    for ((i = 0 ; i < count ; i++ )); do
        elementJson=$(echo "$str" | jq -r .Devices["$i"].Subsystems[0].Controllers[0].Controller)
        elementJson="/dev/$elementJson"
        nvmeDevices+=("$elementJson") 
    done
}
nvmeNumDevices () {
    printf "${#nvmeDevices[@]}"
}
nvmeGetModelNumberForDevice () {
    dev="${1}"
    str=$(nvme id-ctrl "${nvmeDevices[$dev]}" -o json | jq -r .mn)
    printf "%s" "$str"
}
nvmeGetSerialNumberForDevice () {
    dev="${1}"
    str=$(nvme id-ctrl "${nvmeDevices[$dev]}" -o json | jq -r .sn)
    printf "%s" "$str"
}
nvmeGetEuiForDevice () {
    dev="${1}"
    str=$(nvme id-ns "${nvmeDevices[$dev]}n1" -o json | jq -r .eui64)
    printf "%s" "$str"
}
nvmeGetNguidForDevice () {
    dev="${1}"
    str=$(nvme id-ns "${nvmeDevices[$dev]}n1" -o json | jq -r .nguid)
    printf "%s" "$str"
}
