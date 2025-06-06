#!/bin/bash

### User customizable values
APP_HOME=$(dirname "$0")
ENTERPRISE_NUMBERS_FILE="$APP_HOME""/enterprise-numbers"
PEN_ROOT="1.3.6.1.4.1." # OID root for the private enterprise numbers

### JSON Structure Keywords
JSON_COMPONENTS="COMPONENTS"
JSON_COMPONENTSURI="COMPONENTSURI"
JSON_PROPERTIES="PROPERTIES"
JSON_PROPERTIESURI="PROPERTIESURI"
JSON_PLATFORM="PLATFORM"
#### JSON Component Keywords
JSON_COMPONENTCLASS="COMPONENTCLASS"
JSON_COMPONENTCLASSREGISTRY="COMPONENTCLASSREGISTRY"
JSON_COMPONENTCLASSVALUE="COMPONENTCLASSVALUE"
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
JSON_COMPONENTPLATFORMCERT="PLATFORMCERT"
JSON_ATTRIBUTECERTIDENTIFIER="ATTRIBUTECERTIDENTIFIER"
JSON_GENERICCERTIDENTIFIER="GENERICCERTIDENTIFIER"
JSON_ISSUER="ISSUER"
JSON_COMPONENTPLATFORMCERTURI="PLATFORMCERTURI"
JSON_STATUS="STATUS"
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
JSON_NAME="PROPERTYNAME"
JSON_VALUE="PROPERTYVALUE"
JSON_PROP_STATUS="PROPERTYSTATUS"
#### JSON Status Keywords
# shellcheck disable=SC2034
JSON_STATUS_ADDED="ADDED"
# shellcheck disable=SC2034
JSON_STATUS_MODIFIED="MODIFIED"
# shellcheck disable=SC2034
JSON_STATUS_REMOVED="REMOVED"
# shellcheck disable=SC2034
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
JSON_COMPONENTSURI_TEMPLATE='
    \"'"$JSON_COMPONENTSURI"'\": {
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
JSON_PROPERTY_TEMPLATE_OPT='
        {
            \"'"$JSON_NAME"'\": \"%s\",
            \"'"$JSON_VALUE"'\": \"%s\",
            \"'"$JSON_PROP_STATUS"'\": \"%s\"
        }
'
JSON_ADDRESSES_TEMPLATE=' \"'"$JSON_ADDRESSES"'\": [%s]'
JSON_ETHERNETMAC_TEMPLATE=' {
                \"'"$JSON_ETHERNETMAC"'\": \"%s\" } '
JSON_WLANMAC_TEMPLATE=' {
                \"'"$JSON_WLANMAC"'\": \"%s\" } '
JSON_BLUETOOTHMAC_TEMPLATE=' {
                \"'"$JSON_BLUETOOTHMAC"'\": \"%s\" } '
JSON_COMPONENTCLASS_TEMPLATE=' \"'"$JSON_COMPONENTCLASS"'\": {
        \"'"$JSON_COMPONENTCLASSREGISTRY"'\": \"%s\",
        \"'"$JSON_COMPONENTCLASSVALUE"'\": \"%s\"
    }'
# shellcheck disable=SC2034
JSON_ATTRIBUTECERTIDENTIFIER_TEMPLATE=' \"'"$JSON_ATTRIBUTECERTIDENTIFIER"'\": {
        \"'"$JSON_HASHALG"'\": \"%s\",
        \"'"$JSON_HASHVALUE"'\": \"%s\"
    },'
# shellcheck disable=SC2034
JSON_GENERICCERTIDENTIFIER_TEMPLATE=' \"'"$JSON_GENERICCERTIDENTIFIER"'\": {
        \"'"$JSON_ISSUER"'\": \"%s\",
        \"'"$JSON_SERIAL"'\": \"%s\"
    },'
# shellcheck disable=SC2034
JSON_COMPONENTPLATFORMCERT_TEMPLATE='
    \"'"$JSON_COMPONENTPLATFORMCERT"'\": {
        %s
    }'
# shellcheck disable=SC2034
JSON_COMPONENTPLATFORMCERTURI_TEMPLATE='
    \"'"$JSON_COMPONENTPLATFORMCERTURI"'\": {
        %s
    }'
# shellcheck disable=SC2034
JSON_STATUS_TEMPLATE='
    \"'"$JSON_STATUS"'\": {

    }'

### JSON Constructor Aides
jsonComponentClass () {
    # variable contains the format string
    # shellcheck disable=SC2059
    printf "$JSON_COMPONENTCLASS_TEMPLATE" "${1}" "${2}"
}
jsonManufacturer () {
    manufacturer=$(printf '\"'"$JSON_MANUFACTURER"'\": \"%s\"' "${1}")
    #tmpManufacturerId=$(queryForPen "${1}")
    #if [ -n "$tmpManufacturerId" ] && [ "$tmpManufacturerId" != "$PEN_ROOT" ]; then
    #    tmpManufacturerId=$(jsonManufacturerId "$tmpManufacturerId")
    #    manufacturer="$manufacturer"",""$tmpManufacturerId"
    #fi
    printf "%s" "$manufacturer"
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
    # variable contains the format string
    # shellcheck disable=SC2059
    printf "$JSON_ETHERNETMAC_TEMPLATE" "${1}"
}
jsonWlanMac () {
    # variable contains the format string
    # shellcheck disable=SC2059
    printf "$JSON_WLANMAC_TEMPLATE" "${1}"
}
jsonBluetoothMac () {
    # variable contains the format string
    # shellcheck disable=SC2059
    printf "$JSON_BLUETOOTHMAC_TEMPLATE" "${1}"
}
jsonPlatformModel () {
    printf '\"'"$JSON_PLATFORMMODEL"'\": \"%s\"' "${1}"
}
jsonPlatformManufacturerStr () {
    manufacturer=$(printf '\"'"$JSON_PLATFORMMANUFACTURERSTR"'\": \"%s\"' "${1}")
    #tmpManufacturerId=$(queryForPen "${1}")
    #if [ -n "$tmpManufacturerId" ] && [ "$tmpManufacturerId" != "$PEN_ROOT" ]; then
    #    tmpManufacturerId=$(jsonPlatformManufacturerId "$tmpManufacturerId")
    #    manufacturer="$manufacturer"",""$tmpManufacturerId"
    #fi
    printf "%s" "$manufacturer"
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
    pen=$(grep -B 1 "^[ \t]*""${1}""$" "$ENTERPRISE_NUMBERS_FILE" | sed -n '1p' | tr -d '[:space:]')
    printf "%s%s" "$PEN_ROOT" "$pen"
}
jsonProperty () {
    if [ -n "${1}" ] && [ -n "${2}" ]; then
        if [ -n "${3}" ]; then
            # variable contains the format string
            # shellcheck disable=SC2059
            printf "$JSON_PROPERTY_TEMPLATE_OPT" "${1}" "${2}" "${3}"
        else
            # variable contains the format string
            # shellcheck disable=SC2059
            printf "$JSON_PROPERTY_TEMPLATE" "${1}" "${2}"
        fi
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
    local value=""
    local IFS=','
    value="$*"
    # trim leading and trailing commas
    value=$(printf "%s" "$value" | tr -s , | sed -e '1s/^[,]*//' | sed -e '$s/[,]*$//')
    printf "%s" "$value"
}
jsonAddress () {
    # variable contains the format string
    # shellcheck disable=SC2059
    printf "$JSON_ADDRESSES_TEMPLATE" "$(toCSV "$@")"
}
jsonComponent () {
    # variable contains the format string
    # shellcheck disable=SC2059
    printf "$JSON_COMPONENT_TEMPLATE" "$(toCSV "$@")"
}
jsonComponentArray () {
    # variable contains the format string
    # shellcheck disable=SC2059
    printf "$JSON_COMPONENT_ARRAY_TEMPLATE" "$(toCSV "$@")"
}
jsonPropertyArray () {
    if [ "$#" -ne 0 ]; then
        # variable contains the format string
        # shellcheck disable=SC2059
        printf "$JSON_PROPERTY_ARRAY_TEMPLATE" "$(toCSV "$@")"
    fi
}
jsonPlatformObject () {
    # variable contains the format string
    # shellcheck disable=SC2059
    printf "$JSON_PLATFORM_TEMPLATE" "$(toCSV "$@")"
}
jsonComponentsUri () {
    COMPONENTS_URI="$1"
    COMPONENTS_URI_LOCAL_COPY_FOR_HASH="$2"
    if [[ -n "$COMPONENTS_URI" && "$COMPONENTS_URI" != *[[:space:]]* ]]; then
        componentsUri=$(jsonUri "$COMPONENTS_URI")
        componentsUriDetails=""
        if [[ -n "$COMPONENTS_URI_LOCAL_COPY_FOR_HASH" && "$COMPONENTS_URI_LOCAL_COPY_FOR_HASH" != *[[:space:]]* && -f "$COMPONENTS_URI_LOCAL_COPY_FOR_HASH" ]]; then
            hashAlg="2.16.840.1.101.3.4.2.1" # SHA256, see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs
            hashValue=$(sha256sum "$COMPONENTS_URI_LOCAL_COPY_FOR_HASH" | sed -r 's/^([0-9a-f]+).*/\1/' | tr -d '[:space:]' | xxd -r -p | base64 -w 0)
            hashAlgStr=$(jsonHashAlg "$hashAlg")
            hashValueStr=$(jsonHashValue "$hashValue")
            componentsUriDetails="$hashAlgStr"",""$hashValueStr"
        fi
        # variable contains the format string
        # shellcheck disable=SC2059
        printf "$JSON_COMPONENTSURI_TEMPLATE" "$(toCSV "$componentsUri" "$componentsUriDetails")"
    fi
}
jsonPropertiesUri () {
    PROPERTIES_URI="$1"
    PROPERTIES_URI_LOCAL_COPY_FOR_HASH="$2"
    if [[ -n "$PROPERTIES_URI" && "$PROPERTIES_URI" != *[[:space:]]* ]]; then
        propertiesUri=$(jsonUri "$PROPERTIES_URI")
        propertiesUriDetails=""
        if [[ -n "$PROPERTIES_URI_LOCAL_COPY_FOR_HASH" && "$PROPERTIES_URI_LOCAL_COPY_FOR_HASH" != *[[:space:]]* && -f "$PROPERTIES_URI_LOCAL_COPY_FOR_HASH" ]]; then
            hashAlg="2.16.840.1.101.3.4.2.1" # SHA256, see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs
            hashValue=$(sha256sum "$PROPERTIES_URI_LOCAL_COPY_FOR_HASH" | sed -r 's/^([0-9a-f]+).*/\1/' | tr -d '[:space:]' | xxd -r -p | base64 -w 0)
            hashAlgStr=$(jsonHashAlg "$hashAlg")
            hashValueStr=$(jsonHashValue "$hashValue")
            propertiesUriDetails="$hashAlgStr"",""$hashValueStr"
        fi
        # variable contains the format string
        # shellcheck disable=SC2059
        printf "$JSON_PROPERTIESURI_TEMPLATE" "$(toCSV "$propertiesUri" "$propertiesUriDetails")"
    fi
}
jsonIntermediateFile () {
    # variable contains the format string
    # shellcheck disable=SC2059
    printf "$JSON_INTERMEDIATE_FILE_OBJECT" "$(toCSV "$@")"
}
