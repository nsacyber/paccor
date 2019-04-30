#!/bin/bash

### User customizable values
#### Certificate Policies is a mandatory extension.  To add additional policies, more variables must be created and referenced below.
certPolicyOid1="1.2.3" # Replace with a real Certificate Policy OID
certPolicyQualifierCPS1=""
certPolicyQualifierUserNotice1="TCG Trusted Platform Endorsement" # Don't change this value.
#### Authority Information Access is an optional extension. To add additional access methods, more variables must be created and referenced below.
authorityInfoAccessMethod1="" # valid options are OCSP or CAISSUERS
authorityInfoAccessLocation1=""  # DN
#### CRL Distribution is an optional extension.  Leave any blank to omit the extension.
crlType="" # valid options are 0 or 1
crlName="" # DN
crlReasonFlags="" # valid options are integers 0 thru 16
crlIssuer="" # CRL issuer DN
#### Targeting Information is an optional extension.  Leave the targetFile variable blank to omit the extension.
targetFile="" # provide comma separated file paths to EK certificates

### The logic below can be changed by advanced users.
#### SHA-256 was assumed to be acceptable for each of the hashAlg choices for URI References
#### 2.16.840.1.101.3.4.2.1 is the oid for SHA-256. see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs 

### JSON Structure Keywords
JSON_CERTIFICATEPOLICIES="CERTIFICATEPOLICIES"
JSON_POLICYIDENTIFIER="POLICYIDENTIFIER"
JSON_POLICYQUALIFIERS="POLICYQUALIFIERS"
JSON_POLICYQUALIFIERID="POLICYQUALIFIERID"
JSON_QUALIFIER="QUALIFIER"
JSON_CPS="CPS"
JSON_USERNOTICE="USERNOTICE"
JSON_AUTHORITYINFOACCESS="AUTHORITYINFOACCESS"
JSON_ACCESSMETHOD="ACCESSMETHOD"
JSON_ACCESSLOCATION="ACCESSLOCATION"
JSON_OCSP="OCSP"
JSON_CAISSUERS="CAISSUERS"
JSON_CRLDISTRIBUTION="CRLDISTRIBUTION"
JSON_DISTRIBUTIONNAME="DISTRIBUTIONNAME"
JSON_TYPE="TYPE"
JSON_NAME="NAME"
JSON_REASON="REASON"
JSON_ISSUER="ISSUER"
JSON_TARGETINGINFORMATION="TARGETINGINFORMATION"
JSON_FILE="FILE"

### JSON Structure Format
JSON_OTHER_EXTENSIONS_TEMPLATE='{%s
}'
JSON_CERTIFICATE_POLICIES_TEMPLATE='
    \"'"$JSON_CERTIFICATEPOLICIES"'\": [
        %s
    ]'
JSON_POLICY_IDENTIFIER_TEMPLATE='{
            \"'"$JSON_POLICYIDENTIFIER"'\": \"%s\",
            \"'"$JSON_POLICYQUALIFIERS"'\": [
                %s
            ]
        }'
JSON_POLICY_QUALIFIER_TEMPLATE='{
                    \"'"$JSON_POLICYQUALIFIERID"'\": \"%s\",
                    \"'"$JSON_QUALIFIER"'\": \"%s\"
                }'
JSON_AUTHORITY_INFO_ACCESS_TEMPLATE='
    \"'"$JSON_AUTHORITYINFOACCESS"'\": [
        %s
    ]'
JSON_AUTH_ACCESS_TEMPLATE='{
            \"'"$JSON_ACCESSMETHOD"'\": \"%s\",
            \"'"$JSON_ACCESSLOCATION"'\": \"%s\"
        }'
JSON_CRL_DISTRIBUTION_TEMPLATE='
    \"'"$JSON_CRLDISTRIBUTION"'\": {
        \"'"$JSON_DISTRIBUTIONNAME"'\": {
            \"'"$JSON_TYPE"'\": \"%s\",
            \"'"$JSON_NAME"'\": \"%s\"
        },
        \"'"$JSON_REASON"'\": \"%s\",
        \"'"$JSON_ISSUER"'\": \"%s\"
    }
'
JSON_TARGETING_INFORMATION_TEMPLATE='
    \"'"$JSON_TARGETINGINFORMATION"'\": [%s
    ]'
JSON_TARGETING_INFORMATION_FILE_TEMPLATE='
        {\"'"$JSON_FILE"'\": \"%s\"}
'

### JSON Constructor Aides
toCSV () {
    old="$IFS"
    IFS=','
    value="$*"
    printf "$value"
}
jsonCertificatePolicies() {
    printf "$JSON_CERTIFICATE_POLICIES_TEMPLATE" "$(toCSV "$@")"
}
jsonPolicyIdentifier() {
    printf "$JSON_POLICY_IDENTIFIER_TEMPLATE" "${1}" "${2}"
}
jsonPolicyQualifierCPS() {
    printf "$JSON_POLICY_QUALIFIER_TEMPLATE" "$JSON_CPS" "${1}"
}
jsonPolicyQualifierUserNotice() {
    printf "$JSON_POLICY_QUALIFIER_TEMPLATE" "$JSON_USERNOTICE" "${1}"
}
jsonAuthInfoAccess() {
    printf "$JSON_AUTHORITY_INFO_ACCESS_TEMPLATE" "$(toCSV "$@")"
}
jsonAuthInfoAccessElement() {
    printf "$JSON_AUTH_ACCESS_TEMPLATE" "${1}" "${2}"
}
jsonCRLDist() {
    printf "$JSON_CRL_DISTRIBUTION_TEMPLATE" "$crlType" "$crlName" "$crlReasonFlags" "$crlIssuer"
}
jsonTargetingInformation() {
    targetInfo=()
    targetFileSplit=$(echo "$targetFile" | sed -n 1'p' | tr ',' '\n')
    while read file; do
        formatted=$(printf "$JSON_TARGETING_INFORMATION_FILE_TEMPLATE" "$file")
        targetInfo+=("$formatted")
    done <<< "$targetFileSplit"
    printf "$JSON_TARGETING_INFORMATION_TEMPLATE" "$(toCSV "${targetInfo[@]}")"
}

jsonOtherExtensionsFile() {
    # work on making this script more intuitive
    usernotice1=$(jsonPolicyQualifierUserNotice "$certPolicyQualifierUserNotice1")
    qualifier1="$usernotice1"
    if [ -n "$certPolicyQualifierCPS1" ]; then
        cps1=$(jsonPolicyQualifierCPS "$certPolicyQualifierCPS1")
        qualifier1="$qualifier"",""$cps1"
    fi
    policyId1=$(jsonPolicyIdentifier "$certPolicyOid1" "$qualifier1")
    certPolicies=$(jsonCertificatePolicies "$policyId1")
    tmpData="$certPolicies"

    if [ -n "$authorityInfoAccessMethod1" ] && [ -n "$authorityInfoAccessLocation1" ]; then
        access1=$(jsonAuthInfoAccessElement "$authorityInfoAccessMethod1" "$authorityInfoAccessLocation1")
        access=$(jsonAuthInfoAccess "$access1")
        tmpData="$tmpData"",""$access"
    fi

    if [ -n "$crlType" ] && [ -n "$crlName" ] && [ -n "$crlReasonFlags" ] && [ -n "$crlIssuer" ]; then
        crlName=$(jsonCRLDist)
        tmpData="$tmpData"",""$crlName"
    fi

    if [ -n "$targetFile" ]; then
        targets=$(jsonTargetingInformation)
        tmpData="$tmpData"",""$targets"
    fi
    
    printf "$JSON_OTHER_EXTENSIONS_TEMPLATE" "$tmpData"
}


### Put it all together
finalData=$(jsonOtherExtensionsFile)
printf "$finalData""\n"

