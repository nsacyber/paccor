#!/bin/bash
dmidecodeHandles () {
    type="${1}"
    str=$(dmidecode -t "$type" | grep -e '^Handle.*' | sed 's/Handle \(0x[0-9A-F^,]*\),.*/\1/' | tr "\n\r\t" ' ' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
    old="$IFS"
    IFS=' '
    tableHandles=($str)
    IFS="$old"
}
dmidecodeData () {
    handle="${1}"
    if  [[ $handle =~ ^0x[0-9A-Fa-f]+$ ]]; then
        str=$(dmidecode -H "$handle" -u | awk '/Header and Data:/{f=1;next} /Strings/{f=0} f' | tr "\n\r\t" ' ' | tr -s ' ' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]*$//')
        old="$IFS"
        IFS=' '
        tableData=($str)
        IFS="$old"
    fi
}
dmidecodeStrings () {
    handle="${1}"
    if  [[ $handle =~ ^0x[0-9A-Fa-f]+$ ]]; then
        str=$(dmidecode -H "$handle" -u | awk '/Strings/{f=1;next} /^\w+$/{f=0} f' | sed 's/^[^"]*$//g' | sed 's/^\w+//g' | sed '/^[[:space:]]*$/d')
        old="$IFS"
        IFS=$'\n'
        tableStrings=($str)
        IFS="$old"
    fi
}
dmidecodeParseHandle () {
    handle="${1}"
    dmidecodeData "$handle"
    dmidecodeStrings "$handle"
}
dmidecodeNumHandles () {
    printf "${#tableHandles[@]}"
}
dmidecodeParseTypeAssumeOneHandle () {
    type="${1}"
    dmidecodeHandles "$type" > /dev/null
    dmidecodeParseHandle "${tableHandles[0]}"
}
dmidecodeGetByte () {
    index="${1}"
    index=$(printf "%d" $index)
    printf "${tableData[$index]}"
}
dmidecodeGetByteRange () {
    first="${1}"
    last="${2}"
    firstDec=$(printf "%d" $first)
    lastDec=$(printf "%d" $last)
    str=""
    for ((i = firstDec ; i <= lastDec ; i++ )); do
        str="$str""${tableData[$i]}"
    done
    printf "$str"
}
dmidecodeGetString () {
    strref="${1}"
    str=""
    if [[ $strref =~ ^[0-9A-Fa-f]+$ ]]; then
        strrefDec=$(printf "%d" "0x""$strref")
        lenDec=$(printf "%d" "0x"${#tableStrings[@]})
        if [ $strrefDec -le $lenDec ] && [ $strrefDec -gt 0 ]; then
            str="${tableStrings[$strrefDec-1]}"
            str=$(printf "$str" | sed 's/^[ \t]*"\?//;s/"\?[ \t]*$//')
        fi
    fi
    printf "$str"
}

SMBIOS_TYPE_SYSTEM="1"
SMBIOS_TYPE_CHASSIS="3"
SMBIOS_TYPE_BIOS="0"
SMBIOS_TYPE_BASEBOARD="2"
SMBIOS_TYPE_PROCESSOR="4"
SMBIOS_TYPE_RAM="17"
SMBIOS_TYPE_POWERSUPPLY="39"
SMBIOS_TYPE_TPM="43"
dmidecodeGetType () {
    type="${tableData[0]}"
    printf "%s" "$type"
}
dmidecodeGetComponentClassValue () {
    class=""
    type=$(dmidecodeGetType)
    typeDec=$(printf "%d" "0x""$type") # Convert to decimal
    case $typeDec in 
        $SMBIOS_TYPE_BASEBOARD)
            lsb=$(dmidecodeGetByte "0xD") # least significant byte
            class=$(printf "00%s00%s" "$type" "$lsb")
        ;;
        $SMBIOS_TYPE_BIOS)
            lsw=$(dmidecodeGetByteRange "0x12" "0x13") # least significant word
	        class=$(printf "00%s%s" "$type" "$lsw")
        ;;
        $SMBIOS_TYPE_CHASSIS)
            lsb=$(dmidecodeGetByte "0x5")
            class=$(printf "00%s00%s" "$type" "$lsb")
        ;;
        $SMBIOS_TYPE_PROCESSOR)
            lsb=$(dmidecodeGetByte "0x5")
            class=$(printf "00%s00%s" "$type" "$lsb")
        ;;
        $SMBIOS_TYPE_RAM) 
            lsb=$(dmidecodeGetByte "0x12")
            class=$(printf "00%s00%s" "$type" "$lsb")
        ;;
        $SMBIOS_TYPE_SYSTEM)
	        class=$(printf "00%s0000" "$type")
        ;;
        $SMBIOS_TYPE_POWERSUPPLY)
            class=$(printf "00%s0000" "$type")
        ;;
        $SMBIOS_TYPE_TPM)
            class=$(printf "00%s0000" "$type")
        ;;
    esac
    printf "$class"
}
dmidecodeGetManufacturer () {
    manufacturer=""
    type=$(dmidecodeGetType)
    typeDec=$(printf "%d" "0x""$type") # Convert to decimal
    case $typeDec in
        $SMBIOS_TYPE_BASEBOARD)
            manufacturer=$(dmidecodeGetString $(dmidecodeGetByte "0x4"))
        ;;
        $SMBIOS_TYPE_BIOS)
            manufacturer=$(dmidecodeGetString $(dmidecodeGetByte "0x4"))
        ;;
        $SMBIOS_TYPE_CHASSIS)
            manufacturer=$(dmidecodeGetString $(dmidecodeGetByte "0x4"))
        ;;
        $SMBIOS_TYPE_PROCESSOR)
            manufacturer=$(dmidecodeGetString $(dmidecodeGetByte "0x7"))
        ;;
        $SMBIOS_TYPE_RAM) 
            manufacturer=$(dmidecodeGetString $(dmidecodeGetByte "0x17"))
        ;;
        $SMBIOS_TYPE_SYSTEM)
	        manufacturer=$(dmidecodeGetString $(dmidecodeGetByte "0x4"))
        ;;
        $SMBIOS_TYPE_POWERSUPPLY)
            manufacturer=$(dmidecodeGetString $(dmidecodeGetByte "0x7"))
        ;;
        $SMBIOS_TYPE_TPM)
            value=$(dmidecodeGetByteRange "0x4" "0x7")
            manufacturer=$(printf "%s" "$value")
        ;;
    esac
    printf "$manufacturer"
}
dmidecodeGetModel () {
    model=""
    type=$(dmidecodeGetType)
    typeDec=$(printf "%d" "0x""$type") # Convert to decimal
    case $typeDec in
        $SMBIOS_TYPE_BASEBOARD)
            model=$(dmidecodeGetString $(dmidecodeGetByte "0x5"))
        ;;
        $SMBIOS_TYPE_BIOS)
            model=$(dmidecodeGetString $(dmidecodeGetByte "0x5"))
        ;;
        $SMBIOS_TYPE_CHASSIS)
            value=$(dmidecodeGetByte "0x5")
            model=$(printf "%s" "$value")
        ;;
        $SMBIOS_TYPE_PROCESSOR)
            value=$(dmidecodeGetByte "0x6")
            model=$(printf "%s" "$value")
        ;;
        $SMBIOS_TYPE_RAM) 
            model=$(dmidecodeGetString $(dmidecodeGetByte "0x1A"))
        ;;
        $SMBIOS_TYPE_SYSTEM)
	        model=$(dmidecodeGetString $(dmidecodeGetByte "0x5"))
        ;;
        $SMBIOS_TYPE_POWERSUPPLY)
            model=$(dmidecodeGetString $(dmidecodeGetByte "0xA"))
        ;;
        $SMBIOS_TYPE_TPM)
            value=$(dmidecodeGetByteRange "0x8" "0x9")
            model=$(printf "%s" "$value")
        ;;
    esac
    printf "$model"
}
dmidecodeGetSerialNumber() {
    serialNumber=""
    type=$(dmidecodeGetType)
    typeDec=$(printf "%d" "0x""$type") # Convert to decimal
    case $typeDec in
        $SMBIOS_TYPE_BASEBOARD)
            serialNumber=$(dmidecodeGetString $(dmidecodeGetByte "0x7"))
        ;;
        ###    $SMBIOS_TYPE_BIOS
        ###        N/A
        ###
        $SMBIOS_TYPE_CHASSIS)
            serialNumber=$(dmidecodeGetString $(dmidecodeGetByte "0x7"))
        ;;
        $SMBIOS_TYPE_PROCESSOR)
            serialNumber=$(dmidecodeGetString $(dmidecodeGetByte "0x20"))
        ;;
        $SMBIOS_TYPE_RAM) 
            serialNumber=$(dmidecodeGetString $(dmidecodeGetByte "0x18"))
        ;;
        $SMBIOS_TYPE_SYSTEM)
	        serialNumber=$(dmidecodeGetString $(dmidecodeGetByte "0x7"))
        ;;
        $SMBIOS_TYPE_POWERSUPPLY)
            serialNumber=$(dmidecodeGetString $(dmidecodeGetByte "0x8"))
        ;;
        ###    $SMBIOS_TYPE_TPM
        ###        N/A
        ###
    esac
    printf "$serialNumber"
}
dmidecodeGetRevision () {
    revision=""
    type=$(dmidecodeGetType)
    typeDec=$(printf "%d" "0x""$type") # Convert to decimal
    case $typeDec in
        $SMBIOS_TYPE_BASEBOARD)
            revision=$(dmidecodeGetString $(dmidecodeGetByte "0x6"))
        ;;
        $SMBIOS_TYPE_BIOS)
            value=$(dmidecodeGetByteRange "0x14" "0x15")
            revision=$(printf "%s" "$value")
        ;;
        $SMBIOS_TYPE_CHASSIS)
            revision=$(dmidecodeGetString $(dmidecodeGetByte "0x6"))
        ;;
        $SMBIOS_TYPE_PROCESSOR)
            revision=$(dmidecodeGetString $(dmidecodeGetByte "0x10"))
        ;;
        $SMBIOS_TYPE_RAM) 
            revision=$(dmidecodeGetString $(dmidecodeGetByte "0x2B"))
        ;;
        $SMBIOS_TYPE_SYSTEM)
	        revision=$(dmidecodeGetString $(dmidecodeGetByte "0x6"))
        ;;
        $SMBIOS_TYPE_POWERSUPPLY)
            revision=$(dmidecodeGetString $(dmidecodeGetByte "0xB"))
        ;;
        $SMBIOS_TYPE_TPM)
            value=$(dmidecodeGetByteRange "0xA" "0x11")
            revision=$(printf "%s" "$value")
        ;;
    esac
    printf "$revision"
}
dmidecodeGetFieldReplaceable () {
    fieldReplaceable=""
    type=$(dmidecodeGetType)
    typeDec=$(printf "%d" "0x""$type") # Convert to decimal
    case $typeDec in 
        $SMBIOS_TYPE_BASEBOARD)
            bitField=$(dmidecodeGetByte "0x9")
            bitFieldDec=$(printf "%d" "0x""$bitField") # Convert to decimal
            mask="28" # 0x1C
            fieldReplaceable="false"
            if (((bitFieldDec&mask)!=0)); then
                fieldReplaceable="true"
            fi
        ;;
        ###    $SMBIOS_TYPE_BIOS
        ###        N/A
        ###
        ###    $SMBIOS_TYPE_CHASSIS
        ###        N/A
        ###
        $SMBIOS_TYPE_PROCESSOR)
            bitField=$(dmidecodeGetByte "0x19")
            bitFieldDec=$(printf "%d" "0x""$bitField") # Convert to decimal
            mask="6"
            fieldReplaceable="true"
            if [ $bitFieldDec -eq $mask ]; then
                fieldReplaceable="false"
            fi
        ;;
        ###    $SMBIOS_TYPE_RAM
        ###        N/A
        ###
        ###    $SMBIOS_TYPE_SYSTEM
        ###        N/A
        ###
        $SMBIOS_TYPE_POWERSUPPLY)
            bitField=$(dmidecodeGetByteRange "0xE" "0xF")
            bitFieldDec=$(printf "%d" "0x""$bitField") # Convert to decimal
            mask="256" # 0x0100
            fieldReplaceable="false"
            if (((bitFieldDec&mask)!=0)); then
                fieldReplaceable="true"
            fi
        ;;
        ###    $SMBIOS_TYPE_TPM
        ###        N/A
        ###
    esac
    printf "$fieldReplaceable"
}
# Examples:
#dmidecodeHandles "1"
#numHandles=$(dmidecodeNumHandles)
#echo $numHandles
#echo ${tableHandles[*]}

#dmidecodeStrings "${tableHandles[0]}"

#echo ${tableStrings[0]}

#dmidecodeData "${tableHandles[0]}"

#manufacturer=$(dmidecodeGetString $(dmidecodeGetByte "0x4"))
#model=$(dmidecodeGetByte "0x6")
#model=$(printf "%d" "0x""$model") # Convert to decimal
#model=$(dmidecodeGetString $(dmidecodeGetByte "0x5"))
#serial=$(dmidecodeGetString $(dmidecodeGetByte "0x7"))
#revision=$(dmidecodeGetString $(dmidecodeGetByte "0x6"))
#echo $manufacturer
#echo $model
#echo $serial
#echo $revision

