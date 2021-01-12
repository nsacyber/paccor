$toolpath=(Split-Path -parent $PSCommandPath)
$timestamp=(Get-Date -UFormat "%Y%m%d%H%M%S")
#### Scripts and executable
$componentlister_script="$toolpath" + "/componentlist.ps1"
$policymaker_script="$toolpath" + "/referenceoptions.ps1"
$get_ek_script="$toolpath" + "/get_ek.ps1"
$extensions_script="$toolpath" + "/otherextensions.ps1"
$signer_bin="$toolpath" + "/../../bin/signer.bat"
$validator_bin="$toolpath" + "/../../bin/validator.bat"
#### Files
$workspace="$toolpath" + "/../pc_testgen"
$componentlist="$workspace" + "/localhost-componentlist.json"
$policyreference="$workspace" + "/localhost-policyreference.json"
$ekcert="$workspace" + "/ek.pem"
$pccert="$workspace" + "/platform_cert." + "$timestamp" + ".crt"
$sigkey="$workspace" + "/CAcert.p12"
$pcsigncert="$workspace" + "/PCTestCA.example.com.cer"
$extsettings="$workspace" + "/extentions.json"
### Certificate params
$serialnumber="0001"
$dateNotBefore="20180101"
$dateNotAfter="20280101"
### Key Pair params
$subjectDN="C=US,O=example.com,OU=PCTest"
$daysValid=(Get-Date).AddYears(10)
$sigalg="RSA"
$sigalgbits="2048"
$certStoreLocation="Cert:\CurrentUser\My\"
$pfxpassword="password"

if (!(Test-Path -Path $workspace )) {
    if( (New-Object Security.Principal.WindowsPrincipal(
            [Security.Principal.WindowsIdentity]::GetCurrent())
        ).IsInRole(
            [Security.Principal.WindowsBuiltInRole]::Administrator)
      ) {
      md "$workspace" -ea 0
      if(!$?) {
          echo "Failed to make a working directory in " + "$workspace"
          exit 1
      }
    } else {
        echo "The first time this script is run, this script requires administrator privileges.  Please run as admin"
        exit 1
    }
}

# Step 1 get the ek (requires admin)
if (!(Test-Path "$ekcert" -PathType Leaf)) {
    echo "Retrieving Endorsement Certificate from the TPM"
    powershell -ExecutionPolicy Bypass "$get_ek_script" "$ekcert"
    if (!$?) {
        echo "Failed to retrieve the ek cert from the TPM, exiting"
        Remove-Item "$ekcert" -Confirm:$false -Force
        exit 1
    }
} else {
    echo "Endorsement Credential file exists, skipping retrieval"
}

# Step 2 create the components file (does not require admin on Windows)
if (!(Test-Path "$componentlist" -PathType Leaf)) {
    echo "Retrieving component info from this device"
    powershell -ExecutionPolicy Bypass "$componentlister_script" "$componentlist" 
    if (!$?) {
        echo "Failed to create a device component list, exiting"
        Remove-Item "$componentlist" -Confirm:$false -Force
        exit 1
    }
} else {
    echo "Component file exists, skipping"
}

# Step 3 create the reference options file
if (!(Test-Path "$policyreference" -PathType Leaf)) {
    echo "Creating a Platform policy JSON file"
    powershell -ExecutionPolicy Bypass "$policymaker_script" "$policyreference"
    if (!$?) {
        echo "Failed to create the policy reference, exiting"
        Remove-Item "$policyreference" -Confirm:$false -Force
        exit 1
    }
} else {
    echo "Policy settings file exists, skipping"
}

# Step 4 create the extensions settings file
if (!(Test-Path "$extsettings" -PathType Leaf)) {
    echo "Creating an extensions JSON file"
    powershell -ExecutionPolicy Bypass "$extensions_script" "$extsettings"
    if (!$?) {
        echo "Failed to create the extensions file, exiting"
        Remove-Item "$extsettings" -Confirm:$false -Force
        exit 1
    }
} else {
    echo "Extensions file exists, skipping"
}

# Step 5 check for JSON errors
Write-Progress -Activity "Checking JSON files" -CurrentOperation "components" -PercentComplete 25
try {
    [IO.File]::ReadAllText("$componentlist") | ConvertFrom-Json -ErrorAction Stop > $null
} catch {
    echo "Component file has JSON errors, exiting"
    exit 1
}
Write-Progress -Activity "Checking JSON files" -CurrentOperation "policy" -PercentComplete 50
try {
    [IO.File]::ReadAllText("$policyreference") | ConvertFrom-Json -ErrorAction Stop > $null
} catch {
    echo "Policy settings file has JSON errors, exiting"
    exit 1
}
Write-Progress -Activity "Checking JSON files" -CurrentOperation "extensions" -PercentComplete 75
try {
    [IO.File]::ReadAllText("$extsettings") | ConvertFrom-Json -ErrorAction Stop > $null
} catch {
    echo "Extensions file has JSON errors, exiting"
    exit 1
}
Write-Progress -Activity "Checking JSON files" -CurrentOperation "Done" -PercentComplete 100
echo "All JSON structures look valid."

# Step 6 create a sample signing key pair
if (!(Test-Path "$pcsigncert" -PathType Leaf)) {
    echo "Creating a signing key for signing platform credentials"
    $newcert=(New-SelfSignedCertificate -Type Custom -KeyExportPolicy Exportable -Subject "$subjectDN" -KeyUsage DigitalSignature -KeyAlgorithm "$sigalg" -KeyLength "$sigalgbits" -NotAfter "$daysValid" -CertStoreLocation "$certStoreLocation")
    if (!$?) {
        echo "Failed to create the key pair, exiting"
        exit 1
    }
    $passw=ConvertTo-SecureString -String "$pfxpassword" -Force -AsPlainText;
    $certStoreAddress="$certStoreLocation"
    $certStoreAddress+=($newcert.Thumbprint)
    Export-PfxCertificate -Cert "$certStoreAddress" -FilePath "$sigkey" -Password $passw
    if (!$?) {
        echo "Failed to export the PFX file, exiting"
        exit 1
    }
    Export-Certificate  -Cert "$certStoreAddress" -FilePath "$pcsigncert"
    if (!$?) {
        echo "Failed to export the certificate, exiting"
        exit 1
    }
    Get-ChildItem "$certStoreLocation" | Where-Object { $_.Thumbprint -match ($newcert.Thumbprint) } | Remove-Item
} else { 
    echo "Platform Signing file exists, skipping"
}

# Step 7 create and sign the new platform credential
echo "Generating a signed Platform Credential"
& $signer_bin -x "$extsettings" -c "$componentlist" -e "$ekcert" -p "$policyreference" -k "$sigkey" -N "$serialnumber" -b "$dateNotBefore" -a "$dateNotAfter" -f "$pccert" 
if (!$?) {
    echo "The signer could not produce a Platform Credential, exiting"
    exit 1
}

# Step 8 validate the signature
echo "Validating the signature"
& $validator_bin -P "$pcsigncert" -X "$pccert"

if ($?) {
    echo "PC Credential Creation Complete."
    echo "Platform Credential has been placed in ""$pccert"
} else {
    Remove-Item "$pccert" -Confirm:$false -Force
    echo "Error with signature validation of the credential."
}

