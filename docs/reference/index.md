# Reference

Most of this section is generated from the JSON field metadata in `src/main/java/json/schema/` and the ASN.1 blocks extracted from `tcg.credential.*` Javadoc. The generated pages are meant to answer the practical questions quickly: which JSON names are accepted, which aliases still work, which OIDs or tag values they map to, and which Java/ASN.1 type ultimately receives the data.

## Specifications

- [TCG Platform Certificate Profile v2.1][spec-tcg-platform-certificate-profile-v2-1]
- [TCG Platform Certificate Profile v1.1r19][spec-tcg-platform-certificate-profile-v1-1r19]
- [PCIe-based Component Class Registry v1.0r18][spec-pcie-component-class-registry-v1-0r18]
- [SMBIOS-based Component Class Registry v1r01][spec-smbios-component-class-registry-v1r01]
- [Storage Component Class Registry v1.0r22][spec-storage-component-class-registry-v1-0r22]
- [TCG Platform Requirements for Certificates and RIMs v1.0][spec-tcg-platform-requirements-certificates-rims-v1-0]

## Content generated from source code

These pages are generated based on annotations and notes within the code base.

- [CLI Commands](cli-commands.md) — root command, subcommands, usage patterns, and option descriptions.
- [Signing Algorithms](signing-algorithms.md) — supported `--sig-profile` values, signing families, and detached-signature encodings.
- [Output Snapshots](output-snapshots.md) — first-pass examples of envelope, validation, and view output to help readers orient quickly.
- [Field Sets](field-sets.md) — every documented JSON field set, path, alias, ASN.1 mapping, description, and Mermaid graph.
- [Vocabularies](vocabularies.md) — enumerated value sets for specific fields.
- [Global ASN.1 Types](global-asn1-types.md) — JSON acceptance rules for ASN.1-backed Java types.
- [JSON Schemas](json-schemas.md) — generated Draft 2020-12 schemas for the main JSON helpers.
- [Javadocs](javadocs/index.html) — generated Java API documentation.

## Adjacent reference

- [HardwareManifest Proto](hardware-manifest-proto.md) — the `ManifestV2` protobuf used by the .NET collection libraries.

## How to regenerate

```bash
./gradlew generateDocs
```

That task runs the ASN.1 extractor and schema generator in `docs/doc-tools/`, renders the reference Markdown fragments, stages the output under `docs/reference/_generated/`, and stages Javadocs under `docs/reference/javadocs/` for MkDocs to include.

--8<-- "_includes/specification-links.md"
