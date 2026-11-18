package paccor.json;

import paccor.cert.PlatformCertificate;
import paccor.model.CertificateReference;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.PolicyInformation;
import paccor.tcg.credential.TCGObjectIdentifier;

/**
 * Classifies FILE-backed cryptographic anchors into TCG certificate categories.
 *
 * Keep category detection here rather than in the deserializer so future
 * certificate inspection logic can grow without turning deserialization into
 * a large conditional block.
 */
public final class CryptographicAnchorClassifier {
    // DMTF DSP0274 (SPDM) 1.3.2, 10.8.2: id-DMTF-spdm OBJECT IDENTIFIER ::= { 1 3 6 1 4 1 412 274 }
    private static final ASN1ObjectIdentifier idDmtfSpdm = new ASN1ObjectIdentifier("1.3.6.1.4.1.412.274").intern();
    private static final ASN1ObjectIdentifier idDmtfEkuResponderAuth = idDmtfSpdm.branch("3").intern();
    private static final ASN1ObjectIdentifier idDmtfEkuRequesterAuth = idDmtfSpdm.branch("4").intern();
    private static final ASN1ObjectIdentifier idDmtfSpdmExtension = idDmtfSpdm.branch("6").intern();

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

    /**
     * TCG EK Credential Profile for TPM Family 2.0 v2.5, 3.2.16: tcg-kp-EKCertificate in the
     * extended key usage identifies an EK certificate. 3.2.9: the subject alternative name MUST
     * carry the TPM manufacturer and part number (tcg-at-tpmManufacturer, tcg-at-tpmModel) in
     * directoryName form, which also covers EK certificates that omit the optional EKU.
     */
    public static Optional<ASN1ObjectIdentifier> detectEkCertificate(
            PlatformCertificate certificate) {
        boolean ekUsage = extendedKeyUsages(certificate).contains(TCGObjectIdentifier.tcgKpEkCertificate);
        boolean tpmDeviceName = Optional.ofNullable(certificate.subjectAlternativeNames())
                .map(GeneralNames::getNames)
                .stream()
                .flatMap(Arrays::stream)
                .filter(name -> name.getTagNo() == GeneralName.directoryName)
                .map(name -> List.of(X500Name.getInstance(name.getName()).getAttributeTypes()))
                .anyMatch(types -> types.contains(TCGObjectIdentifier.tcgAtTpmManufacturer)
                        && types.contains(TCGObjectIdentifier.tcgAtTpmModel));
        return ekUsage || tpmDeviceName
                ? Optional.of(TCGObjectIdentifier.tcgTrCatEkCertificate)
                : Optional.empty();
    }

    /**
     * TCG TPM 2.0 Keys for Device Identity and Attestation v1.00 r12, 8.2: an IDevID includes the
     * certificate policy tcg-cap-verifiedTPMFixed, an IAK includes tcg-cap-verifiedTPMRestricted.
     * Restricted (IAK) certificates are not reported as IDevIDs.
     */
    public static Optional<ASN1ObjectIdentifier> detectDevIdCertificate(
            PlatformCertificate certificate) {
        Set<ASN1ObjectIdentifier> policies = certificatePolicies(certificate);
        return policies.contains(TCGObjectIdentifier.tcgCapVerifiedTPMFixed)
                && !policies.contains(TCGObjectIdentifier.tcgCapVerifiedTPMRestricted)
                ? Optional.of(TCGObjectIdentifier.tcgTrCatIdevidCertificate)
                : Optional.empty();
    }

    /**
     * TCG DICE Attestation Architecture v1.1, 6.1.1, 6.1.2 and 6.1.4: the DiceTcbInfo,
     * multiple DiceTcbInfo and UEID certificate extensions. TCG DICE Certificate Profiles v1.0,
     * 5.1.5 and 5.1.6: the tcg-dice-kp OIDs, carried as extended key usage or certificate policy.
     */
    public static Optional<ASN1ObjectIdentifier> detectDiceCertificate(
            PlatformCertificate certificate) {
        boolean diceExtension = certificate.getExtension(TCGObjectIdentifier.tcgDiceTcbInfo) != null
                || certificate.getExtension(TCGObjectIdentifier.tcgDiceMultiTcbInfo) != null
                || certificate.getExtension(TCGObjectIdentifier.tcgDiceUeid) != null;
        boolean diceKeyPurpose = extendedKeyUsages(certificate).stream()
                .anyMatch(oid -> oid.on(TCGObjectIdentifier.tcgDiceKp))
                || certificatePolicies(certificate).stream()
                .anyMatch(oid -> oid.on(TCGObjectIdentifier.tcgDiceKp));
        return diceExtension || diceKeyPurpose
                ? Optional.of(TCGObjectIdentifier.tcgTrCatDiceCertificate)
                : Optional.empty();
    }

    /**
     * DMTF DSP0274 (SPDM) 1.3.2, 10.8.2.1: the SPDM Responder and Requester Authentication extended
     * key usages. 10.8.2.2: the id-DMTF-spdm-extension certificate extension.
     */
    public static Optional<ASN1ObjectIdentifier> detectSpdmCertificate(
            PlatformCertificate certificate) {
        Set<ASN1ObjectIdentifier> usages = extendedKeyUsages(certificate);
        boolean spdm = usages.contains(idDmtfEkuResponderAuth)
                || usages.contains(idDmtfEkuRequesterAuth)
                || certificate.getExtension(idDmtfSpdmExtension) != null;
        return spdm
                ? Optional.of(TCGObjectIdentifier.tcgTrCatSpdmCertificate)
                : Optional.empty();
    }

    private static Set<ASN1ObjectIdentifier> extendedKeyUsages(PlatformCertificate certificate) {
        try {
            return Optional.ofNullable(certificate.getExtension(Extension.extendedKeyUsage))
                    .map(extension -> ExtendedKeyUsage.getInstance(extension.getParsedValue()))
                    .map(ExtendedKeyUsage::getUsages)
                    .stream()
                    .flatMap(Arrays::stream)
                    .map(KeyPurposeId::toOID)
                    .collect(Collectors.toSet());
        } catch (Exception ignored) {
            return Set.of();
        }
    }

    private static Set<ASN1ObjectIdentifier> certificatePolicies(PlatformCertificate certificate) {
        try {
            return Optional.ofNullable(certificate.getExtension(Extension.certificatePolicies))
                    .map(extension -> CertificatePolicies.getInstance(extension.getParsedValue()))
                    .map(CertificatePolicies::getPolicyInformation)
                    .stream()
                    .flatMap(Arrays::stream)
                    .map(PolicyInformation::getPolicyIdentifier)
                    .collect(Collectors.toSet());
        } catch (Exception ignored) {
            return Set.of();
        }
    }
}
