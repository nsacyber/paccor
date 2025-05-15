param(
    [switch] $componentsonly,
    [string]$printoutfile=""
)

$APP_HOME=(Split-Path -parent $PSCommandPath)
$JSON_SCRIPT="$APP_HOME/json.ps1" # Defines JSON structure and provides methods for producing relevant JSON
$SMBIOS_SCRIPT="$APP_HOME/SMBios.ps1"
$HW_SCRIPT="$APP_HOME/hw.ps1" # For components not covered by SMBIOS
$NVME_SCRIPT="$APP_HOME/nvme.ps1" # For NVMe components

### Load Raw SMBios Data
. $SMBIOS_SCRIPT
$smbios=(Get-SMBiosStructures)
$SMBIOS_TYPE_SYSTEM="1"
$SMBIOS_TYPE_PLATFORM="$SMBIOS_TYPE_SYSTEM"
$SMBIOS_TYPE_CHASSIS="3"
$SMBIOS_TYPE_BIOS="0"
$SMBIOS_TYPE_BASEBOARD="2"
$SMBIOS_TYPE_CPU="4"
$SMBIOS_TYPE_RAM="17"

### hw
. $HW_SCRIPT
### nvme
. $NVME_SCRIPT

### ComponentClass values
$COMPCLASS_REGISTRY_TCG="2.23.133.18.3.1" # Could lookup values within SMBIOS to reveal accurate component classes.
$COMPCLASS_BASEBOARD="00030003" # these values are meant to be an example.  check the TCG component class registry.
$COMPCLASS_BIOS="00130003"
#$COMPCLASS_UEFI="00130002" # available as an example. uncomment to utilize.
$COMPCLASS_CHASSIS="00020001"
$COMPCLASS_CPU="00010002"
$COMPCLASS_HDD="00070002"
$COMPCLASS_NIC="00090002"
$COMPCLASS_RAM="00060001"
$COMPCLASS_GFX="00050002"

### JSON
. $JSON_SCRIPT

## Some of the commands below require admin.
If(!(New-Object Security.Principal.WindowsPrincipal(
        [Security.Principal.WindowsIdentity]::GetCurrent())).IsInRole(
            [Security.Principal.WindowsBuiltInRole]::Administrator)) {
	Write-Output "Please run as admin"
	exit
}

function parseSystemData() {
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Collecting data from SMBIOS" -PercentComplete 0
	### Gather platform details for the subject alternative name
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting system data" -CurrentOperation "Collecting manufacturer" -PercentComplete 0
	$platformManufacturer=(Get-SMBiosString $smbios "$SMBIOS_TYPE_PLATFORM" 0x4)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting system data" -CurrentOperation "Collecting model" -PercentComplete 25
	$platformModel=(Get-SMBiosString $smbios "$SMBIOS_TYPE_PLATFORM" 0x5)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting system data" -CurrentOperation "Collecting version" -PercentComplete 50
	$platformVersion=(Get-SMBiosString $smbios "$SMBIOS_TYPE_PLATFORM" 0x6)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting system data" -CurrentOperation "Collecting serial number" -PercentComplete 75
	$platformSerial=(Get-SMBiosString $smbios "$SMBIOS_TYPE_PLATFORM" 0x7)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting system data" -CurrentOperation "Done" -Completed

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
	$platformVersion=$(jsonPlatformVersion "$platformVersion".Trim())

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Cleaning output" -PercentComplete 85
	if (![string]::IsNullOrEmpty($platformSerial) -and ($platformSerial.Trim().Length -ne 0)) {
		$platformSerial=$(jsonPlatformSerial "$platformSerial".Trim())
	}
	$platform=$(toCSV "$platformManufacturer" "$platformModel" "$platformVersion" "$platformSerial")
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering platform information" -CurrentOperation "Done" -Completed
	return "$platform"
}

function parseChassisData() {
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Collecting data from SMBIOS" -PercentComplete 0
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting chassis data" -CurrentOperation "Collecting manufacturer" -PercentComplete 0
	$chassisManufacturer=$(Get-SMBiosString $smbios "$SMBIOS_TYPE_CHASSIS" 0x4)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting chassis data" -CurrentOperation "Collecting model" -PercentComplete 25
	$chassisModel=[string]($smbios["$SMBIOS_TYPE_CHASSIS"].data[0x5])
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting chassis data" -CurrentOperation "Collecting serial" -PercentComplete 50
	$chassisSerial=$(Get-SMBiosString $smbios "$SMBIOS_TYPE_CHASSIS" 0x7)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting chassis data" -CurrentOperation "Collecting revision" -PercentComplete 75
	$chassisRevision=$(Get-SMBiosString $smbios "$SMBIOS_TYPE_CHASSIS" 0x6)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting chassis data" -CurrentOperation "Done" -Completed

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Cleaning output" -PercentComplete 30
	$chassisClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_CHASSIS")

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Cleaning output" -PercentComplete 40
	if ([string]::IsNullOrEmpty($chassisManufacturer) -or ($chassisManufacturer.Trim().Length -eq 0)) {
		$chassisManufacturer="$NOT_SPECIFIED"
	}
	$chassisManufacturer=$(jsonManufacturer "$chassisManufacturer".Trim())

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Cleaning output" -PercentComplete 55
	if ([string]::IsNullOrEmpty($chassisModel) -or ($chassisModel.Trim().Length -eq 0)) {
		$chassisModel="$NOT_SPECIFIED"
	}
	$chassisModel=$(jsonModel "$chassisModel".Trim())

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Cleaning output" -PercentComplete 70
	if (![string]::IsNullOrEmpty($chassisSerial) -and ($chassisSerial.Trim().Length -ne 0)) {
		$chassisSerial=$(jsonSerial "$chassisSerial".Trim())
	}

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Cleaning output" -PercentComplete 85
	if (![string]::IsNullOrEmpty($chassisRevision) -and ($chassisRevision.Trim().Length -ne 0)) {
		$chassisRevision=$(jsonRevision "$chassisRevision".Trim())
	}
	$componentChassis=$(jsonComponent "$chassisClass" "$chassisManufacturer" "$chassisModel" "$chassisSerial" "$chassisRevision")
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering chassis information" -CurrentOperation "Done" -Completed
	
	return "$componentChassis"
}

function parseBaseboardData() {
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Collecting data from SMBIOS" -PercentComplete 0
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting baseboard data" -CurrentOperation "Collecting manufacturer" -PercentComplete 0
	$baseboardManufacturer=$(Get-SMBiosString $smbios "$SMBIOS_TYPE_BASEBOARD" 0x4)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting baseboard data" -CurrentOperation "Collecting model" -PercentComplete 20
	$baseboardModel=$(Get-SMBiosString $smbios "$SMBIOS_TYPE_BASEBOARD" 0x5)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting baseboard data" -CurrentOperation "Collecting serial" -PercentComplete 40
	$baseboardSerial=$(Get-SMBiosString $smbios "$SMBIOS_TYPE_BASEBOARD" 0x7)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting baseboard data" -CurrentOperation "Collecting revision" -PercentComplete 60
	$baseboardRevision=$(Get-SMBiosString $smbios "$SMBIOS_TYPE_BASEBOARD" 0x6)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting baseboard data" -CurrentOperation "Collecting replaceable indicator" -PercentComplete 80
	$baseboardFeatureFlags=$smbios["$SMBIOS_TYPE_BASEBOARD"].data[0x9]
	$baseboardReplaceableIndicator=0x1C # from Table 14
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting baseboard data" -CurrentOperation "Done" -Completed
	
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 30
	$baseboardClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_BASEBOARD")

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 40
	$baseboardFieldReplaceableAnswer="false"
	if ("$baseboardFeatureFlags" -band "$baseboardReplaceableIndicator") {
		$baseboardFieldReplaceableAnswer="true"
	}
	$baseboardFieldReplaceable=$(jsonFieldReplaceable "$baseboardFieldReplaceableAnswer")

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 52
	if ([string]::IsNullOrEmpty($baseboardManufacturer) -or ($baseboardManufacturer.Trim().Length -eq 0)) {
		$baseboardManufacturer="$NOT_SPECIFIED"
	}
	$baseboardManufacturer=$(jsonManufacturer "$baseboardManufacturer".Trim())

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 64
	if ([string]::IsNullOrEmpty($baseboardModel) -or ($baseboardModel.Trim().Length -eq 0)) {
		$baseboardModel="$NOT_SPECIFIED"
	}
	$baseboardModel=$(jsonModel "$baseboardModel".Trim())

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 76
	if (![string]::IsNullOrEmpty($baseboardSerial) -and ($baseboardSerial.Trim().Length -ne 0)) {
		$baseboardSerial=$(jsonSerial "$baseboardSerial".Trim())
	}

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Cleaning output" -PercentComplete 88
	if (![string]::IsNullOrEmpty($baseboardRevision) -and ($baseboardRevision.Trim().Length -ne 0)) {
		$baseboardRevision=$(jsonRevision "$baseboardRevision".Trim())
	}
	$componentBaseboard=$(jsonComponent "$baseboardClass" "$baseboardManufacturer" "$baseboardModel" "$baseboardFieldReplaceable" "$baseboardSerial" "$baseboardRevision")

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Done" -Completed
	
	return "$componentBaseboard"
}

function parseBiosData() {
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Collecting data from SMBIOS" -PercentComplete 0
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting BIOS data" -CurrentOperation "Collecting manufacturer" -PercentComplete 0
	$biosManufacturer=$(Get-SMBiosString $smbios "$SMBIOS_TYPE_BIOS" 0x4)
	$biosModel=""
	$biosSerial=""
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting BIOS data" -CurrentOperation "Collecting revision" -PercentComplete 75
	$biosRevision=(Get-SMBiosString $smbios "$SMBIOS_TYPE_BIOS" 0x5)
	Write-Progress -Id 3 -ParentId 2 -Activity "Collecting BIOS data" -CurrentOperation "Done" -Completed
	
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Cleaning output" -PercentComplete 30
	$biosClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_BIOS")

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Cleaning output" -PercentComplete 40
	if ([string]::IsNullOrEmpty($biosManufacturer) -or ($biosManufacturer.Trim().Length -eq 0)) {
		$biosManufacturer="$NOT_SPECIFIED"
	}
	$biosManufacturer=$(jsonManufacturer "$biosManufacturer".Trim())

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Cleaning output" -PercentComplete 55
	if ([string]::IsNullOrEmpty($biosModel) -or ($biosModel.Trim().Length -eq 0)) {
		$biosModel="$NOT_SPECIFIED"
	}
	$biosModel=$(jsonModel "$biosModel".Trim())

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Cleaning output" -PercentComplete 70
	if (![string]::IsNullOrEmpty($biosSerial) -and ($biosSerial.Trim().Length -ne 0)) {
		$biosSerial=$(jsonSerial "$biosSerial".Trim())
	}

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering BIOS information" -CurrentOperation "Cleaning output" -PercentComplete 85
	if (![string]::IsNullOrEmpty($biosRevision) -and ($biosRevision.Trim().Length -ne 0)) {
		$biosRevision=$(jsonRevision "$biosRevision".Trim())
	}
	$componentBios=$(jsonComponent "$biosClass" "$biosManufacturer" "$biosModel" "$biosSerial" "$biosRevision")

	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering baseboard information" -CurrentOperation "Done" -Completed
	
	return "$componentBios"
}

function parseCpuData() {
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering CPU information" -CurrentOperation "Collecting data from SMBIOS" -PercentComplete 0
    $RS=@($smbios["$SMBIOS_TYPE_CPU"])
    $component=""
    $numRows=$RS.Count
    $processorNotUpgradableIndicator=0x6

    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering CPU information" -CurrentOperation ("Parsing data from SMBIOS for CPU " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

        Write-Progress -Id 3 -ParentId 2 -Activity "Collecting CPU data" -CurrentOperation "Collecting manufacturer" -PercentComplete 0
        $tmpManufacturer=$(Get-SMBiosString $RS $i 0x7)
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting CPU data" -CurrentOperation "Collecting model" -PercentComplete 20
        $tmpModel=[string]($RS[$i].data[0x6]) # Enum value for Family
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting CPU data" -CurrentOperation "Collecting serial" -PercentComplete 40
        $tmpSerial=$(Get-SMBiosString $RS $i 0x20)
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting CPU data" -CurrentOperation "Collecting revision" -PercentComplete 60
        $tmpRevision=$(Get-SMBiosString $RS $i 0x10)
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting CPU data" -CurrentOperation "Collecting replaceable indicator" -PercentComplete 80
        $tmpUpgradeMethod=$RS[$i].data[0x19] # Enum for Processor Upgrade
		
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering CPU information" -CurrentOperation "Cleaning output" -PercentComplete 90
		$cpuClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_CPU")

        if ([string]::IsNullOrEmpty($tmpManufacturer) -or ($tmpManufacturer.Trim().Length -eq 0)) {
            $tmpManufacturer="$NOT_SPECIFIED"
        }
        $tmpManufacturer=$(jsonManufacturer "$tmpManufacturer".Trim())

        if ([string]::IsNullOrEmpty($tmpModel) -or ($tmpModel.Trim().Length -eq 0)) {
            $tmpModel="$NOT_SPECIFIED"
        }
        $tmpModel=$(jsonModel "$tmpModel".Trim())

        if (![string]::IsNullOrEmpty($tmpSerial) -and ($tmpSerial.Trim().Length -ne 0)) {
            $tmpSerial=$(jsonSerial "$tmpSerial".Trim())
        } else {
            $tmpSerial=""
        }

        if (![string]::IsNullOrEmpty($tmpRevision) -and ($tmpRevision.Trim().Length -ne 0)) {
            $tmpRevision=$(jsonRevision "$tmpRevision".Trim())
        } else {
            $tmpRevision=""
        }

        if ("$tmpUpgradeMethod" -eq "$processorNotUpgradableIndicator") {
            $tmpUpgradeMethod="false"
        } else {
            $tmpUpgradeMethod="true"
        }
        $replaceable=$(jsonFieldReplaceable "$tmpUpgradeMethod")

        $tmpComponent=$(jsonComponent $cpuClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $tmpRevision)
        $component+="$tmpComponent,"
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering CPU information" -CurrentOperation "Cleaning output" -Completed
    }
    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering CPU information" -CurrentOperation "Done" -Completed
    return "$component".Trim(",")
}

function parseRamData() {
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering RAM information" -CurrentOperation "Collecting data from SMBIOS" -PercentComplete 0
    $RS=@($smbios["$SMBIOS_TYPE_RAM"])
    $component=""
    $replaceable=$(jsonFieldReplaceable "true") # Looking for reliable indicator
    $numRows=$RS.Count
	
	$ramClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_RAM")

    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering RAM information" -CurrentOperation ("Parsing data from SMBIOS for Memory Chip " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

        Write-Progress -Id 3 -ParentId 2 -Activity "Collecting RAM data" -CurrentOperation "Collecting manufacturer" -PercentComplete 0
        $tmpManufacturer=(Get-SMBiosString $RS $i 0x17)
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting RAM data" -CurrentOperation "Collecting model" -PercentComplete 20
        $tmpModel=(Get-SMBiosString $RS $i 0x1A)
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting RAM data" -CurrentOperation "Collecting serial" -PercentComplete 40
        $tmpSerial=(Get-SMBiosString $RS $i 0x18)
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting RAM data" -CurrentOperation "Collecting revision" -PercentComplete 60
        $tmpRevision=(Get-SMBiosString $RS $i 0x19)
		
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering RAM information" -CurrentOperation "Cleaning output" -PercentComplete 90

        if ([string]::IsNullOrEmpty($tmpManufacturer) -and [string]::IsNullOrEmpty($tmpModel) -and [string]::IsNullOrEmpty($tmpSerial) -and [string]::IsNullOrEmpty($tmpRevision)) {
            Continue;
        }

        if ([string]::IsNullOrEmpty($tmpManufacturer) -or ($tmpManufacturer.Trim().Length -eq 0)) {
            $tmpManufacturer="$NOT_SPECIFIED"
        }
        $tmpManufacturer=$(jsonManufacturer "$tmpManufacturer".Trim())

        if ([string]::IsNullOrEmpty($tmpModel) -or ($tmpModel.Trim().Length -eq 0)) {
            $tmpModel="$NOT_SPECIFIED"
        }
        $tmpModel=$(jsonModel "$tmpModel".Trim())

        if (![string]::IsNullOrEmpty($tmpSerial) -and ($tmpSerial.Trim().Length -ne 0)) {
            $tmpSerial=$(jsonSerial "$tmpSerial".Trim())
        } else {
            $tmpSerial=""
        }

        if (![string]::IsNullOrEmpty($tmpRevision) -and ($tmpRevision.Trim().Length -ne 0)) {
            $tmpRevision=$(jsonRevision "$tmpRevision".Trim())
        } else {
            $tmpRevision=""
        }
        $tmpComponent=$(jsonComponent $ramClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $tmpRevision)
        $component+="$tmpComponent,"
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering RAM information" -CurrentOperation "Cleaning output" -Completed
    }

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering RAM information" -CurrentOperation "Done" -Completed
    return "$component".Trim(",")
}

function parseNicData() {
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NIC information" -CurrentOperation "Collecting data from Powershell" -PercentComplete 0
    $RS=(Get-NetAdapter | Select-Object MacAddress,PhysicalMediaType,PNPDeviceID | Where-Object {($_.PhysicalMediaType -eq "Native 802.11" -or "802.3") -and ($_.PNPDeviceID -Match "^(PCI)\\.*$")})
    $component=""
    $replaceable=$(jsonFieldReplaceable "true")
    $numRows=$RS.Count
	
	$nicClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_NIC")

    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NIC information" -CurrentOperation ("Parsing data from Powershell for NIC " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

        $pnpDevID=""
        if(isPCI($RS[$i].PNPDeviceID)) {
            $pnpDevID=$(pciParse $RS[$i].PNPDeviceID)
        } else {
            Continue
        }

		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting NIC data" -CurrentOperation "Collecting manufacturer" -PercentComplete 0
        $tmpManufacturer=$pnpDevID.vendor # PCI Vendor ID
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting NIC data" -CurrentOperation "Collecting model" -PercentComplete 20
        $tmpModel=$pnpDevID.product  # PCI Device Hardware ID
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting NIC data" -CurrentOperation "Collecting serial and MAC" -PercentComplete 40
        $tmpSerialConstant=($RS[$i].MacAddress)
        $tmpSerialConstant=$(standardizeMACAddr $tmpSerialConstant)
        $tmpSerial=""
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting NIC data" -CurrentOperation "Collecting revision" -PercentComplete 60
        $tmpRevision=$pnpDevID.revision
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering NIC information" -CurrentOperation "Collecting NIC type" -PercentComplete 80
        $tmpMediaType=$RS[$i].PhysicalMediaType
        $thisAddress=""
		
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering RAM information" -CurrentOperation "Cleaning output" -PercentComplete 90
		
        if ([string]::IsNullOrEmpty($tmpManufacturer) -or ($tmpManufacturer.Trim().Length -eq 0)) {
            $tmpManufacturer="$NOT_SPECIFIED"
        }
        $tmpManufacturer=$(jsonManufacturer "$tmpManufacturer".Trim())


        if ([string]::IsNullOrEmpty($tmpModel) -or ($tmpModel.Trim().Length -eq 0)) {
            $tmpModel="$NOT_SPECIFIED"
        }
        $tmpModel=$(jsonModel "$tmpModel".Trim())



        if (![string]::IsNullOrEmpty($tmpSerialConstant) -and ($tmpSerialConstant.Trim().Length -ne 0)) {
            $tmpSerial=$(jsonSerial "$tmpSerialConstant".Trim())
        } else {
            $tmpSerial=""
        }


        if (![string]::IsNullOrEmpty($tmpRevision) -and ($tmpRevision.Trim().Length -ne 0)) {
            $tmpRevision=$(jsonRevision "$tmpRevision".Trim())
        } else {
            $tmpRevision=""
        }

        if ($tmpMediaType -and $tmpSerial) {
            if ("$tmpMediaType" -match "^.*802[.]11.*$") {
                $thisAddress=$(jsonWlanMac $tmpSerialConstant)
            }
            elseif ("$tmpMediaType" -match "^.*[Bb]lue[Tt]ooth.*$") {
                $thisAddress=$(jsonBluetoothMac $tmpSerialConstant)
            }
            elseif ("$tmpMediaType" -match "^.*802[.]3.*$") {
                $thisAddress=$(jsonEthernetMac $tmpSerialConstant)
            }
            if ($thisAddress) {
                $thisAddress=$(jsonAddress "$thisAddress")
            }
        }

        $tmpComponent=$(jsonComponent $nicClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $tmpRevision $thisAddress)
        $component+="$tmpComponent,"
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering NIC information" -CurrentOperation "Cleaning output" -Completed
    }

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NIC information" -CurrentOperation "Done" -Completed
    return "$component".Trim(",")
}

function parseHddData() {
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Hard Disk information" -CurrentOperation "Collecting data from Powershell" -PercentComplete 0
    #$RS=(Get-CimInstance -ClassName CIM_DiskDrive | select serialnumber,mediatype,pnpdeviceid,manufacturer,model | where mediatype -eq "Fixed hard disk media")
    $RS=(Get-PhysicalDisk | Select-Object serialnumber,mediatype,manufacturer,model,bustype | Where-Object BusType -ne NVMe)
    $component=""
    $replaceable=$(jsonFieldReplaceable "true")
    $numRows=0
    if ($null -ne $RS) { # powershell $null should be left operand
		if ($RS -is [array]) {
			$numRows=($RS.Count)
		} else {
			$numRows=1
        }
    }
	
	$hddClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_HDD")
	
    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Hard Disk information" -CurrentOperation ("Parsing data from Powershell for HDD " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)
		
        #$pnpDevID=""
        #if(isIDE($RS[$i].PNPDeviceID)) {
        #   $pnpDevID=(ideDiskParse $RS[$i].PNPDeviceID)
        #} elseif(isSCSI($RS[$i].PNPDeviceID)) {
        #   $pnpDevID=(scsiDiskParse $RS[$i].PNPDeviceID)
        #} else {
        #  Continue
        #}

        #if(($pnpDevID -eq $null) -or (($pnpDevID -eq "(Standard disk drives)") -and ($pnpDevID.product -eq $null))) {
		#   $regex="^.{,16}$"
        #    $pnpDevID=[pscustomobject]@{
        #       product=($RS[$i].model -replace '^(.{0,16}).*$','$1')  # Strange behavior for this case, will return
        #   }
        #}

        #$tmpManufacturer=$pnpDevID.vendor 
        #$tmpModel=$pnpDevID.product 
        #$tmpSerial=$RS[$i].serialnumber
        #$tmpRevision=$pnpDevID.revision

		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting Hard Disk data" -CurrentOperation "Collecting manufacturer" -PercentComplete 0
        $tmpManufacturer=$RS[$i].manufacturer
        Write-Progress -Id 3 -ParentId 2 -Activity "Collecting Hard Disk data" -CurrentOperation "Collecting model" -PercentComplete 30
        $tmpModel=($RS[$i].model -replace '^(.{0,16}).*$','$1')
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting Hard Disk data" -CurrentOperation "Collecting serial" -PercentComplete 60
        $tmpSerial=$RS[$i].serialnumber
		
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering Hard Disk information" -CurrentOperation "Cleaning output" -PercentComplete 90
		
        if ([string]::IsNullOrEmpty($tmpManufacturer) -or ($tmpManufacturer.Trim().Length -eq 0) -or ($tmpManufacturer.Trim() -eq "NVMe")) {
            $tmpManufacturer="$NOT_SPECIFIED"
        }
        $tmpManufacturer=$(jsonManufacturer "$tmpManufacturer".Trim())

        if ([string]::IsNullOrEmpty($tmpModel) -or ($tmpModel.Trim().Length -eq 0)) {
            $tmpModel="$NOT_SPECIFIED"
        }
        $tmpModel=$(jsonModel "$tmpModel".Trim())

        if (![string]::IsNullOrEmpty($tmpSerial) -and ($tmpSerial.Trim().Length -ne 0)) {
            $tmpSerial=$(jsonSerial ("$tmpSerial".Trim() -replace '[\x00-\x20]+$' ,''))
        } else {
            $tmpSerial=""
        }

        if (![string]::IsNullOrEmpty($tmpRevision) -and ($tmpRevision.Trim().Length -ne 0)) {
            $tmpRevision=$(jsonRevision "$tmpRevision".Trim())
        } else {
            $tmpRevision=""
        }

        $tmpComponent=$(jsonComponent $hddClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $tmpRevision)
        $component+="$tmpComponent,"
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering Hard Disk information" -CurrentOperation "Cleaning output" -Completed
    }

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Hard Disk information" -CurrentOperation "Done" -Completed
    return "$component".Trim(",")
}

function parseNvmeData() {
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NVMe Disk information" -CurrentOperation "Collecting data from Powershell and Windows" -PercentComplete 0
    $RS=((Get-PhysicalDisk | Where-Object BusType -eq NVMe).DeviceID)
    $component=""
    $replaceable=(jsonFieldReplaceable "true") # Looking for reliable indicator

    $hddClass=$(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_HDD")

    $nvme=$(Get-NVMeIdentifyData $RS)
    $numRows=$RS.Count

    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NVMe Disk information" -CurrentOperation ("Parsing data from Windows for NVMe Disk " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

        $tmpManufacturer=""
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting NVMe Disk data" -CurrentOperation "Collecting model" -PercentComplete 30
        $tmpModel=(NvmeGetModelNumberForDeviceNumber $nvme $RS[$i])
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting NVMe Disk data" -CurrentOperation "Collecting serial" -PercentComplete 60
        $tmpSerial=(NvmeGetNguidForDevice $nvme $RS[$i]) 
        if ("$tmpSerial" -match "^[0]+$") {
            $tmpSerial=(NvmeGetEuiForDevice $nvme $RS[$i])
        }
		
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering NVMe Disk information" -CurrentOperation "Cleaning output" -PercentComplete 90

        if ([string]::IsNullOrEmpty($tmpManufacturer) -and [string]::IsNullOrEmpty($tmpModel) -and [string]::IsNullOrEmpty($tmpSerial) -and [string]::IsNullOrEmpty($tmpRevision)) {
            Continue;
        }

        if ([string]::IsNullOrEmpty($tmpManufacturer) -or ($tmpManufacturer.Trim().Length -eq 0)) {
            $tmpManufacturer="$NOT_SPECIFIED"
        }
        $tmpManufacturer=$(jsonManufacturer "$tmpManufacturer".Trim())

        if ([string]::IsNullOrEmpty($tmpModel) -or ($tmpModel.Trim().Length -eq 0)) {
            $tmpModel="$NOT_SPECIFIED"
        } else {
            $tmpModel=($tmpModel -replace '^(.{0,16}).*$','$1') # Reformatting for consistency for now.
        }
        $tmpModel=$(jsonModel "$tmpModel".Trim())

        if (![string]::IsNullOrEmpty($tmpSerial) -and ($tmpSerial.Trim().Length -ne 0)) {
            $tmpSerial=("$tmpSerial".Trim())
            $tmpSerial=($tmpSerial -replace "(.{4})", '$1_' -replace "_$", '.') # Reformatting for consistency for now.
            $tmpSerial=$(jsonSerial $tmpSerial)
        } else {
            $tmpSerial=""
        }

        if (![string]::IsNullOrEmpty($tmpRevision) -and ($tmpRevision.Trim().Length -ne 0)) {
            $tmpRevision=$(jsonRevision "$tmpRevision".Trim())
        } else {
            $tmpRevision=""
        }
        $tmpComponent=$(jsonComponent $hddClass $tmpManufacturer $tmpModel $replaceable $tmpSerial $tmpRevision)
        $component+="$tmpComponent,"
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering NVMe Disk information" -CurrentOperation "Cleaning output" -Completed
    }
    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering NVMe Disk information" -CurrentOperation "Done" -Completed
    return "$component".Trim(",")
}

function parseGfxData() {
	Write-Progress -Id 2 -ParentId 1 -Activity "Gathering GFX information" -CurrentOperation "Collecting data from Powershell" -PercentComplete 0
    $RS=(Get-CimInstance -ClassName CIM_VideoController | Select-Object pnpdeviceid )
    $component=""
    $replaceable=(jsonFieldReplaceable "true")
    $numRows=1
    if ($RS.Count -gt 1) {
        $numRows=($RS.Count)
    }
	
	$gfxClass=(jsonComponentClass "$COMPCLASS_REGISTRY_TCG" "$COMPCLASS_GFX")
	
    for($i=0;$i -lt $numRows;$i++) {
        Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Graphics information" -CurrentOperation ("Parsing data from Powershell for HDD " + ($i+1)) -PercentComplete ((($i+1) / $numRows) * 100)

        $pnpDevID=""
        if(isPCI($RS[$i].PNPDeviceID)) {
            $pnpDevID=$(pciParse $RS[$i].PNPDeviceID)
        } else {
            Continue
        }

		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting Graphics data" -CurrentOperation "Collecting manufacturer" -PercentComplete 0
        $tmpManufacturer=$pnpDevID.vendor # PCI Vendor ID
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting Graphics data" -CurrentOperation "Collecting model" -PercentComplete 30
        $tmpModel=$pnpDevID.product  # PCI Device Hardware ID
		Write-Progress -Id 3 -ParentId 2 -Activity "Collecting Graphics data" -CurrentOperation "Collecting revision" -PercentComplete 60
        $tmpRevision=$pnpDevID.revision
        # CIM Class does not contain serialnumber

		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering Graphics information" -CurrentOperation "Cleaning output" -PercentComplete 90

        if ([string]::IsNullOrEmpty($tmpManufacturer) -or ($tmpManufacturer.Trim().Length -eq 0)) {
            $tmpManufacturer="$NOT_SPECIFIED"
        }
        $tmpManufacturer=$(jsonManufacturer "$tmpManufacturer".Trim())

        if ([string]::IsNullOrEmpty($tmpModel) -or ($tmpModel.Trim().Length -eq 0)) {
            $tmpModel="$NOT_SPECIFIED"
        }
        $tmpModel=$(jsonModel "$tmpModel".Trim())

        if (![string]::IsNullOrEmpty($tmpRevision) -and ($tmpRevision.Trim().Length -ne 0)) {
            $tmpRevision=$(jsonRevision "$tmpRevision".Trim())
        } else {
            $tmpRevision=""
        }

        $tmpComponent=$(jsonComponent $gfxClass $tmpManufacturer $tmpModel $replaceable $tmpRevision)
        $component+="$tmpComponent,"
		Write-Progress -Id 3 -ParentId 2 -Activity "Gathering Graphics information" -CurrentOperation "Cleaning output" -Completed
    }

    Write-Progress -Id 2 -ParentId 1 -Activity "Gathering Graphics information" -CurrentOperation "Done" -Completed
    return "$component".Trim(",")
}

### Collate the component details
function collectOldTcgRegistryComponents () {
	Write-Progress -Id 2 -Activity "Gathering component details" -PercentComplete 10
	$componentChassis=$(parseChassisData)
	Write-Progress -Id 2 -Activity "Gathering component details" -PercentComplete 20
	$componentBaseboard=$(parseBaseboardData)
	Write-Progress -Id 2 -Activity "Gathering component details" -PercentComplete 30
	$componentBios=$(parseBiosData)
	Write-Progress -Id 2 -Activity "Gathering component details" -PercentComplete 40
	$componentsCPU=$(parseCpuData)
	Write-Progress -Id 2 -Activity "Gathering component details" -PercentComplete 50
	$componentsRAM=$(parseRamData)
	Write-Progress -Id 2 -Activity "Gathering component details" -PercentComplete 60
	$componentsNIC=$(parseNicData)
	Write-Progress -Id 2 -Activity "Gathering component details" -PercentComplete 70
	$componentsHDD=$(parseHddData)
	Write-Progress -Id 2 -Activity "Gathering component details" -PercentComplete 80
	$componentsNVMe=$(parseNvmeData)
	Write-Progress -Id 2 -Activity "Gathering component details" -PercentComplete 90
	$componentsGFX=$(parseGfxData)
	$componentList=$(toCSV "$componentChassis" "$componentBaseboard" "$componentBios" "$componentsCPU" "$componentsRAM" "$componentsNIC" "$componentsHDD" "$componentsNVMe" "$componentsGFX")

	Write-Progress -Id 2 -Activity "Gathering component details" -CurrentOperation "Done" -Completed
    return "$componentList"
}

function collectProperties () {
    Write-Progress -Id 2 -Activity "Gathering properties" -PercentComplete 10
    $osCaption=$((Get-WmiObject -Class Win32_OperatingSystem).caption)
    if ($null -ne $osCaption) {
        $osCaption = $osCaption.Trim()
    }
    $property1=$(jsonProperty "caption" "$osCaption")  ## Example1
    #$property2=(jsonProperty "caption" "$osCaption" "$JSON_STATUS_ADDED") ## Example with optional third status argument

    $propertyList=$(toCSV "$property1")

    Write-Progress -Id 2 -Activity "Gathering properties" -CurrentOperation "Done" -Completed
    return "$propertyList"
}

if (![string]::IsNullOrEmpty($printoutfile)) {
    $componentList=$(collectOldTcgRegistryComponents)

    $output=$componentList
    if (!$componentsonly) {
        $platformList=$(parseSystemData)
        $propertyList=$(collectProperties)

        $platformObject=$(jsonPlatformObject "$platformList")
        $componentArray=$(jsonComponentArray "$componentList")
        $propertyArray=$(jsonPropertyArray "$propertyList")

        #$componentsUri=$(jsonComponentsUri "https:// example.uri/componentpolicy.pdf" "C:/path/to/local/fileToHash")
        #$propertiesUri=(jsonPropertiesUri "https:// example.uri/propertypolicy.pdf" "C:/path/to/local/fileToHash")
        $output=$(jsonIntermediateFile "$platformObject" "$componentArray" "$propertyArray")
    }

    [IO.File]::WriteAllText($printoutfile, "$output")
}
