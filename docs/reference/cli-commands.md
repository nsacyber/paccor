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

These options are accepted by each command:

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
- a serialized `PlatformCertificateInformationModel`
- the signature `AlgorithmIdentifier`
- If you omit `--sig-profile`, paccor tries to infer the algorithm from the issuer certificate and otherwise falls back to an RSA default.
- Use [Signing Algorithms](signing-algorithms.md) to see the accepted `--sig-profile` values.

What you see when you type `paccor certgen -h`:

{%
include-markdown "./_generated/cli-help/certgen.md"
%}

Example usage:

```bash
paccor certgen \
  --kind AC
  --issuer-cert TestCA.cert.example.pem \
  --holder-cert TCG_EK_ecc_p384_P-384_Test.pem \
  --attributes-json localhost-policyreference-v2.json \
  --components-json componentswithtraits.json \
  --extensions-json extentions.json \
  --sig-profile rsa-sha256 \
  --finalize \
  --out example-envelope.json
```

## `paccor assemble`

Consumes an envelope and produces the signed certificate. You must choose exactly one signing mode:

- local private key with `--local-key`
- PKCS#11 token with `--pkcs11-module`
- remote signer with `--remote-url`
- detached signature with `--signature`
- See [Signing Modes](../tutorials/signing-options.md) for sample usage of PKCS#11, remote, detached, and local key signature modes.

- `assemble` will stop if the signature and issuer certificate do not match.
- If the envelope does not yet contain final TBS data or an algorithm identifier, `assemble` can still write a stub instead of a final credential.

What you see when you type `paccor assemble -h`:

{%
include-markdown "./_generated/cli-help/assemble.md"
%}

Example usage:

```bash
paccor assemble \
  --in example-envelope.json \
  --out example-cert.pem \
  --pem \
  --local-key TestCA.private.example.pem \
  --issuer-cert TestCA.cert.example.pem
```

## `paccor validate`

Validates a certificate against three buckets of checks:

- signature verification
- certificate profile/specification checks
- component matching against expected JSON

- Use `--component-matcher RAW` only when you specifically need strict raw comparison rather than normalized matching.

What you see when you type `paccor validate -h`:

{%
include-markdown "./_generated/cli-help/validate.md"
%}

Example usage:

```bash
paccor validate \
  --x509v2AttrCert example-cert.pem \
  --issuer-cert TestCA.cert.example.pem \
  --components-json componentswithtraits.json
```

## `paccor view`

Prints a compact summary of the certificate contents without validating against external inputs.

The output includes the certificate kind, certificate type, resolved spec version, holder or subject, issuer, serial, platform specification, platform facts, component count, and counts for previous certificates and cryptographic anchors.

What you see when you type `paccor view -h`:

{%
include-markdown "./_generated/cli-help/view.md"
%}

Example usage:

```bash
paccor view --certificate example-cert.pem
```

### `paccor`

What you see when you type `paccor -h`:

{%
  include-markdown "./_generated/cli-help/root.md"
%}
