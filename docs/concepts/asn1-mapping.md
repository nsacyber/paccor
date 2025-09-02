# ASN.1 Mapping

paccor exists to bridge a flexible JSON authoring experience with the strict ASN.1 structures defined by the TCG credential specifications. The input side is intentionally tolerant enough to support real collection tools and compatibility aliases. The output side is rigid: once the model is resolved, paccor will output DER (or PEM) for the certificate profile being built.

## How JSON maps to ASN.1

At a high level, paccor follows three rules:

1. Parse input JSON into strongly typed helper classes and platform model objects.
2. Normalize aliases, case differences, and alternate encodings into the canonical internal representation.
3. Encode that normalized model into the profile-specific ASN.1 structure required by the target certificate family.

This is why the docs emphasize both accepted input forms and canonical field names. The parser may accept several forms, but the certificate always has a consistent encoded outcome.

## Per-field mapping

For the field sets backed directly by a `tcg.credential.*` class, the generated reference page embeds the ASN.1 block extracted from the class Javadoc above the JSON field table. That ASN.1 block should match the relevant platform certificate specification. That gives you the exact sequence shape and the accepted JSON names in one place.

For example, the [Component Fields reference](../reference/_generated/fields/component-fields.md) shows the backing class, the ASN.1 block, and the canonical/alias property names accepted by the deserializer.

## Global ASN.1 types

The leaf types matter because many fields are wrappers around ASN.1 primitives rather than plain strings. An OID may arrive as a simple dotted string or an object. A bit string may arrive as base64, hex, or an array of bytes. A trait value may resolve through an alias, a type OID, or a category OID before paccor knows what concrete ASN.1 type to build.

See [Global ASN.1 Types](../reference/global-asn1-types.md) for the canonical table.

## Round-trip guarantees and caveats

The round trip is intentionally asymmetric:

- Input aliases normalize to canonical internal fields.
- Output is encoded from the normalized model, not from the original spelling of the JSON.
- Trait resolution is deterministic: by `traitId`, then by alias property name, then by `traitCategory`.
- Profile family matters. The same manifest may downcast or materialize into different `PlatformConfiguration*` variants depending on whether you target v1.0, v1.1, or the v2.x family.

When in doubt, use the generated field references to glean accepted input, and the per-profile tutorials as the first step to produce a particular certificate.
