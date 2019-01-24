### User customizable values
#### Certificate Policies is a mandatory extension.  To add additional policies, more variables must be created and referenced below.
$certPolicyOid1="1.2.3" # Replace with a real Certificate Policy OID
$certPolicyQualifierCPS1=""
$certPolicyQualifierUserNotice1="TCG Trusted Platform Endorsement" # Don't change this value.
#### Authority Information Access is an optional extension. To add additional access methods, more variables must be created and referenced below.
$authorityInfoAccessMethod1="" # valid options are OCSP or CAISSUERS
$authorityInfoAccessLocation1=""  # DN
#### CRL Distribution is an optional extension.  Leave any blank to omit the extension.
$crlType="" # valid options are 0 or 1
$crlName="" # DN
$crlReasonFlags="" # valid options are integers 0 thru 16
$crlIssuer="" # CRL issuer DN

### The logic below can be changed by advanced users.
#### SHA-256 was assumed to be acceptable for each of the hashAlg choices for URI References
#### 2.16.840.1.101.3.4.2.1 is the oid for SHA-256. see https://tools.ietf.org/html/rfc5754 for other common hash algorithm IDs 

### JSON Structure Keywords
$JSON_CERTIFICATEPOLICIES="CERTIFICATEPOLICIES"
$JSON_POLICYIDENTIFIER="POLICYIDENTIFIER"
$JSON_POLICYQUALIFIERS="POLICYQUALIFIERS"
$JSON_POLICYQUALIFIERID="POLICYQUALIFIERID"
$JSON_QUALIFIER="QUALIFIER"
$JSON_CPS="CPS"
$JSON_USERNOTICE="USERNOTICE"
$JSON_AUTHORITYINFOACCESS="AUTHORITYINFOACCESS"
$JSON_ACCESSMETHOD="ACCESSMETHOD"
$JSON_ACCESSLOCATION="ACCESSLOCATION"
$JSON_OCSP="OCSP"
$JSON_CAISSUERS="CAISSUERS"
$JSON_CRLDISTRIBUTION="CRLDISTRIBUTION"
$JSON_DISTRIBUTIONNAME="DISTRIBUTIONNAME"
$JSON_TYPE="TYPE"
$JSON_NAME="NAME"
$JSON_REASON="REASON"
$JSON_ISSUER="ISSUER"

### JSON Structure Format
$JSON_OTHER_EXTENSIONS_TEMPLATE="{{
    {0}
}}"
$JSON_CERTIFICATE_POLICIES_TEMPLATE="
    `"$JSON_CERTIFICATEPOLICIES`": [
        {0}
    ]"
$JSON_POLICY_IDENTIFIER_TEMPLATE="{{
            `"$JSON_POLICYIDENTIFIER`": `"{0}`",
            `"$JSON_POLICYQUALIFIERS`": [
                {1}
            ]
        }}"
$JSON_POLICY_QUALIFIER_TEMPLATE="{{
                    `"$JSON_POLICYQUALIFIERID`": `"{0}`",
                    `"$JSON_QUALIFIER`": `"{1}`"
                }}"
$JSON_AUTHORITY_INFO_ACCESS_TEMPLATE="
    `"$JSON_AUTHORITYINFOACCESS`": [
        {0}
    ]"
$JSON_AUTH_ACCESS_TEMPLATE='{
            `"$JSON_ACCESSMETHOD`": `"{0}`",
            `"$JSON_ACCESSLOCATION`": `"{1}`"
        }'
$JSON_CRL_DISTRIBUTION_TEMPLATE="
    `"$JSON_CRLDISTRIBUTION`": {
        `"$JSON_DISTRIBUTIONNAME`": {
            `"$JSON_TYPE`": `"{0}`",
            `"$JSON_NAME`": `"{1}`"
        },
        `"$JSON_REASON`": `"{2}`",
        `"$JSON_ISSUER`": `"{3}`"
    }
"

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
function jsonCertificatePolicies() {
    echo ("$JSON_CERTIFICATE_POLICIES_TEMPLATE" -f "$(toCSV($args))")
}
function jsonPolicyIdentifier() {
    echo ("$JSON_POLICY_IDENTIFIER_TEMPLATE" -f "$($args[0])","$($args[1])")
}
function jsonPolicyQualifierCPS() {
    echo ("$JSON_POLICY_QUALIFIER_TEMPLATE" -f "$JSON_CPS","$($args[0])")
}
function jsonPolicyQualifierUserNotice() {
    echo ("$JSON_POLICY_QUALIFIER_TEMPLATE" -f "$JSON_USERNOTICE","$($args[0])")
}
function jsonAuthInfoAccess() {
    echo ("$JSON_AUTHORITY_INFO_ACCESS_TEMPLATE" -f "$(toCSV($args))")
}
function jsonAuthInfoAccessElement() {
    echo ("$JSON_AUTH_ACCESS_TEMPLATE" -f "$($args[0])","$($args[1])")
}
function jsonCRLDist() {
    echo ("$JSON_CRL_DISTRIBUTION_TEMPLATE" -f "$crlType","$crlName","$crlReasonFlags","$crlIssuer")
}
function jsonOtherExtensionsFile() {
    # work on making this script more intuitive
    $usernotice1=(jsonPolicyQualifierUserNotice "$certPolicyQualifierUserNotice1")
    $qualifier1="$usernotice1"
    if ($certPolicyQualifierCPS1) {
        $cps1=(jsonPolicyQualifierCPS "$certPolicyQualifierCPS1")
        $qualifier1+="," + "$cps1"
    }
    $policyId1=(jsonPolicyIdentifier "$certPolicyOid1" "$qualifier1")
    $certPolicies=(jsonCertificatePolicies "$policyId1")
    $tmpData="$certPolicies"

    if ($authorityInfoAccessMethod1 -and $authorityInfoAccessLocation1) {
        $access1=(jsonAuthInfoAccessElement "$authorityInfoAccessMethod1" "$authorityInfoAccessLocation1")
        $access=(jsonAuthInfoAccess "$access1")
        $tmpData+="," + "$access"
    }

    if ($crlType -and $crlName -and $crlReasonFlags -and $crlIssuer) {
        $crlDist=(jsonCRLDist)
        $tmpData=","+ "$crlDist"
    }

    echo ("$JSON_OTHER_EXTENSIONS_TEMPLATE" -f "$tmpData")
}


### Put it all together
$finalData=(jsonOtherExtensionsFile)
echo "$finalData"