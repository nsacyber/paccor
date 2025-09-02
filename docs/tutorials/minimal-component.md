# Tutorial: Minimal component

This is the smallest useful hardware manifest to prove the tools work. It keeps the component list to a single entry and omits optional certificate references, addresses, and platform properties.

## Canonical JSON

```json
{
  "PLATFORM": {
    "PLATFORMMANUFACTURERSTR": "Sample Platform Manufacturer",
    "PLATFORMMANUFACTURERID": "1.3.6.1.4.1.32473",
    "PLATFORMMODEL": "Sample Platform Model",
    "PLATFORMVERSION": "1.0",
    "PLATFORMSERIAL": "ABC123"
  },
  "COMPONENTS": [
    {
      "COMPONENTCLASS": {
        "COMPONENTCLASSREGISTRY": "2.23.133.18.3.1",
        "COMPONENTCLASSVALUE": "00010002"
      },
      "MANUFACTURER": "Sample CPU Manufacturer",
      "MODEL": "Sample CPU Model",
      "SERIAL": "CPU-0001",
      "REVISION": "A1",
      "MANUFACTURERID": "1.3.6.1.4.1.32473",
      "FIELDREPLACEABLE": "true"
    }
  ]
}
```

## Compatibility JSON

```json
{
  "platform": {
    "platformManufacturerStr": "Sample Platform Manufacturer",
    "platformManufacturerId": "1.3.6.1.4.1.32473",
    "platformModel": "Sample Platform Model",
    "platformVersion": "1.0",
    "platformSerial": "ABC123"
  },
  "components": [
    {
      "componentClass": {
        "componentClassRegistry": "2.23.133.18.3.1",
        "componentClassValue": "00010002"
      },
      "manufacturer": "Sample CPU Manufacturer",
      "model": "Sample CPU Model",
      "serial": "CPU-0001",
      "revision": "A1",
      "manufacturerId": "1.3.6.1.4.1.32473",
      "fieldReplaceable": "true"
    }
  ]
}
```

## Representative ASN.1

```asn1
PlatformConfigurationV2 ::= SEQUENCE {
  componentIdentifiers  SEQUENCE OF ComponentIdentifier,
  ...
}

ComponentIdentifier ::= SEQUENCE {
  componentClass        ComponentClass,
  componentManufacturer UTF8String OPTIONAL,
  componentModel        UTF8String OPTIONAL,
  componentSerial       UTF8String OPTIONAL,
  componentRevision     UTF8String OPTIONAL,
  ...
}
```

## CLI

Use these supporting files. Download them into your working directory, or substitute your own local paths:

- [base-bare-bones-componentlist.json][res-bare-components]
- [base-bare-bones-policyreference.json][res-bare-policy]
- [base-bare-bones-extentions-no-ti.json][res-bare-extensions-no-ti]
- [TestCA.cert.example.pem][res-testca-cert]

```bash
bin/paccor certgen \
  --serial 1 \
  --not-before 20240101 \
  --not-after 20300101 \
  --issuer-cert TestCA.cert.example.pem \
  --holder-cert TCG_EK_ecc_p384_P-384_Test.pem \
  --attributes-json base-bare-bones-policyreference.json \
  --components-json minimal-manifest.json \
  --extensions-json base-bare-bones-extentions-no-ti.json \
  --sig-profile rsa-sha256 \
  --finalize \
  --out minimal-envelope.json
```

## Notes

This example intentionally omits addresses, platform properties, certificate identifiers, and trait-heavy component modeling. For the exact field mapping, jump to [Component Fields](../reference/_generated/fields/component-fields.md).

--8<-- "_includes/test-resource-links.md"
