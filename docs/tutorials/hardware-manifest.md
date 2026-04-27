# Tutorial: Hand-craft a hardware manifest

You do not need the .NET collection tools to author a valid hardware manifest. This tutorial is for the cases where the manifest is assembled by another manufacturing system, generated from inventory data, or edited by hand during debugging.

## The shape

The JSON mirrors the [`ManifestV2`](../reference/hardware-manifest-proto.md) message in `HardwareManifest.proto`:

```text
PLATFORM        SubjectAlternativeName platform identifiers + traits
COMPONENTS[]    list of ComponentIdentifier or Trait-based entries
PROPERTIES[]    optional platform properties
COMPONENTSURI   v1.1 external component list URI
PROPERTIESURI   v1.1 external property list URI
```

See the [Hardware Manifest Fields reference](../reference/_generated/fields/hardware-manifest-fields.md) for names and aliases.

## Canonical JSON

```json
{
  "PLATFORM": {
    "PLATFORMMANUFACTURERSTR": "Sample Platform Manufacturer",
    "PLATFORMMANUFACTURERID": "1.3.6.1.4.1.32473",
    "PLATFORMMODEL": "Sample Platform Model",
    "PLATFORMVERSION": "2.1",
    "PLATFORMSERIAL": "PLATFORM-0001"
  },
  "COMPONENTS": [
    {
      "COMPONENTCLASS": {
        "COMPONENTCLASSREGISTRY": "2.23.133.18.3.1",
        "COMPONENTCLASSVALUE": "00020002"
      },
      "MANUFACTURER": "Sample Chassis Manufacturer",
      "MODEL": "Sample Chassis Model",
      "SERIAL": "CHASSIS-0001",
      "REVISION": "A0"
    },
    {
      "COMPONENTCLASS": {
        "COMPONENTCLASSREGISTRY": "2.23.133.18.3.1",
        "COMPONENTCLASSVALUE": "00040009"
      },
      "MANUFACTURER": "Sample TPM Manufacturer",
      "MODEL": "Sample TPM Model",
      "SERIAL": "TPM-0001",
      "REVISION": "1.2"
    }
  ],
  "PROPERTIES": [
    {
      "propertyName": "assetTag",
      "propertyValue": "LAB-A-42"
    }
  ]
}
```

## Aliases and mixed case

```json
{
  "platform": {
    "platformManufacturerStr": "Sample Platform Manufacturer",
    "platformManufacturerId": "1.3.6.1.4.1.32473",
    "platformModel": "Sample Platform Model",
    "platformVersion": "2.1",
    "platformSerial": "PLATFORM-0001"
  },
  "components": [
    {
      "componentClass": {
        "componentClassRegistry": "2.23.133.18.3.1",
        "componentClassValue": "00020002"
      },
      "manufacturer": "Sample Chassis Manufacturer",
      "model": "Sample Chassis Model"
    }
  ]
}
```

## Validate the shape

Use [base-bare-bones-policyreference.json][res-bare-policy], [base-bare-bones-extentions-no-ti.json][res-bare-extensions-no-ti], and [TestCA.cert.example.pem][res-testca-cert]. Download them into your working directory, or substitute your own local paths.

```bash
bin/paccor certgen \
  --issuer-cert TestCA.cert.example.pem \
  --holder-cert TCG_EK_ecc_p384_P-384_Test.pem \
  --attributes-json base-bare-bones-policyreference.json \
  --components-json manifest.json \
  --extensions-json base-bare-bones-extentions-no-ti.json \
  --sig-profile rsa-sha256 \
  --finalize \
  --out manifest-check.json
```

## Notes

The same manifest shape is used by the .NET collection libraries and by hand-authored flows. That is deliberate: one stable JSON model makes it much easier to debug collection issues and certificate issues.

--8<-- "_includes/test-resource-links.md"
