param(
    [parameter(Mandatory=$true)]
    [ValidateNotNull()]
    [string]$filename
)

### User customizable values
$APP_HOME=(Split-Path -parent $PSCommandPath)
$PROPERTIES_URI="" # Specify the optional properties URI field
$PROPERTIES_URI_LOCAL_COPY_FOR_HASH="" # If empty, the optional hashAlgorithm and hashValue fields will not be included for the URI
$JSON_SCRIPT="$APP_HOME/json.ps1" # Defines JSON structure and provides methods for producing relevant JSON
$SMBIOS_SCRIPT="$APP_HOME/SMBios.ps1" # Handles parsing of SMBIOS data
$HW_SCRIPT="$APP_HOME/hw.ps1" # Handles parsing of WMI and CIM

### JSON
. $JSON_SCRIPT

### Load Raw SMBios Data
. $SMBIOS_SCRIPT # See the TCG SMBIOS Component Class Registry specification.
$smbios=(Get-SMBiosStructures)
$COMPCLASS_REGISTRY_SMBIOS="2.23.133.18.3.3" # See the TCG OID Registry.

### hw
. $HW_SCRIPT

### ComponentClass values
$COMPCLASS_REGISTRY_TCG="2.23.133.18.3.1" 
$COMPCLASS_BASEBOARD="00030003" # these values are meant to be an example.  check the TCG component class registry.
$COMPCLASS_BIOS="00130003"
$COMPCLASS_UEFI="00130002"
$COMPCLASS_CHASSIS="00020001"
$COMPCLASS_CPU="00010002"
$COMPCLASS_HDD="00070002"
$COMPCLASS_NIC="00090002"
$COMPCLASS_RAM="00060001" 
$COMPCLASS_GFX="00050002"

# Progress Group IDs:
#     1: Overall progress
#     2: Component type
Write-Progress -Id 1 -Activity "Setting up to gather component details" -PercentComplete 0

### Gather platform details for the subject alternative name
### Platform attributes in the SAN only need to be consistent between base and delta platform certificates
function gatherSmbiosSystemForSubjectAlternativeName() {
    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Querying" -PercentComplete 0
    ### Gather platform details for the subject alternative name
    $platformManufacturer=(Get-SMBiosString $smbios "$SMBIOS_TYPE_SYSTEM" 0x4)
    $platformModel=(Get-SMBiosString $smbios "$SMBIOS_TYPE_SYSTEM" 0x5)
    $platformVersion=(Get-SMBiosString $smbios "$SMBIOS_TYPE_SYSTEM" 0x6)
    $platformSerial=(Get-SMBiosString $smbios "$SMBIOS_TYPE_SYSTEM" 0x7)

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Cleaning output" -PercentComplete 40
    if ([string]::IsNullOrEmpty($platformManufacturer) -or ($platformManufacturer.Trim().Length -eq 0)) {
        $platformManufacturer="$NOT_SPECIFIED"
    }
    $platformManufacturer=$(jsonPlatformManufacturerStr "$platformManufacturer".Trim())

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Cleaning output" -PercentComplete 55
    if ([string]::IsNullOrEmpty($platformModel) -or ($platformModel.Trim().Length -eq 0)) {
        $platformModel="$NOT_SPECIFIED"
    }
    $platformModel=$(jsonPlatformModel "$platformModel".Trim())

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Cleaning output" -PercentComplete 70
    if ([string]::IsNullOrEmpty($platformVersion) -or ($platformVersion.Trim().Length -eq 0)) {
        $platformVersion="$NOT_SPECIFIED"
    }
    $platformVersion=(jsonPlatformVersion "$platformVersion".Trim())

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Cleaning output" -PercentComplete 85
    if (![string]::IsNullOrEmpty($platformSerial) -and ($platformSerial.Trim().Length -ne 0)) {
        $platformSerial=(jsonPlatformSerial "$platformSerial".Trim())
    }
    $platform=(jsonPlatformObject "$platformManufacturer" "$platformModel" "$platformVersion" "$platformSerial")
    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Done" -PercentComplete 100
    return $platform.Trim(",")
}

### Gather data from SMBIOS
function gatherSmbiosData() {
    $components=""
    $types=@($SMBIOS_TYPE_BASEBOARD,$SMBIOS_TYPE_BIOS,$SMBIOS_TYPE_CHASSIS,$SMBIOS_TYPE_PROCESSOR,$SMBIOS_TYPE_RAM,$SMBIOS_TYPE_SYSTEM,$SMBIOS_TYPE_POWERSUPPLY,$SMBIOS_TYPE_TPM)

    foreach ($type in $types) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering information about SMBIOS type $type" -CurrentOperation "Gathering" -PercentComplete 0
        $RS=$smbios[$type]
        $numRows=$RS.Count

        for($i=0;$i -lt $numRows;$i++) {
            Write-Progress -Id 2 -ParentId 1 -Activity "Gathering information about SMBIOS type $type" -CurrentOperation "Working on element ($i+1) of $numRows" -PercentComplete ((($i+1)/$numRows)*100)
            $component=""
            $struct=$RS[$i]

            $componentClassValue=(GetComponentClassValue $struct)
            $manufacturer=(GetManufacturer $struct)
            $model=(GetModel $struct)
            $serialNumber=(GetSerialNumber $struct)
            $revision=(GetRevision $struct)
            $fieldReplaceable=(GetFieldReplaceable $struct)

            # Do not include empty slots
            if ([string]::IsNullOrEmpty($manufacturer) -and [string]::IsNullOrEmpty($model) -and [string]::IsNullOrEmpty($serialNumber) -and [string]::IsNullOrEmpty($revision)) {
                continue
            }

            $componentClass=(jsonComponentClass "$COMPCLASS_REGISTRY_SMBIOS" "$componentClassValue")
            $manufacturer=(jsonManufacturer "$manufacturer")
            $model=(jsonModel "$model")
            if (![string]::IsNullOrEmpty($serialNumber)) {
                $serialNumber=(jsonSerial "$serialNumber")
            }
            if (![string]::IsNullOrEmpty($revision)) {
                $revision=(jsonRevision "$revision")
            }
            if (![string]::IsNullOrEmpty($fieldReplaceable)) {
                $fieldReplaceable=(jsonFieldReplaceable "$fieldReplaceable")
            }
            $component=(jsonComponent "$componentClass" "$manufacturer" "$model" "$serialNumber" "$revision" "$fieldReplaceable")
            $components+="$component,"
        } # for numRows (for each handle)
    } # foreach type
    Write-Progress -Id 2 -ParentId 1 -Activity "Complete" -CurrentOperation "Complete" -PercentComplete 100
    return "$components".Trim(",")
}

### Gather NIC details
function parseNicData() {
    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NIC information" -CurrentOperation "Querying CIM" -PercentComplete 0
    $RS=@(Get-NetAdapter | select MacAddress,PhysicalMediaType,PNPDeviceID | where {($_.PhysicalMediaType -eq "Native 802.11" -or "802.3") -and ($_.PNPDeviceID -Match "^(PCI)\\.*$")})
    $component=""
    $replaceable=(jsonFieldReplaceable "true")
    $numRows=$RS.Count

    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NIC information" -CurrentOperation ("Cleaning output for NIC " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

        $nicClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_NIC")

        $pnpDevID=""
        if(isPCI($RS[$i].PNPDeviceID)) {
            $pnpDevID=(pciParse $RS[$i].PNPDeviceID)
        } else {
            Continue
        }

        $tmpManufacturer=$pnpDevID.vendor # PCI Vendor ID
        $tmpModel=$pnpDevID.product  # PCI Device Hardware ID
        $tmpSerialConstant=($RS[$i].MacAddress)
        $tmpSerialConstant=(standardizeMACAddr $tmpSerialConstant)
        $tmpSerial=""
        $tmpRevision=$pnpDevID.revision
        $tmpMediaType=$RS[$i].PhysicalMediaType
        $thisAddress=""

        if ([string]::IsNullOrEmpty($tmpManufacturer) -or ($tmpManufacturer.Trim().Length -eq 0)) {
            $tmpManufacturer="$NOT_SPECIFIED"
        }
        $tmpManufacturer=$(jsonManufacturer "$tmpManufacturer".Trim())


        if ([string]::IsNullOrEmpty($tmpModel) -or ($tmpModel.Trim().Length -eq 0)) {
            $tmpModel="$NOT_SPECIFIED"
        }
        $tmpModel=$(jsonModel "$tmpModel".Trim())



        if (![string]::IsNullOrEmpty($tmpSerialConstant) -and ($tmpSerialConstant.Trim().Length -ne 0)) {
            $tmpSerial=(jsonSerial "$tmpSerialConstant".Trim())
        } else {
            $tmpSerial=""
        }


        if (![string]::IsNullOrEmpty($tmpRevision) -and ($tmpRevision.Trim().Length -ne 0)) {
            $tmpRevision=(jsonRevision "$tmpRevision".Trim())
        } else {
            $tmpRevision=""
        }

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

        $tmpComponent=(jsonComponent $nicClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $tmpRevision $thisAddress)
        $component+="$tmpComponent,"
    }

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NIC information" -CurrentOperation "Done" -PercentComplete 100
    return "$component".Trim(",")
}

### Gather HDD details
function parseHddData() {
    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering HDD information" -CurrentOperation "Querying" -PercentComplete 0
    $RS=(Get-CimInstance -ClassName CIM_DiskDrive | select serialnumber,mediatype,pnpdeviceid,manufacturer,model | where mediatype -eq "Fixed hard disk media")
    $component=""
    $replaceable=(jsonFieldReplaceable "true")
    $numRows=1
    if ($RS.Count -gt 1) {
        $numRows=($RS.Count)
    }
    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Hard Disk information" -CurrentOperation ("Cleaning output for HDD " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

        $hddClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_HDD")

        $pnpDevID=""
        if(isIDE($RS[$i].PNPDeviceID)) {
            $pnpDevID=(ideDiskParse $RS[$i].PNPDeviceID)
        } elseif(isSCSI($RS[$i].PNPDeviceID)) {
            $pnpDevID=(scsiDiskParse $RS[$i].PNPDeviceID)
        } else {
            Continue
        }

        if(($pnpDevID -eq $null) -or (($pnpDevID -eq "(Standard disk drives)") -and ($pnpDevID.product -eq $null))) {
            $regex="^.{,16}$"
            $pnpDevID=[pscustomobject]@{
                product=($RS[$i].model -replace '^(.{0,16}).*$','$1')  # Strange behavior for this case, will return
            }
        }

        $tmpManufacturer=$pnpDevID.vendor # PCI Vendor ID
        $tmpModel=$pnpDevID.product  # PCI Device Hardware ID
        $tmpSerial=$RS[$i].serialnumber
        $tmpRevision=$pnpDevID.revision

        if ([string]::IsNullOrEmpty($tmpManufacturer) -or ($tmpManufacturer.Trim().Length -eq 0)) {
            $tmpManufacturer="$NOT_SPECIFIED"
        }
        $tmpManufacturer=$(jsonManufacturer "$tmpManufacturer".Trim())

        if ([string]::IsNullOrEmpty($tmpModel) -or ($tmpModel.Trim().Length -eq 0)) {
            $tmpModel="$NOT_SPECIFIED"
        }
        $tmpModel=$(jsonModel "$tmpModel".Trim())

        if (![string]::IsNullOrEmpty($tmpSerial) -and ($tmpSerial.Trim().Length -ne 0)) {
            $tmpSerial=(jsonSerial "$tmpSerial".Trim())
        } else {
            $tmpSerial=""
        }

        if (![string]::IsNullOrEmpty($tmpRevision) -and ($tmpRevision.Trim().Length -ne 0)) {
            $tmpRevision=(jsonRevision "$tmpRevision".Trim())
        } else {
            $tmpRevision=""
        }

        $tmpComponent=(jsonComponent $hddClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $tmpRevision)
        $component+="$tmpComponent,"
    }

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Hard Disk information" -CurrentOperation "Done" -PercentComplete 100
    return "$component".Trim(",")
}

### Gather GFX details
function parseGfxData() {
    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering GFX information" -CurrentOperation "Querying" -PercentComplete 0
    $RS=(Get-CimInstance -ClassName CIM_VideoController | select pnpdeviceid )
    $component=""
    $replaceable=(jsonFieldReplaceable "true")
    $numRows=1
    if ($RS.Count -gt 1) {
        $numRows=($RS.Count)
    }
    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Graphics information" -CurrentOperation ("Cleaning output for HDD " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

        $gfxClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_GFX")

        $pnpDevID=""
        if(isPCI($RS[$i].PNPDeviceID)) {
            $pnpDevID=(pciParse $RS[$i].PNPDeviceID)
        } else {
            Continue
        }

        $tmpManufacturer=$pnpDevID.vendor # PCI Vendor ID
        $tmpModel=$pnpDevID.product  # PCI Device Hardware ID
        $tmpRevision=$pnpDevID.revision
        # CIM Class does not contain serialnumber

        if ([string]::IsNullOrEmpty($tmpManufacturer) -or ($tmpManufacturer.Trim().Length -eq 0)) {
            $tmpManufacturer="$NOT_SPECIFIED"
        }
        $tmpManufacturer=$(jsonManufacturer "$tmpManufacturer".Trim())

        if ([string]::IsNullOrEmpty($tmpModel) -or ($tmpModel.Trim().Length -eq 0)) {
            $tmpModel="$NOT_SPECIFIED"
        }
        $tmpModel=$(jsonModel "$tmpModel".Trim())

        if (![string]::IsNullOrEmpty($tmpRevision) -and ($tmpRevision.Trim().Length -ne 0)) {
            $tmpRevision=(jsonRevision "$tmpRevision".Trim())
        } else {
            $tmpRevision=""
        }

        $tmpComponent=(jsonComponent $gfxClass $tmpManufacturer $tmpModel $replaceable $tmpRevision)
        $component+="$tmpComponent,"
    }

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Graphics information" -CurrentOperation "Done" -PercentComplete 100
    return "$component".Trim(",")
}

### Collate the component details
Write-Progress -Id 1 -Activity "Gathering platform details" -PercentComplete 10
$platform=(gatherSmbiosSystemForSubjectAlternativeName)
Write-Progress -Id 1 -Activity "Gathering data from SMBIOS" -PercentComplete 20
$componentsSMBIOS=(gatherSmbiosData)
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 40
$componentsNIC=(parseNicData)
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 60
$componentsHDD=(parseHddData)
Write-Progress -Id 1 -Activity "Gathering component details" -PercentComplete 80
$componentsGFX=(parseGfxData)
$componentArray=(jsonComponentArray "$componentsSMBIOS" "$componentsNIC" "$componentsHDD" "$componentsGFX")

### Gather property details
Write-Progress -Id 1 -Activity "Gathering properties" -PercentComplete 90
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

Write-Progress -Id 1 -Activity "Forming final output" -PercentComplete 95
### Construct the final JSON object
$FINAL_JSON_OBJECT=(jsonIntermediateFile "$platform" "$componentArray" "$componentsUri" "$propertyArray" "$propertiesUri")

Write-Progress -Id 1 -Activity "Done" -PercentComplete 100
[IO.File]::WriteAllText($filename, "$FINAL_JSON_OBJECT")

