# HardwareManifest Proto

`HardwareManifest.proto` defines the protobuf `ManifestV2` format used project-wide, including by the ComponentClassRegistry libraries. paccor's `HardwareManifestJsonHelper` reads JSON that mirrors that shape closely enough that the same manifest can move from collection tools to certificate generation without an intermediate translation layer.

> Source: [`dotnet/HardwareManifestPlugin/HardwareManifestPlugin/Resources/HardwareManifest.proto`](https://github.com/nsacyber/paccor/blob/main/dotnet/HardwareManifestPlugin/HardwareManifestPlugin/Resources/HardwareManifest.proto)

## Automatic conversion of hardware data based on the target specification version

PACCOR will automatically convert COMPONENTS and PLATFORM to or from TRAITS as necessary to meet the v2.X or v1.1
Platform Certificate requirements.

## ManifestV2

At the top level, the protobuf carries platform identifiers, component entries, optional platform properties, and optional URI indirections for hosted component or property lists.

```proto
message ManifestV2 {
  SanPlatformFields PLATFORM = 1;
  repeated ComponentIdentifier COMPONENTS = 2;
  repeated Property PROPERTIES = 3;
  UriReference COMPONENTSURI = 4;
  UriReference PROPERTIESURI = 5;
}
```

| Proto field | JSON field | paccor schema |
| --- | --- | --- |
| `PLATFORM` | `PLATFORM` (or `platform`) | [SAN Platform Fields](_generated/fields/subject-alternative-name-platform-fields.md) |
| `COMPONENTS` | `COMPONENTS` (or `components`) | [Component Fields](_generated/fields/component-fields.md) |
| `PROPERTIES` | `PROPERTIES` (or `properties`) | [Component Property Fields](_generated/fields/component-property-fields.md) |
| `COMPONENTSURI` | `COMPONENTSURI` (or `componentsUri`) | [URI Reference Fields](_generated/fields/uri-reference-fields.md) |
| `PROPERTIESURI` | `PROPERTIESURI` (or `propertiesUri`) | [URI Reference Fields](_generated/fields/uri-reference-fields.md) |

## ComponentIdentifier (proto)

`ComponentIdentifier` carries the hardware facts that eventually become `PlatformConfiguration*` content inside the certificate.

```proto
message ComponentIdentifier {
  ComponentClass COMPONENTCLASS = 1;
  string MANUFACTURER = 2;
  string MODEL = 3;
  string SERIAL = 4;
  string REVISION = 5;
  string MANUFACTURERID = 6;
  string FIELDREPLACEABLE = 7;
  repeated Address ADDRESSES = 8;
  string STATUS = 9;
  CertificateIdentifier PLATFORMCERT = 10;
  UriReference COMPONENTPLATFORMCERTURI = 11;
  CertificateIdentifier CERTIFICATEIDENTIFIER = 12;
  repeated PlatformCertificateProto.Trait TRAITS = 13;
}
```

paccor accepts the protobuf-style all-caps property names as well as the canonical Java-style aliases documented in the generated field pages. The runtime deserializers are intentionally tolerant on input so that manifests produced by older tools, mixed-case serializers, or hand-authored JSON can still be normalized into the canonical internal model.

See [Component Fields](_generated/fields/component-fields.md) for the exact field table and the backing ASN.1 type.

## Case-insensitive input

Several JSON helpers are configured to accept case-insensitive property names. In practice, that means `COMPONENTCLASS`, `componentClass`, and similar variations can still resolve successfully as long as the underlying field is known. The generated reference pages still present the canonical form first, because that is the clearest representation for new documents and long-term tooling.

## See also

- [Pipeline](../concepts/pipeline.md) — how this proto fits into the .NET to paccor flow.
- [Hand-craft a hardware manifest](../tutorials/hardware-manifest.md) — author the JSON directly.
