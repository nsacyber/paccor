### User customizable values
$tcgPlatformSpecificationMajorVersion="1" # Released May 22, 2017
$tcgPlatformSpecificationMinorVersion="3"
$tcgPlatformSpecificationRevision="22"
$tcgPlatformSpecificationClass="00000001"  # In HEX.  For the Client (TPM_PS_PC) in the structures document.
$tcgCredentialSpecificationMajorVersion="1" # Released Jan 16, 2018
$tcgCredentialSpecificationMinorVersion="0"
$tcgCredentialSpecificationRevision="16"
$platformConfigUri="" # URL to a platform configuration document
$platformConfigLocalCopyForHashing=""
$tbbSecurityAssertionVersion="1" # default is 1 for this version of credential specification
#### Common Criteria specific values
$commonCriteriaMeasuresVersion="" # see reference publications at https://CommonCriteriaPortal.org/cc
$assuranceLevel="" # valid options are 1 thru 7
$evaluationStatus="" # valid options: designedToMeet, evaluationInProgress, evaluationCompleted
$ccPlus="" # default false, valid options: true, false
$strengthOfFunction="" # valid options: basic, medium, high
$profileOid="" # OID of the protection profile
$profileUri=""
$profileLocalCopyForHashing=""
$targetOid=""
$targetUri=""
$targetLocalCopyForHashing=""
#### FIPS specific values
$fipsVersion="" # see reference publications at https://csrc.nist.gov/Projects/Cryptographic-Module-Validation-Program/Standards
$fipsLevel=""
$fipsPlus="" # default false, valid options: true, false
#### Other TBB assertions
$measurementRootType="" # valid options: static, dynamic, nonHost, hybrid, physical, virtual
$iso9000Certified="false" # default false, valid options: true, false
$iso9000Uri=""  # This is referenced as a IA5String in v1 of the spec.

### The logic below can be changed by advanced users.
#### SHA-256 was assumed to be acceptable for each of the hashAlg choices for URI References
#### 2.16.840.1.101.3.4.2.1 is the oid for SHA-256. see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs 


### JSON Structure Keywords
$JSON_TCGPLATFORMSPECIFICATION="TCGPLATFORMSPECIFICATION"
$JSON_TCGCREDENTIALSPECIFICATION="TCGCREDENTIALSPECIFICATION"
$JSON_MAJORVERSION="MAJORVERSION"
$JSON_MINORVERSION="MINORVERSION"
$JSON_REVISION="REVISION"
$JSON_PLATFORMCLASS="PLATFORMCLASS"
$JSON_TBBSECURITYASSERTIONS="TBBSECURITYASSERTIONS"
$JSON_VERSION="VERSION"
$JSON_CCINFO="CCINFO"
$JSON_ASSURANCELEVEL="ASSURANCELEVEL"
$JSON_EVALUATIONSTATUS="EVALUATIONSTATUS"
$JSON_PLUS="PLUS"
$JSON_STRENGTHOFFUNCTION="STRENGTHOFFUNCTION"
$JSON_PROFILEOID="PROFILEOID"
$JSON_PROFILEURI="PROFILEURI"
$JSON_TARGETOID="TARGETOID"
$JSON_TARGETURI="TARGETURI"
$JSON_FIPSLEVEL="FIPSLEVEL"
$JSON_LEVEL="LEVEL"
$JSON_MEASUREMENTROOTTYPE="RTMTYPE"
$JSON_ISO9000CERTIFIED="ISO9000CERTIFIED"
$JSON_ISO9000URI="ISO9000URI"
$JSON_PLATFORMCONFIGURI="PLATFORMCONFIGURI"
#### JSON Platform URI Keywords
$JSON_URI="UNIFORMRESOURCEIDENTIFIER"
$JSON_HASHALG="HASHALGORITHM"
$JSON_HASHVALUE="HASHVALUE"

### JSON Structure Format
$JSON_REFERENCE_OPTIONS_TEMPLATE="{{
    {0}
}}"
$JSON_PLATFORM_SPEC_TEMPLATE="
    `"$JSON_TCGPLATFORMSPECIFICATION`": {{
        `"$JSON_VERSION`": {{
            `"$JSON_MAJORVERSION`": `"{0}`",
            `"$JSON_MINORVERSION`": `"{1}`",
            `"$JSON_REVISION`": `"{2}`"
        }},
        `"$JSON_PLATFORMCLASS`": `"{3}`"
    }}"
$JSON_CREDENTIAL_SPEC_TEMPLATE="
    `"$JSON_TCGCREDENTIALSPECIFICATION`": {{
        `"$JSON_MAJORVERSION`": `"{0}`",
        `"$JSON_MINORVERSION`": `"{1}`",
        `"$JSON_REVISION`": `"{2}`"
    }}"
$JSON_TBB_ASSERTIONS_TEMPLATE="
    `"$JSON_TBBSECURITYASSERTIONS`": {{
        `"$JSON_VERSION`": `"{0}`",
        `"$JSON_ISO9000CERTIFIED`": `"{1}`"{2}
    }}"
$JSON_CC_INFO_TEMPLATE="
        `"$JSON_CCINFO`": {{
            `"$JSON_VERSION`": `"{0}`",
            `"$JSON_ASSURANCELEVEL`": `"{1}`",
            `"$JSON_EVALUATIONSTATUS`": `"{2}`",
            `"$JSON_PLUS`": `"{3}`"{4}
        }}"
$JSON_STRENGTHOFFUNCTION_TEMPLATE="
            `"$JSON_STRENGTHOFFUNCTION`": `"{0}`""
$JSON_PROFILEOID_TEMPLATE="
            `"$JSON_PROFILEOID`": `"{0}`""
$JSON_TARGETOID_TEMPLATE="
            `"$JSON_TARGETOID`": `"{0}`""
$JSON_FIPS_LEVEL_TEMPLATE="
        `"$JSON_FIPSLEVEL`": {{
            `"$JSON_VERSION`": `"{0}`",
            `"$JSON_LEVEL`": `"{1}`",
            `"$JSON_PLUS`": `"{2}`"
        }}"
$JSON_MEASUREMENTROOTTYPE_TEMPLATE="
        `"$JSON_MEASUREMENTROOTTYPE`": `"{0}`""
$JSON_ISO9000CERTIFIED_TEMPLATE="
        `"$JSON_ISO9000CERTIFIED`": `"{0}`""
$JSON_URIREFERENCE_TEMPLATE="
    `"{0}`": {{
        {1}
    }}"

### JSON Constructor Aides
function toCSV() {
    if ($args.Length -ne 0) {
        for ($i=0; $i -lt $args[0].Length; $i++) {
            $item=($args[0].Get($i))

            if ($item) {
	            $value="$value,$($args[0].Get($i))"
            }
        }
        echo "$value".Trim(" ", ",")
    }
}
function HexToByteArray { # Powershell doesn't have a built in BinToHex function
    Param ([String] $str )

    if ($str.Length % 2 -ne 0) {
        $str="0$str"
    }

    if ($str.Length -ne 0) {
        ,@($str -split '([a-f0-9]{2})' | foreach-object { 
            if ($_) {
                [System.Convert]::ToByte($_,16)
            }
        })
    }
}

function jsonPlatformSpec() {
    $platformClass=([System.Convert]::ToBase64String($(HexToByteArray $(echo "$tcgPlatformSpecificationClass"))))
    echo ("$JSON_PLATFORM_SPEC_TEMPLATE" -f "$tcgPlatformSpecificationMajorVersion","$tcgPlatformSpecificationMinorVersion","$tcgPlatformSpecificationRevision","$platformClass")
}
function jsonCredentialSpec() {
    echo ("$JSON_CREDENTIAL_SPEC_TEMPLATE" -f "$tcgCredentialSpecificationMajorVersion","$tcgCredentialSpecificationMinorVersion","$tcgCredentialSpecificationRevision")
}
function jsonStrengthOfFunction() {
    if ($strengthOfFunction) {
        echo ("$JSON_STRENGTHOFFUNCTION_TEMPLATE" -f "$strengthOfFunction")
    }
}
function jsonProfileOid() {
    if ($profileOid) {
        echo ("$JSON_PROFILEOID_TEMPLATE" -f "$profileOid")
    }
}
function jsonTargetOid() {
    if ($targetOid) {
        echo ("$JSON_TARGETOID_TEMPLATE" -f "$targetOid")
    }
}
function jsonMeasurementRootType() {
    if ($measurementRootType) {
        echo ("$JSON_MEASUREMENTROOTTYPE_TEMPLATE" -f "$measurementRootType")
    }
}
function jsonIso9000Certified() {
    echo ("$JSON_ISO9000CERTIFIED_TEMPLATE" -f "$($args[0])")
}
function jsonIso9000UriStr() {
    echo ("`"$JSON_ISO9000URI`": `"{0}`"" -f "$($args[0])")
}
function jsonUri () {
    echo ("`"$JSON_URI`": `"{0}`"" -f "$($args[0])")
}
function jsonHashAlg () {
    echo ("`"$JSON_HASHALG`": `"{0}`"" -f "$($args[0])")
}
function jsonHashValue () {
    echo ("`"$JSON_HASHVALUE`": `"{0}`"" -f "$($args[0])")
}
function jsonUriBuilder () {
    ## Usage: Requires 3 parameters.  See below for the assumed hashAlg.
    ##  ${1} - The json object name.  i.e. JSON_PROFILEURI
    ##  ${2} - The URI
    ##  ${3} - Full path to the file to provide a hash over. 
    if ($args.Length -eq 3) {
        $tmpUri=(jsonUri "$($args[1])")
        $tmpUriDetails=""
        if ($($args[1])) {
            $tmpHashAlg="2.16.840.1.101.3.4.2.1" # OID for SHA256
            $tmpHashValue=([System.Convert]::ToBase64String($(HexToByteArray $(Get-FileHash "$($args[2])"  -Algorithm SHA256).Hash.Trim())))
            $tmpHashAlgStr=(jsonHashAlg "$tmpHashAlg")
            $tmpHashValueStr=(jsonHashValue "$tmpHashValue")
            $tmpUriDetails="$tmpHashAlgStr" + "," + "$tmpHashValueStr"
        }
	echo ("$JSON_URIREFERENCE_TEMPLATE" -f "$($args[0])","$(toCSV "$tmpUri","$tmpUriDetails")")
    }
}
function jsonCcInfo() {
    if ($commonCriteriaMeasuresVersion -and $assuranceLevel -and $evaluationStatus) {
        if ($ccPlus) {
            $ccPlus="FALSE"
        }
        $tmpRest=""
        if ($strengthOfFunction) {
            $tmpRest="$tmpRest" + "," + (jsonStrengthOfFunction)
        }
        if ($profileOid) {
            $tmpRest="$tmpRest" + "," + (jsonProfileOid)
        }
        if ($profileUri -and $profileLocalCopyForHashing) {
            $tmpProfileUri=(jsonUriBuilder "$JSON_PROFILEURI" "$profileUri" "$profileLocalCopyForHashing")
            $tmpRest+="," + "$tmpProfileUri"
        }
        if ($targetOid) {
            $tmpRest+="," + (jsonTargetOid)
        }
        if ($targetUri -and $targetLocalCopyForHashing) {
            $tmpTargetUri=(jsonUriBuilder "$JSON_TARGETURI" "$targetUri" "$targetLocalCopyForHashing")
            $tmpRest+="," + "$tmpTargetUri"
        }

        echo ("$JSON_CC_INFO_TEMPLATE" -f "$commonCriteriaMeasuresVersion","$assuranceLevel","$evaluationStatus","$ccPlus","$tmpRest")
    }
}
function jsonFipsLevel() {
    if ($fipsVersion -and $fipsLevel) {
        if (-not $fipsPlus) {
            $fipsPlus="FALSE"
        }
        echo ("$JSON_FIPS_LEVEL_TEMPLATE" -f "$fipsVersion","$fipsLevel","$fipsPlus")
    }
}
function jsonTbbSecurityAssertions() {
    if (-not $tbbSecurityAssertionVersion) {
        $tbbSecurityAssertionVersion="1"
    }
    if (-not $iso9000Certified) {
        $iso9000Certified="FALSE"
    }
    $tmpRest=""
    $finalCcInfo=(jsonCcInfo)
    if ($finalCcInfo) {
        $tmpRest+="," + "$finalCcInfo"
    }
    $finalFipsLevel=(jsonFipsLevel)
    if ($finalFipsLevel) {
        $tmpRest=","+ "$finalFipsLevel"
    }
    if ($measurementRootType) {
        $tmpRtmType=(jsonMeasurementRootType)
        $tmpRest+="," + "$tmpRtmType"
    }
    if ($iso9000Uri) {
        $tmpIso9000Uri=(jsonIso9000UriStr "$iso9000Uri")
        $tmpRest+="," + "$tmpIso9000Uri"
    }
    
    echo ("$JSON_TBB_ASSERTIONS_TEMPLATE" -f "$tbbSecurityAssertionVersion","$iso9000Certified","$tmpRest")
}
function jsonReferenceOptionsFile() {
    $tmpData=(jsonPlatformSpec)
    $tmpData+=","+(jsonCredentialSpec)
    $tmpData+=","+(jsonTbbSecurityAssertions)
    if ($platformConfigUri -and $platformConfigLocalCopyForHashing) {
        $tmpPlatformConfigUri=(jsonUriBuilder "$JSON_PLATFORMCONFIGURI" "$platformConfigUri" "$platformConfigLocalCopyForHashing")
        $tmpData+="," + "$tmpPlatformConfigUri"
    }
    echo ("$JSON_REFERENCE_OPTIONS_TEMPLATE" -f "$tmpData")
}


### Put it all together
$finalData=(jsonReferenceOptionsFile)
echo "$finalData"