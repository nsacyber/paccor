# Credential Specification Version Attribute
There is an attribute within the Platform Certificate where the exact version of specification is captured. In the
definition of the attribute you'll see three integers where each part of the specification version should be found.
PACCOR looks for that value within the Attributes JSON file and enforces requirements for that version on the rest of
the platform certificate it generates.

# TCG Certificate Specification definition
```

tcg-at-tcgCredentialSpecification = 2.23.133.4.23

tCGCredentialSpecification ATTRIBUTE ::= {
     WITH SYNTAX TCGSpecificationVersion
     ID tcg-at-tcgCredentialSpecification }

TCGSpecificationVersion ::= SEQUENCE {
     majorVersion INTEGER,
     minorVersion INTEGER,
     revision INTEGER }
```

# paccor view
PACCOR's view command will tell you the version of an existing Platform Certificate.

```text
paccor view -X <path to platform certificate>
```

# Attributes JSON

If you run the `pc_certgen` script, PACCOR will generate the Attributes JSON and choose a default version of
platform certificate to create.

First, look for this file:
```text
paccor/
  scripts/pc_testgen/localhost-policyreference.json
```

# Check the version of Platform Certificate paccor will generate and change if desired

1. Open the Attributes JSON file.
2. Look for these properties and notice how they relate to the [spec definition](#tcg-certificate-specification-definition):

```text
"TCGCREDENTIALSPECIFICATION": {
        "MAJORVERSION": "1",
        "MINORVERSION": "1",
        "REVISION": "19"
    },
```

If that doesn't exist, you'll need to create it or run the referenceoptions script.

Within that file you can find the TCGCREDENTIALSPECIFICATION key to review or change that value.

# If the JSON doesn't exist yet.

Check the referenceoptions script on your chosen platform, and review or edit these keys. Once the JSON is generated,
PACCOR will not overwrite the Attributes JSON file.

## Properties for the TCGCredentialSpecification attribute:
```text
tcgCredentialSpecificationMajorVersion
tcgCredentialSpecificationMinorVersion
tcgCredentialSpecificationRevision
```

# Script to generate the Attributes JSON on your chosen platform:
```bash
scripts/referenceoptions.sh
```

```powershell
scripts/windows/referenceoptions.ps1
```

# Steps to change the version of the platform certificate paccor will create

1. Delete the existing Attributes JSON file.
2. Edit the reference options script.
3. Set the properties to the desired platform certificate specification version.
4. Run the `pc_certgen` or `referenceoptions` script.

# Generate a 2.1 platform certificate

Follow [these steps](<#steps-to-change-the-version-of-the-platform-certificate-paccor-will-create>) and set the
properties to match the following.
```text
tcgCredentialSpecificationMajorVersion="2" # Released Jan 27, 2026
tcgCredentialSpecificationMinorVersion="1"
tcgCredentialSpecificationRevision="0"
```

# Generate a 1.1 platform certificate

Follow [these steps](<#steps-to-change-the-version-of-the-platform-certificate-paccor-will-create>) and set the
properties to match the following.
```text
tcgCredentialSpecificationMajorVersion="1" # Released April 10, 2020
tcgCredentialSpecificationMinorVersion="1"
tcgCredentialSpecificationRevision="19"
```
