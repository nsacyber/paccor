#!/bin/bash

### componentlist.sh
### gathers identifiers for all components on the system for creation of a platform certificate
### multiple component class registries are used, and each component on the system will
### have one componentIdentifier in the resulting JSON output

### User customizable values
APP_HOME="`dirname "$0"`"
COMPONENTS_URI="" # Specify the optional components URI field
COMPONENTS_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
PROPERTIES_URI="" # Specify the optional properties URI field
PROPERTIES_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
JSON_SCRIPT="$APP_HOME""/json.sh" # Defines JSON structure and provides methods for producing relevant JSON
SMBIOS_SCRIPT="$APP_HOME""/smbios.sh" # Handles parsing of SMBIOS data
HW_SCRIPT="$APP_HOME""/hw.sh" # Handles parsing of lshw
NVME_SCRIPT="$APP_HOME""/nvme.sh" # For nvme components

### JSON
source $JSON_SCRIPT

### SMBIOS
source $SMBIOS_SCRIPT # See the TCG SMBIOS Component Class Registry specification.
COMPCLASS_REGISTRY_SMBIOS="2.23.133.18.3.3" # See the TCG OID Registry.

### hw
source $HW_SCRIPT
source $NVME_SCRIPT

### TCG ComponentClass values
COMPCLASS_REGISTRY_TCG="2.23.133.18.3.1"
COMPCLASS_BASEBOARD="00030003" # these values are meant to be an example.  check the TCG component class registry.
COMPCLASS_BIOS="00130003"
COMPCLASS_UEFI="00130002"
COMPCLASS_CHASSIS="00020001"
COMPCLASS_CPU="00010002"
COMPCLASS_HDD="00070002"
COMPCLASS_NIC="00090002"
COMPCLASS_RAM="00060001"
COMPCLASS_GFX="00050002"

## Some of the commands below require root.
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

### Gather platform details for the subject alternative name
### Platform attributes in the SAN only need to be consistent between base and delta platform certificates
gatherSmbiosSystemForSubjectAlternativeName () {
    dmidecodeParseTypeAssumeOneHandle "$SMBIOS_TYPE_SYSTEM"
    platformManufacturer=$(dmidecodeGetString $(dmidecodeGetByte "0x4"))
    platformModel=$(dmidecodeGetString $(dmidecodeGetByte "0x5"))
    platformVersion=$(dmidecodeGetString $(dmidecodeGetByte "0x6"))
    platformSerial=$(dmidecodeGetString $(dmidecodeGetByte "0x7"))

    if [[ -z "${platformManufacturer// }" ]]; then
        platformManufacturer="$NOT_SPECIFIED"
    fi
    platformManufacturer=$(echo "$platformManufacturer" | sed 's/^[     ]*//;s/[     ]*$//')
    platformManufacturer=$(jsonPlatformManufacturerStr "$platformManufacturer")

    if [[ -z "${platformModel// }" ]]; then
        platformModel="$NOT_SPECIFIED"
    fi
    platformModel=$(echo "$platformModel" | sed 's/^[     ]*//;s/[     ]*$//')
    platformModel=$(jsonPlatformModel "$platformModel")

    if [[ -z "${platformVersion// }" ]]; then
        platformVersion="$NOT_SPECIFIED"
    fi
    platformVersion=$(echo "$platformVersion" | sed 's/^[     ]*//;s/[     ]*$//')
    platformVersion=$(jsonPlatformVersion "$platformVersion")

    if ! [[ -z "${platformSerial// }" ]]; then
        platformSerial=$(echo "$platformSerial" | sed 's/^[     ]*//;s/[     ]*$//')
        platformSerial=$(jsonPlatformSerial "$platformSerial")
    fi
    platform=$(jsonPlatformObject "$platformManufacturer" "$platformModel" "$platformVersion" "$platformSerial")
    platform=$(printf "$platform" | cut -c2-)
    printf "$platform"
}

### Gather data from SMBIOS
gatherSmbiosData () {
    components=""
    for type in $SMBIOS_TYPE_BASEBOARD $SMBIOS_TYPE_BIOS $SMBIOS_TYPE_CHASSIS $SMBIOS_TYPE_PROCESSOR $SMBIOS_TYPE_RAM $SMBIOS_TYPE_SYSTEM $SMBIOS_TYPE_POWERSUPPLY $SMBIOS_TYPE_TPM
    do
        dmidecodeHandles "$type"
        numHandles=$(dmidecodeNumHandles)

        for ((i = 0 ; i < numHandles ; i++ ));
        do
            component=""
            dmidecodeParseHandle "${tableHandles[$i]}"

            componentClassValue=$(dmidecodeGetComponentClassValue)
            manufacturer=$(dmidecodeGetManufacturer)
            model=$(dmidecodeGetModel)
            serialNumber=$(dmidecodeGetSerialNumber)
            revision=$(dmidecodeGetRevision)
            fieldReplaceable=$(dmidecodeGetFieldReplaceable)

            # Do not include empty slots
            if [[ -z "$manufacturer" ]] && [[ -z "$model" ]] && [[ -z "$serialNumber" ]] && [[ -z "$revision" ]]; then
                continue
            fi

            componentClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_SMBIOS" "$componentClassValue")
            manufacturer=$(jsonManufacturer "$manufacturer")
            model=$(jsonModel "$model")
            if ! [[ -z "$serialNumber" ]]; then
                serialNumber=$(jsonSerial "$serialNumber")
            fi
            if ! [[ -z "$revision" ]]; then
                revision=$(jsonRevision "$revision")
            fi
            if ! [[ -z "$fieldReplaceable" ]]; then
                fieldReplaceable=$(jsonFieldReplaceable "$fieldReplaceable")
            fi
            component=$(jsonComponent "$componentClass" "$manufacturer" "$model" "$serialNumber" "$revision" "$fieldReplaceable")
            components="$components"",""$component"
        done # handles for loop
    done # type for loop
    components=$(printf "$components" | cut -c2-)
    printf "$components"
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

        if [[ -z "${manufacturer// }" ]] && [[ -z "${model// }" ]] && (! [[ -z "${serialConstant// }" ]] || ! [[ -z "${revision// }" ]]); then
            manufacturer=$(lshwGetVendorNameFromBusItem "$i")
        model=$(lshwGetProductNameFromBusItem "$i")
        fi

    if [[ -z "${manufacturer// }" ]]; then
        manufacturer="$NOT_SPECIFIED"
    fi
    manufacturer=$(echo "$manufacturer" | sed 's/^[     ]*//;s/[     ]*$//')
    manufacturer=$(jsonManufacturer "$manufacturer")

    if [[ -z "${model// }" ]]; then
        model="$NOT_SPECIFIED"
    fi
    model=$(echo "$model" | sed 's/^[     ]*//;s/[     ]*$//')
    model=$(jsonModel "$model")

    optional=""
    if ! [[ -z "${serialConstant// }" ]]; then
        serial=$(echo "$serialConstant" | sed 's/^[     ]*//;s/[     ]*$//')
        serial=$(jsonSerial "$serialConstant")
        optional="$optional"",""$serial"
    fi
    if ! [[ -z "${revision// }" ]]; then
        revision=$(echo "$revision" | sed 's/^[     ]*//;s/[     ]*$//' | awk '{ print toupper($0) }')
        revision=$(jsonRevision "$revision")
        optional="$optional"",""$revision"
    fi
        bluetoothCap=$(lshwBusItemBluetoothCap "$i")
        ethernetCap=$(lshwBusItemEthernetCap "$i")
        wirelessCap=$(lshwBusItemWirelessCap "$i")

        if ([ -n "$bluetoothCap" ] || [ -n "$ethernetCap" ] || [ -n "$wirelessCap" ]) && ! [[ -z "${serialConstant// }" ]]; then
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
    optional=$(printf "$optional" | cut -c2-)

        newNicData=$(jsonComponent "$class" "$manufacturer" "$model" "$replaceable" "$optional")
        tmpData="$tmpData"",""$newNicData"
    done

    # remove leading comma
    tmpData=$(printf "$tmpData" | cut -c2-)

    printf "$tmpData"
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

        if [[ -z "${manufacturer// }" ]] && [[ -z "${model// }" ]] && (! [[ -z "${serial// }" ]] || ! [[ -z "${revision// }" ]]); then
            model=$(lshwGetProductNameFromBusItem "$i")
            manufacturer=""
            revision="" # Seeing inconsistent behavior cross-OS for this case, will return
        fi

    if [[ -z "${manufacturer// }" ]]; then
        manufacturer="$NOT_SPECIFIED"
    fi
    manufacturer=$(echo "$manufacturer" | sed 's/^[     ]*//;s/[     ]*$//')
    manufacturer=$(jsonManufacturer "$manufacturer")

    if [[ -z "${model// }" ]]; then
        model="$NOT_SPECIFIED"
    fi
    model=$(echo "$model" | sed 's/^[     ]*//;s/[     ]*$//')
    model=$(jsonModel "$model")

    optional=""
    if ! [[ -z "${serial// }" ]]; then
        serial=$(echo "$serial" | sed 's/^[     ]*//;s/[     ]*$//')
        serial=$(jsonSerial "$serial")
        optional="$optional"",""$serial"
    fi
    if ! [[ -z "${revision// }" ]]; then
        revision=$(echo "$revision" | sed 's/^[     ]*//;s/[     ]*$//' | awk '{ print toupper($0) }')
        revision=$(jsonRevision "$revision")
        optional="$optional"",""$revision"
    fi
    optional=$(printf "$optional" | cut -c2-)

        newHddData=$(jsonComponent "$class" "$manufacturer" "$model" "$replaceable" "$optional")
        tmpData="$tmpData"",""$newHddData"
    done

    # remove leading comma
    tmpData=$(printf "$tmpData" | cut -c2-)

    printf "$tmpData"
}

### Gather data from NVMe CLI
parseNvmeData () {
    nvmeParse

    replaceable=$(jsonFieldReplaceable "true")
    tmpData=""
    numHandles=$(nvmeNumDevices)
    class=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_HDD")

    for ((i = 0 ; i < numHandles ; i++ )); do
        manufacturer="" # Making this appear as it does on windows, lshw doesn't see nvme drives and nvme-cli doesn't return a manufacturer field
        model=$(nvmeGetModelNumberForDevice "$i")
        serial=$(nvmeGetNguidForDevice "$i")
        if [[ $serial =~ ^[0]+$ ]]; then
            serial=$(nvmeGetEuiForDevice "$i")
        fi
        revision="" # empty for a similar reason to the manufacturer field

    if [[ -z "${manufacturer// }" ]]; then
        manufacturer="$NOT_SPECIFIED"
    fi
    manufacturer=$(echo "$manufacturer" | sed 's/^[     ]*//;s/[     ]*$//')
    manufacturer=$(jsonManufacturer "$manufacturer")

    if [[ -z "${model// }" ]]; then
        model="$NOT_SPECIFIED"
    fi
    model=$(echo "${model:0:16}" | sed 's/^[     ]*//;s/[     ]*$//') # limited to 16 characters for compatibility to windows, then trimmed
    model=$(jsonModel "$model")

    optional=""
    if ! [[ -z "${serial// }" ]]; then
        serial=$(echo "${serial^^}" | sed 's/^[     ]*//;s/[     ]*$//' | sed 's/.\{4\}/&_/g' | sed 's/_$/\./')
        serial=$(jsonSerial "$serial")
        optional="$optional"",""$serial"
    fi
    optional=$(printf "$optional" | cut -c2-)

        newHddData=$(jsonComponent "$class" "$manufacturer" "$model" "$replaceable" "$optional")
        tmpData="$tmpData"",""$newHddData"
    done

    # remove leading comma
    tmpData=$(printf "$tmpData" | cut -c2-)

    printf "$tmpData"
}

### Gather GFX details
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

            if [[ -z "${manufacturer// }" ]] && [[ -z "${model// }" ]] && (! [[ -z "${serial// }" ]] || ! [[ -z "${revision// }" ]]); then
                manufacturer=$(lshwGetVendorNameFromBusItem "$i")
                model=$(lshwGetProductNameFromBusItem "$i")
            fi

        if [[ -z "${manufacturer// }" ]]; then
            manufacturer="$NOT_SPECIFIED"
        fi
        manufacturer=$(echo "$manufacturer" | sed 's/^[     ]*//;s/[     ]*$//')
        manufacturer=$(jsonManufacturer "$manufacturer")

        if [[ -z "${model// }" ]]; then
            model="$NOT_SPECIFIED"
        fi
        model=$(echo "$model" | sed 's/^[     ]*//;s/[     ]*$//')
        model=$(jsonModel "$model")

        optional=""
        if ! [[ -z "${serial// }" ]]; then
            serial=$(echo "$serial" | sed 's/^[     ]*//;s/[     ]*$//')
            serial=$(jsonSerial "$serial")
            optional="$optional"",""$serial"
        fi
        if ! [[ -z "${revision// }" ]]; then
            revision=$(echo "$revision" | sed 's/^[     ]*//;s/[     ]*$//' | awk '{ print toupper($0) }')
            revision=$(jsonRevision "$revision")
            optional="$optional"",""$revision"
        fi
        optional=$(printf "$optional" | cut -c2-)

            newGfxData=$(jsonComponent "$class" "$manufacturer" "$model" "$replaceable" "$optional")
            tmpData="$tmpData"",""$newGfxData"
        done

        # remove leading comma
        tmpData=$(printf "$tmpData" | cut -c2-)

        printf "$tmpData"
}

### Gather property details
property1=$(jsonProperty "uname -r" "$(uname -r)")  ## Example1
property2=$(jsonProperty "OS Release" "$(grep 'PRETTY_NAME=' /etc/os-release | sed 's/[^=]*=//' | sed -e 's/^[[:space:]\"]*//' | sed -e 's/[[:space:]\"]*$//')") ## Example2

### Collate the component details
platform=$(gatherSmbiosSystemForSubjectAlternativeName)
componentsSMBIOS=$(gatherSmbiosData)
componentsNIC=$(parseNicData)
componentsHDD=$(parseHddData)
componentsNVMe=$(parseNvmeData)
componentsGFX=$(parseGfxData)
componentArray=$(jsonComponentArray "$componentsSMBIOS" "$componentsNIC" "$componentsHDD" "$componentsNVMe" "$componentsGFX")

### Collate the property details
propertyArray=$(jsonPropertyArray "$property1" "$property2")

### Construct the final JSON object
FINAL_JSON_OBJECT=$(jsonIntermediateFile "$platform" "$componentArray" "$propertyArray")

### Collate the URI details, if parameters above are blank, the fields will be excluded from the final JSON structure
if [ -n "$COMPONENTS_URI" ]; then
    componentsUri=$(jsonComponentsUri)
    FINAL_JSON_OBJECT="$FINAL_JSON_OBJECT"",""$componentsUri"
fi
if [ -n "$PROPERTIES_URI" ]; then
    propertiesUri=$(jsonPropertiesUri)
    FINAL_JSON_OBJECT="$FINAL_JSON_OBJECT"",""$propertiesUri"
fi

printf "$FINAL_JSON_OBJECT""\n\n"
