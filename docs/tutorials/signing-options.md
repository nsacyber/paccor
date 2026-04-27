# Signing Modes

All certificate-flow tutorials use the same assembly command surface. The only thing that changes is how the signature is produced.

Start with any tutorial that produces an envelope, then pick one of the signing modes below. Download the linked input files into your working directory, or substitute any local paths that fit your environment.

For CI-oriented examples, see `.github/workflows/integration.yml`.

## Shared validation command

After any successful `assemble` run, validate the result with the issuer certificate and expected components:

```bash
bin/paccor validate \
  --x509v2AttrCert example-cert.pem \
  --issuer-cert TestCA.cert.example.pem \
  --components-json componentswithtraits.json
```

Adjust `--pkcPlatformCert` and the component file as needed for the specific tutorial.

## Local key

Use this when the signing key is available as a file on disk.

```bash
bin/paccor assemble \
  --in example-envelope.json \
  --out example-cert.pem \
  --pem \
  --local-key TestCA.private.example.pem \
  --issuer-cert TestCA.cert.example.pem
```

For PKCS#12:

```bash
bin/paccor assemble \
  --in example-envelope.json \
  --out example-cert.pem \
  --pem \
  --local-key signer.p12 \
  --local-key-password-file signer.password \
  --issuer-cert issuer-cert.pem
```

## Detached signature

Use this when another system signs the TBS bytes and you only need paccor to wrap the result into the final certificate.

```bash
base64 -w0 signature.der > signature.b64

bin/paccor assemble \
  --in example-envelope.json \
  --out example-cert.pem \
  --pem \
  --signature "$(cat signature.b64)" \
  --sig-encoding der \
  --issuer-cert TestCA.cert.example.pem
```

If the detached ECDSA signature is in raw P1363 format, switch `--sig-encoding` to `p1363`.

## PKCS#11 token or HSM

Use this when the private key lives on a token or HSM exposed through PKCS#11.

Compact example:

```bash
bin/paccor assemble \
  --in example-envelope.json \
  --out example-cert.pem \
  --pem \
  --issuer-cert issuer-cert.pem \
  --pkcs11-module /usr/local/lib/your-pkcs11.so \
  --pkcs11-slot 0 \
  --pkcs11-key-alias signing-key \
  --pkcs11-pin-file pkcs11.pin
```

You can select the token by label with `--pkcs11-token-label` and the key by `--pkcs11-key-id` instead of alias.

Workflow-style example using a concrete key ID:

```bash
bin/paccor assemble \
  --in /tmp/envelope.json \
  --out /tmp/signed.cer \
  --issuer-cert /tmp/test-cert.pem \
  --pkcs11-module "$PKCS11_LIB" \
  --pkcs11-slot 0 \
  --pkcs11-key-id AABBCCDD \
  --pkcs11-pin "$PKCS11_PIN"
```

## Remote signer

Use this when signing happens behind an HTTP service.

Compact example:

```bash
bin/paccor assemble \
  --in example-envelope.json \
  --out example-cert.pem \
  --pem \
  --issuer-cert issuer-cert.pem \
  --remote-url https://signer.example/sign \
  --remote-auth bearer:REPLACE_ME \
  --remote-timeout 15000
```

Workflow-style minimal shape:

```bash
bin/paccor assemble \
  --in /tmp/envelope.json \
  --out /tmp/signed.cer \
  --issuer-cert /tmp/test-cert.pem \
  --remote-url http://127.0.0.1:8080/sign \
  --remote-auth bearer:REPLACE_ME
```

The remote signer request body contains:

- `algOid`: the signature algorithm OID from the envelope
- `payloadB64`: the Base64-encoded TBS bytes

The response is expected to return:

- `signatureB64`
- `encoding`, defaulting to `der` if omitted

## Which mode should you document first?

For reproducible tutorials:

1. Local key is the best default because it is self-contained.
2. PKCS#11 is the right second example for hardware-backed manufacturing flows.
3. Remote signing is the right third example when signing is centralized.
4. Detached signature is useful when another tool already owns the signing step.
