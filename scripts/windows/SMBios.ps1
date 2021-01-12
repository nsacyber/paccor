#
# This method converts the raw SMBIOS data into an associative array indexed by type.
# 
# Usage:  $smbios=(Get-SMBiosStructures)
#         $smbios[$Type]
#
# Adapted from SysToolsLib Powershell Library released under Apache 2.0 License
# https://github.com/JFLarvoire/SysToolsLib/blob/master/PowerShell/Library.ps1#Get-SMBiosStructures
# 
$SMBIOS_VERSION=(Get-WmiObject -Class MSSMBios_RawSMBiosTables -Namespace root\wmi -ErrorAction SilentlyContinue).SMBiosMajorVersion + ((Get-WmiObject -Class MSSMBios_RawSMBiosTables -Namespace root\wmi -ErrorAction SilentlyContinue).SMBiosMinorVersion/10)
Function Get-SMBiosStructures() {
  $structs = @{}
  $data = (Get-WmiObject -Class MSSMBios_RawSMBiosTables -Namespace root\wmi -ErrorAction SilentlyContinue).SMBiosData
  $i = 0
  while (($data[$i+1] -ne $null) -and ($data[$i+1] -ne 0)) { # While the structure has non-0 length
    $i0 = $i
    $n = $data[$i]   # Structure type
    $l = $data[$i+1] # Structure length
    $i += $l # Count bytes from the start of the structure to the beginning of the strings section
    if ($data[$i] -eq 0) {$i++} # If there's no trailing string, count the extra NUL
	$strings=@()
    $strings += "" # Index 0 of the string array should be blocked so that string references match array indicies
    while ($data[$i] -ne 0) { # Count the size of the string section
      $s = ""
      while ($data[$i] -ne 0) { $s += [char]$data[$i++] } # Count the byte length of each string
	  $strings += $s
      $i++ # Count the string terminator NUL
    }
    $i1 = $i
	
	$obj=[pscustomobject]@{
	    type=$n
		data=@($data[$i0..$i1])
		strings=$strings
	}
	
	if ($structs["$n"] -eq $null) {
	    $structs["$n"] = @()
	}
    if ($l -gt 0) {
        $structs["$n"] += $obj
    }
    $i++ # Count the final NUL of the table, and get ready for the next table
  }
  return @($structs)
}

Function Get-SMBiosString($struct, $type, $refbyte) {
    $str=""
    if ($struct[$type] -ne $null -and $struct[$type].data -ne $null -and $struct[$type].strings -ne $null) {
        $strref=$struct[$type].data[$refbyte]
        $len=@($struct[$type].strings).Count
        if ($strref -le $len  -and $strref -gt 0) {
            $str=@($struct[$type].strings)[$struct[$type].data[$refbyte]]
        }
    }
    return $str
}

$SMBIOS_TYPE_SYSTEM="1"
$SMBIOS_TYPE_CHASSIS="3"
$SMBIOS_TYPE_BIOS="0"
$SMBIOS_TYPE_BASEBOARD="2"
$SMBIOS_TYPE_PROCESSOR="4"
$SMBIOS_TYPE_RAM="17"
$SMBIOS_TYPE_POWERSUPPLY="39"
$SMBIOS_TYPE_TPM="43"

Function GetType($struct) {
    $type=$struct.data[0x0]
    return $type
}
Function GetComponentClassValue($struct) {
    $class=""
    $type=(GetType $struct)

    switch($type) {
        $SMBIOS_TYPE_BASEBOARD {
            $lsb=$struct.data[0xD] # least significant byte
            $class="00{0:X2}00{1:X2}" -f $type,$lsb
            break;
        }
        $SMBIOS_TYPE_BIOS {
            $lsw=$struct.data[0x12..0x13] # least significant word
            $lsw=($lsw|ForEach-Object ToString X2) -join ''
            $class="00{0:X2}{1}" -f $type,$lsw
            break;
        }
        $SMBIOS_TYPE_CHASSIS {
            $lsb=$struct.data[0x5]
            $class="00{0:X2}00{1:X2}" -f $type,$lsb
            break;
        }
        $SMBIOS_TYPE_PROCESSOR {
            $lsb=$struct.data[0x5]
            $class="00{0:X2}00{1:X2}" -f $type,$lsb
            break;
        }
        $SMBIOS_TYPE_RAM {
            $lsb=$struct.data[0x12]
            $class="00{0:X2}00{1:X2}" -f $type,$lsb
            break;
        }
        $SMBIOS_TYPE_SYSTEM {
            $class="00{0:X2}0000" -f $type
            break;
        }
        $SMBIOS_TYPE_POWERSUPPLY {
            $class="00{0:X2}0000" -f $type
            break;
        }
        $SMBIOS_TYPE_TPM {
            $class="00{0:X2}0000" -f $type
            break;
        }
    }
    return $class
}
Function GetManufacturer($struct) {
    $manufacturer=""
    $type=(GetType $struct)
    switch($type) {
        $SMBIOS_TYPE_BASEBOARD {
            $manufacturer=$struct.strings[$struct.data[0x4]]
            break;
        }
        $SMBIOS_TYPE_BIOS {
            $manufacturer=$struct.strings[$struct.data[0x4]]
            break;
        }
        $SMBIOS_TYPE_CHASSIS {
            $manufacturer=$struct.strings[$struct.data[0x4]]
            break;
        }
        $SMBIOS_TYPE_PROCESSOR {
            $manufacturer=$struct.strings[$struct.data[0x7]]
            break;
        }
        $SMBIOS_TYPE_RAM {
            $manufacturer=$struct.strings[$struct.data[0x17]]
            break;
        }
        $SMBIOS_TYPE_SYSTEM {
            $manufacturer=$struct.strings[$struct.data[0x4]]
            break;
        }
        $SMBIOS_TYPE_POWERSUPPLY {
            $manufacturer=$struct.strings[$struct.data[0x7]]
            break;
        }
        $SMBIOS_TYPE_TPM {
            $value=$struct.data[0x4..0x7]
            $manufacturer=($value|ForEach-Object ToString X2) -join ''
            break;
        }
    }
    return $manufacturer
}
Function GetModel($struct) {
    $model=""
    $type=(GetType $struct)
    switch($type) {
        $SMBIOS_TYPE_BASEBOARD {
            $model=$struct.strings[$struct.data[0x5]]
            break;
        }
        $SMBIOS_TYPE_BIOS {
            $model=$struct.strings[$struct.data[0x5]]
            break;
        }
        $SMBIOS_TYPE_CHASSIS {
            $value=$struct.data[0x5]
            $model=($value|ForEach-Object ToString X2) -join ''
            break;
        }
        $SMBIOS_TYPE_PROCESSOR {
            $value=$struct.data[0x6]
            $model=($value|ForEach-Object ToString X2) -join ''
            break;
        }
        $SMBIOS_TYPE_RAM {
            $model=$struct.strings[$struct.data[0x1A]]
            break;
        }
        $SMBIOS_TYPE_SYSTEM {
            $model=$struct.strings[$struct.data[0x5]]
            break;
        }
        $SMBIOS_TYPE_POWERSUPPLY {
            $model=$struct.strings[$struct.data[0xA]]
            break;
        }
        $SMBIOS_TYPE_TPM {
            $value=$struct.data[0x8..0x9]
            $model=($value|ForEach-Object ToString X2) -join ''
            break;
        }
    }
    return $model
}
Function GetSerialNumber($struct) {
    $serialNumber=""
    $type=(GetType $struct)
    switch($type) {
        $SMBIOS_TYPE_BASEBOARD {
            $serialNumber=$struct.strings[$struct.data[0x7]]
            break;
        }
        ###    $SMBIOS_TYPE_BIOS
        ###        N/A
        ###
        $SMBIOS_TYPE_CHASSIS {
            $serialNumber=$struct.strings[$struct.data[0x7]]
            break;
        }
        $SMBIOS_TYPE_PROCESSOR {
            $serialNumber=$struct.strings[$struct.data[0x20]]
            break;
        }
        $SMBIOS_TYPE_RAM {
            $serialNumber=$struct.strings[$struct.data[0x18]]
            break;
        }
        $SMBIOS_TYPE_SYSTEM {
            $serialNumber=$struct.strings[$struct.data[0x7]]
            break;
        }
        $SMBIOS_TYPE_POWERSUPPLY {
            $serialNumber=$struct.strings[$struct.data[0x8]]
            break;
        }
        ###    $SMBIOS_TYPE_TPM {
        ###        N/A
        ###
    }
    return $serialNumber
}
Function GetRevision($struct) {
    $revision=""
    $type=(GetType $struct)
    switch($type) {
        $SMBIOS_TYPE_BASEBOARD {
            $revision=$struct.strings[$struct.data[0x6]]
            break;
        }
        $SMBIOS_TYPE_BIOS {
            $value=$struct.data[0x14..0x15]
            $revision=($value|ForEach-Object ToString X2) -join ''
            break;
        }
        $SMBIOS_TYPE_CHASSIS {
            $revision=$struct.strings[$struct.data[0x6]]
            break;
        }
        $SMBIOS_TYPE_PROCESSOR {
            $revision=$struct.strings[$struct.data[0x10]]
            break;
        }
        $SMBIOS_TYPE_RAM {
            if ($SMBIOS_VERSION -ge 3.2) {
                $revision=$struct.strings[$struct.data[0x2B]]
            }
            break;
        }
        $SMBIOS_TYPE_SYSTEM {
            $revision=$struct.strings[$struct.data[0x6]]
            break;
        }
        $SMBIOS_TYPE_POWERSUPPLY {
            $revision=$struct.strings[$struct.data[0xB]]
            break;
        }
        $SMBIOS_TYPE_TPM {
            $value=$struct.data[0xA..0x11]
            $revision=($value|ForEach-Object ToString X2) -join ''
            break;
        }
    }
    return $revision
}
Function GetFieldReplaceable($struct) {
    $fieldReplaceable=""
    $type=(GetType $struct)
    switch($type) {
        $SMBIOS_TYPE_BASEBOARD {
            $bitField=$struct.data[0x9]
            $mask=0x1C
            $fieldReplaceable="false"
            if (($bitField -band $mask) -ne 0) {
                $fieldReplaceable="true"
            }
            break;
        }
        ###    $SMBIOS_TYPE_BIOS
        ###        N/A
        ###
        ###    $SMBIOS_TYPE_CHASSIS
        ###        N/A
        ###
        $SMBIOS_TYPE_PROCESSOR {
            $bitField=$struct.data[0x19]
            $mask=0x6
            $fieldReplaceable="true"
            if ($bitField -eq $mask) {
                $fieldReplaceable="false"
            }
            break;
        }
        ###    $SMBIOS_TYPE_RAM
        ###        N/A
        ###
        ###    $SMBIOS_TYPE_SYSTEM
        ###        N/A
        ###
        $SMBIOS_TYPE_POWERSUPPLY {
            $bitField=$struct.data[0xE]
            $mask=0x01
            $fieldReplaceable="false"
            if (($bitField -band $mask) -ne 0) {
                $fieldReplaceable="true"
            }
            break;
        }
        ###    $SMBIOS_TYPE_TPM {
        ###        N/A
        ###
    }
    return $fieldReplaceable
}
# Example:
# $smbios=(Get-SMBiosStructures)
# echo $smbios["3"]
# echo $smbios["17"]
# echo $smbios["3"].strings
# echo @($smbios["3"].strings)[$smbios["3"].data[4]-1]
# $platformManufacturer=(Get-SMBiosString $smbios "1" 0x4)
# $platformModel=(Get-SMBiosString $smbios "1" 0x5)
# $platformVersion=(Get-SMBiosString $smbios "1" 0x6)
# $platformSerial=(Get-SMBiosString $smbios "1" 0x7)
# echo $platformManufacturer
# echo $platformModel
# echo $platformVersion
# echo $platformSerial
# $testType=$SMBIOS_TYPE_CHASSIS
# echo (($smbios[$testType].data|ForEach-Object ToString X2) -join ' ')
# echo $smbios[$testType].strings
# $type=(GetType $smbios[$testType][0])
# echo "a$type"
# $class=(GetComponentClassValue $smbios[$testType][0])
# echo "b$class"
# $manufacturer=(GetManufacturer $smbios[$testType][0])
# echo "c$manufacturer"
# $model=(GetModel $smbios[$testType][0])
# echo "d$model"
# $serialNumber=(GetSerialNumber $smbios[$testType][0])
# echo "e$serialNumber"
# $revision=(GetRevision $smbios[$testType][0])
# echo "f$revision"
# $fieldReplaceable=(GetFieldReplaceable $smbios[$testType][0])
# echo "g$fieldReplaceable"


