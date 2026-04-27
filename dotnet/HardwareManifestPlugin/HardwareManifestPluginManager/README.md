# HardwareManifestPluginManager

[paccor](https://github.com/nsacyber/paccor) is a solution for creating the TCG Platform Certificate. The platform certificate enables traceability in the hardware supply chain of a computing device.

## Overview
[HardwareManifestPluginManager](https://github.com/nsacyber/paccor/tree/main/dotnet/HardwareManifestPlugin/HardwareManifestPluginManager) is intended to allow a program to utilize independent hardware evidence collectors via Hardware Manifest plugins. The [IHardwareManifestPlugin interface](https://www.nuget.org/packages/paccor.HardwareManifestPlugin) defines the interface used by the Provisioner to discover plugins.

## Related repositories
- [HIRS .NET Provisioner](https://github.com/nsacyber/hirs/)
- [paccor](https://github.com/nsacyber/paccor)