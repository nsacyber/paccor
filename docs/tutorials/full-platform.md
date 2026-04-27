# Tutorial: Full platform with traits

This walkthrough moves beyond a single component and uses the v2.x trait model to describe both platform facts and component details.

## Canonical JSON

```json
{
  "PLATFORM": {
    "TRAITS": [
      {
        "traitCategory": "2.23.133.19.2.1",
        "traitRegistry": "2.23.133.18.3.3",
        "utf8": "ASUSTeK COMPUTER INC."
      },
      {
        "traitCategory": "2.23.133.19.2.2",
        "traitRegistry": "2.23.133.18.3.3",
        "utf8": "Zenbook UP6502ZA_Q529ZA"
      },
      {
        "traitCategory": "2.23.133.19.2.4",
        "traitRegistry": "2.23.133.18.3.3",
        "utf8": "A3A2PI88M1789543"
      }
    ]
  },
  "COMPONENTS": [
    {
      "COMPONENTCLASS": {
        "COMPONENTCLASSREGISTRY": "2.23.133.18.3.3",
        "COMPONENTCLASSVALUE": "0000810D"
      },
      "MANUFACTURER": "American Megatrends International, LLC.",
      "MODEL": "UP6502ZA.305",
      "REVISION": "0519"
    },
    {
      "TRAITS": [
        {
          "TRAITREGISTRY": "2.23.133.18.3.5",
          "COMPONENTCLASS": "02010400"
        },
        {
          "traitCategory": "2.23.133.19.2.8",
          "traitRegistry": "2.23.133.18.3.5",
          "utf8": "1C5C:1C5C:ACE42E"
        },
        {
          "traitCategory": "2.23.133.19.2.9",
          "traitRegistry": "2.23.133.18.3.5",
          "utf8": "HFM512GD3JX013N"
        }
      ]
    }
  ],
  "PROPERTIES": []
}
```

## Representative ASN.1

```asn1
PlatformConfigurationV3 ::= SEQUENCE {
  platformComponents  SEQUENCE OF TraitMap,
  platformProperties  SEQUENCE OF PlatformPropertyV2 OPTIONAL
}
```

## CLI

Use [componentswithtraits.json][res-test4-components-with-traits], [localhost-policyreference.json][res-different-policy], and [extentions.json][res-different-extensions]. Download them into your working directory, or substitute your own local paths.

```bash
bin/paccor certgen \
  --serial 1891 \
  --not-before 20240101 \
  --not-after 20300101 \
  --issuer-cert TestCA.cert.example.pem \
  --holder-cert TCG_EK_ecc_p384_P-384_Test.pem \
  --attributes-json localhost-policyreference.json \
  --components-json componentswithtraits.json \
  --extensions-json extentions.json \
  --sig-profile rsa-sha256 \
  --finalize \
  --out full-platform-envelope.json
```

## Notes

Keep the [Traits](../concepts/traits.md) page close by, and use the generated [Hardware Manifest Fields](../reference/_generated/fields/hardware-manifest-fields.md) and [Component Fields](../reference/_generated/fields/component-fields.md) pages when reviewing these manifests.

--8<-- "_includes/test-resource-links.md"
