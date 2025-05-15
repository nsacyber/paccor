#!/bin/bash
POSITIONAL_ARGS=()
while [[ $# -gt 0 ]]; do
  case $1 in
    --componentsonly)
      componentsOnly=YES
      shift
      ;;
    --*|-*)
      echo "tcg_ccr.sh: Unknown option $1"
      exit 1
      ;;
    *)
     POSITIONAL_ARGS+=("$1") # save positional arg
     # shift # past argument
     break
      ;;
  esac
done

printOutFile="${POSITIONAL_ARGS[*]}"

### User customizable values
APP_HOME=$(dirname "$0")
JSON_SCRIPT="$APP_HOME""/json.sh" # Defines JSON structure and provides methods for producing relevant JSON
SMBIOS_SCRIPT="$APP_HOME""/smbios.sh"
HW_SCRIPT="$APP_HOME""/hw.sh" # For some components not covered by SMBIOS
NVME_SCRIPT="$APP_HOME""/nvme.sh" # For nvme components

### SMBIOS Type Constants
# shellcheck source=./smbios.sh
source "$SMBIOS_SCRIPT"
SMBIOS_TYPE_SYSTEM="1"
# shellcheck disable=SC2034
SMBIOS_TYPE_PLATFORM="$SMBIOS_TYPE_SYSTEM"
SMBIOS_TYPE_CHASSIS="3"
SMBIOS_TYPE_BIOS="0"
SMBIOS_TYPE_BASEBOARD="2"
SMBIOS_TYPE_CPU="4"
SMBIOS_TYPE_RAM="17"

### hw
# shellcheck source=./hw.sh
source "$HW_SCRIPT"
# shellcheck source=./nvme.sh
source "$NVME_SCRIPT"

### ComponentClass values
COMPCLASS_REGISTRY_TCG="2.23.133.18.3.1" # Could lookup values within SMBIOS to reveal accurate component classes.
COMPCLASS_BASEBOARD="00030003" # these values are meant to be an example.  check the TCG component class registry.
COMPCLASS_BIOS="00130003"
#COMPCLASS_UEFI="00130002" # available as an example. uncomment to utilize.
COMPCLASS_CHASSIS="00020001"
COMPCLASS_CPU="00010002"
COMPCLASS_HDD="00070002"
COMPCLASS_NIC="00090002"
COMPCLASS_RAM="00060001"
COMPCLASS_GFX="00050002"

### JSON
# shellcheck source=./json.sh
source "$JSON_SCRIPT"

## Some of the commands below require root.
if [ "$EUID" -ne 0 ]
    then echo "Please run as root"
    exit
fi

parseSystemData () {
    IFS=' ' read -r -a tableHandles <<< "$(dmidecodeHandles "$SMBIOS_TYPE_PLATFORM")"
    # AssumeOneHandle
    IFS=' ' read -r -a tableData <<< "$(dmidecodeData "${tableHandles[0]}")"
    mapfile -t tableStrings <<< "$(dmidecodeStrings "${tableHandles[0]}")"

    platformManufacturer=$(dmidecodeGetString "$(dmidecodeGetByte "0x4" "${tableData[@]}")" "${tableStrings[@]}")
    platformModel=$(dmidecodeGetString "$(dmidecodeGetByte "0x5" "${tableData[@]}")" "${tableStrings[@]}")
    platformVersion=$(dmidecodeGetString "$(dmidecodeGetByte "0x6" "${tableData[@]}")" "${tableStrings[@]}")
    platformSerial=$(dmidecodeGetString "$(dmidecodeGetByte "0x7" "${tableData[@]}")" "${tableStrings[@]}")

    if [[ -z "${platformManufacturer// }" ]]; then
        platformManufacturer="$NOT_SPECIFIED"
    fi
    platformManufacturer=$(echo "$platformManufacturer" | sed 's/^[ \t]*//;s/[ \t]*$//')
    platformManufacturer=$(jsonPlatformManufacturerStr "$platformManufacturer")

    if [[ -z "${platformModel// }" ]]; then
        platformModel="$NOT_SPECIFIED"
    fi
    platformModel=$(echo "$platformModel" | sed 's/^[ \t]*//;s/[ \t]*$//')
    platformModel=$(jsonPlatformModel "$platformModel")

    if [[ -z "${platformVersion// }" ]]; then
        platformVersion="$NOT_SPECIFIED"
    fi
    platformVersion=$(echo "$platformVersion" | sed 's/^[ \t]*//;s/[ \t]*$//')
    platformVersion=$(jsonPlatformVersion "$platformVersion")

    if [[ -n "${platformSerial// }" ]]; then
        platformSerial=$(echo "$platformSerial" | sed 's/^[ \t]*//;s/[ \t]*$//')
        platformSerial=$(jsonPlatformSerial "$platformSerial")
    fi
    platform=$(toCSV "$platformManufacturer" "$platformModel" "$platformVersion" "$platformSerial")

    printf "%s" "$platform"
}

parseChassisData () {
    IFS=' ' read -r -a tableHandles <<< "$(dmidecodeHandles "$SMBIOS_TYPE_CHASSIS")"
    # AssumeOneHandle
    IFS=' ' read -r -a tableData <<< "$(dmidecodeData "${tableHandles[0]}")"
    mapfile -t tableStrings <<< "$(dmidecodeStrings "${tableHandles[0]}")"

    chassisClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_CHASSIS")
    chassisManufacturer=$(dmidecodeGetString "$(dmidecodeGetByte "0x4" "${tableData[@]}")" "${tableStrings[@]}")
    chassisModel=$(dmidecodeGetByte "0x5" "${tableData[@]}")
    chassisModel=$(printf "%d" "0x""$chassisModel") # Convert to decimal
    chassisSerial=$(dmidecodeGetString "$(dmidecodeGetByte "0x7" "${tableData[@]}")" "${tableStrings[@]}")
    chassisRevision=$(dmidecodeGetString "$(dmidecodeGetByte "0x6" "${tableData[@]}")" "${tableStrings[@]}")

    if [[ -z "${chassisManufacturer// }" ]]; then
        chassisManufacturer="$NOT_SPECIFIED"
    fi
    chassisManufacturer=$(echo "$chassisManufacturer" | sed 's/^[ \t]*//;s/[ \t]*$//')
    chassisManufacturer=$(jsonManufacturer "$chassisManufacturer")

    if [[ -z "${chassisModel// }" ]]; then
        chassisModel="$NOT_SPECIFIED"
    fi
    chassisModel=$(echo "$chassisModel" | sed 's/^[ \t]*//;s/[ \t]*$//')
    chassisModel=$(jsonModel "$chassisModel")

    chassisOptional=""
    if [[ -n "${chassisSerial// }" ]]; then
        chassisSerial=$(echo "$chassisSerial" | sed 's/^[ \t]*//;s/[ \t]*$//')
        chassisSerial=$(jsonSerial "$chassisSerial")
        chassisOptional="$chassisOptional"",""$chassisSerial"
    fi
    if [[ -n "${chassisRevision// }" ]]; then
        chassisRevision=$(echo "$chassisRevision" | sed 's/^[ \t]*//;s/[ \t]*$//')
        chassisRevision=$(jsonRevision "$chassisRevision")
        chassisOptional="$chassisOptional"",""$chassisRevision"
    fi
    chassisOptional=$(printf "%s" "$chassisOptional" | cut -c2-)
    componentChassis=$(jsonComponent "$chassisClass" "$chassisManufacturer" "$chassisModel" "$chassisOptional")

    printf "%s" "$componentChassis"
}

parseBaseboardData () {
    IFS=' ' read -r -a tableHandles <<< "$(dmidecodeHandles "$SMBIOS_TYPE_BASEBOARD")"
    # AssumeOneHandle
    IFS=' ' read -r -a tableData <<< "$(dmidecodeData "${tableHandles[0]}")"
    mapfile -t tableStrings <<< "$(dmidecodeStrings "${tableHandles[0]}")"

    baseboardClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_BASEBOARD")
    baseboardManufacturer=$(dmidecodeGetString "$(dmidecodeGetByte "0x4" "${tableData[@]}")" "${tableStrings[@]}")
    baseboardModel=$(dmidecodeGetString "$(dmidecodeGetByte "0x5" "${tableData[@]}")" "${tableStrings[@]}")
    baseboardSerial=$(dmidecodeGetString "$(dmidecodeGetByte "0x7" "${tableData[@]}")" "${tableStrings[@]}")
    baseboardRevision=$(dmidecodeGetString "$(dmidecodeGetByte "0x6" "${tableData[@]}")" "${tableStrings[@]}")
    baseboardFeatureFlags=$(dmidecodeGetByte "0x9" "${tableData[@]}")
    baseboardFeatureFlags=$(printf "%d" "0x""$baseboardFeatureFlags") # Convert to decimal
    baseboardReplaceableIndicator="28"
    baseboardFieldReplaceableAnswer="false"
    if (((baseboardFeatureFlags&baseboardReplaceableIndicator)!=0)); then
        baseboardFieldReplaceableAnswer="true"
    fi
    baseboardFieldReplaceable=$(jsonFieldReplaceable "$baseboardFieldReplaceableAnswer")

    if [[ -z "${baseboardManufacturer// }" ]]; then
        baseboardManufacturer="$NOT_SPECIFIED"
    fi
    baseboardManufacturer=$(echo "$baseboardManufacturer" | sed 's/^[ \t]*//;s/[ \t]*$//')
    baseboardManufacturer=$(jsonManufacturer "$baseboardManufacturer")

    if [[ -z "${baseboardModel// }" ]]; then
        baseboardModel="$NOT_SPECIFIED"
    fi
    baseboardModel=$(echo "$baseboardModel" | sed 's/^[ \t]*//;s/[ \t]*$//')
    baseboardModel=$(jsonModel "$baseboardModel")

    baseboardOptional=""
    if [[ -n "${baseboardSerial// }" ]]; then
        baseboardSerial=$(echo "$baseboardSerial" | sed 's/^[ \t]*//;s/[ \t]*$//')
        baseboardSerial=$(jsonSerial "$baseboardSerial")
        baseboardOptional="$baseboardOptional"",""$baseboardSerial"
    fi
    if [[ -n "${baseboardRevision// }" ]]; then
        baseboardRevision=$(echo "$baseboardRevision" | sed 's/^[ \t]*//;s/[ \t]*$//')
        baseboardRevision=$(jsonRevision "$baseboardRevision")
        baseboardOptional="$baseboardOptional"",""$baseboardRevision"
    fi
    baseboardOptional=$(printf "%s" "$baseboardOptional" | cut -c2-)
    componentBaseboard=$(jsonComponent "$baseboardClass" "$baseboardManufacturer" "$baseboardModel" "$baseboardFieldReplaceable" "$baseboardOptional")

    printf "%s" "$componentBaseboard"
}

parseBiosData () {
    IFS=' ' read -r -a tableHandles <<< "$(dmidecodeHandles "$SMBIOS_TYPE_BIOS")"
    # AssumeOneHandle
    IFS=' ' read -r -a tableData <<< "$(dmidecodeData "${tableHandles[0]}")"
    mapfile -t tableStrings <<< "$(dmidecodeStrings "${tableHandles[0]}")"
    biosClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_BIOS")
    biosManufacturer=$(dmidecodeGetString "$(dmidecodeGetByte "0x4" "${tableData[@]}")" "${tableStrings[@]}")
    biosModel=""
    biosSerial=""
    biosRevision=$(dmidecodeGetString "$(dmidecodeGetByte "0x5" "${tableData[@]}")" "${tableStrings[@]}")

    if [[ -z "${biosManufacturer// }" ]]; then
        biosManufacturer="$NOT_SPECIFIED"
    fi
    biosManufacturer=$(echo "$biosManufacturer" | sed 's/^[ \t]*//;s/[ \t]*$//')
    biosManufacturer=$(jsonManufacturer "$biosManufacturer")

    if [[ -z "${biosModel// }" ]]; then
        biosModel="$NOT_SPECIFIED"
    fi
    biosModel=$(echo "$biosModel" | sed 's/^[ \t]*//;s/[ \t]*$//')
    biosModel=$(jsonModel "$biosModel")

    biosOptional=""
    if [[ -n "${biosSerial// }" ]]; then
        biosSerial=$(echo "$biosSerial" | sed 's/^[ \t]*//;s/[ \t]*$//')
        biosSerial=$(jsonSerial "$biosSerial")
        biosOptional="$biosOptional"",""$biosSerial"
    fi
    if [[ -n "${biosRevision// }" ]]; then
        biosRevision=$(echo "$biosRevision" | sed 's/^[ \t]*//;s/[ \t]*$//')
        biosRevision=$(jsonRevision "$biosRevision")
        biosOptional="$biosOptional"",""$biosRevision"
    fi
    biosOptional=$(printf "%s" "$biosOptional" | cut -c2-)
    componentBios=$(jsonComponent "$biosClass" "$biosManufacturer" "$biosModel" "$biosOptional")

    printf "%s" "$componentBios"
}

parseCpuData () {
    IFS=' ' read -r -a tableHandles <<< "$(dmidecodeHandles "$SMBIOS_TYPE_CPU")"

    notReplaceableIndicator="6"
    tmpData=""
    numHandles=$(dmidecodeNumHandles "${tableHandles[@]}")
    class=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_CPU")

    for ((i = 0 ; i < numHandles ; i++ )); do
        IFS=' ' read -r -a tableData <<< "$(dmidecodeData "${tableHandles[$i]}")"
        mapfile -t tableStrings <<< "$(dmidecodeStrings "${tableHandles[$i]}")"

        manufacturer=$(dmidecodeGetString "$(dmidecodeGetByte "0x7" "${tableData[@]}")" "${tableStrings[@]}")
        model=$(dmidecodeGetByte "0x6" "${tableData[@]}")
        model=$(printf "%d" "0x""$model") # Convert to decimal
        serial=$(dmidecodeGetString "$(dmidecodeGetByte "0x20" "${tableData[@]}")" "${tableStrings[@]}")
        revision=$(dmidecodeGetString "$(dmidecodeGetByte "0x10" "${tableData[@]}")" "${tableStrings[@]}")
        processorUpgrade=$(dmidecodeGetByte "0x19" "${tableData[@]}")
        processorUpgrade=$(printf "%d" "0x""$processorUpgrade") # Convert to decimal

        if [[ -z "${manufacturer// }" ]]; then
            manufacturer="$NOT_SPECIFIED"
        fi
        manufacturer=$(echo "$manufacturer" | sed 's/^[ \t]*//;s/[ \t]*$//')
        manufacturer=$(jsonManufacturer "$manufacturer")

        if [[ -z "${model// }" ]]; then
            model="$NOT_SPECIFIED"
        fi
        model=$(echo "$model" | sed 's/^[ \t]*//;s/[ \t]*$//')
        model=$(jsonModel "$model")

        optional=""
        if [[ -n "${serial// }" ]]; then
            serial=$(echo "$serial" | sed 's/^[ \t]*//;s/[ \t]*$//')
            serial=$(jsonSerial "$serial")
            optional="$optional"",""$serial"
        fi
        if [[ -n "${revision// }" ]]; then
            revision=$(echo "$revision" | sed 's/^[ \t]*//;s/[ \t]*$//')
            revision=$(jsonRevision "$revision")
            optional="$optional"",""$revision"
        fi
        optional=$(printf "%s" "$optional" | cut -c2-)

        replaceable="true"
        if [ "$processorUpgrade" -eq $notReplaceableIndicator ]; then
            replaceable="false"
        fi
        replaceable=$(jsonFieldReplaceable "$replaceable")

        newCpuData=$(jsonComponent "$class" "$manufacturer" "$model" "$replaceable" "$optional")
        tmpData="$tmpData"",""$newCpuData"
    done

    # remove leading comma
    tmpData=$(printf "%s" "$tmpData" | cut -c2-)

    printf "%s" "$tmpData"
}

parseRamData () {
    IFS=' ' read -r -a tableHandles <<< "$(dmidecodeHandles "$SMBIOS_TYPE_RAM")"

    replaceable=$(jsonFieldReplaceable "true")
    tmpData=""
    numHandles=$(dmidecodeNumHandles "${tableHandles[@]}")
    class=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_RAM")

    for ((i = 0 ; i < numHandles ; i++ )); do
        IFS=' ' read -r -a tableData <<< "$(dmidecodeData "${tableHandles[$i]}")"
        mapfile -t tableStrings <<< "$(dmidecodeStrings "${tableHandles[$i]}")"

        manufacturer=$(dmidecodeGetString "$(dmidecodeGetByte "0x17" "${tableData[@]}")" "${tableStrings[@]}")
        model=$(dmidecodeGetString "$(dmidecodeGetByte "0x1A" "${tableData[@]}")" "${tableStrings[@]}")
        serial=$(dmidecodeGetString "$(dmidecodeGetByte "0x18" "${tableData[@]}")" "${tableStrings[@]}")
        revision=$(dmidecodeGetString "$(dmidecodeGetByte "0x19" "${tableData[@]}")" "${tableStrings[@]}")

        if [[ -z "${manufacturer// }" ]] && [[ -z "${model// }" ]] && [[ -z "${serial// }" ]] && [[ -z "${revision// }" ]]; then
            continue
        fi

        if [[ -z "${manufacturer// }" ]]; then
            manufacturer="$NOT_SPECIFIED"
        fi
        manufacturer=$(echo "$manufacturer" | sed 's/^[ \t]*//;s/[ \t]*$//')
        manufacturer=$(jsonManufacturer "$manufacturer")

        if [[ -z "${model// }" ]]; then
            model="$NOT_SPECIFIED"
        fi
        model=$(echo "$model" | sed 's/^[ \t]*//;s/[ \t]*$//')
        model=$(jsonModel "$model")

        optional=""
        if [[ -n "${serial// }" ]]; then
            serial=$(echo "$serial" | sed 's/^[ \t]*//;s/[ \t]*$//')
            serial=$(jsonSerial "$serial")
            optional="$optional"",""$serial"
        fi
        if [[ -n "${revision// }" ]]; then
            revision=$(echo "$revision" | sed 's/^[ \t]*//;s/[ \t]*$//')
            revision=$(jsonRevision "$revision")
            optional="$optional"",""$revision"
        fi
        optional=$(printf "%s" "$optional" | cut -c2-)

        newRamData=$(jsonComponent "$class" "$manufacturer" "$model" "$replaceable" "$optional")
        tmpData="$tmpData"",""$newRamData"
    done

    # remove leading comma
    tmpData=$(printf "%s" "$tmpData" | cut -c2-)

    printf "%s" "$tmpData"
}

# Write script to parse multiple responses
# Network:
# lshw description: type of address.
#                 : Ethernet interface, Wireless interface, Bluetooth wireless interface
#           vendor: manufacturer
#          product: model
#           serial: address & serial number
#          version: revision
#
# Example:
# ADDRESS1=$(jsonEthernetMac "AB:CD:EE:EE:DE:34")
# ADDR_LIST=$(jsonAddress "$ADDRESS1" "$ADDRESS2")
parseNicData () {
    lshwNetwork

    replaceable=$(jsonFieldReplaceable "true")
    tmpData=""
    numHandles=$(lshwNumBusItems)
    class=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_NIC")

    for ((i = 0 ; i < numHandles ; i++ )); do
        manufacturer=$(lshwGetVendorIDFromBusItem "$i")
        model=$(lshwGetProductIDFromBusItem "$i")
        serialConstant=$(lshwGetLogicalNameFromBusItem "$i")
        serialConstant=$(ethtoolPermAddr "$serialConstant")
        serialConstant=$(standardizeMACAddr "${serialConstant}")
        serial=""
        revision=$(lshwGetVersionFromBusItem "$i")

        if [[ -z "${manufacturer// }" ]] && [[ -z "${model// }" ]] && { [[ -n "${serialConstant// }" ]] || [[ -n "${revision// }" ]]; }; then
            manufacturer=$(lshwGetVendorNameFromBusItem "$i")
        model=$(lshwGetProductNameFromBusItem "$i")
        fi

        if [[ -z "${manufacturer// }" ]]; then
            manufacturer="$NOT_SPECIFIED"
        fi
        manufacturer=$(echo "$manufacturer" | sed 's/^[ \t]*//;s/[ \t]*$//')
        manufacturer=$(jsonManufacturer "$manufacturer")

        if [[ -z "${model// }" ]]; then
            model="$NOT_SPECIFIED"
        fi
        model=$(echo "$model" | sed 's/^[ \t]*//;s/[ \t]*$//')
        model=$(jsonModel "$model")

        optional=""
        if [[ -n "${serialConstant// }" ]]; then
            serial=$(echo "$serialConstant" | sed 's/^[ \t]*//;s/[ \t]*$//')
            serial=$(jsonSerial "$serialConstant")
            optional="$optional"",""$serial"
        fi
        if [[ -n "${revision// }" ]]; then
            revision=$(echo "$revision" | sed 's/^[ \t]*//;s/[ \t]*$//' | awk '{ print toupper($0) }')
            revision=$(jsonRevision "$revision")
            optional="$optional"",""$revision"
        fi
            bluetoothCap=$(lshwBusItemBluetoothCap "$i")
            ethernetCap=$(lshwBusItemEthernetCap "$i")
            wirelessCap=$(lshwBusItemWirelessCap "$i")

            if { [ -n "$bluetoothCap" ] || [ -n "$ethernetCap" ] || [ -n "$wirelessCap" ]; } && [[ -n "${serialConstant// }" ]]; then
                thisAddress=
                if [ -n "$wirelessCap" ]; then
                    thisAddress=$(jsonWlanMac "$serialConstant")
                elif [ -n "$bluetoothCap" ]; then
                    thisAddress=$(jsonBluetoothMac "$serialConstant")
                elif [ -n "$ethernetCap" ]; then
                    thisAddress=$(jsonEthernetMac "$serialConstant")
                fi
                if [ -n "$thisAddress" ]; then
                    thisAddress=$(jsonAddress "$thisAddress")
                    optional="$optional"",""$thisAddress"
                fi
            fi
        optional=$(printf "%s" "$optional" | cut -c2-)

        newNicData=$(jsonComponent "$class" "$manufacturer" "$model" "$replaceable" "$optional")
        tmpData="$tmpData"",""$newNicData"
    done

    # remove leading comma
    tmpData=$(printf "%s" "$tmpData" | cut -c2-)

    printf "%s" "$tmpData"
}

parseHddData () {
    lshwDisk

    replaceable=$(jsonFieldReplaceable "true")
    tmpData=""
    numHandles=$(lshwNumBusItems)
    class=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_HDD")

    for ((i = 0 ; i < numHandles ; i++ )); do
        manufacturer=$(lshwGetVendorIDFromBusItem "$i")
        model=$(lshwGetProductIDFromBusItem "$i")
        serial=$(lshwGetSerialFromBusItem "$i")
        revision=$(lshwGetVersionFromBusItem "$i")

        if [[ -z "${manufacturer// }" ]] && [[ -z "${model// }" ]] && { [[ -n "${serial// }" ]] || [[ -n "${revision// }" ]]; }; then
            model=$(lshwGetProductNameFromBusItem "$i")
            manufacturer=""
            revision="" # Seeing inconsistent behavior cross-OS for this case, will return
        fi

        if [[ -z "${manufacturer// }" ]]; then
            manufacturer="$NOT_SPECIFIED"
        fi
        manufacturer=$(echo "$manufacturer" | sed 's/^[ \t]*//;s/[ \t]*$//')
        manufacturer=$(jsonManufacturer "$manufacturer")

        if [[ -z "${model// }" ]]; then
            model="$NOT_SPECIFIED"
        fi
        model=$(echo "$model" | sed 's/^[ \t]*//;s/[ \t]*$//')
        model=$(jsonModel "$model")

        optional=""
        if [[ -n "${serial// }" ]]; then
            serial=$(echo "$serial" | sed 's/^[ \t]*//;s/[ \t]*$//')
            serial=$(jsonSerial "$serial")
            optional="$optional"",""$serial"
        fi
        if [[ -n "${revision// }" ]]; then
            revision=$(echo "$revision" | sed 's/^[ \t]*//;s/[ \t]*$//' | awk '{ print toupper($0) }')
            revision=$(jsonRevision "$revision")
            optional="$optional"",""$revision"
        fi
        optional=$(printf "%s" "$optional" | cut -c2-)

        newHddData=$(jsonComponent "$class" "$manufacturer" "$model" "$replaceable" "$optional")
        tmpData="$tmpData"",""$newHddData"
    done

    # remove leading comma
    tmpData=$(printf "%s" "$tmpData" | cut -c2-)

    printf "%s" "$tmpData"
}

parseNvmeData () {
    nvmeParse

    replaceable=$(jsonFieldReplaceable "true")
    tmpData=""
    numHandles=$(nvmeNumDevices)
    class=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_HDD")

    for ((i = 0 ; i < numHandles ; i++ )); do
        manufacturer="" # Making this appear as it does on windows, nvme-cli doesn't return a manufacturer field
        model=$(nvmeGetModelNumberForDevice "$i")
        serial=$(nvmeGetNguidForDevice "$i")
        if [[ $serial =~ ^[0]+$ ]]; then
            serial=$(nvmeGetEuiForDevice "$i")
        fi
        revision="" # empty for a similar reason to the manufacturer field

    if [[ -z "${manufacturer// }" ]]; then
        manufacturer="$NOT_SPECIFIED"
    fi
    manufacturer=$(echo "$manufacturer" | sed 's/^[ \t]*//;s/[ \t]*$//')
    manufacturer=$(jsonManufacturer "$manufacturer")

    if [[ -z "${model// }" ]]; then
        model="$NOT_SPECIFIED"
    fi
    model=$(echo "${model:0:16}" | sed 's/^[ \t]*//;s/[ \t]*$//') # limited to 16 characters for compatibility to windows, then trimmed
    model=$(jsonModel "$model")

    optional=""
    if [[ -n "${serial// }" ]]; then
        serial=$(echo "${serial^^}" | sed 's/^[ \t]*//;s/[ \t]*$//' | sed 's/.\{4\}/&_/g' | sed 's/_$/\./')
        serial=$(jsonSerial "$serial")
        optional="$optional"",""$serial"
    fi
    optional=$(printf "%s" "$optional" | cut -c2-)

        newHddData=$(jsonComponent "$class" "$manufacturer" "$model" "$replaceable" "$optional")
        tmpData="$tmpData"",""$newHddData"
    done

    # remove leading comma
    tmpData=$(printf "%s" "$tmpData" | cut -c2-)

    printf "%s" "$tmpData"
}

parseGfxData () {
    lshwDisplay

    replaceable=$(jsonFieldReplaceable "true")
    tmpData=""
    numHandles=$(lshwNumBusItems)
    class=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_GFX")

    for ((i = 0 ; i < numHandles ; i++ )); do
        manufacturer=$(lshwGetVendorIDFromBusItem "$i")
    model=$(lshwGetProductIDFromBusItem "$i")
    serial=$(lshwGetSerialFromBusItem "$i")
    revision=$(lshwGetVersionFromBusItem "$i")

        if [[ -z "${manufacturer// }" ]] && [[ -z "${model// }" ]] && { [[ -n "${serial// }" ]] || [[ -n "${revision// }" ]]; }; then
            manufacturer=$(lshwGetVendorNameFromBusItem "$i")
            model=$(lshwGetProductNameFromBusItem "$i")
        fi

    if [[ -z "${manufacturer// }" ]]; then
        manufacturer="$NOT_SPECIFIED"
    fi
    manufacturer=$(echo "$manufacturer" | sed 's/^[ \t]*//;s/[ \t]*$//')
    manufacturer=$(jsonManufacturer "$manufacturer")

    if [[ -z "${model// }" ]]; then
        model="$NOT_SPECIFIED"
    fi
    model=$(echo "$model" | sed 's/^[ \t]*//;s/[ \t]*$//')
    model=$(jsonModel "$model")

    optional=""
    if [[ -n "${serial// }" ]]; then
        serial=$(echo "$serial" | sed 's/^[ \t]*//;s/[ \t]*$//')
        serial=$(jsonSerial "$serial")
        optional="$optional"",""$serial"
    fi
    if [[ -n "${revision// }" ]]; then
        revision=$(echo "$revision" | sed 's/^[ \t]*//;s/[ \t]*$//' | awk '{ print toupper($0) }')
        revision=$(jsonRevision "$revision")
        optional="$optional"",""$revision"
    fi
    optional=$(printf "%s" "$optional" | cut -c2-)

        newGfxData=$(jsonComponent "$class" "$manufacturer" "$model" "$replaceable" "$optional")
        tmpData="$tmpData"",""$newGfxData"
    done

    # remove leading comma
    tmpData=$(printf "%s" "$tmpData" | cut -c2-)

    printf "%s" "$tmpData"
}

### Collate the component details
collectOldTcgRegistryComponents () {
    componentChassis=$(parseChassisData)
	  componentBaseboard=$(parseBaseboardData)
	  componentBios=$(parseBiosData)
    componentsCPU=$(parseCpuData)
    componentsRAM=$(parseRamData)
    componentsNIC=$(parseNicData)
    componentsHDD=$(parseHddData)
    componentsNVMe=$(parseNvmeData)
    componentsGFX=$(parseGfxData)
    componentList=$(toCSV "$componentChassis" "$componentBaseboard" "$componentBios" "$componentsCPU" "$componentsRAM" "$componentsNIC" "$componentsHDD" "$componentsNVMe" "$componentsGFX")

    printf "%s" "$componentList"
}

function collectProperties () {
    property1=$(jsonProperty "uname -r" "$(uname -r)")  ## Example1
    property2=$(jsonProperty "OS Release" "$(grep 'PRETTY_NAME=' /etc/os-release | sed 's/[^=]*=//' | sed -e 's/^[[:space:]\"]*//' | sed -e 's/[[:space:]\"]*$//')")
    #property3=$(jsonProperty "another property" "property value" "$JSON_STATUS_ADDED") ## Example2 with optional third status argument

    propertyList=$(toCSV "$property1" "$property2")

    printf "%s" "$propertyList"
}

if [ -n "$printOutFile" ]; then
    componentList=$(collectOldTcgRegistryComponents)

    output="$componentList"
    if [ -z "$componentsOnly" ]; then
        platformList=$(parseSystemData)
        propertyList=$(collectProperties);

        platformObject=$(jsonPlatformObject "$platformList")
        componentArray=$(jsonComponentArray "$componentList")
        propertyArray=$(jsonPropertyArray "$propertyList")

        componentsUri="" # Example: $(jsonComponentsUri "https:// example.uri/componentPolicy.pdf" "C:/path/to/local/fileToHash")
        propertiesUri="" # Example: $(jsonPropertiesUri "https:// example.uri/propertyPolicy.pdf" "C:/path/to/local/fileToHash")
        output=$(jsonIntermediateFile "$platformObject" "$componentArray" "$componentsUri" "$propertyArray" "$propertiesUri")
    fi

    printf "%s\n\n" "$output" > "$printOutFile"
fi
