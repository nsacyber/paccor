### User customizable values
$APP_HOME=(Split-Path -parent $PSCommandPath)
$ENTERPRISE_NUMBERS_FILE="$APP_HOME/../enterprise-numbers"
$PEN_ROOT="1.3.6.1.4.1." # OID root for the private enterprise numbers

### JSON Structure Keywords
$JSON_COMPONENTS="COMPONENTS"
$JSON_COMPONENTSURI="COMPONENTSURI"
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
    Write-Output ("$JSON_COMPONENTCLASS_TEMPLATE" -f "$($args[0])","$($args[1])")
}
function jsonManufacturer () {
    $manufacturer=("`"$JSON_MANUFACTURER`": `"{0}`"" -f "$($args[0])")
    #$tmpManufacturerId=(queryForPen "$($args[0])")
    #if (($tmpManufacturerId) -and ("$tmpManufacturerId" -ne "$PEN_ROOT")) {
    #    $tmpManufacturerId=(jsonManufacturerId "$tmpManufacturerId")
    #    $manufacturer="$manufacturer,$tmpManufacturerId"
    #}
    Write-Output "$manufacturer"
}
function jsonModel () {
    Write-Output ("`"$JSON_MODEL`": `"{0}`"" -f "$($args[0])")
}
function jsonSerial () {
    Write-Output ("`"$JSON_SERIAL`": `"{0}`"" -f "$($args[0])")
}
function jsonRevision () {
    Write-Output ("`"$JSON_REVISION`": `"{0}`"" -f "$($args[0])")
}
function jsonManufacturerId () {
    Write-Output ("`"$JSON_MANUFACTURERID`": `"{0}`"" -f "$($args[0])")
}
function jsonFieldReplaceable () {
    Write-Output ("`"$JSON_FIELDREPLACEABLE`": `"{0}`"" -f "$($args[0])")
}
function jsonEthernetMac () {
    Write-Output ("$JSON_ETHERNETMAC_TEMPLATE" -f "$($args[0])")
}
function jsonWlanMac () {
    Write-Output ("$JSON_WLANMAC_TEMPLATE" -f "$($args[0])")
}
function jsonBluetoothMac () {
    Write-Output ("$JSON_BLUETOOTHMAC_TEMPLATE" -f "$($args[0])")
}
function jsonPlatformModel () {
    Write-Output ("`"$JSON_PLATFORMMODEL`": `"{0}`"" -f "$($args[0])")
}
function jsonPlatformManufacturerStr () {
    $manufacturer=("`"$JSON_PLATFORMMANUFACTURERSTR`": `"{0}`"" -f "$($args[0])")
    #$tmpManufacturerId=(queryForPen "$($args[0])")
    #if (($tmpManufacturerId) -and ("$tmpManufacturerId" -ne "$PEN_ROOT")) {
    #    $tmpManufacturerId=(jsonPlatformManufacturerId "$tmpManufacturerId")
    #    $manufacturer="$manufacturer,$tmpManufacturerId"
    #}
    Write-Output "$manufacturer"
}
function jsonPlatformVersion () {
    Write-Output ("`"$JSON_PLATFORMVERSION`": `"{0}`"" -f "$($args[0])")
}
function jsonPlatformSerial () {
    Write-Output ("`"$JSON_PLATFORMSERIAL`": `"{0}`"" -f "$($args[0])")
}
function jsonPlatformManufacturerId () {
    Write-Output ("`"$JSON_PLATFORMMANUFACTURERID`": `"{0}`"" -f "$($args[0])")
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
    Write-Output $result
}
function jsonProperty () {
    if ($args.Length -eq 2) {
        Write-Output ("$JSON_PROPERTY_TEMPLATE" -f "$($args[0])","$($args[1])")
    } elseif ($args.Length -eq 3) {
        Write-Output ("$JSON_PROPERTY_TEMPLATE_OPT" -f "$($args[0])","$($args[1])","$($args[2])")
    }
}
function jsonUri () {
    Write-Output ("`"$JSON_URI`": `"{0}`"" -f "$($args[0])")
}
function jsonHashAlg () {
    Write-Output ("`"$JSON_HASHALG`": `"{0}`"" -f "$($args[0])")
}
function jsonHashValue () {
    Write-Output ("`"$JSON_HASHVALUE`": `"{0}`"" -f "$($args[0])")
}
function toCSV () {
    Write-Output ((($args | Where-Object { $_ -and $_.Trim() -ne "" } | ForEach-Object { $_.ToString() }) -join ",") -replace "}\s*,", "},")
}
function jsonAddress () {
    Write-Output ("$JSON_ADDRESSES_TEMPLATE" -f "$(toCSV @args)")
}
function jsonComponent () {
    Write-Output ("$JSON_COMPONENT_TEMPLATE" -f "$(toCSV @args)")
}
function jsonComponentArray () {
    Write-Output ("$JSON_COMPONENT_ARRAY_TEMPLATE" -f "$(toCSV @args)")
}
function jsonPropertyArray () {
    Write-Output ("$JSON_PROPERTY_ARRAY_TEMPLATE" -f "$(toCSV @args)")
}
function jsonPlatformObject () {
    Write-Output ("$JSON_PLATFORM_TEMPLATE" -f "$(toCSV @args)")
}
function jsonComponentsUri ([string]$COMPONENTS_URI="", [string]$COMPONENTS_URI_LOCAL_COPY_FOR_HASH="") {
    if (![string]::IsNullOrEmpty($COMPONENTS_URI) -and ($COMPONENTS_URI.Trim().Length -ne 0)) {
        $componentsUri=$(jsonUri "$COMPONENTS_URI")
        $componentsUriDetails=""
        if (![string]::IsNullOrEmpty($COMPONENTS_URI_LOCAL_COPY_FOR_HASH) -and ($COMPONENTS_URI_LOCAL_COPY_FOR_HASH.Trim().Length -ne 0) -and (Test-Path -Path $COMPONENTS_URI_LOCAL_COPY_FOR_HASH)) {
            $hashAlg="2.16.840.1.101.3.4.2.1" # SHA256, see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs
            $hashValue=([System.Convert]::ToBase64String($(HexToByteArray $(Get-FileHash "$COMPONENTS_URI_LOCAL_COPY_FOR_HASH"  -Algorithm SHA256).Hash.Trim())))
            $hashAlgStr=$(jsonHashAlg "$hashAlg")
            $hashValueStr=$(jsonHashValue "$hashValue")
            $componentsUriDetails="$hashAlgStr,$hashValueStr"
        }
        Write-Output ("$JSON_COMPONENTSURI_TEMPLATE" -f "$(toCSV "$componentsUri" "$componentsUriDetails")")
    }
}
function jsonPropertiesUri ([string]$PROPERTIES_URI="", [string]$PROPERTIES_URI_LOCAL_COPY_FOR_HASH="") {
    if (![string]::IsNullOrEmpty($PROPERTIES_URI) -and ($PROPERTIES_URI.Trim().Length -ne 0)) {
        $propertiesUri=$(jsonUri "$PROPERTIES_URI")
        $propertiesUriDetails=""
        if (![string]::IsNullOrEmpty($PROPERTIES_URI_LOCAL_COPY_FOR_HASH) -and ($PROPERTIES_URI_LOCAL_COPY_FOR_HASH.Trim().Length -ne 0) -and (Test-Path -Path $PROPERTIES_URI_LOCAL_COPY_FOR_HASH)) {
            $hashAlg="2.16.840.1.101.3.4.2.1" # SHA256, see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs
            $hashValue=$([System.Convert]::ToBase64String($(HexToByteArray $(Get-FileHash "$PROPERTIES_URI_LOCAL_COPY_FOR_HASH"  -Algorithm SHA256).Hash.Trim())))
            $hashAlgStr=$(jsonHashAlg "$hashAlg")
            $hashValueStr=$(jsonHashValue "$hashValue")
            $propertiesUriDetails="$hashAlgStr,$hashValueStr"
        }
        Write-Output ("$JSON_PROPERTIESURI_TEMPLATE" -f "$(toCSV "$propertiesUri" "$propertiesUriDetails")")
    }
}
function jsonIntermediateFile () {
    Write-Output ("$JSON_INTERMEDIATE_FILE_OBJECT" -f "$(toCSV @args)")
}
