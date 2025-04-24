### User customizable values
$APP_HOME=(Split-Path -parent $PSCommandPath)
$ENTERPRISE_NUMBERS_FILE="$APP_HOME/../enterprise-numbers"
$PEN_ROOT="1.3.6.1.4.1." # OID root for the private enterprise numbers

### JSON Structure Keywords
$JSON_COMPONENTS="COMPONENTS"
$JSON_PROPERTIES="PROPERTIES"
$JSON_PROPERTIESURI="PROPERTIESURI"
$JSON_PLATFORM="PLATFORM"
#### JSON Component Keywords
$JSON_COMPONENTCLASS="COMPONENTCLASS"
$JSON_COMPONENTCLASSREGISTRY="COMPONENTCLASSREGISTRY"
$JSON_COMPONENTCLASSVALUE="COMPONENTCLASSVALUE"
$JSON_MANUFACTURER="MANUFACTURER"
$JSON_MODEL="MODEL"
$JSON_SERIAL="SERIAL"
$JSON_REVISION="REVISION"
$JSON_MANUFACTURERID="MANUFACTURERID"
$JSON_FIELDREPLACEABLE="FIELDREPLACEABLE"
$JSON_ADDRESSES="ADDRESSES"
$JSON_ETHERNETMAC="ETHERNETMAC"
$JSON_WLANMAC="WLANMAC"
$JSON_BLUETOOTHMAC="BLUETOOTHMAC"
$JSON_COMPONENTPLATFORMCERT="PLATFORMCERT"
$JSON_ATTRIBUTECERTIDENTIFIER="ATTRIBUTECERTIDENTIFIER"
$JSON_GENERICCERTIDENTIFIER="GENERICCERTIDENTIFIER"
$JSON_ISSUER="ISSUER"
$JSON_COMPONENTPLATFORMCERTURI="PLATFORMCERTURI"
$JSON_STATUS="STATUS"
#### JSON Platform Keywords (Subject Alternative Name)
$JSON_PLATFORMMODEL="PLATFORMMODEL"
$JSON_PLATFORMMANUFACTURERSTR="PLATFORMMANUFACTURERSTR"
$JSON_PLATFORMVERSION="PLATFORMVERSION"
$JSON_PLATFORMSERIAL="PLATFORMSERIAL"
$JSON_PLATFORMMANUFACTURERID="PLATFORMMANUFACTURERID"
#### JSON Platform URI Keywords
$JSON_URI="UNIFORMRESOURCEIDENTIFIER"
$JSON_HASHALG="HASHALGORITHM"
$JSON_HASHVALUE="HASHVALUE"
#### JSON Properties Keywords
$JSON_NAME="PROPERTYNAME"
$JSON_VALUE="PROPERTYVALUE"
$JSON_PROP_STATUS="PROPERTYSTATUS"
#### JSON Status Keywords
$JSON_STATUS_ADDED="ADDED"
$JSON_STATUS_MODIFIED="MODIFIED"
$JSON_STATUS_REMOVED="REMOVED"
$NOT_SPECIFIED="Not Specified"


### JSON Structure Format
$JSON_INTERMEDIATE_FILE_OBJECT="{{
    {0}
}}"
$JSON_PLATFORM_TEMPLATE="
    `"$JSON_PLATFORM`": {{
        {0}
    }}"
$JSON_PROPERTIESURI_TEMPLATE="
    `"$JSON_PROPERTIESURI`": {{
        {0}
    }}"
$JSON_COMPONENTSURI_TEMPLATE="
    `"$JSON_COMPONENTSURI`": {{
        {0}
    }}"
$JSON_PROPERTY_ARRAY_TEMPLATE="
    `"$JSON_PROPERTIES`": [{0}
    ]"
$JSON_COMPONENT_ARRAY_TEMPLATE="
    `"$JSON_COMPONENTS`": [{0}
    ]"
$JSON_COMPONENT_TEMPLATE="
        {{
            {0}
        }}"
$JSON_PROPERTY_TEMPLATE="
        {{
            `"$JSON_NAME`": `"{0}`",
            `"$JSON_VALUE`": `"{1}`"
        }}
"
$JSON_PROPERTY_TEMPLATE_OPT="
        {{
            `"$JSON_NAME`": `"{0}`",
            `"$JSON_VALUE`": `"{1}`",
            `"$JSON_PROP_STATUS`": `"{2}`"
        }}
"
$JSON_ADDRESSES_TEMPLATE=" `"$JSON_ADDRESSES`": [{0}]"
$JSON_ETHERNETMAC_TEMPLATE=" {{
                `"$JSON_ETHERNETMAC`": `"{0}`" }} "
$JSON_WLANMAC_TEMPLATE=" {{
                `"$JSON_WLANMAC`": `"{0}`" }} "
$JSON_BLUETOOTHMAC_TEMPLATE=" {{
                `"$JSON_BLUETOOTHMAC`": `"{0}`" }} "
$JSON_COMPONENTCLASS_TEMPLATE=" `"$JSON_COMPONENTCLASS`": {{
        `"$JSON_COMPONENTCLASSREGISTRY`": `"{0}`",
        `"$JSON_COMPONENTCLASSVALUE`": `"{1}`"
    }}"
$JSON_ATTRIBUTECERTIDENTIFIER_TEMPLATE=" `"$JSON_ATTRIBUTECERTIDENTIFIER`": {{
        `"$JSON_HASHALG`": `"{0}`",
        `"$JSON_HASHVALUE`": `"{1}`"
    }},"
$JSON_GENERICCERTIDENTIFIER_TEMPLATE=" `"$JSON_GENERICCERTIDENTIFIER`": {{
        `"$JSON_ISSUER`": `"{0}`",
        `"$JSON_SERIAL`": `"{1}`"
    }},"
$JSON_COMPONENTPLATFORMCERT_TEMPLATE="
    `"$JSON_COMPONENTPLATFORMCERT`": {{
        {0}
    }}"
$JSON_COMPONENTPLATFORMCERTURI_TEMPLATE='
    `"$JSON_COMPONENTPLATFORMCERTURI`": {{
        {0}
    }}'
$JSON_STATUS_TEMPLATE="
    `"$JSON_STATUS`": {{
    }}"

### JSON Constructor Aides
function HexToByteArray { # Powershell doesn't have a built in BinToHex function
    Param ([String] $str )

    if ($str.Length % 2 -ne 0) {
        $str="0$str"
    }

    if ($str.Length -ne 0) {
        ,@($str -split '([a-f0-9]{2})' | foreach-object {
            if ($_) {
                [System.Convert]::ToByte($_,16)
            }
        })
    }
}
function jsonComponentClass () {
    echo ("$JSON_COMPONENTCLASS_TEMPLATE" -f "$($args[0])","$($args[1])")
}
function jsonManufacturer () {
    $manufacturer=("`"$JSON_MANUFACTURER`": `"{0}`"" -f "$($args[0])")
    #$tmpManufacturerId=(queryForPen "$($args[0])")
    #if (($tmpManufacturerId) -and ("$tmpManufacturerId" -ne "$PEN_ROOT")) {
    #    $tmpManufacturerId=(jsonManufacturerId "$tmpManufacturerId")
    #    $manufacturer="$manufacturer,$tmpManufacturerId"
    #}
    echo "$manufacturer"
}
function jsonModel () {
    echo ("`"$JSON_MODEL`": `"{0}`"" -f "$($args[0])")
}
function jsonSerial () {
    echo ("`"$JSON_SERIAL`": `"{0}`"" -f "$($args[0])")
}
function jsonRevision () {
    echo ("`"$JSON_REVISION`": `"{0}`"" -f "$($args[0])")
}
function jsonManufacturerId () {
    echo ("`"$JSON_MANUFACTURERID`": `"{0}`"" -f "$($args[0])")
}
function jsonFieldReplaceable () {
    echo ("`"$JSON_FIELDREPLACEABLE`": `"{0}`"" -f "$($args[0])")
}
function jsonEthernetMac () {
    echo ("$JSON_ETHERNETMAC_TEMPLATE" -f "$($args[0])")
}
function jsonWlanMac () {
    echo ("$JSON_WLANMAC_TEMPLATE" -f "$($args[0])")
}
function jsonBluetoothMac () {
    echo ("$JSON_BLUETOOTHMAC_TEMPLATE" -f "$($args[0])")
}
function jsonPlatformModel () {
    echo ("`"$JSON_PLATFORMMODEL`": `"{0}`"" -f "$($args[0])")
}
function jsonPlatformManufacturerStr () {
    $manufacturer=("`"$JSON_PLATFORMMANUFACTURERSTR`": `"{0}`"" -f "$($args[0])")
    #$tmpManufacturerId=(queryForPen "$($args[0])")
    #if (($tmpManufacturerId) -and ("$tmpManufacturerId" -ne "$PEN_ROOT")) {
    #    $tmpManufacturerId=(jsonPlatformManufacturerId "$tmpManufacturerId")
    #    $manufacturer="$manufacturer,$tmpManufacturerId"
    #}
    echo "$manufacturer"
}
function jsonPlatformVersion () {
    echo ("`"$JSON_PLATFORMVERSION`": `"{0}`"" -f "$($args[0])")
}
function jsonPlatformSerial () {
    echo ("`"$JSON_PLATFORMSERIAL`": `"{0}`"" -f "$($args[0])")
}
function jsonPlatformManufacturerId () {
    echo ("`"$JSON_PLATFORMMANUFACTURERID`": `"{0}`"" -f "$($args[0])")
}
function queryForPen () {
    Write-Progress -Id 3 -ParentId 2 -Activity "Searching for PEN..."
    $result=$PEN_ROOT
    if($args[0]) {
        $penObject=(Get-Content "$ENTERPRISE_NUMBERS_FILE" | Select-String -Pattern "^[ \t]*$($args[0])`$" -Context 1)
        if ($penObject) {
            Write-Progress -Id 3 -ParentId 2 -Activity "Searching for PEN..." -CurrentOperation "Found"
            $pen=$penObject.Context.PreContext[0]
            $result+="$pen"
        }
    }
    Write-Progress -Id 3 -ParentId 2 -Activity "Searching for PEN..." -PercentComplete 100
    echo $result
}
function jsonProperty () {
    if ($args.Length -eq 2) {
        echo ("$JSON_PROPERTY_TEMPLATE" -f "$($args[0])","$($args[1])")
    } elseif ($args.Length -eq 3) {
        echo ("$JSON_PROPERTY_TEMPLATE_OPT" -f "$($args[0])","$($args[1])","$($args[2])")
    }
}
function jsonUri () {
    echo ("`"$JSON_URI`": `"{0}`"" -f "$($args[0])")
}
function jsonHashAlg () {
    echo ("`"$JSON_HASHALG`": `"{0}`"" -f "$($args[0])")
}
function jsonHashValue () {
    echo ("`"$JSON_HASHVALUE`": `"{0}`"" -f "$($args[0])")
}
function toCSV () {
    if ($args.Length -ne 0) {
        Write-Progress -Id 3 -ParentId 2 -Activity "CSV..." -PercentComplete 0

        $size = $args[0].Length
        for ($i=0; $i -lt $size; $i++) {
            Write-Progress -Id 3 -ParentId 2 -Activity "CSV..." -PercentComplete (($i / $size) * 100)

            $item=($args[0].Get($i))

            if ($item) {
                $value="$value,$($args[0].Get($i))"
            }
        }
        echo "$value".Trim(" ", ",")
        Write-Progress -Id 3 -ParentId 2 -Activity "CSV..." -PercentComplete 100
    }
}
function jsonAddress () {
    echo ("$JSON_ADDRESSES_TEMPLATE" -f "$(toCSV($args))")
}
function jsonComponent () {
    echo ("$JSON_COMPONENT_TEMPLATE" -f "$(toCSV($args))")
}
function jsonComponentArray () {
    echo ("$JSON_COMPONENT_ARRAY_TEMPLATE" -f "$(toCSV($args))")
}
function jsonPropertyArray () {
    echo ("$JSON_PROPERTY_ARRAY_TEMPLATE" -f "$(toCSV($args))")
}
function jsonPlatformObject () {
    echo ("$JSON_PLATFORM_TEMPLATE" -f "$(toCSV($args))")
}
function jsonComponentsUri () {
    if ($COMPONENTS_URI) {
        $componentsUri=(jsonUri "$COMPONENTS_URI")
        $componentsUriDetails=""
        if ($COMPONENTS_URI_LOCAL_COPY_FOR_HASH) {
            $hashAlg="2.16.840.1.101.3.4.2.1" # SHA256, see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs
            $hashValue=([System.Convert]::ToBase64String($(HexToByteArray $(Get-FileHash "$COMPONENTS_URI_LOCAL_COPY_FOR_HASH"  -Algorithm SHA256).Hash.Trim())))
            $hashAlgStr=(jsonHashAlg "$hashAlg")
            $hashValueStr=(jsonHashValue "$hashValue")
            $componentsUriDetails="$hashAlgStr"",""$hashValueStr"
        }
    echo ("$JSON_COMPONENTSURI_TEMPLATE" -f "$(toCSV("$componentsUri","$componentsUriDetails"))")
    }
}
function jsonPropertiesUri () {
    if ($PROPERTIES_URI) {
        $propertiesUri=(jsonUri "$PROPERTIES_URI")
        $propertiesUriDetails=""
        if ($PROPERTIES_URI_LOCAL_COPY_FOR_HASH) {
            $hashAlg="2.16.840.1.101.3.4.2.1" # SHA256, see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs
            $hashValue=([System.Convert]::ToBase64String($(HexToByteArray $(Get-FileHash "$PROPERTIES_URI_LOCAL_COPY_FOR_HASH"  -Algorithm SHA256).Hash.Trim())))
            $hashAlgStr=(jsonHashAlg "$hashAlg")
            $hashValueStr=(jsonHashValue "$hashValue")
            $propertiesUriDetails="$hashAlgStr,$hashValueStr"
        }
        echo ("$JSON_PROPERTIESURI_TEMPLATE" -f "$(toCSV("$propertiesUri","$propertiesUriDetails"))")
    }
}
function jsonIntermediateFile () {
    echo ("$JSON_INTERMEDIATE_FILE_OBJECT" -f "$(toCSV($args))")
}
