# Getting Started

Run these commands from an unpacked paccor install folder. The main learning path, and the intended golden path through the docs, is `scripts/pc_certgen.sh` on Linux or `scripts/windows/pc_certgen.ps1` on Windows.

Before you start:

- Check [Requirements](setup/requirements.md)
- Pick an [Install](setup/install.md) path
- If you are building from source, use [Build](setup/build.md) to produce and unpack a distribution first

The install layout looks like this:

```text
paccor/
  bin/paccor
  scripts/pc_certgen.sh
  scripts/windows/pc_certgen.ps1
  scripts/pc_testgen/          # created by the helper scripts
```

## Golden path

On Linux:

```bash
bash scripts/pc_certgen.sh
```

On Windows:

```powershell
pwsh scripts/windows/pc_certgen.ps1
```

If you only run one thing, run this script. It demonstrates the full paccor flow on one machine: collect facts, build the envelope, sign it, and validate the result.

## What happens behind the scenes

The script is intentionally linear. Each stage leaves behind files you can inspect, which is why it is the best first tour of the system.

### 1. Resolve paths and defaults

The script starts by defining:

- where the helper scripts live
- where the `bin/paccor` launcher lives
- the working directory under `scripts/pc_testgen`
- default certificate dates, serial number, and demo signing settings

That is the contract for the rest of the walkthrough: all generated material stays under `scripts/pc_testgen`.

### 2. Bootstrap the workspace

On the first run, the script creates `scripts/pc_testgen`.

- Linux requires `root` for the first run because the script creates and permissions the workspace before TPM and hardware collection steps.
- Windows requires Administrator privileges on first run for the same reason.

If the directory already exists, later runs reuse it and skip completed steps when the output files are still present.

### 3. Retrieve the Endorsement Key certificate

The script writes the EK certificate to:

- `scripts/pc_testgen/ek.cer` on Linux
- `scripts/pc_testgen/ek.pem` on Windows

This is the holder-side input used later by `paccor certgen`. If the file already exists, the script reuses it.

### 4. Collect component inventory

The component collection helpers write a manifest JSON file:

- `scripts/pc_testgen/localhost-componentlist.json`

This is the hardware evidence payload that paccor turns into certificate attributes. This step shows the key value of the demo flow: it starts with local machine facts and carries them all the way into a signed credential. By default, it uses the component class registries to collect component information, but the data sources can be changed.

### 5. Create policy JSON

The policy helper writes:

- `scripts/pc_testgen/localhost-policyreference.json`

This file contains the non-component certificate assertions that become part of the certificate model. They eventually become attributes in the certificate.

### 6. Create extensions JSON

The extensions helper writes:

- `scripts/pc_testgen/extensions.json`

This is the extension-side input later passed to `paccor certgen`.

### 7. Validate the generated JSON

Before paccor is invoked, the script sanity-checks the generated JSON structures.

- Linux uses `jq`
- Windows uses `ConvertFrom-Json`

This is a cheap early failure point. If the helper scripts emitted malformed JSON, the workflow stops before certificate generation.

### 8. Create a demo signing identity

The script creates a self-signed issuer and private key strictly for demonstration:

- Linux writes `private.pem` and `PCTestCA.example.com.pem`
- Windows writes a `.p12` plus exported certificate material using the Windows certificate manager API

This is the signer later used by `paccor assemble`.

## Generate Local Demo PKI

If you do not want to use linked test issuer material in the tutorial pages, generate local demo material instead.

This is the same idea as the OpenSSL command used in `scripts/pc_certgen.sh` when it creates the demo signer.

```bash
openssl req -x509 -nodes -days 3652 \
  -newkey rsa:2048 \
  -keyout demo-issuer.key.pem \
  -out demo-issuer.cert.pem \
  -subj "/C=US/O=example.com/OU=PCTest"
```

Then substitute:

- `demo-issuer.cert.pem` anywhere a tutorial uses the issuer certificate
- `demo-issuer.key.pem` anywhere a tutorial uses the local signing key

That keeps the learning flow self-contained without relying on distributed private key material.

### 9. Build the TBS envelope

The script then runs `paccor certgen --finalize`.

Inputs:

- EK certificate
- component manifest JSON
- policy JSON
- extensions JSON
- issuer certificate
- dates, serial, and signature profile

Output:

- `scripts/pc_testgen/tbs.json`

That file is the to-be-signed envelope. It is the handoff point between certificate modeling and signing.

### 10. Assemble and sign the certificate

Next, the script runs `paccor assemble`.

- Linux signs with the generated PEM private key
- Windows signs with the generated PKCS#12 and password

Output:

- `scripts/pc_testgen/platform_cert.<timestamp>.cer`

This is the finished platform certificate.

### 11. Validate the result

Finally, the script runs `paccor validate` with:

- the generated certificate
- the generated issuer certificate
- the generated component manifest

If validation fails, the script removes the generated certificate so the workspace does not preserve a failed final artifact.

## Files to inspect after the run

The most useful outputs are:

- `scripts/pc_testgen/localhost-componentlist.json`
- `scripts/pc_testgen/localhost-policyreference.json`
- `scripts/pc_testgen/extensions.json`
- `scripts/pc_testgen/tbs.json`
- `scripts/pc_testgen/platform_cert.<timestamp>.cer`

Recommended inspection order:

1. Open the component manifest to see what facts were collected.
2. Open the policy and extensions JSON to see the non-component inputs.
3. Open `tbs.json` to see the envelope paccor actually assembled.
4. Run `bin/paccor view --certificate scripts/pc_testgen/platform_cert.<timestamp>.cer` to inspect the resulting certificate.

## Next steps

- For profile-specific command walkthroughs instead of live collection, see [Tutorials](tutorials/index.md).
- For signing variants, see [Signing Modes](tutorials/signing-options.md).
- For supported algorithm options, see [Signing Algorithms](reference/signing-algorithms.md).
- For command-by-command help, see [CLI Commands](reference/cli-commands.md).
