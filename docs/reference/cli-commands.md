# CLI Commands

Use this page to view the command options in one place.

## Command map

| Command | Purpose | Typical next step |
| --- | --- | --- |
| `paccor certgen` | Build or update a to-be-signed envelope from JSON inputs and certificate context. | Run `paccor assemble` |
| `paccor assemble` | Turn an envelope into a signed certificate or stub. | Run `paccor validate` or `paccor view` |
| `paccor validate` | Check signature, profile structure, and component matching. | Use in CI or manufacturing checks |
| `paccor view` | Print a compact human-readable summary of a certificate. | Use during debugging or review |

## Global options

These options are accepted by the root command and the subcommands:

| Option | Meaning |
| --- | --- |
| `--log-level` | Logging verbosity. `DEBUG` and `TRACE` also enable extra validation detail in `validate`. |
| `--log-file` | Write logs to a file. |
| `-q`, `--quiet` | Suppress normal output. |
| `-h`, `--help` | Show command help. |
| `-V`, `--version` | Print the version. |

## `paccor certgen`

Builds a JSON envelope that contains:

- the certificate kind and specification version
- the finalized TBS bytes when enough input is available
- the signature `AlgorithmIdentifier`
- a serialized `PlatformCertificateInformationModel`

Typical usage:

```bash
bin/paccor certgen \
  --issuer-cert TestCA.cert.example.pem \
  --holder-cert TCG_EK_ecc_p384_P-384_Test.pem \
  --attributes-json localhost-policyreference-v2.json \
  --components-json componentswithtraits.json \
  --extensions-json extentions.json \
  --sig-profile rsa-sha256 \
  --finalize \
  --out example-envelope.json
```

Notes:

- `certgen` is the command that chooses the signing algorithm OID placed in the envelope.
- If you omit `--sig-profile`, paccor tries to infer the algorithm from the issuer certificate and otherwise falls back to an RSA default.
- Use [Signing Algorithms](signing-algorithms.md) when you need the accepted `--sig-profile` values.

## `paccor assemble`

Consumes an envelope and produces the signed certificate. You must choose exactly one signing mode:

- local private key with `--local-key`
- PKCS#11 token with `--pkcs11-module`
- remote signer with `--remote-url`
- detached signature with `--signature`

Typical usage:

```bash
bin/paccor assemble \
  --in example-envelope.json \
  --out example-cert.pem \
  --pem \
  --local-key TestCA.private.example.pem \
  --issuer-cert TestCA.cert.example.pem
```

Notes:

- `assemble` verifies the signature before writing the certificate. If the signature and issuer certificate do not match, the command fails.
- If the envelope does not yet contain final TBS data or an algorithm identifier, `assemble` can still write a stub instead of a final credential.
- Use [Signing Modes](../tutorials/signing-options.md) for compact PKCS#11, remote, detached, and local examples.

## `paccor validate`

Validates a certificate against three buckets of checks:

- signature verification
- certificate profile/specification checks
- component matching against expected JSON

Typical usage:

```bash
bin/paccor validate \
  --x509v2AttrCert example-cert.pem \
  --issuer-cert TestCA.cert.example.pem \
  --components-json componentswithtraits.json
```

Notes:

- Skipping component or signature validation is intentional and still results in a non-zero exit code.
- Use `--component-matcher RAW` only when you specifically need strict raw comparison rather than normalized matching.

## `paccor view`

Prints a compact summary of the certificate contents without validating against external inputs.

Typical usage:

```bash
bin/paccor view --certificate example-cert.pem
```

The output includes the certificate kind, certificate type, resolved spec version, holder or subject, issuer, serial, platform specification, platform facts, component count, and counts for previous certificates and cryptographic anchors.

## Generated Help

These blocks are generated from the Picocli command definitions so the raw help text stays tied to the code.

### `paccor`

{%
  include-markdown "./_generated/cli-help/root.md"
%}

### `paccor certgen`

{%
  include-markdown "./_generated/cli-help/certgen.md"
%}

### `paccor assemble`

{%
  include-markdown "./_generated/cli-help/assemble.md"
%}

### `paccor validate`

{%
  include-markdown "./_generated/cli-help/validate.md"
%}

### `paccor view`

{%
  include-markdown "./_generated/cli-help/view.md"
%}
