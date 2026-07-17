# Getting Started

The scripts folder contains a script `pc_certgen` that will guide you through the creation of your first platform certificate.

This demo script gathers information from the TPM, the local device, creates a key and certificate, and then produces a signed platform certificate.

The getting started guide will walk through that demo script and show you how to use paccor to create your own platform certificates.

The demo script is available for Linux and Windows. The same steps are performed on both platforms.

Before you start:

- Check [Requirements](setup/requirements.md).
- Choose a device that possesses a TPM and that you can run commands with elevated privileges.
- Pick an [Installation](setup/install.md) method for that device.
- Open a terminal and ensure you can run with elevated privileges.
- You may be interested to review the [CLI command options](reference/cli-commands.md).

**NOTE:**

    All of the paths below are relative to the root of the paccor install folder.
    
    If you installed with an RPM or DEB package, the install folder is `/opt/paccor`.
    If you unpacked a distributable zip file, the install folder is the root of the unpacked folder.
    
    Run these commands from an unpacked paccor install folder. Follow the link below to learn how to install paccor.

The main layout should look like this:

```text
paccor/
  bin/paccor
  scripts/pc_certgen.sh
  scripts/windows/pc_certgen.ps1
  scripts/pc_testgen/          # might not be created yet. The demo script creates it.
```

## Demo

__By default, this tutorial will create a v1.1 Base Platform Attribute Certificate__. You can learn how to target a different version [here](tutorials/change-target-version.md).

On Linux:

```bash
sudo scripts/pc_certgen.sh
```

On Windows:

```powershell
powershell scripts/windows/pc_certgen.ps1
```

## What happens behind the scenes

The script is intentionally linear. Each stage leaves behind files you can inspect, which is why it is the best first tour of the system.

1. Set up default variables that will be used throughout the script.
2. Create the workspace and ensure privileges are correct.
3. Retrieve an Endorsement Key (EK) Certificate from the TPM.
4. Collect information about the platform and its components into a hardware manifest JSON file.
5. Create the `attributes` JSON file that will define the certificate model.
6. Create the `extensions` JSON file that will specify certificate extensions.
7. Ensure all JSON data is valid.
8. Create a demo signing identity. That includes a key pair and a self-signed certificate to be used as the issuer CA.
9. Generate and finalize the to-be-signed (TBS) data.
10. Assemble and sign the certificate.
11. Validate the certificate, its issuer, and the hardware manifest.
12. Inspect the results.

### 1. Resolve paths and defaults

The script starts by defining where the executable and helper scripts live relative to the demo script. 

It then defines default values for:

- Platform Certificate Serial Number
- Platform Certificate Validity Period
     - `notBefore` and `notAfter` Dates
- The Algorithm for the Demo Signing Key Pair
     - Java, openssl, and Windows have different formats for specifying the same algorithm.
     - You will see more than one variable for similar information.
- The Distinguished Name for the `TestCA` demo Issuer Certificate.
- The Time Period that the `TestCA` demo Issuer Certificate is valid.

#### Put it to work

- Most of the default values are plugged directly in to the `paccor certgen` command to create the to-be-signed data.
     - Peek further into the script to see how those defaults are used.
     - Example: `paccor certgen ... -N "$serialnumber" -b "$dateNotBefore" -a "$dateNotAfter" ...`
- You can change the defaults to your own values and see the results in the generated certificate.

### 2. Create the workspace

On its first run, the script creates `scripts/pc_testgen`.

- If the directory already exists, later runs reuse it.
- If any of the EK certificate or JSON files already exist, those files are reused and those steps of the script are skipped.
- All generated material stays under `scripts/pc_testgen`.
- You can move or delete any of the files in that folder. They will be re-generated if needed.

#### Takeaway

- This command should work after running the script: `ls scripts/pc_testgen`

### 3. Retrieve the Endorsement Key certificate from the TPM

The main script to collect the EK certificate is `get_ek.sh` or `get_ek.ps1`.

Platform Certificate trust is anchored in the TPM. The certificate is cryptographically bound to the hardware
via an Endorsement Key (EK). One of the first steps of verification is confirming that the platform's TPM possesses
the EK in the certificate and that the key is not exportable, among other key attributes.

The demo script calls another paccor script (`get_ek`) to retrieve the EK certificate from the local TPM. Like all
other paccor scripts, there is a Linux and Windows version so that `pc_certgen` has the same user experience on both
platforms.

The path and filename the demo script uses the save the EK certificate is defined at the top of the demo script. 
By default, it will be saved to the workspace. 

#### Put it to work.

- The EK certificate is later placed into the TBS data by `paccor certgen`.
     - Peek further into the script to see how.
     - Example: `paccor certgen ... -e "$ekcert" ...`
     - Look for that variable name at the top of the demo script to see where to find that file.
     - After the certificate is created this command can be used to inspect the certificate, ```paccor view -X <platform certificate>```

**Inspect:**

    HIRS or any ASN.1 inspector can be used to verify the EK certificate is represented in the final Platform Certificate.

    In a Platform Attribute Certificate, the retrieved EK Certificate is placed in the Holder attribute.

    In a Platform Key Certificate, the retrieved EK certificate is placed under the Cryptographic Attributes attribute.

### 4. Collect the hardware manifest

The main script to collect the hardware manifest is `allcomponents.sh` or `allcomponents.ps1`.

This is the hardware evidence that paccor turns into component identifiers in the platform certificate. It starts with
local device facts and carries them all the way into a signed credential. By default, it uses the
component class registries to collect component information. The data sources can be changed.

By default, it collects platform and component information from SMBIOS, the PCI bus, and NVMe, ATA, and SCSI nodes.
Information includes manufacturer, model, serial number, firmware version, and more. Soon this will be expanded to
include component identity artifacts.

The hardware manifest is a JSON file that is given to `paccor certgen`. A JSON schema and a lot more details are
available in paccor docs.

#### Takeaways

- The hardware manifest can be opened in any text editor or JSON viewer.
- The hardware manifest is later placed into the TBS data by `paccor certgen`.
    - Peek further into the script to see how.
    - Example: `paccor certgen ... -c "$componentlist" ...`
    - Look for that variable name at the top of the demo script to see where to find that file.
    - After the certificate is created, ```paccor view``` will state some of the platform facts and report the number of components in the certificate.
- Verification tools like HIRS will collect a similar hardware manifest to determine if the system at runtime 
contains the same components that were captured and signed in the certificate.

### 5. Create attributes JSON

The main script to convey certificate attributes is `referenceoptions.sh` or `referenceoptions.ps1`

This file contains the non-component assertions that define the certificate model. These assertions 
become certificate attributes. Attributes have a defined structure within the certificate. paccor uses a JSON file to
enable users to convey facts that will end up as attributes in the certificate. The script bridges the format users
provide those details with the schema that can be given to paccor.

As an example, one attribute controls the version of Platform Certificate specification that paccor
will target for assembly. See [here](tutorials/change-target-version.md).

The attributes JSON file is given to `paccor certgen`. A JSON schema is available in paccor docs.

#### Takeaways

- The `attributes` JSON file can be opened in any text editor or JSON viewer.
- The attributes are later placed into the TBS data by `paccor certgen`.
  - Peek further into the script to see how.
  - Example: `paccor certgen ... -p "$policyreference" ...`
  - Look for that variable name at the top of the demo script to see where to find that file.
  - After the certificate is created, ```paccor view``` will print out a selection of attributes.

**Inspect:**

    HIRS or any ASN.1 inspector can be used to verify the attributes are represented in the final Platform Certificate.

    In a Platform Attribute Certificate, the attributes appear before the extensions sequence.

    In a Platform Key Certificate, the attributes are placed inside the Subject Directory Attributes extension.

### 6. Create extensions JSON

The main script to convey certificate extensions is `otherextensions.sh` or `otherextensions.ps1`

This file contains the information intended for certificate extensions. Extensions have a different structure than
attributes in a certificate. paccor uses a JSON file to enable users to convey facts that will end up as extensions in
the certificate. The script bridges the format users provide those details with the schema that can be given to paccor.

Certificate revocation lists and certificate policies are examples of information captured in this file.

Certain extensions are not included since they have values that are based on other aspects of the information model.
Authority Key Identifier is an example of an extension that is not included.

The extensions JSON file is given to `paccor certgen`. A JSON schema is available in paccor docs.

#### Takeaways

- The `extensions` JSON file can be opened in any text editor or JSON viewer.
- The extensions are later placed into the TBS data by `paccor certgen`.
  - Peek further into the script to see how.
  - Example: `paccor certgen ... -x "$extsettings" ...`
  - Look for that variable name at the top of the demo script to see where to find that file.
  - After the certificate is created, ```paccor view``` will print out a selection of extensions.

**Inspect:**

    HIRS or any ASN.1 inspector can be used to verify the extensions are represented in the final Platform Certificate.

    In both a Platform Attribute Certificate, and a Platform Key Certificate, the extensions appear before the
    signature sequence near the end.

### 7. Validate the generated JSON

Before paccor is invoked, the script sanity-checks the generated JSON structures. You can see the platform-specific
commands used to validate the JSON. If the helper scripts emitted malformed JSON, the script stops before certificate
generation.

#### Note

- You can run your favorite JSON validator to check any of the JSON files.

### 8. Create a demo signing identity

The demo script will generate a key pair and a self-signed certificate to be used as the issuer CA. The parameters
used to generate the material are set at the top of the demo script.

#### Put it to work

You can change any of those defaults in the script. See [this page](reference/signing-algorithms.md) to choose a different algorithm.

- The signing algorithm is part of the to-be-signed (TBS) data and therefore is given to `paccor certgen`.
  - Peek further into the script to see how.
  - Example: `paccor certgen ... --sig-profile "$paccor_sigalg" ...`
- The key and certificate are used in a local signing strategy by `paccor assemble`.
  - Peek further into the script to see how.
  - Example: `paccor assemble ... -k "$sigkey" -P "$pcsigncert" ...`
  - Windows Credential Manager will only export the key with a password. You'll see a slightly different usage in that script.
  - Example: `paccor assemble ... -k "$sigkey" --local-key-password "$pfxpassword" -P "$pcsigncert" ...`
  - Remote signing, PKCS#11 usage, and more are available. See the [Signing Algorithms](reference/signing-algorithms.md) reference page.

### 9. Generate and finalize the TBS data

All along the demo script collected information from various sources on the system or from configuration scripts.

The demo script then puts that all together and asks `paccor certgen` to finalize the to-be-signed (TBS) data.

An example of the full command: `paccor certgen -x "$extsettings" -c "$componentlist" -e "$ekcert" -p "$policyreference"
-P "$pcsigncert" -N "$serialnumber" -b "$dateNotBefore" -a "$dateNotAfter" --sig-profile "$paccor_sigalg"
-f "$tbsout" --finalize`

Example with variables substituted: `paccor certgen -x /opt/paccor/scripts/pc_testgen/extensions.json -c /opt/paccor/scripts/pc_testgen/localhost-componentlist.json -e /opt/paccor/scripts/pc_testgen/ek.cer -p /opt/paccor/scripts/pc_testgen/localhost-policyreference.json -P /opt/paccor/scripts/pc_testgen/PCTestCA.example.com.pem -N 0001 -b 20180101 -a 20380101 --sig-profile rsa-sha256 -f /opt/paccor/scripts/pc_testgen/tbs.json --finalize`

Inputs:

- EK certificate
- hardware manifest JSON
- attributes JSON
- extensions JSON
- issuer certificate
- dates, serial, and signature profile
- where to save the TBS data

If you've been following along with command usage, essentially it's  `paccor certgen ... -f "$tbsout" --finalize`

If there is not enough data within the information model, the command will fail and print out why.

Otherwise, the output file is the to-be-signed data in a JSON envelope. It is the handoff point between certificate modeling and signing.

#### Experiment

- Try removing any of the elements given to that certgen command. `--finalize` will not succeed.

### 10. Assemble and sign the certificate

The next step is to sign the TBS data and generate the platform certificate. All the parts are known. Here is where key
material is accessed and used.

Example: `paccor assemble --in "$tbsout" -k "$sigkey" -P "$pcsigncert" -f "$pccert" --pem`

Example with variables substituted: `paccor assemble --in /opt/paccor/scripts/pc_testgen/tbs.json -k /opt/paccor/scripts/pc_testgen/private.pem -P /opt/paccor/scripts/pc_testgen/PCTestCA.example.com.pem -f /opt/paccor/scripts/pc_testgen/platform_cert.20260715133924.cer --pem`

Inputs:

- TBS data
- signing method to include local key, PKCS#11 information, or remote access information
- issuer certificate
- where to save the signed certificate
- encoding choice for the signed certificate, DER (by default) or specify PEM encoding

If the certificate signature and generation succeeded, it will be written to the location specified. It can be viewed
and validated immediately.

### 11. Validate the result

The last step is to validate the generated platform certificate.

Example: `paccor validate -P "$pcsigncert" -X "$pccert" -c "$componentlist"`

Example with variables substituted: `paccor validate -P /opt/paccor/scripts/pc_testgen/PCTestCA.example.com.pem -X /opt/paccor/scripts/pc_testgen/platform_cert.20260715133924.cer -c /opt/paccor/scripts/pc_testgen/localhost-componentlist.json`

Things checked:

- the generated certificate meets the requirements of the expected version
- the expected issuer signed the generated certificate
- the hardware manifest is represented in the generated certificate

If validation fails, the script removes the generated certificate, so the workspace does not preserve a failed final artifact.

### 12. Inspect the results.

#### Files to inspect after the run

These files can be opened in any text editor:

- `scripts/pc_testgen/localhost-componentlist.json`
- `scripts/pc_testgen/localhost-policyreference.json`
- `scripts/pc_testgen/extensions.json`
- `scripts/pc_testgen/tbs.json`

#### Use paccor view to look at the certificate
- `scripts/pc_testgen/platform_cert.<timestamp>.cer`

#### Recommended inspection order:

1. Open the hardware manifest to see what facts were collected.
2. Open the policy and extensions JSON to see the non-component inputs.
3. Open `tbs.json` to see the envelope paccor actually assembled.
4. Run `paccor view --certificate scripts/pc_testgen/platform_cert.<timestamp>.cer` to inspect the resulting certificate.

## Next steps

- For profile-specific command walkthroughs instead of live collection, see [Tutorials](tutorials/index.md).
- For signing variants, see [Signing Modes](tutorials/signing-options.md).
- For supported algorithm options, see [Signing Algorithms](reference/signing-algorithms.md).
- For command-by-command help, see [CLI Commands](reference/cli-commands.md).
