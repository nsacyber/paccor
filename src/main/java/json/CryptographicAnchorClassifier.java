package json;

import cert.PlatformCertificate;
import model.CertificateReference;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import tcg.credential.TCGObjectIdentifier;

/**
 * Classifies FILE-backed cryptographic anchors into TCG certificate categories.
 *
 * Keep category detection here rather than in the deserializer so future
 * certificate inspection logic can grow without turning deserialization into
 * a large conditional block.
 */
public final class CryptographicAnchorClassifier {
    private CryptographicAnchorClassifier() {}

    public static Optional<ASN1ObjectIdentifier> classify(
            PlatformCertificate certificate) {
        if (certificate == null) {
            return Optional.empty();
        }

        return detectEkCertificate(certificate)
                .or(() -> detectDevIdCertificate(certificate))
                .or(() -> detectDiceCertificate(certificate))
                .or(() -> detectSpdmCertificate(certificate));
    }

    public static CertificateReference toReference(PlatformCertificate certificate) {
        if (certificate == null) {
            return null;
        }
        ASN1ObjectIdentifier category = classify(certificate)
                .orElse(TCGObjectIdentifier.tcgTrCatGenericCertificate);
        return certificate.toReference(category);
    }

    public static Optional<ASN1ObjectIdentifier> detectEkCertificate(
            PlatformCertificate certificate) {
        // TODO: Identify EK certificates from subject/issuer/extension policy.
        return Optional.empty();
    }

    public static Optional<ASN1ObjectIdentifier> detectDevIdCertificate(
            PlatformCertificate certificate) {
        // TODO: Identify I/LDevID certificates from relevant extension/profile markers.
        return Optional.empty();
    }

    public static Optional<ASN1ObjectIdentifier> detectDiceCertificate(
            PlatformCertificate certificate) {
        // TODO: Identify DICE certificates from relevant extension/profile markers.
        return Optional.empty();
    }

    public static Optional<ASN1ObjectIdentifier> detectSpdmCertificate(
            PlatformCertificate certificate) {
        // TODO: Identify SPDM certificates from profile or extension markers.
        return Optional.empty();
    }
}
