#!/bin/bash

### User customizable values
APP_HOME="`dirname "$0"`"
COMPONENTS_URI="" # Specify the optional components URI field
COMPONENTS_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
PROPERTIES_URI="" # Specify the optional properties URI field
PROPERTIES_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
ENTERPRISE_NUMBERS_FILE="$APP_HOME""/enterprise-numbers"
PEN_ROOT="1.3.6.1.4.1." # OID root for the private enterprise numbers
JSON_SCRIPT="$APP_HOME""/json.sh" # Defines JSON structure and provides methods for producing relevant JSON
SMBIOS_SCRIPT="$APP_HOME""/smbios.sh"
HW_SCRIPT="$APP_HOME""/hw.sh" # For some components not covered by SMBIOS
NVME_SCRIPT="$APP_HOME""/nvme.sh" # For nvme components

#### Registry Options
INCLUDE_SMBIOS_REGISTRY="1"
INCLUDE_PCIE_REGISTRY="1"
INCLUDE_STORAGE_REGISTRY="1"
INCLUDE_TCG_REGISTRY=""
SMBIOS_REGISTRY_UTILITY="$APP_HOME""/SmbiosCli"
PCIE_REGISTRY_UTILITY="$APP_HOME""/PcieCli"
STORAGE_REGISTRY_UTILITY="$APP_HOME""/StorageCli"
TCG_REGISTRY_SCRIPT="$APP_HOME""/tcg_ccr.sh"

## Some of the commands below require root.
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

### SMBIOS Type Constants
source $SMBIOS_SCRIPT
SMBIOS_TYPE_SYSTEM="1"
SMBIOS_TYPE_PLATFORM="$SMBIOS_TYPE_SYSTEM"

### JSON
source $JSON_SCRIPT

### Gather platform details for the subject alternative name
parseSystemData () {
    dmidecodeParseTypeAssumeOneHandle "$SMBIOS_TYPE_PLATFORM"
    platformManufacturer=$(dmidecodeGetString $(dmidecodeGetByte "0x4"))
    platformModel=$(dmidecodeGetString $(dmidecodeGetByte "0x5"))
    platformVersion=$(dmidecodeGetString $(dmidecodeGetByte "0x6"))
    platformSerial=$(dmidecodeGetString $(dmidecodeGetByte "0x7"))

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

    if ! [[ -z "${platformSerial// }" ]]; then
        platformSerial=$(echo "$platformSerial" | sed 's/^[ \t]*//;s/[ \t]*$//')
        platformSerial=$(jsonPlatformSerial "$platformSerial")
    fi
    platform=$(jsonPlatformObject "$platformManufacturer" "$platformModel" "$platformVersion" "$platformSerial")

    printf "$platform"
}

### Gather component details
platform=$(parseSystemData)
smbiosRegistryData=""
pcieRegistryData=""
storageRegistryData=""
tcgRegistryData=""
if ! [[ -z "${INCLUDE_SMBIOS_REGISTRY// }" ]]; then
    smbiosRegistryData=$($SMBIOS_REGISTRY_UTILITY --components-only)
fi
if ! [[ -z "${INCLUDE_PCIE_REGISTRY// }" ]]; then
    pcieRegistryData=$($PCIE_REGISTRY_UTILITY --components-only)
fi
if ! [[ -z "${INCLUDE_STORAGE_REGISTRY// }" ]]; then
    storageRegistryData=$($STORAGE_REGISTRY_UTILITY --components-only)
fi
if ! [[ -z "${INCLUDE_TCG_REGISTRY// }" ]]; then
    source $TCG_REGISTRY_SCRIPT
    tcgRegistryData=$(collectBasicRegistryComponents)
fi
componentArray=$(jsonComponentArray "$smbiosRegistryData" "$pcieRegistryData" "$storageRegistryData" "$tcgRegistryData")

### Gather property details
property1=$(jsonProperty "uname -r" "$(uname -r)")  ## Example1
property2=$(jsonProperty "OS Release" "$(grep 'PRETTY_NAME=' /etc/os-release | sed 's/[^=]*=//' | sed -e 's/^[[:space:]\"]*//' | sed -e 's/[[:space:]\"]*$//')") # "$JSON_STATUS_ADDED") ## Example2 with optional third status argument

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


