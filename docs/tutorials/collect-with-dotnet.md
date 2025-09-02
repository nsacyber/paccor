# Collect hardware data with the component class registry libraries

The ComponentClassRegistry libraries gather hardware facts from a live system and emit the same JSON data that plugs directly into paccor.

This page assumes two things:

- you have a source checkout for the `.NET` projects under `dotnet/`
- you also have a runnable paccor install layout with `bin/paccor`

## Build and runtime expectations

- Building these projects from source requires the .NET SDK
- Running already-built or already-published tools does not require the .NET SDK
- For setup details, see [Requirements](../setup/requirements.md) and [Build](../setup/build.md)

## Available libraries

| NuGet package | Reads from | TCG registry |
| --- | --- | --- |
| [`paccor.smbios`](https://www.nuget.org/packages/paccor.smbios) | SMBIOS tables | [SMBIOS Component Class Registry](https://trustedcomputinggroup.org/resource/smbios-based-component-class-registry/) |
| [`paccor.pcie`](https://www.nuget.org/packages/paccor.pcie) | PCI Express devices | [PCI-E Component Class Registry](https://trustedcomputinggroup.org/resource/pcie-based-component-class-registry/) |
| [`paccor.storage`](https://www.nuget.org/packages/paccor.storage) | NVMe / ATA / SCSI devices | [Storage Component Class Registry](https://trustedcomputinggroup.org/resource/storage-component-class-registry/) |

## CLI usage

The CLI projects live under `dotnet/ComponentClassRegistry/{SmbiosCli,PcieCli,StorageCli}`.

These commands show how to run the tools individually. The output can be combined as the hardware manifest JSON file.

### SMBIOS

```bash
dotnet run --project dotnet/ComponentClassRegistry/SmbiosCli -- --print-v2
```

### PCI-E

```bash
dotnet run --project dotnet/ComponentClassRegistry/PcieCli -- --print-v2 --components-only
```

### Storage

```bash
dotnet run --project dotnet/ComponentClassRegistry/StorageCli -- --print-v2 --components-only
```

On Linux, SMBIOS and PCI-E collection require elevated privileges. Storage collection requires elevated privileges on both Linux and Windows.

## Hand-off to paccor

Use [TestCA.cert.example.pem][res-testca-cert], [localhost-policyreference.json][res-different-policy], and [extentions.json][res-different-extensions] for the signing-side demo inputs. Download them into your working directory, or substitute your own local paths.

```bash
dotnet run --project dotnet/ComponentClassRegistry/SmbiosCli -- --print-v2 > manifest.json

bin/paccor certgen \
  --issuer-cert TestCA.cert.example.pem \
  --holder-cert TCG_EK_ecc_p384_P-384_Test.pem \
  --attributes-json localhost-policyreference.json \
  --components-json manifest.json \
  --extensions-json extentions.json \
  --sig-profile rsa-sha256 \
  --finalize \
  --out manifest-envelope.json
```

## Plugin integration with HIRS

The same manifest-producing libraries can be loaded through the `IHardwareManifestPlugin`. This plugin is also used by the [HIRS .NET Provisioner](https://github.com/nsacyber/hirs/) so that it accepts the same JSON format.

See the [Pipeline concept page](../concepts/pipeline.md) for the full picture.

--8<-- "_includes/test-resource-links.md"
