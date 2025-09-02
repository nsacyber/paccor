# Tutorial: Trait collection

This tutorial focuses on the same logical fact expressed three different ways: by a value-property alias, by an explicit `traitId`, and by a category-driven fallback.

## By alias (`pen`)

```json
{
  "traitCategory": "2.23.133.19.2.1",
  "traitRegistry": "2.23.133.18.3.3",
  "pen": "32473"
}
```

## By `traitId`

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

## By `traitCategory`

```json
{
  "traitCategory": "2.23.133.19.2.14",
  "traitRegistry": "2.23.133.18.3.3",
  "bool": "true"
}
```

## Representative ASN.1

```asn1
Trait ::= SEQUENCE {
  traitId         OBJECT IDENTIFIER OPTIONAL,
  traitCategory   OBJECT IDENTIFIER OPTIONAL,
  traitRegistry   OBJECT IDENTIFIER OPTIONAL,
  traitValue      ANY DEFINED BY traitId OPTIONAL,
  ...
}
```

## Resolution priority recap

1. By ID
2. By alias
3. By category

See [Concepts -> Traits](../concepts/traits.md) for the full discussion.
