# Requirements

## Runtime

You need:

- Java 25 to run `paccor`
- Downloaded example certificates and manifests if you want to follow the deterministic tutorials with the linked repository-backed inputs

You may also want:

- `jq` and `openssl` if you plan to use [`scripts/pc_certgen.sh`](../getting-started.md)
- PowerShell if you plan to use [`scripts/windows/pc_certgen.ps1`](../getting-started.md)

## Build-time extras

- .NET 10 is required to build the `dotnet/ComponentClassRegistry/` tools from source
- .NET 10 is not required to run already-published or already-built ComponentClassRegistry tools
- Python tooling for MkDocs is only required if you are rebuilding the documentation site locally

## Privileges

Some collection flows need elevated privileges:

- Linux: TPM operations, EK retrieval, certain component collection may require `root`
- Windows: Similar to Linux, the first run of `pc_certgen.ps1` typically needs Administrator privileges for the same reasons.

See [Collect hardware with the .NET libraries](../tutorials/collect-with-dotnet.md) for collection-specific details.
