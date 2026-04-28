# Which Spec Version?

paccor can generate certificates for three profile families: v1.0, v1.1, and the v2.x family represented in code by `CertSpecVersion.V2_0`. The right choice depends less on “latest is best” and more on what certificate type, platform-configuration shape, and downstream validator you need to interoperate with.

## Quick guide

| Profile | When to use | TCG document |
| --- | --- | --- |
| **v2.1** | Use when you want the trait-based `PlatformConfigurationV3` model and modern component registries. This is the default family for new work. | [Platform Certificate Profile 2.1](https://trustedcomputinggroup.org/wp-content/uploads/TCG_Platform_Certificate_Profile_2.1_Pub.pdf) |
| **v1.1** | Use when you need `PlatformConfigurationV2` compatibility and an attribute-certificate output. | [Platform Certificate Profile v1.1 r19](https://trustedcomputinggroup.org/wp-content/uploads/IWG_Platform_Certificate_Profile_v1p1_r19_pub_fixed.pdf) |
| **v1.0** | Use when you must interoperate with legacy `PlatformConfigurationV1` workflows or public-key-certificate outputs tied to the older model. | [Platform Attribute Credential Profile v1.0](https://trustedcomputinggroup.org/wp-content/uploads/TCG-Platform-Attribute-Credential-Profile-Version-1.0.pdf) |

## What changes between profiles

The differences readers usually care about are practical, not academic:

- **Platform configuration type**: v1.0 uses `PlatformConfiguration`, v1.1 uses `PlatformConfigurationV2`, and the v2.x family uses `PlatformConfigurationV3`.
- **Certificate kind support**: v1.1 supports attribute certificates only, while v1.0 and the v2.x family support both AC and PKC outputs.
- **Trait usage**: the v2.x family is where trait-oriented component and platform modeling becomes the normal path.
- **Compatibility behavior**: paccor can upcast or downcast JSON when targeting profiles with older or newer data. Not every v2.x construct has a lossless older equivalent.

In the codebase, 2.0 and 2.1 are treated as the same `V2_0` family because they share the `PlatformConfigurationV3` path and the same general certificate-construction flow.

## Generating each version

paccor produces certificates for any of the three profiles. See the version-target tutorials:

- [v2.1 tutorial](../tutorials/by-spec-version/v2.1.md)
- [v1.1 tutorial](../tutorials/by-spec-version/v1.1.md)
- [v1.0 tutorial](../tutorials/by-spec-version/v1.0.md)
