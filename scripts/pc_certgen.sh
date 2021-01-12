#!/bin/bash
#############################################################################
#  Platform Certificate Test generator 
# 
#
#
###########################################################################

toolpath="`dirname "$0"`"
timestamp=$(date +%Y%m%d%H%M%S)
#### Scripts and executable
componentlister_script="$toolpath""/componentlist.sh"
policymaker_script="$toolpath""/referenceoptions.sh"
get_ek_script="$toolpath""/get_ek.sh"
extensions_script="$toolpath""/otherextensions.sh"
signer_bin="$toolpath""/../bin/signer"
validator_bin="$toolpath""/../bin/validator"
#### Files
workspace="$toolpath""/pc_testgen"
tmpspace="/tmp"
componentlist="$workspace""/localhost-componentlist.json"
policyreference="$workspace""/localhost-policyreference.json"
ekcert="$workspace""/ek.crt"
pccert="$workspace""/platform_cert.""$timestamp"".crt"
sigkey="$workspace""/private.pem"
pcsigncert="$workspace""/PCTestCA.example.com.pem"
extsettings="$workspace""/extentions.json"
### Certificate params
serialnumber="0001"
dateNotBefore="20180101"
dateNotAfter="20280101"
### Key Pair params
subjectDN="/C=US/O=example.com/OU=PCTest"
daysValid="3652"
sigalg="rsa:2048"

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
    echo "Creating a signing key for signing platform credentials"
    $(openssl req -x509 -nodes -days "$daysValid" -newkey "$sigalg" -keyout "$sigkey" -out "$pcsigncert" -subj "$subjectDN" >> /dev/null)
    if [ $? -ne 0 ]; then
        echo "Failed to create the key pair, exiting"
        exit 1
    fi
else 
    echo "Platform Signing file exists, skipping"
fi

# Step 7 create and sign the new platform credential
echo "Generating a signed Platform Credential"
bash $signer_bin -x "$extsettings" -c "$componentlist" -e "$ekcert" -p "$policyreference" -k "$sigkey" -P "$pcsigncert" -N "$serialnumber" -b "$dateNotBefore" -a "$dateNotAfter" -f "$pccert" 
if [ $? -ne 0 ]; then
    echo "The signer could not produce a Platform Credential, exiting"
    exit 1
fi

# Step 8 validate the signature
echo "Validating the signature"
bash $validator_bin -P "$pcsigncert" -X "$pccert"

if [ $? -eq 0 ]; then
    echo "PC Credential Creation Complete."
    echo "Platform Credential has been placed in ""$pccert"
else
    rm -f "$pccert"
    echo "Error with signature validation of the credential."
fi

