using System.Formats.Asn1;
using System.Security.Cryptography.X509Certificates;

namespace HpEpsc;

/// <summary>
/// Reads the parts of an X.509 certificate: Certificate ::= SEQUENCE { tbsCertificate, signatureAlgorithm, signatureValue BIT STRING }
/// </summary>
internal static class CertificateParts {
    public static byte[] TbsCertificate(X509Certificate2 certificate) {
        return CertificateSequence(certificate).ReadEncodedValue().ToArray();
    }

    /// <summary>
    /// The signature value bits. paccor hashes these bytes when it matches a hashed certificate identifier (hashOverSignatureValue).
    /// </summary>
    public static byte[] SignatureValue(X509Certificate2 certificate) {
        AsnReader sequence = CertificateSequence(certificate);
        sequence.ReadEncodedValue(); // tbsCertificate
        sequence.ReadEncodedValue(); // signatureAlgorithm
        return sequence.ReadBitString(out _);
    }

    private static AsnReader CertificateSequence(X509Certificate2 certificate) {
        return new AsnReader(certificate.RawData, AsnEncodingRules.DER).ReadSequence();
    }
}
