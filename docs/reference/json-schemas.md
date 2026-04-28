# JSON Schemas

paccor publishes JSON Schema artifacts for the same helper classes the CLI reads at runtime. These schemas are useful for editor validation and for readers who want a slightly more detailed view.

{% 
  include-markdown "./_generated/json-schemas.md"
%}

---

## Detail pages

- [Attributes Schema](_generated/schemas/attributes-schema.md)
- [Extensions Schema](_generated/schemas/extensions-schema.md)
- [Hardware Manifest Schema](_generated/schemas/hardwaremanifest-schema.md)
- [Global Definitions](_generated/schemas/global-definitions.md)

## Generation

The schemas are produced by `json.SchemaUtils` in `docs/doc-tools/` using [victools/jsonschema-generator](https://github.com/victools/jsonschema-generator). Run:

```bash
./gradlew generateSchemas
```

Outputs to `build/schema/`, and `./gradlew generateDocs` stages the JSON plus the rendered schema pages under `docs/reference/_generated/` for MkDocs publication.
