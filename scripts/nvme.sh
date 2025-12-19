#!/bin/bash
# Gather descriptors for NVMe devices  
nvmeParse () {
    str=$(nvme list -o json -v)
    controllers=$(echo "$str" | jq -r '.Devices[].Subsystems[].Controllers[].Controller')
    nvmeDevices=()

    for path in $controllers; do
        elementJson="/dev/$path"
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
