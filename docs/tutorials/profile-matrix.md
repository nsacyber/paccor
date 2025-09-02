# Profile Matrix

Use this page when you need to decide which certificate family and tutorial to start with.

| Spec family | AC / PKC | Base / Delta / Rebase | Manifest style                           | Typical signing profiles | Best starting tutorial | Notes                                                                             |
| --- | --- |-----------------------|------------------------------------------| --- | --- |-----------------------------------------------------------------------------------|
| v2.1 | AC and PKC | Base, delta, rebase   | Trait-oriented `PlatformConfigurationV3` | `rsa-sha256`, `ed25519`, `ecdsa-p256-sha256`, `ml-dsa-65` | [v2.1 AC](paccor/v2.1-attribute-certificate.md) or [v2.1 PKC](paccor/v2.1-public-key-certificate.md) | Default family when you want current modeling and the richest manifest semantics. |
| v1.1 | AC only | Base, delta           | legacy `PlatformConfigurationV2`         | `rsa-sha256` | [v1.1 AC](paccor/v1.1-attribute-certificate.md) | Use when you need legacy interoperability.                                        |
| v1.0 | AC and PKC | Base only             | obsolete `PlatformConfiguration`         | `rsa-sha256` | [v1.0 AC](paccor/v1.0-attribute-certificate.md) or [v1.0 PKC](paccor/v1.0-public-key-certificate.md) | Obsolete profile family; retained for comparison. Keep manifests conservative.    |

## Quick chooser

Start with:

1. v2.1 if you are building something new.
2. v1.1 only when an external system still expects that family.
3. v1.0 only when you are reviewing or reproducing old flows.

## Related pages

- [Signing Algorithms](../reference/signing-algorithms.md)
- [Signing Modes](signing-options.md)
- [Output Snapshots](../reference/output-snapshots.md)
