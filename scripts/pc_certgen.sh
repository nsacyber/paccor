#!/bin/bash
#############################################################################
#  Platform Certificate Test generator 
# 
#
#
###########################################################################

toolpath=$(dirname "$0")
timestamp=$(date +%Y%m%d%H%M%S)
#### Scripts and executable
componentlister_script="$toolpath/allcomponents.sh"
policymaker_script="$toolpath/referenceoptions.sh"
get_ek_script="$toolpath/get_ek.sh"
extensions_script="$toolpath/otherextensions.sh"
paccor_bin="$toolpath/../bin/paccor"
#### Files
workspace="$toolpath/pc_testgen"
componentlist="$workspace/localhost-componentlist.json"
policyreference="$workspace/localhost-policyreference.json"
ekcert="$workspace/ek.cer"
pccert="$workspace/platform_cert.$timestamp.cer"
sigkey="$workspace/private.pem"
pcsigncert="$workspace/PCTestCA.example.com.pem"
extsettings="$workspace/extensions.json"
tbsout="$workspace/tbs.json"
### Certificate params
serialnumber="0001"
dateNotBefore="20180101"
dateNotAfter="20380101"
### Key Pair params
subjectDN="/C=US/O=example.com/OU=PCTest"
daysValid="3652"
openssl_sigalg="rsa:2048"
paccor_sigalg="rsa-sha256"

if [ ! -d "$workspace" ]; then
    if [ "$EUID" -ne 0 ]
        then echo "The first time this script is run, this script requires root.  Please run as root"
        exit 1
    fi
    mkdir "$workspace"
    chmod -R 777 "$workspace"
    if [ $? -ne 0 ]; then
        echo "Failed to make a working directory in ""$workspace"
        exit 1
    fi
fi

# Step 1 get the ek (requires root)
if ! [ -e "$ekcert" ]; 
    then
        echo "Retrieving Endorsement Certificate from the TPM"
        bash "$get_ek_script" > "$ekcert"
        if [ $? -ne 0 ]; then
            echo "Failed to retrieve the ek cert from the TPM, exiting"
            rm -f "$ekcert"
            exit 1
        fi
else
    echo "Endorsement Credential file exists, skipping retrieval"
fi

# Step 2 create the components file (requires root)
if ! [ -e "$componentlist" ]; then
    echo "Retrieving component info from this device"
    bash "$componentlister_script" > "$componentlist"
    if [ $? -ne 0 ]; then
        echo "Failed to create a device component list, exiting"
        rm -f "$componentlist"
        exit 1
    fi
else
    echo "Component file exists, skipping"
fi

# Step 3 create the reference options file
if ! [ -e "$policyreference" ]; then
    echo "Creating a Platform policy JSON file"
    bash "$policymaker_script" > "$policyreference"
    if [ $? -ne 0 ]; then
        echo "Failed to create the policy reference, exiting"
        rm -f "$policyreference"
        exit 1
    fi
else
    echo "Policy settings file exists, skipping"
fi

# Step 4 create the extensions settings file
if ! [ -e "$extsettings" ]; then
    echo "Creating an extensions JSON file"
    bash "$extensions_script" > "$extsettings"
    if [ $? -ne 0 ]; then
        echo "Failed to create the extensions file, exiting"
        rm -f "$extsettings"
        exit 1
    fi
else
    echo "Extensions file exists, skipping"
fi

# Step 5 check for JSON errors
printf "Checking JSON files"
if ! cat "$componentlist" | jq -e . >/dev/null; then
    echo "Component file has JSON errors, exiting"
    exit 1
fi

if ! cat "$policyreference" | jq -e . >/dev/null; then
    echo "Policy settings file has JSON errors, exiting"
    exit 1
fi

if ! cat "$extsettings" | jq -e . >/dev/null; then
    echo "Extensions file has JSON errors, exiting"
    exit 1
fi
printf "...OK\n"

# Step 6 create a sample signing key pair
if ! [ -e "$pcsigncert" ]; then
    echo "Creating a signing key for signing platform certificates"
    if ! openssl req -x509 -nodes -days "$daysValid" -newkey "$openssl_sigalg" -keyout "$sigkey" -out "$pcsigncert" -subj "$subjectDN" &> /dev/null; then
        echo "Failed to create the key pair, exiting"
        exit 1
    fi
else 
    echo "Platform Signing file exists, skipping"
fi

# Step 7 create and sign the new platform certificate
echo "Generating a signed Platform Certificate"
if ! bash "$paccor_bin" certgen -x "$extsettings" -c "$componentlist" -e "$ekcert" -p "$policyreference" -P "$pcsigncert" -N "$serialnumber" -b "$dateNotBefore" -a "$dateNotAfter" --sig-profile "$paccor_sigalg" -f "$tbsout" --finalize; then
    echo "The Platform Certificate data could not be gathered, exiting"
    exit 1
fi
if ! bash "$paccor_bin" assemble --in "$tbsout" -k "$sigkey" -P "$pcsigncert" -f "$pccert" --pem; then
    echo "The Platform Certificate could not be signed, exiting"
    exit 1
fi

# Step 8 validate the signature
echo "Validating the signature"
if bash "$paccor_bin" validate -P "$pcsigncert" -X "$pccert" -c "$componentlist"; then
    echo "PC Creation Complete."
    echo "Platform Certificate has been placed in ""$pccert"
else
    rm -f "$pccert"
    echo "Error with signature validation of the certificate."
fi

