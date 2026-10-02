using HardwareManifestProto;
using OidsProto;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;

namespace HpEpsc;

/// <summary>
/// Describes the EpSC as a component, using its Device ID certificate.
/// Subject example: SERIALNUMBER=0x01775AB4FCD34768DB, CN="HP EpSC 24 ROM v21.22.00.03, L0 v21.21.01.80"
/// </summary>
internal static class EpscComponent {
    public const string Manufacturer = "HP Inc.";
    public const string ComponentClassValue = "00010008"; // TCG component class registry: Embedded processor
    public const string Sha384Oid = "2.16.840.1.101.3.4.2.2";
    private const string CommonNameOid = "2.5.4.3";
    private const string SerialNumberOid = "2.5.4.5";
    private const string RomVersionMarker = " ROM ";

    public static ComponentIdentifier FromDeviceIdCertificate(X509Certificate2 deviceId) {
        string commonName = GetSubjectAttribute(deviceId, CommonNameOid);
        int romVersion = commonName.IndexOf(RomVersionMarker, StringComparison.Ordinal);

        return new ComponentIdentifier {
            COMPONENTCLASS = new ComponentClass {
                COMPONENTCLASSREGISTRY = OidsUtils.Find(TCG_REGISTRY_COMPONENTCLASS_NODE.TcgRegistryComponentclassTcg),
                COMPONENTCLASSVALUE = ComponentClassValue
            },
            MANUFACTURER = Manufacturer,
            MODEL = romVersion < 0 ? commonName : commonName[..romVersion],
            SERIAL = GetSubjectAttribute(deviceId, SerialNumberOid),
            REVISION = romVersion < 0 ? "" : commonName[(romVersion + 1)..],
            PLATFORMCERT = HashedIdentifier(deviceId)
        };
    }

    /// <summary>
    /// Identifies the Device ID certificate in the platform certificate, so a verifier can tie the EpSC that signs evidence to this platform.
    /// </summary>
    public static CertificateIdentifier HashedIdentifier(X509Certificate2 certificate) {
        return new CertificateIdentifier {
            HASHEDCERTIDENTIFIER = new HashedCertificateIdentifier {
                HASHALG = Sha384Oid,
                HASHVALUE = Convert.ToBase64String(SHA384.HashData(CertificateParts.SignatureValue(certificate)))
            }
        };
    }

    private static string GetSubjectAttribute(X509Certificate2 certificate, string oid) {
        return certificate.SubjectName.EnumerateRelativeDistinguishedNames()
            .Where(rdn => !rdn.HasMultipleElements && rdn.GetSingleElementType().Value == oid)
            .Select(rdn => rdn.GetSingleElementValue() ?? "")
            .FirstOrDefault("");
    }
}
