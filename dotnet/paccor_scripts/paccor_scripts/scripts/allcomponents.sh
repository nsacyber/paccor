#!/bin/bash

### User customizable values
APP_HOME=$(dirname "$0")
COMPONENTS_URI="" # Specify the optional components URI field
# shellcheck disable=SC2034
COMPONENTS_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
PROPERTIES_URI="" # Specify the optional properties URI field
# shellcheck disable=SC2034
PROPERTIES_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
JSON_SCRIPT="$APP_HOME""/json.sh" # Defines JSON structure and provides methods for producing relevant JSON

#### Registry Options
INCLUDE_SMBIOS_REGISTRY=YES
INCLUDE_PCIE_REGISTRY=YES
INCLUDE_STORAGE_REGISTRY=YES
INCLUDE_TCG_REGISTRY=
SMBIOS_REGISTRY_UTILITY="$APP_HOME""/SmbiosCli"
PCIE_REGISTRY_UTILITY="$APP_HOME""/PcieCli"
STORAGE_REGISTRY_UTILITY="$APP_HOME""/StorageCli"

## Some of the commands below require root.
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

### JSON
# shellcheck source=./json.sh
source "$JSON_SCRIPT"

### Base Registry Platform and Component data
TCG_REGISTRY_SCRIPT="$APP_HOME""/tcg_ccr.sh" # Functions to collect hardware information labeled with the original TCG Component Class Registry
# shellcheck source=./tcg_ccr.sh
source "$TCG_REGISTRY_SCRIPT"

### Gather component details
platformList=$(parseSystemData)
platformObject=$(jsonPlatformObject "$platformList")
smbiosRegistryData=""
pcieRegistryData=""
storageRegistryData=""
tcgRegistryData=""
if [[ -n "$INCLUDE_SMBIOS_REGISTRY" && -f "$SMBIOS_REGISTRY_UTILITY" ]]; then
    smbiosRegistryData=$($SMBIOS_REGISTRY_UTILITY --components-only)
fi
if [[ -n "$INCLUDE_PCIE_REGISTRY" && -f "$PCIE_REGISTRY_UTILITY" ]]; then
    pcieRegistryData=$($PCIE_REGISTRY_UTILITY --components-only)
fi
if [[ -n "$INCLUDE_STORAGE_REGISTRY" && -f "$STORAGE_REGISTRY_UTILITY" ]]; then
    storageRegistryData=$($STORAGE_REGISTRY_UTILITY --components-only)
fi
if [[ -n "$INCLUDE_TCG_REGISTRY" ]]; then
    tcgRegistryData=$(collectOldTcgRegistryComponents)
fi
componentArray=$(jsonComponentArray "$smbiosRegistryData" "$pcieRegistryData" "$storageRegistryData" "$tcgRegistryData")

### Gather property details
property1=$(jsonProperty "uname -r" "$(uname -r)")  ## Example1
property2=$(jsonProperty "OS Release" "$(grep 'PRETTY_NAME=' /etc/os-release | sed 's/[^=]*=//' | sed -e 's/^[[:space:]\"]*//' | sed -e 's/[[:space:]\"]*$//')") # "$JSON_STATUS_ADDED") ## Example2 with optional third status argument

### Collate the property details
propertyArray=$(jsonPropertyArray "$property1" "$property2")

### Collate the URI details, if parameters above are blank, the fields will be excluded from the final JSON structure
componentsUri=""
if [ -n "$COMPONENTS_URI" ]; then
    componentsUri=$(jsonComponentsUri "$COMPONENTS_URI" "$COMPONENTS_URI_LOCAL_COPY_FOR_HASH")
fi
propertiesUri=""
if [ -n "$PROPERTIES_URI" ]; then
    propertiesUri=$(jsonPropertiesUri "$PROPERTIES_URI" "$PROPERTIES_URI_LOCAL_COPY_FOR_HASH")
fi

### Construct the final JSON object
FINAL_JSON_OBJECT=$(jsonIntermediateFile "$platformObject" "$componentArray" "$componentsUri" "$propertyArray" "$propertiesUri")

printf "%s\n\n" "$FINAL_JSON_OBJECT"


