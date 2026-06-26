# Requirements

## Runtime Requirements

- JDK 25

### Demo script Linux requirements

- `jq`, `openssl`, and  `tpm2-tools` if you plan to use [`scripts/pc_certgen.sh`](../getting-started.md)

### Demo script Windows requirements

- Powershell if you plan to use [`scripts/windows/pc_certgen.ps1`](../getting-started.md)

### Tutorial requirements

- Each tutorial lists sample certificates and manifests at the top of the page that you can download and use within that tutorial.

### Optional dependencies on Linux

These dependencies are required by the `scripts/tcg_ccr.sh` script. Some of them are standard Linux packages:

- `dmidecode` >= 3.2
- `ethtool`
- `lshw`
- `nvme-cli`

## Build-time extras

- .NET 10 SDK is required to build the `dotnet/ComponentClassRegistry/` tools from source
- .NET does not need to be installed to run the tools.
- Python for MkDocs is only required if you are rebuilding the documentation site locally (uncommon).

## Privileges

Most scripts need elevated privileges:

- Linux: TPM operations, EK retrieval, certain component collection may require `root` or `sudo`
- Windows: Administrator privileges are needed for the same reasons.
