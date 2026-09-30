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
| `--log-level` | JUL logging level, such as `FINE`, `FINER`, `INFO`, or `WARNING`. `FINE` and more verbose levels also enable extra validation detail in `validate`. |
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
- If you omit `--sig-profile`, paccor infers the algorithm from `--issuer-cert`, or reuses the algorithm recorded in the envelope passed with `--in`. If neither is available, no signature algorithm is set and the TBS cannot be finalized.
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

For PKC generation, `--subject-key` can provide a DER or PEM `SubjectPublicKeyInfo` directly, with `--subject-dn` supplying its X.500 subject name. The DN may instead come from a platform model supplied with `--in-platform-model`. These options are mutually exclusive with `--holder-cert`.

### Delta and rebase certificates

Pass the certificate the new one builds on with `--prev-pcert`. It must name exactly one readable platform certificate. Use `previousPlatformCertificates` JSON for additional history.

`certgen` does not check the previous certificate's signature, because it may come from a different CA than `--issuer-cert`. Before issuing, check it with `validate`, using the CA that signed it:

```bash
paccor validate \
  --x509v2AttrCert example-cert.pem \
  --issuer-cert TestCA.cert.example.pem \
  --skip-component-validation
```

Add `--trust-anchor` and `--crl` to that command to also check the issuer's trust path and revocation.

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

Validates a certificate against these groups of checks:

- signature verification against `--issuer-cert`
- issuer trust path, when `--trust-anchor` is given
- revocation, when `--crl` is given
- certificate profile/specification checks
- component matching against expected JSON (`--components-json`)
- For a delta or rebase certificate, pass previous platform certificates with `--prev-pcert`  

Each check prints its own result line, followed by an overall result. The command exits `0` only when signature and component validation both pass, along with any profile, trust-anchor, or CRL checks that ran. Leaving out `--components-json` is a failure:

```text
Component validation: FAILED (--components-json not provided)
```

To check a certificate without a components file, for example right after issuing it, pass `--skip-component-validation`. Components are then not checked, and the exit code reflects the remaining checks:

```text
Component validation: SKIPPED (user requested to bypass)
```

`--skip-component-validation` cannot be combined with `--components-json`.

Component matching pairs every hardware component with a certificate component. For a delta or rebase certificate, pass the earlier certificates in the chain with `--prev-pcert` (repeatable, globs allowed). The base and deltas are applied in order to produce the expected component list. Every delta component must carry a status. A `removed` or `modified` entry must identify a component from the earlier certificates.

Previous platform certificates must be signed by `--issuer-cert` or by a certificate given with `--trust-anchor`. `--trust-anchor` also accepts intermediate CA certificates. For example, when the base certificate comes from an OEM sub-CA and the delta from a different sub-CA, pass the OEM sub-CA certificate and the shared root with `--trust-anchor`.

- Use `--component-matcher RAW` only when you specifically need strict raw comparison rather than normalized matching. The default normalized matcher ignores case and extra whitespace in manufacturer and model, and treats values such as `Unknown` and `N/A` as empty.

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

Validating a delta certificate against its base:

```bash
paccor validate \
  --x509v2AttrCert example-delta.pem \
  --issuer-cert TestCA.cert.example.pem \
  --prev-pcert example-cert.pem \
  --components-json current-components.json
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
