#!/bin/bash

### User customizable values
tcgPlatformSpecificationMajorVersion="1" # Released May 22, 2017
tcgPlatformSpecificationMinorVersion="3"
tcgPlatformSpecificationRevision="22"
tcgPlatformSpecificationClass="00000001"  # In HEX.  For the Client (TPM_PS_PC) in the structures document.
tcgCredentialSpecificationMajorVersion="1" # Released Jan 16, 2018
tcgCredentialSpecificationMinorVersion="0"
tcgCredentialSpecificationRevision="16"
platformConfigUri="" # URL to a platform configuration document
platformConfigLocalCopyForHashing=""
tbbSecurityAssertionVersion="1" # default is 1 for this version of credential specification
#### Common Criteria specific values
commonCriteriaMeasuresVersion="" # see reference publications at https://CommonCriteriaPortal.org/cc
assuranceLevel="" # valid options are 1 thru 7
evaluationStatus="" # valid options: designedToMeet, evaluationInProgress, evaluationCompleted
ccPlus="" # default false, valid options: true, false
strengthOfFunction="" # valid options: basic, medium, high
profileOid="" # OID of the protection profile
profileUri=""
profileLocalCopyForHashing=""
targetOid=""
targetUri=""
targetLocalCopyForHashing=""
#### FIPS specific values
fipsVersion="" # see reference publications at https://csrc.nist.gov/Projects/Cryptographic-Module-Validation-Program/Standards
fipsLevel=""
fipsPlus="" # default false, valid options: true, false
#### Other TBB assertions
measurementRootType="" # valid options: static, dynamic, nonHost, hybrid, physical, virtual
iso9000Certified="false" # default false, valid options: true, false
iso9000Uri=""  # This is referenced as a IA5String in v1 of the spec.

### The logic below can be changed by advanced users.
#### SHA-256 was assumed to be acceptable for each of the hashAlg choices for URI References
#### 2.16.840.1.101.3.4.2.1 is the oid for SHA-256. see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs 


### JSON Structure Keywords
JSON_TCGPLATFORMSPECIFICATION="TCGPLATFORMSPECIFICATION"
JSON_TCGCREDENTIALSPECIFICATION="TCGCREDENTIALSPECIFICATION"
JSON_MAJORVERSION="MAJORVERSION"
JSON_MINORVERSION="MINORVERSION"
JSON_REVISION="REVISION"
JSON_PLATFORMCLASS="PLATFORMCLASS"
JSON_TBBSECURITYASSERTIONS="TBBSECURITYASSERTIONS"
JSON_VERSION="VERSION"
JSON_CCINFO="CCINFO"
JSON_ASSURANCELEVEL="ASSURANCELEVEL"
JSON_EVALUATIONSTATUS="EVALUATIONSTATUS"
JSON_PLUS="PLUS"
JSON_STRENGTHOFFUNCTION="STRENGTHOFFUNCTION"
JSON_PROFILEOID="PROFILEOID"
JSON_PROFILEURI="PROFILEURI"
JSON_TARGETOID="TARGETOID"
JSON_TARGETURI="TARGETURI"
JSON_FIPSLEVEL="FIPSLEVEL"
JSON_LEVEL="LEVEL"
JSON_MEASUREMENTROOTTYPE="RTMTYPE"
JSON_ISO9000CERTIFIED="ISO9000CERTIFIED"
JSON_ISO9000URI="ISO9000URI"
JSON_PLATFORMCONFIGURI="PLATFORMCONFIGURI"
#### JSON Platform URI Keywords
JSON_URI="UNIFORMRESOURCEIDENTIFIER"
JSON_HASHALG="HASHALGORITHM"
JSON_HASHVALUE="HASHVALUE"

### JSON Structure Format
JSON_REFERENCE_OPTIONS_TEMPLATE='{
    %s
}'
JSON_PLATFORM_SPEC_TEMPLATE='
    \"'"$JSON_TCGPLATFORMSPECIFICATION"'\": {
        \"'"$JSON_VERSION"'\": {
            \"'"$JSON_MAJORVERSION"'\": \"%s\",
            \"'"$JSON_MINORVERSION"'\": \"%s\",
            \"'"$JSON_REVISION"'\": \"%s\"
        },
        \"'"$JSON_PLATFORMCLASS"'\": \"%s\"
    }'
JSON_CREDENTIAL_SPEC_TEMPLATE='
    \"'"$JSON_TCGCREDENTIALSPECIFICATION"'\": {
        \"'"$JSON_MAJORVERSION"'\": \"%s\",
        \"'"$JSON_MINORVERSION"'\": \"%s\",
        \"'"$JSON_REVISION"'\": \"%s\"
    }'
JSON_TBB_ASSERTIONS_TEMPLATE='
    \"'"$JSON_TBBSECURITYASSERTIONS"'\": {
        \"'"$JSON_VERSION"'\": \"%s\",
        \"'"$JSON_ISO9000CERTIFIED"'\": \"%s\"%s
    }'
JSON_CC_INFO_TEMPLATE='
        \"'"$JSON_CCINFO"'\": {
            \"'"$JSON_VERSION"'\": \"%s\",
            \"'"$JSON_ASSURANCELEVEL"'\": \"%s\",
            \"'"$JSON_EVALUATIONSTATUS"'\": \"%s\",
            \"'"$JSON_PLUS"'\": \"%s\"%s
        }'
JSON_STRENGTHOFFUNCTION_TEMPLATE='
            \"'"$JSON_STRENGTHOFFUNCTION"'\": \"%s\"'
JSON_PROFILEOID_TEMPLATE='
            \"'"$JSON_PROFILEOID"'\": \"%s\"'
JSON_TARGETOID_TEMPLATE='
            \"'"$JSON_TARGETOID"'\": \"%s\"'
JSON_FIPS_LEVEL_TEMPLATE='
        \"'"$JSON_FIPSLEVEL"'\": {
            \"'"$JSON_VERSION"'\": \"%s\",
            \"'"$JSON_LEVEL"'\": \"%s\",
            \"'"$JSON_PLUS"'\": \"%s\"
        }'
JSON_MEASUREMENTROOTTYPE_TEMPLATE='
        \"'"$JSON_MEASUREMENTROOTTYPE"'\": \"%s\"'
JSON_ISO9000CERTIFIED_TEMPLATE='
        \"'"$JSON_ISO9000CERTIFIED"'\": \"%s\"'
JSON_URIREFERENCE_TEMPLATE='
    \"%s\": {
        %s
    }'

### JSON Constructor Aides
toCSV () {
    old="$IFS"
    IFS=','
    value="$*"
    printf "$value"
}
jsonPlatformSpec() {
    platformClass=$(printf "$tcgPlatformSpecificationClass" | xxd -r -p | base64 -w 0)
    printf "$JSON_PLATFORM_SPEC_TEMPLATE" "$tcgPlatformSpecificationMajorVersion" "$tcgPlatformSpecificationMinorVersion" "$tcgPlatformSpecificationRevision" "$platformClass"
}
jsonCredentialSpec() {
    printf "$JSON_CREDENTIAL_SPEC_TEMPLATE" "$tcgCredentialSpecificationMajorVersion" "$tcgCredentialSpecificationMinorVersion" "$tcgCredentialSpecificationRevision"
}
jsonStrengthOfFunction() {
    if [ -n "$strengthOfFunction" ]; then
        printf "$JSON_STRENGTHOFFUNCTION_TEMPLATE" "$strengthOfFunction"
    fi
}
jsonProfileOid() {
    if [ -n "$profileOid" ]; then
        printf "$JSON_PROFILEOID_TEMPLATE" "$profileOid"
    fi
}
jsonTargetOid() {
    if [ -n "$targetOid" ]; then
        printf "$JSON_TARGETOID_TEMPLATE" "$targetOid"
    fi
}
jsonMeasurementRootType() {
    if [ -n "$measurementRootType" ]; then
        printf "$JSON_MEASUREMENTROOTTYPE_TEMPLATE" "$measurementRootType"
    fi
}
jsonIso9000Certified() {
    printf "$JSON_ISO9000CERTIFIED_TEMPLATE" "${1}"
}
jsonIso9000UriStr() {
    printf '\"'"$JSON_ISO9000URI"'\": \"%s\"' "${1}"
}
jsonUri () {
    printf '\"'"$JSON_URI"'\": \"%s\"' "${1}"
}
jsonHashAlg () {
    printf '\"'"$JSON_HASHALG"'\": \"%s\"' "${1}"
}
jsonHashValue () {
    printf '\"'"$JSON_HASHVALUE"'\": \"%s\"' "${1}"
}
jsonUriBuilder () {
    ## Usage: Requires 3 parameters.  See below for the assumed hashAlg.
    ##  ${1} - The json object name.  i.e. JSON_PROFILEURI
    ##  ${2} - The URI
    ##  ${3} - Full path to the file to provide a hash over. 
    if [ $# -eq 3 ]; then
        tmpUri=$(jsonUri "${2}")
        tmpUriDetails=""
        if [ -n "${2}" ]; then
            tmpHashAlg="2.16.840.1.101.3.4.2.1" # OID for SHA256
            tmpHashValue=$(sha256sum "${3}" | sed -r 's/^([0-9a-f]+).*/\1/' | tr -d [:space:] | xxd -r -p | base64 -w 0)
            tmpHashAlgStr=$(jsonHashAlg "$tmpHashAlg")
            tmpHashValueStr=$(jsonHashValue "$tmpHashValue")
            tmpUriDetails="$tmpHashAlgStr"",""$tmpHashValueStr"
        fi
	printf "$JSON_URIREFERENCE_TEMPLATE" "${1}" "$(toCSV "$tmpUri" "$tmpUriDetails")"
    fi
}
jsonCcInfo() {
    if [ -n "$commonCriteriaMeasuresVersion" ] && [ -n "$assuranceLevel" ] && [ -n "$evaluationStatus" ]; then
        if [ -z "$ccPlus" ]; then
            ccPlus="FALSE"
        fi
        tmpRest=
        if [ -n "$strengthOfFunction" ]; then
            tmpRest="$tmpRest"",""$(jsonStrengthOfFunction)"
        fi
        if [ -n "$profileOid" ]; then
            tmpRest="$tmpRest"",""$(jsonProfileOid)"
        fi
        if [ -n "$profileUri" ] && [ -n "$profileLocalCopyForHashing" ]; then
            tmpProfileUri=$(jsonUriBuilder "$JSON_PROFILEURI" "$profileUri" "$profileLocalCopyForHashing")
            tmpRest="$tmpRest"",""$tmpProfileUri"
        fi
        if [ -n "$targetOid" ]; then
            tmpRest="$tmpRest"",""$(jsonTargetOid)"
        fi
        if [ -n "$targetUri" ] && [ -n "$targetLocalCopyForHashing" ]; then
            tmpTargetUri=$(jsonUriBuilder "$JSON_TARGETURI" "$targetUri" "$targetLocalCopyForHashing")
            tmpRest="$tmpRest"",""$tmpTargetUri"
        fi

        printf "$JSON_CC_INFO_TEMPLATE" "$commonCriteriaMeasuresVersion" "$assuranceLevel" "$evaluationStatus" "$ccPlus" "${tmpRest}"
    fi
}
jsonFipsLevel() {
    if [ -n "$fipsVersion" ] && [ -n "$fipsLevel" ]; then
        if [ -z "$fipsPlus" ]; then
            fipsPlus="FALSE"
        fi
        printf "$JSON_FIPS_LEVEL_TEMPLATE" "$fipsVersion" "$fipsLevel" "$fipsPlus"
    fi
}
jsonTbbSecurityAssertions() {
    if [ -z "$tbbSecurityAssertionVersion" ]; then
        tbbSecurityAssertionVersion="1"
    fi
    if [ -z "$iso9000Certified" ]; then
        iso9000Certified="FALSE"
    fi
    tmpRest=
    finalCcInfo=$(jsonCcInfo)
    if [ -n "$finalCcInfo" ]; then
        tmpRest="$tmpRest"",""$finalCcInfo"
    fi
    finalFipsLevel=$(jsonFipsLevel)
    if [ -n "$finalFipsLevel" ]; then
        tmpRest="$tmpRest"",""$finalFipsLevel"
    fi
    if [ -n "$measurementRootType" ]; then
        tmpRtmType=$(jsonMeasurementRootType)
        tmpRest="$tmpRest"",""$tmpRtmType"
    fi
    if [ -n "$iso9000Uri" ]; then
        tmpIso9000Uri=$(jsonIso9000UriStr "$iso9000Uri")
        tmpRest="$tmpRest"",""$tmpIso9000Uri"
    fi
    
    printf "$JSON_TBB_ASSERTIONS_TEMPLATE" "$tbbSecurityAssertionVersion" "$iso9000Certified" "$tmpRest"
}
jsonReferenceOptionsFile() {
    tmpData=$(jsonPlatformSpec)
    tmpData="$tmpData"",""$(jsonCredentialSpec)"
    tmpData="$tmpData"",""$(jsonTbbSecurityAssertions)"
    if [ -n "$platformConfigUri" ] && [ -n "$platformConfigLocalCopyForHashing" ]; then
        tmpPlatformConfigUri=$(jsonUriBuilder "$JSON_PLATFORMCONFIGURI" "$platformConfigUri" "$platformConfigLocalCopyForHashing")
        tmpData="$tmpData"",""$tmpPlatformConfigUri"
    fi
    printf "$JSON_REFERENCE_OPTIONS_TEMPLATE" "$tmpData"
}


### Put it all together
finalData=$(jsonReferenceOptionsFile)
printf "$finalData""\n"

