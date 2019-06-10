param(
    [parameter(Mandatory=$true)]
    [ValidateNotNull()]
    [string]$filename
)

### User customizable values
$APP_HOME=(Split-Path -parent $PSCommandPath)
$PROPERTIES_URI="" # Specify the optional properties URI field
$PROPERTIES_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
$ENTERPRISE_NUMBERS_FILE="$APP_HOME/../enterprise-numbers"
$PEN_ROOT="1.3.6.1.4.1." # OID root for the private enterprise numbers


### ComponentClass values
$COMPCLASS_REGISTRY_TCG="2.23.133.18.3.1"
$COMPCLASS_BASEBOARD="00030003" # these values are meant to be an example.  check the component class registry.
$COMPCLASS_BIOS="00130003"
$COMPCLASS_UEFI="00130002"
$COMPCLASS_CHASSIS="00020001" # TODO:  chassis type is included in SMBIOS
$COMPCLASS_CPU="00010002"
$COMPCLASS_HDD="00070002"
$COMPCLASS_NIC="00090002"
$COMPCLASS_RAM="00060001"  # TODO: memory type is included in SMBIOS

# Progress Groups
#     1: Overall progress
#     2: Component type
#     3: Function progress per component 
Write-Progress -Id 1 -Activity "Setting up to gather component details" -PercentComplete 0

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
$JSON_NAME="NAME"
$JSON_VALUE="VALUE"
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
    $tmpManufacturerId=(queryForPen "$($args[0])")
    if (($tmpManufacturerId) -and ("$tmpManufacturerId" -ne "$PEN_ROOT")) {
        $tmpManufacturerId=(jsonManufacturerId "$tmpManufacturerId")
        $manufacturer="$manufacturer,$tmpManufacturerId"
    }
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
    $tmpManufacturerId=(queryForPen "$($args[0])")
    if (($tmpManufacturerId) -and ("$tmpManufacturerId" -ne "$PEN_ROOT")) {
        $tmpManufacturerId=(jsonPlatformManufacturerId "$tmpManufacturerId")
        $manufacturer="$manufacturer,$tmpManufacturerId"
    }
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

Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 10

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Querying WMIC" -PercentComplete 0
### Gather platform details for the subject alternative name
$platformManufacturer=((wmic computersystem get manufacturer /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$platformModel=((wmic computersystem get model /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$platformVersion=((wmic csproduct get version /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$platformSerial=((wmic csproduct get identifyingnumber /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Cleaning output" -PercentComplete 40
if (!$platformManufacturer) {
    $platformManufacturer="$NOT_SPECIFIED"
}
$platformManufacturer=(jsonPlatformManufacturerStr "$platformManufacturer")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Cleaning output" -PercentComplete 55
if (!$platformModel) {
    $platformModel="$NOT_SPECIFIED"
}
$platformModel=(jsonPlatformModel "$platformModel")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Cleaning output" -PercentComplete 70
if (!$platformVersion) {
    $platformVersion="$NOT_SPECIFIED"
}
$platformVersion=(jsonPlatformVersion "$platformVersion")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Cleaning output" -PercentComplete 85
if ($platformSerial) {
    $platformSerial=(jsonPlatformSerial "$platformSerial")
}
$platform=(jsonPlatformObject "$platformManufacturer" "$platformModel" "$platformVersion" "$platformSerial")
Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Cleaning output" -PercentComplete 100

### Gather component details
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 20

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Querying WMIC" -PercentComplete 0
$chassisClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_CHASSIS")
$chassisManufacturer=((wmic systemenclosure get manufacturer /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$chassisModel=((wmic systemenclosure get chassistypes /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$chassisSerial=((wmic systemenclosure get serialnumber /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$chassisRevision=((wmic systemenclosure get version /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Cleaning output" -PercentComplete 40
if (!$chassisManufacturer) {
    $chassisManufacturer="$NOT_SPECIFIED"
}
$chassisManufacturer=(jsonManufacturer "$chassisManufacturer")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Cleaning output" -PercentComplete 55
if (!$chassisModel) {
    $chassisModel="$NOT_SPECIFIED"
} 
$chassisModel=(jsonModel "$chassisModel")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Cleaning output" -PercentComplete 70
if ($chassisSerial) {
    $chassisSerial=(jsonSerial "$chassisSerial")
}

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Cleaning output" -PercentComplete 85
if ($chassisRevision) {
    $chassisRevision=(jsonRevision "$chassisRevision")
}
$componentChassis=(jsonComponent "$chassisClass" "$chassisManufacturer" "$chassisModel" "$chassisSerial" "$chassisRevision")
Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Cleaning output" -PercentComplete 100

### Gather baseboard details
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 30

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Querying WMIC" -PercentComplete 0
$baseboardClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_BASEBOARD")
$baseboardManufacturer=((wmic baseboard get manufacturer /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$baseboardModel=((wmic baseboard get product /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$baseboardSerial=((wmic baseboard get serialnumber /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$baseboardRevision=((wmic baseboard get version /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$baseboardHotSwappable=((wmic baseboard get hotswappable /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$baseboardRemovable=((wmic baseboard get removable /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$baseboardReplaceable=((wmic baseboard get replaceable /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 40
$baseboardFieldReplaceableAnswer="false"
if (("$baseboardHotSwappable" -eq "TRUE") -or ("$baseboardRemovable" -eq "TRUE") -or ("$baseboardReplaceable" -eq "TRUE")) {
    $baseboardFieldReplaceableAnswer="true"
}
$baseboardFieldReplaceable=(jsonFieldReplaceable "$baseboardFieldReplaceableAnswer")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 52
if (!$baseboardManufacturer) {
    $baseboardManufacturer="$NOT_SPECIFIED"
}
$baseboardManufacturer=(jsonManufacturer "$baseboardManufacturer")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 64
if (!$baseboardModel) {
    $baseboardModel="$NOT_SPECIFIED"
}
$baseboardModel=(jsonModel "$baseboardModel")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 76
if ($baseboardSerial) {
    $baseboardSerial=(jsonSerial "$baseboardSerial")
}

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 88
if ("$baseboardRevision") {
    $baseboardRevision=(jsonRevision "$baseboardRevision")
}
$componentBaseboard=(jsonComponent "$baseboardClass" "$baseboardManufacturer" "$baseboardModel" "$baseboardFieldReplaceable" "$baseboardSerial" "$baseboardRevision")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 100

### Gather BIOS details
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 30

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Querying WMIC" -PercentComplete 0
$biosClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_BIOS")
$biosManufacturer=((wmic bios get manufacturer /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$biosModel=((wmic bios get name /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$biosSerial=((wmic bios get serialnumber /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$biosRevision=((wmic bios get version /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Cleaning output" -PercentComplete 40
if (!$biosManufacturer) {
    $biosManufacturer="$NOT_SPECIFIED"
}
$biosManufacturer=(jsonManufacturer "$biosManufacturer")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Cleaning output" -PercentComplete 55
if (!$biosModel) {
    $biosModel="$NOT_SPECIFIED"
}
$biosModel=(jsonModel "$biosModel")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Cleaning output" -PercentComplete 70
if ($biosSerial) {
    $biosSerial=(jsonSerial "$biosSerial")
}

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Cleaning output" -PercentComplete 85
if ($biosRevision) {
    $biosRevision=(jsonRevision "$biosRevision")
}
$componentBios=(jsonComponent "$biosClass" "$biosManufacturer" "$biosModel" "$biosSerial" "$biosRevision")

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 100

### Gather CPU details
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 40

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering CPU information" -CurrentOperation "Querying WMIC" -PercentComplete 0
function parseCpuData() {
    $RS=(wmic cpu get manufacturer,family,serialnumber,revision,upgrademethod /FORMAT:CSV | ConvertFrom-Csv)
    $component=""
    $numRows=1
    if ($RS.Count -gt 1) {
        $numRows=($RS.Count)
    }
    
    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering CPU information" -CurrentOperation ("Cleaning output for CPU " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

		$cpuClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_CPU")
        $tmpManufacturer=$RS[$i].Manufacturer
        if (!$tmpManufacturer) {
            $tmpManufacturer=$NOT_SPECIFIED
        }
        $tmpManufacturer=(jsonManufacturer $tmpManufacturer)
        $tmpModel=$RS[$i].Family
        if (!$tmpModel) {
            $tmpModel=$NOT_SPECIFIED
        }
        $tmpModel=(jsonModel $tmpModel)

        $tmpSerial=($RS[$i].Serialnumber)
        if (![string]::IsNullOrEmpty($tmpSerial)) {
            $tmpSerial=(jsonSerial $tmpSerial)
        }
        $tmpRevision=($RS[$i].Revision)
        if (![string]::IsNullOrEmpty($tmpRevision)) {
            $tmpRevision=(jsonRevision $RS[$i].Revision)   
        }
        $tmpUpgradeMethod=($RS[$i].UpgradeMethod)
        if (![string]::IsNullOrEmpty($tmpUpgradeMethod) -and ($tmpUpgradeMethod -eq "6")) {
            $replaceable=(jsonFieldReplaceable "false")
        } else {
            $replaceable=(jsonFieldReplaceable "true")
        }
        $tmpComponent=(jsonComponent $cpuClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $tmpRevision)
        $component+="$tmpComponent,"
    }
    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering CPU information" -CurrentOperation "Done" -PercentComplete 100
    return "$component".Trim(",")
}

### Gather RAM details
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 50

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering RAM information" -CurrentOperation "Querying CIM" -PercentComplete 0
function parseRamData() {
    $RS=(Get-CimInstance -ClassName CIM_PhysicalMemory | select Manufacturer,PartNumber,SerialNumber,Version)
    $component=""
    $replaceable=(jsonFieldReplaceable "true") # WMI and CIM did not populate Removable nor Replaceable flags on my machine
    $numRows=1
    if ($RS.Count -gt 1) {
        $numRows=($RS.Count)
    }
    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering RAM information" -CurrentOperation ("Cleaning output for Memory Chip " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

		$ramClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_RAM")
        $tmpManufacturer=$RS[$i].Manufacturer
        if (!$tmpManufacturer) {
            $tmpManufacturer=$NOT_SPECIFIED
        }
        $tmpManufacturer=(jsonManufacturer $tmpManufacturer)
        $tmpModel=$RS[$i].PartNumber
        if (!$tmpModel) {
            $tmpModel=$NOT_SPECIFIED
        }
        $tmpModel=(jsonModel $tmpModel)

        $tmpSerial=($RS[$i].Serialnumber)
        if (![string]::IsNullOrEmpty($tmpSerial)) {
            $tmpSerial=(jsonSerial $tmpSerial)
        }
        $tmpRevision=($RS[$i].Version)
        if (![string]::IsNullOrEmpty($tmpRevision)) {
            $tmpRevision=(jsonRevision $tmpRevision)   
        }
        $tmpComponent=(jsonComponent $ramClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $tmpRevision)
        $component+="$tmpComponent,"
    }

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering RAM information" -CurrentOperation "Done" -PercentComplete 100
    return "$component".Trim(",")
}

### Gather NIC details
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 60

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NIC information" -CurrentOperation "Querying CIM" -PercentComplete 0
function parseNicData() {
    #$RS=(Get-CimInstance -ClassName CIM_NetworkAdapter | select Manufacturer,ProductName,MacAddress,PhysicalAdapter,PNPDeviceID | where {($_.PhysicalAdapter -eq "True") -and ($_.PNPDeviceID -match "^(PCI|BTH)\.*")})
    $RS=(Get-NetAdapter | select DriverProvider,HardwareInterface,MacAddress,DriverDescription,PhysicalMediaType,PNPDeviceID | where {($_.PhysicalMediaType -eq "BlueTooth" -or "Native 802.11" -or "802.3") -and ($_.PNPDeviceID -Match "^(BTH|PCI)\\.*$")})
    $component=""
    $replaceable=(jsonFieldReplaceable "true")
    $numRows=1
    if ($RS.Count -gt 1) {
        $numRows=($RS.Count)
    }
    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NIC information" -CurrentOperation ("Cleaning output for NIC " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)
        
		$nicClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_NIC")
        $tmpManufacturer=$RS[$i].DriverProvider
        if (!$tmpManufacturer) {
            $tmpManufacturer=$NOT_SPECIFIED
        }
        $tmpManufacturer=(jsonManufacturer $tmpManufacturer)
        $tmpModel=$RS[$i].DriverDescription
        if (!$tmpModel) {
            $tmpModel=$NOT_SPECIFIED
        }
        $tmpModel=(jsonModel $tmpModel)

        $tmpSerialConstant=($RS[$i].MacAddress)
        $tmpSerial=""
        if (![string]::IsNullOrEmpty($tmpSerialConstant)) {
            $tmpSerial=(jsonSerial $tmpSerialConstant)
        }

        $tmpMediaType=$RS[$i].PhysicalMediaType
        $thisAddress=""
        if ($tmpMediaType -and $tmpSerial) {
            if ("$tmpMediaType" -match "^.*802[.]11.*$") {
                $thisAddress=(jsonWlanMac $tmpSerialConstant)
            }
            elseif ("$tmpMediaType" -match "^.*[Bb]lue[Tt]ooth.*$") {
                $thisAddress=(jsonBluetoothMac $tmpSerialConstant)
            }
            elseif ("$tmpMediaType" -match "^.*802[.]3.*$") {
                $thisAddress=(jsonEthernetMac $tmpSerialConstant) 
            }
            if ($thisAddress) {
                $thisAddress=(jsonAddress "$thisAddress")
            }
        }

        $tmpComponent=(jsonComponent $nicClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $thisAddress)
        $component+="$tmpComponent,"
    }

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NIC information" -CurrentOperation "Done" -PercentComplete 100
    return "$component".Trim(",")
}

### Gather HDD details
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 70

Write-Progress -Id 2 -ParentId 1 -Activity "Gathering HDD information" -CurrentOperation "Querying CIM" -PercentComplete 0
function parseHddData() {
    $RS=(Get-CimInstance -ClassName CIM_DiskDrive | select manufacturer,model,serialnumber,firmwarerevision,mediatype | where mediatype -eq "Fixed hard disk media")
    $component=""
    $replaceable=(jsonFieldReplaceable "true")
    $numRows=1
    if ($RS.Count -gt 1) {
        $numRows=($RS.Count)
    }
    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Hard Disk information" -CurrentOperation ("Cleaning output for HDD " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

        $hddClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_HDD")
        $tmpManufacturer=$RS[$i].Manufacturer
        if (!$tmpManufacturer) {
            $tmpManufacturer=$NOT_SPECIFIED
        }
        $tmpManufacturer=(jsonManufacturer $tmpManufacturer)
        $tmpModel=$RS[$i].Model
        if (!$tmpModel) {
            $tmpModel=$NOT_SPECIFIED
        }
        $tmpModel=(jsonModel $tmpModel)

        $tmpSerial=($RS[$i].Serialnumber)
        if (![string]::IsNullOrEmpty($tmpSerial)) {
            $tmpSerial=(jsonSerial $tmpSerial)
        }
        $tmpRevision=($RS[$i].FirmwareRevision)
        if (![string]::IsNullOrEmpty($tmpRevision)) {
            $tmpRevision=(jsonRevision $tmpRevision)   
        }
        $tmpComponent=(jsonComponent $hddClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $tmpRevision)
        $component+="$tmpComponent,"
    }

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Hard Disk information" -CurrentOperation "Done" -PercentComplete 100
    return "$component".Trim(",")
}

### Collate the component details
$componentsCPU=$(parseCpuData)
$componentsRAM=$(parseRamData)
$componentsNIC=$(parseNicData)
$componentsHDD=$(parseHddData)
$componentArray=(jsonComponentArray "$componentChassis" "$componentBaseboard" "$componentBios" "$componentsCPU" "$componentsRAM" "$componentsNIC" "$componentsHDD")

### Gather property details
Write-Progress -Id 1 -Activity "Gathering properties" -PercentComplete 80
$osCaption=((wmic os get caption /value | Select-String -Pattern "^.*=(.*)$").Matches.Groups[1].ToString().Trim())
$property1=(jsonProperty "caption" "$osCaption")  ## Example1
$property2= ## Example2

### Collate the property details
$propertyArray=(jsonPropertyArray "$property1")

### Collate the URI details, if parameters above are blank, the fields will be excluded from the final JSON structure
$componentsUri=""
if ($COMPONENTS_URI) {
    $componentsUri=(jsonComponentsUri)
}
$propertiesUri=""
if ($PROPERTIES_URI) {
    $propertiesUri=(jsonPropertiesUri)
}

Write-Progress -Id 1 -Activity "Forming final output" -PercentComplete 90
### Construct the final JSON object
$FINAL_JSON_OBJECT=(jsonIntermediateFile "$platform" "$componentArray" "$componentsUri" "$propertyArray" "$propertiesUri")

Write-Progress -Id 1 -Activity "Done" -PercentComplete 100
[IO.File]::WriteAllText($filename, "$FINAL_JSON_OBJECT")

