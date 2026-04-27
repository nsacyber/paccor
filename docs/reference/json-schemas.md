# JSON Schemas

paccor publishes JSON Schema artifacts for the same helper classes the CLI reads at runtime. These schemas are useful for editor validation, CI guardrails, pre-flight checks in manufacturing pipelines, and for readers who want a machine-readable counterpart to the human-focused field-set tables.

## Schemas

| Helper | Schema | Description |
| --- | --- | --- |
| `AttributesJsonHelper` | `attributes-schema.json` | Policy and platform assertions used to build the non-component portions of the credential. |
| `ExtensionsJsonHelper` | `extensions-schema.json` | Extension-oriented JSON for AIA, certificate policies, CRL distribution points, and related fields. |
| `HardwareManifestJsonHelper` | `hardwaremanifest-schema.json` | Hardware manifest input for platform facts, component lists, and property lists. |
| Global definitions | `global-definitions.json` | Shared `$defs` collected from ASN.1-backed leaf types and reused across the helper schemas. |

The files are generated into `build/schema/` during `./gradlew generateSchemas` and are also included in the `json-schema-artifacts` workflow artifact when the workflow is configured to upload artifacts.

## Generation

The schemas are produced by `json.SchemaUtils` in `docs/doc-tools/` using [victools/jsonschema-generator](https://github.com/victools/jsonschema-generator). Run:

```bash
./gradlew generateSchemas
```

Outputs to `build/schema/`.
