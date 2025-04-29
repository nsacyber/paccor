paccor is a popular solution for creating the [TCG Platform Certificate](https://trustedcomputinggroup.org/resource/tcg-platform-certificate-profile/). The platform certificate enables traceability of components throughout the hardware supply chain of a computing device. 

The [Storage Component Class Registry](https://trustedcomputinggroup.org/resource/storage-component-class-registry/) specifies how to encode information from ATA-, SCSI-, and NVMe-enabled components into the platform certificate. Note: Currently, this library will only collect NVMe components. On Windows, it requires the Intel RST driver. Greater support for NVMe on Windows is planned as well as support for ATA and SCSI drives.

This library can be used in subsequent programs to perform collection of information from systems according to the specification. 

A command line program is available for this library on [paccor's GitHub page](https://github.com/nsacyber/paccor).

This library is also a hardware manifest plugin that allows paccor's hardware evidence collection methods to be integrated into any .NET program that can [manage](https://www.nuget.org/packages/paccor.HardwareManifestPluginManager) the [IHardwareManifestPlugin interface](https://www.nuget.org/packages/paccor.HardwareManifestPlugin).

See the [HIRS .NET Provisioner](https://github.com/nsacyber/hirs/) code on github for an example usage of this as a plugin.