#!/bin/bash


## SET THESE ACCORDING TO YOUR TPM VERSION, AUTH SETTINGS, and EK NV INDEX
## Base constant values are chosen by default
TPM1_AUTH_SETTINGS=(-z)
TPM1_EK_NV_INDEX="0x1000f000"

TPM2_AUTH_SETTINGS=(-a 0x40000001) # Add auth parameters as set for your TPM. i.e. -P 2a2b2c
TPM2_EK_NV_INDEX="0x1c00002"


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
    tpmAbrmdActive=$(ps -aux | grep "tpm2-abrmd" | grep -v "grep")
    tpm2NvreadpublicSuccess=$(tpm2_nvreadpublic &> /dev/null && echo "1")
    if [ -n "$tpmServerActive" ]; then
        TPM_VER_2_0=1
    elif [ -n "$tpmAbrmdActive" ]; then
        TPM_VER_2_0=1
    elif [ -n "$tpm2NvreadpublicSuccess" ]; then
        TPM_VER_2_0=1
    else
        echo "Could not detect version of TPM.  Please manually set in get_ek.sh"
        exit 1
    fi
fi

ekCertSize=
nvBufferedRead=
maxReadSize=256
reader=
resourceMgrArgs=()
modeArgs=()

if [ -n "$TPM_VER_1_2" ]; then
    ekCertSize=$(tpm_nvinfo | sed -n -e "/""$TPM1_EK_NV_INDEX""/,\$p" | sed -e '/^[ \t\r\n]*$/,$d' | grep "Size" | sed -E 's/^Size[ ]+:[ ]*([0-9]+) .*$/\1/')
    reader=tpm1
    nvBufferedRead="1"
elif [ -n "$TPM_VER_2_0" ]; then 
    TPM2_TOOLS_VER_1=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+1\." && echo "1" || echo "")
    TPM2_TOOLS_VER_2=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+2\." && echo "1" || echo "") 
    TPM2_TOOLS_VER_3=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+3\." && echo "1" || echo "")
    TPM2_TOOLS_VER_4=$("$distCmd" list installed tpm2-tools 2> /dev/null | grep --quiet -E "[ \t]+[4-9]+\." && echo "1" || echo "")
    # Use tpm2_nvlist to see the size of the entry at the TPM2_EK_NV_INDEX
    if [ -n "$TPM2_TOOLS_VER_1" ] || [ -n "$TPM2_TOOLS_VER_2" ]; then
        resourceMgrActive=$(ps -aux | grep "resourcemgr" | grep -v "grep")
        if [ -z "$resourceMgrActive" ]; then
            echo "This version of tpm2-tools requires the resourcemgr service."
            exit 1
        elif [ -n "$TPM2_TOOLS_VER_2" ]; then
            resourceMgrArgs=(-p 2323) # default
        fi
        ekCertSize=$(tpm2_nvlist "${resourceMgrArgs[@]}" | sed -n -e "/""$TPM2_EK_NV_INDEX""/,\$p" | sed -e '/}/,$d' | grep "size of" | sed 's/.*size.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]$//')
        reader=tpm2-old
        nvBufferedRead="1"
    elif [ -n "$TPM2_TOOLS_VER_3" ] || [ -n "$TPM2_TOOLS_VER_4" ]; then
    	abrmdActive=$(ps -aux | grep "tpm2-abrmd" | grep -v "grep")
        modeArgs=(-T device)
        if [ -n "$abrmdActive" ]; then
            if [ -n "$TPM2_TOOLS_VER_3" ]; then
		modeArgs=(-T abrmd)
	    else
		modeArgs=()
	    fi
        fi
        ekCertSize=
        if [ -n "$TPM2_TOOLS_VER_3" ]; then
            ekCertSize=$(tpm2_nvlist "${modeArgs[@]}" | sed -n -e "/""$TPM2_EK_NV_INDEX""/,\$p" | sed -e '/^[ \r\n\t]*$/,$d' | grep "size" | sed 's/.*size.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]$//')
            reader=tpm2-new
        else
            ekCertSize=$(tpm2_nvreadpublic "${modeArgs[@]}" 2> /dev/null | sed -n -e "/""$TPM2_EK_NV_INDEX""/,\$p" | sed -e '/^[ \r\n\t]*$/,$d' | grep "size" | sed 's/.*size.*://' | sed -e 's/^[[:space:]]*//' | sed -e 's/[[:space:]]$//')
            reader=tpm2-new-public
        fi
    else
        echo "Please install tpm2-tools"
        exit 1
    fi
fi

read_ek_block() {
    local size=$1
    local offset=$2

    case "$reader" in
        tpm1)
            tpm_nvread "${TPM1_AUTH_SETTINGS[@]}" \
                -i "$TPM1_EK_NV_INDEX" -s "$size" -n "$offset" |
                sed -r 's/[0-9a-f]+ ([ 0-9a-f]{48}).*/\1/' |
                tr -d '[:space:]'
            ;;
        tpm2-old)
            tpm2_nvread "${resourceMgrArgs[@]}" "${TPM2_AUTH_SETTINGS[@]}" \
                -x "$TPM2_EK_NV_INDEX" -s "$size" -o "$offset" |
                sed -r -e 's/The size of data:[0-9]+//g' |
                xxd -r -p |
                xxd -p -c "$maxReadSize"
            ;;
        tpm2-new)
            tpm2_nvread "${modeArgs[@]}" "${TPM2_AUTH_SETTINGS[@]}" \
                -x "$TPM2_EK_NV_INDEX" |
                xxd -p
            ;;
        tpm2-new-public)
            tpm2_nvread "$TPM2_EK_NV_INDEX" "${modeArgs[@]}" -C o 2> /dev/null |
                xxd -p
            ;;
    esac
}

if [ -z "$ekCertSize" ]; then
    echo "The size found at the given NV index was 0 bytes."
    echo "1) Check the index given was accurate ("$TPM2_EK_NV_INDEX") and"
    echo "2) that the auth parameters are right."
    exit 1
fi

EK_CERT_HEX=
if [ -z "$nvBufferedRead" ]; then 
    EK_CERT_HEX=$(read_ek_block 0 0)  # Unbuffered read of the entire index from offset 0
else
    # Read maxByteSize at a time until the whole block is read
    sizeToRead=$maxReadSize
    offset=0
    while [ $offset -lt  $ekCertSize ]; 
    do
        if (($offset + $maxReadSize > $ekCertSize)); then
            sizeToRead=$(($ekCertSize - $offset))
        else
            sizeToRead=$maxReadSize
        fi

        blockRead=$(read_ek_block "$sizeToRead" "$offset")
        # Concatenate each block together
        EK_CERT_HEX="$EK_CERT_HEX""$blockRead"

        offset=$(($offset + $sizeToRead))
    done
fi

if [ -z "$EK_CERT_HEX" ]; then
    echo "No data was read."
    exit 1
fi

# Erase padding outside the certificate
EC_BLOB=$(echo -n "$EK_CERT_HEX" | sed 's/.\{2\}/& /g' | tr '[\r\n]+' ' ') # Separate each byte
EC_BYTE_START=$(echo -n "$EC_BLOB" | grep -b -o "30 82") # Look for the outer ASN1 Sequence
if [ -z "$EC_BYTE_START" ]; then
    echo "Data did not contain an EK certificate."
    exit 1
fi
EC_BYTE_START=$(echo -n "$EC_BLOB" | grep -b -o "30 82" | sed -n '1p' | sed -r 's/^([0-9]+):.*$/\1/') # Get outer ASN1 Sequence position
EC_LENGTH=$(echo -n "$EC_BLOB" | awk -F"30 82" '{print $2}' | tr -d '[[:space:]]') # Get the certificate length
EC_LENGTH="16#""$EC_LENGTH" # Convert to decimal
EC_LENGTH=$(((( $EC_LENGTH ) + 4) * 2)) # Calculate the number of nibbles to retain as the EC_BLOB
EC_BLOB=$(echo -n "$EC_BLOB" | tail -c +"$EC_BYTE_START" | tr -d '[[:space:]]' | head -c "$EC_LENGTH") # truncate the extra bytes

echo -n "$EC_BLOB" | xxd -r -p  # User can convert to PEM/whatever else
