# Certificate Flows

These tutorials use repository-backed example resources so the commands are reproducible and line up with the CLI test suite.

Available flows:

- [v2.1 Attribute Certificate](v2.1-attribute-certificate.md)
- [v2.1 Public Key Certificate](v2.1-public-key-certificate.md)
- [v1.1 Attribute Certificate](v1.1-attribute-certificate.md)
- [v1.0 Attribute Certificate](v1.0-attribute-certificate.md)
- [v1.0 Public Key Certificate](v1.0-public-key-certificate.md)

Run each flow anywhere you can invoke `bin/paccor`. Download the linked input files into your working directory, or substitute your own local paths. Each flow generates an envelope first and then lets you choose a signing path. For the command variants, see [Signing Modes](../signing-options.md). For a quick chooser, see [Profile Matrix](../profile-matrix.md). If you prefer not to use the linked test issuer material, use [Generate Local Demo PKI](../../getting-started.md#generate-local-demo-pki) and substitute the resulting filenames.

If you want live host collection instead of example-driven flows, start with [Getting Started](../../getting-started.md). If you need setup details, use the [Setup](../../setup/index.md) pages.
