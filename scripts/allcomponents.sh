#!/bin/bash

### User customizable values
APP_HOME="`dirname "$0"`"
PROPERTIES_URI="" # Specify the optional properties URI field
PROPERTIES_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
ENTERPRISE_NUMBERS_FILE="$APP_HOME""/enterprise-numbers"
PEN_ROOT="1.3.6.1.4.1." # OID root for the private enterprise numbers

### JSON Structure Keywords
JSON_COMPONENTS="COMPONENTS"
JSON_PROPERTIES="PROPERTIES"
JSON_PROPERTIESURI="PROPERTIESURI"
JSON_PLATFORM="PLATFORM"
#### JSON Component Keywords
JSON_MANUFACTURER="MANUFACTURER"
JSON_MODEL="MODEL"
JSON_SERIAL="SERIAL"
JSON_REVISION="REVISION"
JSON_MANUFACTURERID="MANUFACTURERID"
JSON_FIELDREPLACEABLE="FIELDREPLACEABLE"
JSON_ADDRESSES="ADDRESSES"
JSON_ETHERNETMAC="ETHERNETMAC"
JSON_WLANMAC="WLANMAC"
JSON_BLUETOOTHMAC="BLUETOOTHMAC"
#### JSON Platform Keywords (Subject Alternative Name)
JSON_PLATFORMMODEL="PLATFORMMODEL"
JSON_PLATFORMMANUFACTURERSTR="PLATFORMMANUFACTURERSTR"
JSON_PLATFORMVERSION="PLATFORMVERSION"
JSON_PLATFORMSERIAL="PLATFORMSERIAL"
JSON_PLATFORMMANUFACTURERID="PLATFORMMANUFACTURERID"
#### JSON Platform URI Keywords
JSON_URI="UNIFORMRESOURCEIDENTIFIER"
JSON_HASHALG="HASHALGORITHM"
JSON_HASHVALUE="HASHVALUE"
#### JSON Properties Keywords
JSON_NAME="NAME"
JSON_VALUE="VALUE"
NOT_SPECIFIED="Not Specified"


### JSON Structure Format
JSON_INTERMEDIATE_FILE_OBJECT='{
    %s
}'
JSON_PLATFORM_TEMPLATE='
    \"'"$JSON_PLATFORM"'\": {
        %s
    }'
JSON_PROPERTIESURI_TEMPLATE='
    \"'"$JSON_PROPERTIESURI"'\": {
        %s
    }'
JSON_PROPERTY_ARRAY_TEMPLATE='
    \"'"$JSON_PROPERTIES"'\": [%s
    ]'
JSON_COMPONENT_ARRAY_TEMPLATE='
    \"'"$JSON_COMPONENTS"'\": [%s
    ]'
JSON_COMPONENT_TEMPLATE='
        {
            %s
        }'
JSON_PROPERTY_TEMPLATE='
        {
            \"'"$JSON_NAME"'\": \"%s\",
            \"'"$JSON_VALUE"'\": \"%s\"
        }
'
JSON_ADDRESSES_TEMPLATE=' \"'"$JSON_ADDRESSES"'\": [%s]'
JSON_ETHERNETMAC_TEMPLATE=' {
                \"'"$JSON_ETHERNETMAC"'\": \"%s\" } '
JSON_WLANMAC_TEMPLATE=' {
                \"'"$JSON_WLANMAC"'\": \"%s\" } '
JSON_BLUETOOTHMAC_TEMPLATE=' {
                \"'"$JSON_BLUETOOTHMAC"'\": \"%s\" } '

### JSON Constructor Aides
jsonManufacturer () {
    manufacturer=$(printf '\"'"$JSON_MANUFACTURER"'\": \"%s\"' "${1}")
    tmpManufacturerId=$(queryForPen "${1}")
    if [ -n "$tmpManufacturerId" ] && [ "$tmpManufacturerId" != "$PEN_ROOT" ]; then
        tmpManufacturerId=$(jsonManufacturerId "$tmpManufacturerId")
        manufacturer="$manufacturer"",""$tmpManufacturerId"
    fi
    printf "$manufacturer"
}
jsonModel () {
    printf '\"'"$JSON_MODEL"'\": \"%s\"' "${1}"
}
jsonSerial () {
    printf '\"'"$JSON_SERIAL"'\": \"%s\"' "${1}"
}
jsonRevision () {
    printf '\"'"$JSON_REVISION"'\": \"%s\"' "${1}"
}
jsonManufacturerId () {
    printf '\"'"$JSON_MANUFACTURERID"'\": \"%s\"' "${1}"
}
jsonFieldReplaceable () {
    printf '\"'"$JSON_FIELDREPLACEABLE"'\": \"%s\"' "${1}"
}
jsonEthernetMac () {
    printf "$JSON_ETHERNETMAC_TEMPLATE" "${1}"
}
jsonWlanMac () {
    printf "$JSON_WLANMAC_TEMPLATE" "${1}"
}
jsonBluetoothMac () {
    printf "$JSON_BLUETOOTHMAC_TEMPLATE" "${1}"
}
jsonPlatformModel () {
    printf '\"'"$JSON_PLATFORMMODEL"'\": \"%s\"' "${1}"
}
jsonPlatformManufacturerStr () {
    manufacturer=$(printf '\"'"$JSON_PLATFORMMANUFACTURERSTR"'\": \"%s\"' "${1}")
    tmpManufacturerId=$(queryForPen "${1}")
    if [ -n "$tmpManufacturerId" ] && [ "$tmpManufacturerId" != "$PEN_ROOT" ]; then
        tmpManufacturerId=$(jsonPlatformManufacturerId "$tmpManufacturerId")
        manufacturer="$manufacturer"",""$tmpManufacturerId"
    fi
    printf "$manufacturer"
}
jsonPlatformVersion () {
    printf '\"'"$JSON_PLATFORMVERSION"'\": \"%s\"' "${1}"
}
jsonPlatformSerial () {
    printf '\"'"$JSON_PLATFORMSERIAL"'\": \"%s\"' "${1}"
}
jsonPlatformManufacturerId () {
    printf '\"'"$JSON_PLATFORMMANUFACTURERID"'\": \"%s\"' "${1}"
}
queryForPen () {
    pen=$(grep -B 1 "^[ \t]*""${1}""$" "$ENTERPRISE_NUMBERS_FILE" | sed -n '1p' | tr -d [:space:])
    printf "%s%s" "$PEN_ROOT" "$pen"
}
jsonProperty () {
    if [ -n "${1}" ] && [ -n "${2}" ]; then
        printf "$JSON_PROPERTY_TEMPLATE" "${1}" "${2}"
    fi
}
jsonUri () {
    printf '\"'"$JSON_URI"'\": \"%s\"' "${1}"
}
jsonHashAlg () {
    printf '\"'"$JSON_HASHALG"'\": \"%s\"' "${1}"
}
jsonHashValue () {
    printf '\"'"$JSON_HASHVALUE"'\": \"%s\"' "${1}"
}
toCSV () {
    old="$IFS"
    IFS=','
    value="$*"
    value=$(printf "$value" | tr -s , | sed -e '1s/^[,]*//' | sed -e '$s/[,]*$//')
    printf "$value"
}
jsonAddress () {
    printf "$JSON_ADDRESSES_TEMPLATE" "$(toCSV "$@")"
}
jsonComponent () {
    printf "$JSON_COMPONENT_TEMPLATE" "$(toCSV "$@")"
}
jsonComponentArray () {
    printf "$JSON_COMPONENT_ARRAY_TEMPLATE" "$(toCSV "$@")"
}
jsonPropertyArray () {
    if [ "$#" -ne 0 ]; then
        printf "$JSON_PROPERTY_ARRAY_TEMPLATE" "$(toCSV "$@")"
    fi
}
jsonPlatformObject () {
    printf "$JSON_PLATFORM_TEMPLATE" "$(toCSV "$@")"
}
jsonPropertiesUri () {
    if [ -n "$PROPERTIES_URI" ]; then
        propertiesUri=$(jsonUri "$PROPERTIES_URI")
        propertiesUriDetails=""
        if [ -n "$PROPERTIES_URI_LOCAL_COPY_FOR_HASH" ]; then
            hashAlg="2.16.840.1.101.3.4.2.1" # SHA256, see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs
            hashValue=$(sha256sum "$PROPERTIES_URI_LOCAL_COPY_FOR_HASH" | sed -r 's/^([0-9a-f]+).*/\1/' | tr -d [:space:] | xxd -r -p | base64 -w 0)
            hashAlgStr=$(jsonHashAlg "$hashAlg")
            hashValueStr=$(jsonHashValue "$hashValue")
            propertiesUriDetails="$hashAlgStr"",""$hashValueStr"
        fi
	printf "$JSON_PROPERTIESURI_TEMPLATE" "$(toCSV "$propertiesUri" "$propertiesUriDetails")"
    fi
}
jsonIntermediateFile () {
    printf "$JSON_INTERMEDIATE_FILE_OBJECT" "$(toCSV "$@")"
}


## Some of the commands below require root.
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

### Gather platform details for the subject alternative name
platformManufacturer=$(dmidecode -s system-manufacturer)
platformModel=$(dmidecode -s system-product-name)
platformVersion=$(dmidecode -s system-version)
platformSerial=$(dmidecode -s system-serial-number)

if [[ -z "${platformManufacturer// }" ]]; then
    platformManufacturer="$NOT_SPECIFIED"
fi
platformManufacturer=$(jsonPlatformManufacturerStr "$platformManufacturer")

if [[ -z "${platformModel// }" ]]; then
    platformModel="$NOT_SPECIFIED"
fi
platformModel=$(jsonPlatformModel "$platformModel")

if [[ -z "${platformVersion// }" ]]; then
    platformVersion="$NOT_SPECIFIED"
fi
platformVersion=$(jsonPlatformVersion "$platformVersion")

if [ -n "$platformSerial" ]; then
    platformSerial=$(jsonPlatformSerial "$platformSerial")
fi
platform=$(jsonPlatformObject "$platformManufacturer" "$platformModel" "$platformVersion" "$platformSerial")
    


### Gather component details
chassisManufacturer=$(dmidecode -s chassis-manufacturer)
chassisModel=$(dmidecode -s chassis-type)
chassisSerial=$(dmidecode -s chassis-serial-number)
chassisRevision=$(dmidecode -s chassis-version)
if [[ -z "${chassisManufacturer// }" ]]; then
    chassisManufacturer="$NOT_SPECIFIED"
fi
chassisManufacturer=$(jsonManufacturer "$chassisManufacturer")

if [[ -z "${chassisModel// }" ]]; then
    chassisModel="$NOT_SPECIFIED"
fi 
chassisModel=$(jsonModel "$chassisModel")

chassisOptional=""
if [ -n "$chassisSerial" ]; then
    chassisSerial=$(jsonSerial "$chassisSerial")
    chassisOptional="$chassisOptional"",""$chassisSerial"
fi
if [ -n "$chassisRevision" ]; then
    chassisRevision=$(jsonRevision "$chassisRevision")
    chassisOptional="$chassisOptional"",""$chassisRevision"
fi
chassisOptional=$(printf "$chassisOptional" | cut -c2-)
componentChassis=$(jsonComponent "$chassisManufacturer" "$chassisModel" "$chassisOptional")

### Gather baseboard details
baseboardManufacturer=$(dmidecode -s baseboard-manufacturer)
baseboardModel=$(dmidecode -s baseboard-product-name)
baseboardSerial=$(dmidecode -s baseboard-serial-number)
baseboardRevision=$(dmidecode -s baseboard-version)
baseboardFieldReplaceableAnswer=$(dmidecode -t baseboard | grep --quiet "$(dmidecode -s baseboard-serial-number)" && dmidecode -t baseboard | grep --quiet "Board is replaceable" && echo "true" || echo "false")
baseboardFieldReplaceable=$(jsonFieldReplaceable "$baseboardFieldReplaceableAnswer")
componentBaseboard=$(jsonComponent "$baseboardManufacturer" "$baseboardModel" "$baseboardSerial" "$baseboardRevision" "$baseboardFieldReplaceable")
if [[ -z "${baseboardManufacturer// }" ]]; then
    baseboardManufacturer="$NOT_SPECIFIED"
fi
baseboardManufacturer=$(jsonManufacturer "$baseboardManufacturer")

if [[ -z "${baseboardModel// }" ]]; then
    baseboardModel="$NOT_SPECIFIED"
fi
baseboardModel=$(jsonModel "$baseboardModel")

baseboardOptional=""
if [ -n "$baseboardSerial" ]; then
    baseboardSerial=$(jsonSerial "$baseboardSerial")
    baseboardOptional="$baseboardOptional"",""$baseboardSerial"
fi
if [ -n "$baseboardRevision" ]; then
    baseboardRevision=$(jsonRevision "$baseboardRevision")
    baseboardOptional="$baseboardOptional"",""$baseboardRevision"
fi
baseboardOptional=$(printf "$baseboardOptional" | cut -c2-)
componentBaseboard=$(jsonComponent "$baseboardManufacturer" "$baseboardModel" "$baseboardFieldReplaceable" "$baseboardOptional")

### Gather BIOS details
biosUefiManufacturer=$(dmidecode -s bios-vendor)
biosUefiModel=$(jsonModel "$(dmesg | grep efi | grep SMBIOS > /dev/null && printf \"UEFI\" || printf \"BIOS\")")
biosUefiRevision=$(dmidecode -s bios-version)
if [[ -z "${biosUefiManufacturer// }" ]]; then
    biosUefiManufacturer="$NOT_SPECIFIED"
fi
biosUefiManufacturer=$(jsonManufacturer "$biosUefiManufacturer")

if [ -n "$biosUefiRevision" ]; then
    biosUefiRevision=$(jsonRevision "$biosUefiRevision")
fi
componentBiosUefi=$(jsonComponent "$biosUefiManufacturer" "$biosUefiModel" "$biosUefiRevision")

parseCpuData () {
    dmiCpu=$(dmidecode -t processor)

    manufacturer=""
    model=""
    serialnumber=""
    revision=""
    replaceable=$(jsonFieldReplaceable "true")
    tmpData=""
    isNewCpu=""
    numLines=$(printf "$dmiCpu" | wc -l)
    lineItr=0
    while read -r line; do
        isNewCpu=$(printf "$line" | grep --quiet "DMI type 4" || (($lineItr + 1 >= $numLines)) && echo "1" || echo "")
        if [ -n "$isNewCpu" ] && [ -n "$manufacturer" ] && [ -n "$model" ]; then
            theRest=""
            if [ -n "$serialnumber" ]; then
                tmpSerial=$(jsonSerial "$serialnumber")
                theRest="$theRest"",""$tmpSerial"
            fi

            if [ -n "$revision" ]; then
                tmpRevision=$(jsonRevision "$revision")
                theRest="$theRest"",""$tmpRevision"
            fi

            theRest=$(printf "$theRest" | cut -c2-)
            
            tmpManufacturer=$(jsonManufacturer "$manufacturer")
            tmpModel=$(jsonModel "$model")
            newCpuData=$(jsonComponent "$tmpManufacturer" "$tmpModel" "$replaceable" "$theRest")
            tmpData="$tmpData"",""$newCpuData"

            isNewCpu=""
            manufacturer=""
            model=""
            serialnumber=""
            revision=""
        fi

        if printf "$line" | grep --quiet "Manufacturer"; then
            manufacturer=$(printf "$line" | grep "Manufacturer" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi
        
        if printf "$line" | grep --quiet "Family:"; then
            model=$(printf "$line" | grep "Family" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi

        if printf "$line" | grep --quiet "Serial Number"; then
            serialnumber=$(printf "$line" | grep "Serial Number" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi

        if printf "$line" | grep --quiet "Version"; then
            revision=$(printf "$line" | grep "Version" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi
        
        lineItr=$lineItr+1
    done <<< "$dmiCpu"

    # remove leading comma
    tmpData=$(printf "$tmpData" | cut -c2-)

    printf "$tmpData"
}

parseRamData () {
    dmiMem=$(dmidecode -t memory)

    manufacturer=""
    model=""
    serialnumber=""
    revision=""  #memory doesn't have a version field in dmidecode
    replaceable=$(jsonFieldReplaceable "true")
    tmpData=""
    isNewRam=""
    numLines=$(printf "$dmiMem" | wc -l)
    lineItr=0
    while read -r line; do
        isNewRam=$(printf "$line" | grep --quiet "DMI type 17" || (($lineItr + 1 >= $numLines)) && echo "1" || echo "")
        if [ -n "$isNewRam" ] && [ -n "$manufacturer" ] && [ -n "$model" ]; then
            theRest=""
            if [ -n "$serialnumber" ]; then
                tmpSerial=$(jsonSerial "$serialnumber")
                theRest="$theRest"",""$tmpSerial"
            fi

            if [ -n "$revision" ]; then
                tmpRevision=$(jsonRevision "$revision")
                theRest="$theRest"",""$tmpRevision"
            fi

            if [ "$manufacturer" != "$NOT_SPECIFIED" ] || [ "$model" != "$NOT_SPECIFIED" ] || { [ -n "$serialnumber" ] && [ "$serialnumber" != "$NOT_SPECIFIED" ]; } || { [ -n "$revision" ] && [ "$revision" != "$NOT_SPECIFIED" ]; }; then
            theRest=$(printf "$theRest" | cut -c2-)
            
            tmpManufacturer=$(jsonManufacturer "$manufacturer")
            tmpModel=$(jsonModel "$model")
            newRamData=$(jsonComponent "$tmpManufacturer" "$tmpModel" "$replaceable" "$theRest")
            tmpData="$tmpData"",""$newRamData"
            fi
            isNewRam=""
            manufacturer=""
            model=""
            serialnumber=""
            revision=""
        fi

        if printf "$line" | grep --quiet "Manufacturer"; then
            manufacturer=$(printf "$line" | grep "Manufacturer" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi
        
        if printf "$line" | grep --quiet "Part Number"; then
            model=$(printf "$line" | grep "Part Number" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi

        if printf "$line" | grep --quiet "Serial Number"; then
            serialnumber=$(printf "$line" | grep "Serial Number" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi

        if printf "$line" | grep --quiet "Version"; then
            revision=$(printf "$line" | grep "Version" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi
        
        lineItr=$lineItr+1
    done <<< "$dmiMem"

    # remove leading comma
    tmpData=$(printf "$tmpData" | cut -c2-)

    printf "$tmpData"
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
    nicData=$(lshw -class network)

    manufacturer=""
    model=""
    serialnumber=""
    revision=""
    replaceable=$(jsonFieldReplaceable "true")
    addressType=""
    tmpData=""
    isNewNic=""
    numLines=$(printf "$nicData" | wc -l)
    lineItr=0
    while read -r line; do
        isNewNic=$(printf "$line" | grep --quiet "*-network" || (($lineItr + 1 >= $numLines)) && echo "1" || echo "")
        if [ -n "$isNewNic" ] && [ -n "$manufacturer" ] && [ -n "$model" ]; then
            theRest=""
            if [ -n "$serialnumber" ]; then
                tmpSerial=$(jsonSerial "$serialnumber")
                theRest="$theRest"",""$tmpSerial"
            fi            

            if [ -n "$revision" ]; then
                tmpRevision=$(jsonRevision "$revision")
                theRest="$theRest"",""$tmpRevision"
            fi

            if [ -n "$addressType" ] && [ -n "$serialnumber" ]; then
                thisAddress=
                if [ "$addressType" == "$JSON_WLANMAC" ]; then
                    thisAddress=$(jsonWlanMac "$serialnumber")
                elif [ "$addressType" == "$JSON_BLUETOOTHMAC" ]; then
                    thisAddress=$(jsonBluetoothMac "$serialnumber")
                elif [ "$addressType" == "$JSON_ETHERNETMAC" ]; then
                    thisAddress=$(jsonEthernetMac "$serialnumber") 
                fi
                if [ -n "$thisAddress" ]; then
                    thisAddress=$(jsonAddress "$thisAddress")
                    theRest="$theRest"",""$thisAddress"
                fi
            fi

            theRest=$(printf "$theRest" | cut -c2-)
            
            tmpManufacturer=$(jsonManufacturer "$manufacturer")
            tmpModel=$(jsonModel "$model")
            newNicData=$(jsonComponent "$tmpManufacturer" "$tmpModel" "$replaceable" "$theRest")
            tmpData="$tmpData"",""$newNicData"
            
            isNewNic=""
            manufacturer=""
            model=""
            serialnumber=""
            revision=""
            addressType=""
        fi

        if printf "$line" | grep --quiet "vendor"; then
            manufacturer=$(printf "$line" | grep "vendor" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi
        
        if printf "$line" | grep --quiet "product"; then
            model=$(printf "$line" | grep "product" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi

        if printf "$line" | grep --quiet "serial:"; then
            serialnumber=$(printf "$line" | grep "serial" | sed 's/.*serial://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi

        if printf "$line" | grep --quiet "version:"; then
            revision=$(printf "$line" | grep "version" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi

        if printf "$line" | grep --quiet "capabilities.*ethernet"; then
            addressType="$JSON_ETHERNETMAC"
        fi

        if printf "$line" | grep --quiet "capabilities.*wireless"; then
            addressType="$JSON_WLANMAC"
        fi

        if printf "$line" | grep --quiet "capabilities.*bluetooth"; then
            addressType="$JSON_BLUETOOTHMAC"
        fi
        
        lineItr=$lineItr+1
    done <<< "$nicData"

    # remove leading comma
    tmpData=$(printf "$tmpData" | cut -c2-)

    printf "$tmpData"
}

parseHddData () {
    hddData=$(lshw -class disk)

    manufacturer="Not Specified"
    model=""
    serialnumber=""
    revision=""
    replaceable=$(jsonFieldReplaceable "true")
    tmpData=""
    isNewHdd=""
    isNewRemovableDisk=""
    acceptRemovableDisk=""
    skip=""
    numLines=$(printf "$hddData" | wc -l)
    lineItr=0
    while read -r line; do
        isNewHdd=$(printf "$line" | grep --quiet "*-disk" && echo "1" || echo "")
        isNewRemovableDisk=$(printf "$line" | grep --quiet "*-cdrom" && echo "1" || echo "")
        atTheEnd=$((($lineItr + 1 >= $numLines)) && echo "1" || echo "")
        lineItr=$lineItr+1
        if [ -n "$isNewRemovableDisk" ] && [ -z "$acceptRemovableDisk" ]; then
            skip="true"
            isNewRemovableDisk=""
            continue
        elif [ -n "$atTheEnd" ] || [ -n "$isNewHdd" ]; then
            skip=""
        elif [ -n "$skip" ]; then
            continue
        fi

        if { [ -n "$isNewHdd" ] || [ -n "$atTheEnd" ] || [ -n "$isNewRemovableDisk" ]; } && [ -z "$skip" ] && [ -n "$manufacturer" ] && [ -n "$model" ]; then
            theRest=""
            if [ -n "$serialnumber" ]; then
                tmpSerial=$(jsonSerial "$serialnumber")
                theRest="$theRest"",""$tmpSerial"
            fi

            if [ -n "$revision" ]; then
                tmpRevision=$(jsonRevision "$revision")
                theRest="$theRest"",""$tmpRevision"
            fi

            theRest=$(printf "$theRest" | cut -c2-)
            
            tmpManufacturer=$(jsonManufacturer "$manufacturer")
            tmpModel=$(jsonModel "$model")
            newHddData=$(jsonComponent "$tmpManufacturer" "$tmpModel" "$replaceable" "$theRest")
            tmpData="$tmpData"",""$newHddData"

            isNewHdd=""
            manufacturer="Not Specified"
            model=""
            serialnumber=""
            revision=""
        fi

        if printf "$line" | grep --quiet "vendor"; then
            manufacturer=$(printf "$line" | grep "vendor" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi
        
        if printf "$line" | grep --quiet "product"; then
            model=$(printf "$line" | grep "product" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi

        if printf "$line" | grep --quiet "serial:"; then
            serialnumber=$(printf "$line" | grep "serial" | sed 's/.*serial://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi

        if printf "$line" | grep --quiet "version:"; then
            revision=$(printf "$line" | grep "version" | sed 's/.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        fi
    done <<< "$hddData"
    

    # remove leading comma
    tmpData=$(printf "$tmpData" | cut -c2-)

    printf "$tmpData"
}


### Gather property details
property1=$(jsonProperty "uname -r" "$(uname -r)")  ## Example1
property2=$(jsonProperty "cat /etc/centos-release" "$(cat /etc/centos-release)") ## Example2

### Collate the component details
componentsCPU=$(parseCpuData)
componentsRAM=$(parseRamData)
componentsNIC=$(parseNicData)
componentsHDD=$(parseHddData)
componentArray=$(jsonComponentArray "$componentChassis" "$componentBaseboard" "$componentsCPU" "$componentsRAM" "$componentsNIC" "$componentsHDD")

### Collate the property details
propertyArray=$(jsonPropertyArray "$property1" "$property2")

### Construct the final JSON object
FINAL_JSON_OBJECT=$(jsonIntermediateFile "$platform" "$componentArray" "$propertyArray")

### Collate the URI details, if parameters above are blank, the fields will be excluded from the final JSON structure
if [ -n "$PROPERTIES_URI" ]; then
    propertiesUri=$(jsonPropertiesUri)
    FINAL_JSON_OBJECT="$FINAL_JSON_OBJECT"",""$propertiesUri"
fi

printf "$FINAL_JSON_OBJECT""\n\n"


