# Output Snapshots

Use this page as a quick orientation aid. These are intentionally compact examples of what the main commands produce and what to notice first.

## At a glance

| Step | Main artifact | What to notice |
| --- | --- | --- |
| `certgen` | envelope JSON | certificate kind, spec version, TBS bytes, algorithm identifier |
| `assemble` | signed certificate | whether signing succeeded and which signing path was used |
| `validate` | console status lines | which checks ran, which checks were skipped, and final pass/fail |
| `view` | readable summary | issuer, holder, spec family, component count, and platform facts |

## `certgen` console output

```text
$ bin/paccor certgen ... --out example-envelope.json
Wrote TBS envelope to /path/to/example-envelope.json
```

## Envelope JSON excerpt

Representative envelope:

```json
{
  "type": "AC",
  "certSpecVersion": "V2_0",
  "tbsDerB64": "MIIB...",
  "sigAlgDerB64": "MA0GCSqGSIb3DQEBCwUA",
  "sha256OfTbs": "5cdb0b7c8c8b4d1f...",
  "platformInfoJson": "{\"issuer\":...,\"holder\":...,\"platformConfiguration\":...}"
}
```

Read it like this:

- `type`: `AC` or `PKC`
- `certSpecVersion`: the resolved profile family
- `sigAlgDerB64`: the encoded signature `AlgorithmIdentifier`
- `sha256OfTbs`: a stable checksum for the exact TBS payload

## AC vs PKC envelope shape

| Field | AC example | PKC example | Why it matters |
| --- | --- | --- | --- |
| `type` | `AC` | `PKC` | Tells `assemble`, `validate`, and `view` which certificate family is being handled. |
| `certSpecVersion` | `V2_0` | `V2_0` | Identifies the resolved spec family. |
| `sigAlgDerB64` | present | present | Carries the algorithm that the signer must satisfy. |
| `platformInfoJson` | present | present | Holds the serialized certificate model that becomes the final credential content. |

## `assemble` console output

Local-key path:

```text
$ bin/paccor assemble ... --local-key TestCA.private.example.pem
Wrote assembled credential (locally signed) to /path/to/example-cert.pem
```

Non-local signing path:

```text
$ bin/paccor assemble ... --remote-url https://signer.example/sign
Wrote assembled credential to /path/to/example-cert.pem
```

Failure example:

```text
$ bin/paccor assemble ...
Signature verification failed
```

What this tells you:

- if the command writes a credential path, the signature verified against the issuer certificate
- if the command stops at signature verification failure, the envelope, key, or issuer certificate do not agree

## `validate` console output

Passing example:

```text
$ bin/paccor validate ...
Signature validation: OK
Specification validation: OK
Components validation: OK
Platform Certificate validation: OK
```

Missing-input example:

```text
$ bin/paccor validate --x509v2AttrCert example-cert.pem
Signature validation: Skipped. No issuer certificate provided.
Specification validation: OK
Component validation: Skipped. No components JSON provided.
Platform Certificate validation: FAILED
```

That final non-zero result is intentional since some tests were skipped.

Interpretation:

- `OK` means that check ran and passed
- `Skipped` means you did not provide enough inputs for that check
- the final result is still non-zero when required checks were skipped

## `view` console output

Representative output:

```text
$ bin/paccor view --certificate example-cert.pem
Certificate File: example-cert.pem
Certificate Kind: AC
Certificate Type: base
Certificate Spec Version: V2_0
Declared TCG Credential Spec: TCG Platform Certificate Profile v2.1
Holder: CN=...
Issuer: CN=...
Serial: 1891
TCG Platform Spec: v2.1
Platform Components: 2
Platform Facts: manufacturer=ASUSTeK COMPUTER INC., model=Zenbook UP6502ZA_Q529ZA, version=?, serial=A3A2PI88M1789543, manufacturerId=?
Previous Platform Certificates: 0
Cryptographic Anchors: 0
```

Use the output in this order:

1. confirm the certificate kind and spec version
2. confirm the holder and issuer are the ones you expected
3. scan platform facts and component count for obvious mismatches
4. use `validate` afterward if you need trust and matching checks, not just a readable summary

## Fast comparison

| Command | Best use |
| --- | --- |
| `certgen` | confirm the envelope was built with the expected profile and algorithm |
| `assemble` | confirm the signing path succeeded |
| `validate` | confirm trust, structure, and expected component matching |
| `view` | confirm the certificate says what you think it says |

Use `view` when you want a fast sanity check before deeper validation.
