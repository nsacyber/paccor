# Signing Algorithms

Use this page to choose a signing profile first and then confirm the exact accepted values.

## Quick choices

Start with:

| If you need... | Choose... | Why |
| --- | --- | --- |
| broad compatibility and the simplest first run | `rsa-sha256` | It is the easiest profile to reason about and the default docs path. |
| a modern software-signing path without RSA | `ecdsa-p256-sha256` or `ed25519` | Good fit when your issuer material already matches one of those families. |
| a post-quantum example from the current docs set | `ml-dsa-65` | This is the profile used in the v2.1 PKC walkthrough. |
| hardware-backed or centralized signing | keep the profile in the envelope and choose PKCS#11 or remote signing at `assemble` time | The signing mode does not replace the algorithm choice. |

If you already have an issuer certificate and want paccor to follow it, omit `--sig-profile` and let `certgen` infer the algorithm.

## Decision guide

1. Pick the certificate family and tutorial flow you need.
2. Decide whether the signer is a local key, PKCS#11 token, remote service, or detached-signature producer.
3. Choose `--sig-profile` only when you want to force the algorithm instead of inferring it from the issuer certificate.
4. Make sure the signing system actually has a key for that algorithm.

## What changes at `assemble` time

`certgen` decides the signature algorithm and writes it into the envelope. `assemble` then uses that algorithm with one signing mode:

| Signing mode | What you provide | What must match |
| --- | --- | --- |
| Local key | `--local-key` | The key file must match the envelope algorithm. |
| PKCS#11 | `--pkcs11-module` plus token/key selection | The token must expose a compatible key and signing mechanism. |
| Remote signer | `--remote-url` | The remote service must accept the envelope algorithm OID and return a compatible signature. |
| Detached signature | `--signature` | The external signer must already have used the envelope algorithm. |

## Recommended docs paths

| Scenario | Recommended profile | Recommended tutorial |
| --- | --- | --- |
| first end-to-end AC flow | `rsa-sha256` | [v2.1 Attribute Certificate](../tutorials/paccor/v2.1-attribute-certificate.md) |
| legacy interoperability | `rsa-sha256` | [v1.1 Attribute Certificate](../tutorials/paccor/v1.1-attribute-certificate.md) |
| modern PKC example | `ml-dsa-65` | [v2.1 Public Key Certificate](../tutorials/paccor/v2.1-public-key-certificate.md) |
| detached ECDSA signing | `ecdsa-p256-sha256` or `ecdsa-p384-sha384` | [Signing Modes](../tutorials/signing-options.md) |

## `certgen --sig-profile` values

These are the profile IDs currently accepted by `paccor certgen --sig-profile`.

| Profile ID | Family | Notes |
| --- | --- | --- |
| `ecdsa-p256-sha256` | ECDSA | P-256 with SHA-256 |
| `ecdsa-p384-sha384` | ECDSA | P-384 with SHA-384 |
| `ecdsa-p521-sha512` | ECDSA | P-521 with SHA-512 |
| `ed25519` | EdDSA | Pure Ed25519 |
| `rsa-pss-sha256-32` | RSA-PSS | SHA-256, salt length 32 |
| `rsa-pss-sha384-48` | RSA-PSS | SHA-384, salt length 48 |
| `rsa-pss-sha512-64` | RSA-PSS | SHA-512, salt length 64 |
| `rsa-sha256` | RSA PKCS#1 v1.5 | SHA-256 |
| `rsa-sha384` | RSA PKCS#1 v1.5 | SHA-384 |
| `rsa-sha512` | RSA PKCS#1 v1.5 | SHA-512 |
| `ml-dsa-44` | ML-DSA | Post-quantum |
| `ml-dsa-65` | ML-DSA | Post-quantum |
| `ml-dsa-87` | ML-DSA | Post-quantum |

## AlgorithmIdentifier support

When an envelope or certificate already carries an `AlgorithmIdentifier`, paccor’s signing and verification layer recognizes these families:

| Family | Supported identifiers |
| --- | --- |
| ECDSA | `ecdsa-with-SHA1`, `ecdsa-with-SHA256`, `ecdsa-with-SHA384`, `ecdsa-with-SHA512` |
| RSA PKCS#1 v1.5 | `sha1WithRSAEncryption`, `sha256WithRSAEncryption`, `sha384WithRSAEncryption`, `sha512WithRSAEncryption` |
| RSA-PSS | `id-RSASSA-PSS` with parameters carried in the algorithm identifier |
| EdDSA | `Ed25519` |
| ML-DSA | `ML-DSA-44`, `ML-DSA-65`, `ML-DSA-87` |
| ML-DSA pre-hash mapping | `ML-DSA-44-with-SHA512`, `ML-DSA-65-with-SHA512`, `ML-DSA-87-with-SHA512` are recognized in algorithm-name mapping paths |

## How paccor chooses an algorithm

`certgen` resolves the signing algorithm in this order:

1. `--sig-profile`, if provided
2. infer from `--issuer-cert`
3. default fallback when inference is not possible

For most users, inference from the issuer certificate is the safest default because it avoids choosing a profile that the signer cannot actually satisfy.

## Detached signature encodings

`paccor assemble --signature` accepts Base64 signature bytes plus a declared encoding:

| Encoding | Meaning | When to use it |
| --- | --- | --- |
| `DER` | ASN.1 DER signature bytes | Default for most detached signatures |
| `P1363` | Raw `r || s` format | Mainly for detached ECDSA signatures that were produced in IEEE P1363 form |

If you use `P1363`, paccor converts the signature to DER before assembly.

## Key-format support for local signing

`paccor assemble --local-key` supports:

- PKCS#8 PEM or DER private keys
- PKCS#1 RSA private keys
- PKCS#12 files such as `.p12`, `.pfx`, or `.pkcs12`

For PKCS#12, provide the password with `--local-key-password` or `--local-key-password-file`.

## Reference notes

- `rsa-sha256` is the baseline profile in the compatibility-oriented AC walkthroughs.
- `ml-dsa-65` is the profile used in the v2.1 PKC tutorial.
- `ecdsa-p256-sha256` and `ed25519` are useful software-signing options when your issuer material already matches them.
