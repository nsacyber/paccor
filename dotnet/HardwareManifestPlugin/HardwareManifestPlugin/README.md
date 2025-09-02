# HardwareManifestPlugin

[paccor](https://github.com/nsacyber/paccor) is a solution for creating the TCG Platform Certificate. The platform certificate enables traceability in the hardware supply chain of a computing device.

## Overview
[HardwareManifestPlugin](https://github.com/nsacyber/paccor/tree/main/dotnet/HardwareManifestPlugin/HardwareManifestPlugin) is intended to extend the [HIRS .NET Provisioner](https://github.com/nsacyber/hirs/). The `IHardwareManifestPlugin` interface enables a custom hardware evidence collector to be plugged into the Provisioner, and the Provisioner will only consider collectors that implement this interface.

## Sample implementations (NuGet)
The following NuGet packages provide production-ready implementations of `IHardwareManifestPlugin` that map to TCG Component Class Registries:

| NuGet package | TCG Component Class Registry |
| --- | --- |
| [paccor.smbios](https://www.nuget.org/packages/paccor.smbios) | [SMBIOS Component Class Registry](https://trustedcomputinggroup.org/resource/smbios-based-component-class-registry/) |
| [paccor.pcie](https://www.nuget.org/packages/paccor.pcie) | [PCI-E Component Class Registry](https://trustedcomputinggroup.org/resource/pcie-based-component-class-registry/) |
| [paccor.storage](https://www.nuget.org/packages/paccor.storage) | [Storage Component Class Registry](https://trustedcomputinggroup.org/resource/storage-component-class-registry/) |
| [paccor.paccor_scripts](https://www.nuget.org/packages/paccor.paccor_scripts) | [TCG Component Class Registry](https://trustedcomputinggroup.org/resource/storage-component-class-registry/) (earlier-generation collection method)<br/>Does not define how to translate hardware identifiers into the platform certificate and is therefore significantly less interoperable |

## Related repositories
- [paccor](https://github.com/nsacyber/paccor)
- [HIRS .NET Provisioner](https://github.com/nsacyber/hirs/)