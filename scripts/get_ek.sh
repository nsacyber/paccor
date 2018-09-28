#!/bin/bash


## SET THESE ACCORDING TO YOUR TPM VERSION, AUTH SETTINGS, and EK NV INDEX
## Base constant values are chosen by default
TPM1_AUTH_SETTINGS="-z"
TPM1_EK_NV_INDEX="0x1000f000"

TPM2_AUTH_SETTINGS="-a 0x40000001" # Add auth parameters as set for your TPM. i.e. -P 2a2b2c
TPM2_EK_NV_INDEX="0x1c00002"


## Shouldn't need to alter the code below this line.
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

# Determine TPM version
TPM_VER_1_2=$(dmesg | grep -i tpm | grep "1\.2")
TPM_VER_2_0=$(dmesg | grep -i tpm | grep "2\.0")

EK_CERT_HEX=
indexCmd=
if [ -n "$TPM_VER_1_2" ]; then
    indexCmd="-i ""$TPM1_EK_NV_INDEX"
    tpm1ReadOutput=$(tpm_nvread ""$TPM1_AUTH_SETTINGS"" ""$indexCmd"")
    EK_CERT_HEX=
    while read -r line; do
        tmpLine=$(echo -n "$line" | sed -r "s/[0-9a-f]+ ([ 0-9a-f]{48}).*/\1/" | tr -d [[:space:]])
        EK_CERT_HEX="$EK_CERT_HEX""$tmpLine"
    done <<< "$tpm1ReadOutput"
   
    # tpm_nvread places 7 bytes in front of the cert.  they can be removed.
    EK_CERT_HEX=${EK_CERT_HEX:14}
elif [ -n "$TPM_VER_2_0" ]; then 
    TPM2_TOOLS_VER_1=$(yum list installed tpm2-tools | grep --quiet -E "[ \t]+1\." && echo "1" || echo "") # Figure out best way to determine TPM2_TOOLS version.  query yum?
    TPM2_TOOLS_VER_3=$(yum list installed tpm2-tools | grep --quiet -E "[ \t]+[2-9]+\." && echo "1" || echo "") # Can't base this on specific minor versions.  Major version will suffice
    indexCmd="-x ""$TPM2_EK_NV_INDEX"

    if [ -n "$TPM2_TOOLS_VER_1" ]; then
        TPM2_MAX_READ_SIZE=256

        # Use tpm2_nvlist to see the size of the entry at the TPM2_EK_NV_INDEX
        ekCertSize=$(tpm2_nvlist | sed -n -e "/""$TPM2_EK_NV_INDEX""/,\$p" | sed -e '/}/,$d' | grep "size of" | sed 's/.*size.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]$//')

        if [ -z "$ekCertSize" ]; then
            echo "The size found at the given NV index was 0 bytes."
            echo "1) Verify that the resourcemgr service is running."
            echo "   1.x of the tpm2-tools requires it."
            echo "2) Check the index given was accurate ("$TPM2_EK_NV_INDEX") and"
            echo "3) that the auth parameters are right."
            exit
        fi
        
        # Read 256 or less bytes per tpm2_nvread request until the whole block is read
        sizeToRead=$TPM2_MAX_READ_SIZE
        offset=0
        EK_CERT_HEX=
        while [ $offset -lt  $ekCertSize ]; 
        do
           if (($offset + $TPM2_MAX_READ_SIZE > $ekCertSize)); then
                sizeToRead=$(($ekCertSize - $offset))
            else
                sizeToRead=$TPM2_MAX_READ_SIZE
            fi

            sizeCmd="-s ""$sizeToRead"
            offsetCmd="-o ""$offset"
            blockRead=$(tpm2_nvread ""$TPM2_AUTH_SETTINGS"" ""$indexCmd"" ""$sizeCmd"" ""$offsetCmd"" | sed -r -e 's/The size of data:[0-9]+//g' | perl -ne 's/([0-9a-f]{2})/print chr hex $1/gie' | xxd -p -c "$TPM2_MAX_READ_SIZE")
            # Concatenate each block together
            EK_CERT_HEX="$EK_CERT_HEX""$blockRead"

            offset=$(($offset + $sizeToRead))
        done
    elif [ -n "$TPM2_TOOLS_VER_3" ]; then
        abrmdActive=$(ps -aux | grep "tpm2-abrmd$" | grep -v "grep")
        modeCmd="-T device"
        if [ -n "$abrmdActive" ]; then
            modeCmd="-T abrmd"   
        fi
        EK_CERT_HEX=$(tpm2_nvread ""$modeCmd"" ""$TPM2_AUTH_SETTINGS"" ""$indexCmd"" | xxd -p)
    else
        echo "Please install tpm2-tools"
    fi
fi

if [ -z "$EK_CERT_HEX" ]; then
  exit 1
fi

echo -n "$EK_CERT_HEX" | xxd -r -p  # User can convert to PEM/whatever else
