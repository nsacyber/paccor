#!/bin/bash

PC_CERT=

if [ -n "$1" ]; then
    PC_CERT="$1"
fi

## SET THESE ACCORDING TO YOUR TPM VERSION, AUTH SETTINGS, and EK NV INDEX
## Base constant values are chosen by default
TPM1_NVAUTH_SETTINGS="-z"
TPM1_OWNERAUTH_SETTINGS="-y --permissions=\"OWNERREAD|OWNERWRITE\""
TPM1_PC_NV_INDEX="0x0000f002"

TPM2_AUTH_SETTINGS="-a 0x40000001" # Add auth parameters as set for your TPM. i.e. -P 2a2b2c
TPM2_PERMISSIONS="-t 0x2000A"
TPM2_PC_NV_INDEX="0x1c90000"

## Shouldn't need to alter the code below this line, unless your TPM 2.0 resource manager was launched with custom settings.
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root"
    exit 1
fi

# Determine TPM version
TPM_VER_1_2=$(dmesg | grep -i tpm | grep "1\.2")
TPM_VER_2_0=$(dmesg | grep -i tpm | grep "2\.0")

distCmd=
if [ "$(. /etc/os-release; echo $NAME)" = "Ubuntu" ]; then
  distCmd="apt" 
else
  distCmd="yum"
fi

if [ -z "$TPM_VER_1_2" ] && [ -z "$TPM_VER_2_0"  ]; then
    tpmServerActive=$(ps -aux | grep "tpm_server" | grep -v "grep")
    if [ -n "$tpmServerActive" ]; then
        TPM_VER_1_2=1
    else
        echo "Could not detect version of TPM.  Please manually set in write_pc_to_nvram.sh"
        exit 1
    fi
fi

indexCmd=
defineCmd=
writeCmd=
pcCertSize=$(ls -al "$PC_CERT" | sed -r 's/^([^ ]+ ){4}//' | sed -r 's/ .*$//')
nvBufferedWrite=
maxWriteSize=256

if [ -n "$TPM_VER_1_2" ]; then
    indexCmd="-i ""$TPM1_PC_NV_INDEX"
    defineCmd="tpm_nvdefine ""$TPM1_NVAUTH_SETTINGS"" ""$TPM1_OWNERAUTH_SETTINGS"" ""$indexCmd"" -s %s"
    writeCmd="tpm_nvwrite ""$TPM1_NVAUTH_SETTINGS"" ""$indexCmd"" -n %s -f ""$PC_CERT"
    nvBufferedWrite="1" # TODO check if certain TPM 1.2s require a buffered write
elif [ -n "$TPM_VER_2_0" ]; then 
    TPM2_TOOLS_VER_1=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+1\." && echo "1" || echo "") # Figure out best way to determine TPM2_TOOLS version.  query yum/apt?
    TPM2_TOOLS_VER_2=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+2\." && echo "1" || echo "") 
    TPM2_TOOLS_VER_3=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+[3-9]+\." && echo "1" || echo "") # Can't base this on specific minor versions.  Major version will suffice
    indexCmd="-x ""$TPM2_PC_NV_INDEX"

    # Use tpm2_nvlist to see the size of the entry at the TPM2_EK_NV_INDEX
    if [ -n "$TPM2_TOOLS_VER_1" ] || [ -n "$TPM2_TOOLS_VER_2" ]; then
        resourceMgrActive=$(ps -aux | grep "resourcemgr" | grep -v "grep")
        resourceMgrPort=
        if [ -z "$resourceMgrActive" ]; then
            echo "This version of tpm2-tools requires the resourcemgr service."
            exit 1
        elif [ -n "$TPM2_TOOLS_VER_2" ]; then
            resourceMgrPort="-p 2323" # default
        fi
        defineCmd="tpm2_nvdefine ""$resourceMgrPort"" ""$TPM2_AUTH_SETTINGS"" ""$TPM2_PERMISSIONS"" ""$indexCmd"" -s %s"
        writeCmd="tpm2_nvwrite ""$resourceMgrPort"" ""$TPM2_AUTH_SETTINGS"" ""$indexCmd"" -o %s ""$PC_CERT"
        nvBufferedWrite="1"
    elif [ -n "$TPM2_TOOLS_VER_3" ]; then
        abrmdActive=$(ps -aux | grep "tpm2-abrmd" | grep -v "grep")
        modeCmd="-T device"
        if [ -n "$abrmdActive" ]; then
            modeCmd="-T abrmd"   
        fi
        defineCmd="tpm2_nvdefine ""$modeCmd"" ""$TPM2_AUTH_SETTINGS"" ""$TPM2_PERMISSIONS"" ""$indexCmd"" -s %s"
        writeCmd="tpm2_nvwrite ""$modeCmd"" ""$TPM2_AUTH_SETTINGS"" ""$indexCmd"" ""$PC_CERT"
    else
        echo "Please install tpm2-tools"
        exit 1
    fi
fi

# Define NV space, output error if already defined.
defineCmd=$(printf "$defineCmd" "$pcCertSize")
runDefine=$(eval "$defineCmd")

if [ $? -ne 0 ]; then
    echo "Error defining the NV space."
    echo "$runDefine"
    exit 1
fi

# Write Platform Certificate to NV space
writeCmd=$(printf "$writeCmd" "0")  # TODO verify which TPMS require Buffered Write
runWrite=$(eval "$writeCmd")

if [ $? -ne 0 ]; then
    echo "Error writing to NVRAM."
    echo "$runWrite"
    exit 1
fi

echo "Successfully wrote the Platform Certificate to the NVRAM index."

