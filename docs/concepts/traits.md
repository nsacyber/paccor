# Traits

The v2.x family relies heavily on polymorphic `Trait` objects to carry typed platform and component facts. That gives the model flexibility across registries and hardware classes, but it also means the deserializer has to decide what a trait means before it can choose the correct ASN.1 encoding.

## Resolution priority

When deserializing a Trait, paccor tries to identify it in this order:

1. **By ID** — match `traitId` OID against known types.
2. **By Alias** — match a registered value-property name such as `pen`, `bool`, `utf8`, or another supported alias.
3. **By Category** — match `traitCategory` OID if neither `traitId` nor an alias resolved the type.

In practice, explicit `traitId` is the clearest option when you are writing manifests by hand, alias-based traits are the most concise option when the registry already defines a well-known value type, and category-only resolution is the fallback that keeps minimally annotated manifests usable when the category is enough to infer the trait shape.

## Standard properties

| Property | Required | Notes |
| --- | --- | --- |
| `traitId` | optional | OID identifying the trait type. |
| `traitCategory` | optional | OID for the trait category. |
| `traitRegistry` | optional | OID for the registry. |
| `description` | optional | UTF-8 string. |
| `descriptionURI` | optional | IA5 string URI. |
| `traitValue` | fallback | Used if no alias property is present. |

See the [Trait global ASN.1 type entry](../reference/global-asn1-types.md) for the registered alias list and type-specific notes.

## Examples

=== "Using `pen` alias"

    ```json
    {
      "traitCategory": "2.23.133.19.2.1",
      "traitRegistry": "2.23.133.18.3.3",
      "pen": "32473"
    }
    ```

=== "Full Trait object"

    ```json
    {
      "traitId": "2.23.133.2.17",
      "traitCategory": "2.23.133.19.2.1",
      "traitRegistry": "2.23.133.18.3.3",
      "traitValue": {
        "oid": "1.3.6.1.4.1.32473"
      }
    }
    ```

=== "Representative ASN.1 shape"

    ```asn1
    Trait ::= SEQUENCE {
      traitId         OBJECT IDENTIFIER OPTIONAL,
      traitCategory   OBJECT IDENTIFIER OPTIONAL,
      traitRegistry   OBJECT IDENTIFIER OPTIONAL,
      traitValue      ANY DEFINED BY traitId OPTIONAL,
      ...
    }
    ```

## Authoring guidance

Prefer explicit `traitId` when precision matters. Prefer aliases when you want shorter manifests and you already know the registry-specific value type. Use category-only as a fallback.
