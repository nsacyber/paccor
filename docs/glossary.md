# Glossary

Terms used repeatedly across the paccor CLI, JSON model, ASN.1 structures, and the TCG profile documents.

## A

**ASN.1**
:   Abstract Syntax Notation One. The schema language used by X.509 and the TCG profile specifications to define the structures that paccor ultimately encodes in DER.

**Attribute Certificate**
:   An X.509 attribute certificate as defined by RFC 5755. A platform attribute certificate carries platform attributes bound to a holder certificate> It does not represent a new public key pair of its own.

## C

**Component Class Registry**
:   A registry that assigns meaning to component class values for a particular hardware domain. The TCG has published specifications that describe how to encode values from several hardware protocols such as SMBIOS, PCI, or storage. paccor's collection libraries use those registries to consistently describe hardware.

**ComponentIdentifier**
:   The ASN.1 and JSON structure used to describe an individual platform component: its class, manufacturer, model, serial, revision, addresses, optional status, and related certificate or cryptographic references. See the [Component Fields](reference/_generated/fields/component-fields.md) reference page.

## H

**HardwareManifest**
:   The JSON document paccor consumes for platform facts, component lists, and optional property lists. The JSON format defined by paccor is described in a protobuf file and is compatible project-wide and with the HIRS supply chain validation software.

**HIRS**
:   Host Integrity at Runtime and Start-up. The reference implementation family that consumes `IHardwareManifestPlugin` output and fits naturally with paccor's manifest-to-certificate workflow. The HIRS Acceptance Test features a demonstration capability and reference implementation for proving confidence in hardware and firmware configuration from device construction to device end-of-life. 

## P

**PEN (Private Enterprise Number)**
:   An enterprise identifier from the IANA Private Enterprise Numbers registry. In paccor documentation and trait examples, PEN-backed OIDs are commonly used to identify registries, manufacturers, or trait types.

**Platform Certificate**
:   A signed credential defined by the TCG Platform Certificate Profile family.

## S

**SMBIOS**
:   System Management BIOS. A firmware-defined data source commonly used to discover platform and component identity data.

## T

**TCG**
:   Trusted Computing Group. The standards body that publishes the platform certificate, component registry, TPM, and related specifications that paccor implements.

**Trait**
:   A polymorphic ASN.1 and JSON structure introduced in the v2.x model to encode typed facts about a platform or component. A trait may resolve by explicit type OID, by alias property name, or by category OID. See [Traits](concepts/traits.md).
