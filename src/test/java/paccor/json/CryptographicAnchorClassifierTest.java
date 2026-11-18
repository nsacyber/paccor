package paccor.json;

import java.io.File;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.cert.PlatformCertificate;
import paccor.cli.CliHelper;
import paccor.model.CertificateReference;
import paccor.tcg.credential.TCGObjectIdentifier;

class CryptographicAnchorClassifierTest {
    private static final String CA_CERT = "src/test/resources/TestCA.cert.example.pem";
    private static final String CA_KEY = "src/test/resources/TestCA.private.example.pem";
    // Real TPM 1.2 EK certificate: no EKU, TPM manufacturer/model/version in the SAN directoryName.
    private static final String EK_CERT = "src/test/resources/ek.cer";

    private static final ASN1ObjectIdentifier SPDM_EKU_RESPONDER_AUTH = new ASN1ObjectIdentifier("1.3.6.1.4.1.412.274.3");
    private static final ASN1ObjectIdentifier SPDM_EKU_REQUESTER_AUTH = new ASN1ObjectIdentifier("1.3.6.1.4.1.412.274.4");
    private static final ASN1ObjectIdentifier SPDM_EXTENSION = new ASN1ObjectIdentifier("1.3.6.1.4.1.412.274.6");

    @Test
    void nullCertificateHasNoCategory() {
        Assertions.assertEquals(Optional.empty(), CryptographicAnchorClassifier.classify(null));
        Assertions.assertNull(CryptographicAnchorClassifier.toReference(null));
    }

    @Test
    void ordinaryCertificateFallsBackToGeneric() {
        PlatformCertificate certificate = PlatformCertificate.load(new File(CA_CERT));

        Assertions.assertEquals(Optional.empty(), CryptographicAnchorClassifier.classify(certificate));
        Assertions.assertEquals(TCGObjectIdentifier.tcgTrCatGenericCertificate,
                CryptographicAnchorClassifier.toReference(certificate).traitCategory());
    }

    @Test
    void ekCertificateDetectedFromTpmSubjectAlternativeName() {
        PlatformCertificate certificate = PlatformCertificate.load(new File(EK_CERT));

        assertCategory(TCGObjectIdentifier.tcgTrCatEkCertificate, certificate);
    }

    @Test
    void ekCertificateDetectedFromExtendedKeyUsage() throws Exception {
        PlatformCertificate certificate = certificate(Extension.extendedKeyUsage,
                new ExtendedKeyUsage(KeyPurposeId.getInstance(TCGObjectIdentifier.tcgKpEkCertificate)));

        assertCategory(TCGObjectIdentifier.tcgTrCatEkCertificate, certificate);
    }

    @Test
    void idevidCertificateDetectedFromVerifiedTpmFixedPolicy() throws Exception {
        PlatformCertificate certificate = certificate(Extension.certificatePolicies, policies(
                TCGObjectIdentifier.tcgCapVerifiedTPMResidency,
                TCGObjectIdentifier.tcgCapVerifiedTPMFixed));

        assertCategory(TCGObjectIdentifier.tcgTrCatIdevidCertificate, certificate);
    }

    @Test
    void iakCertificateIsNotReportedAsIdevid() throws Exception {
        PlatformCertificate certificate = certificate(Extension.certificatePolicies, policies(
                TCGObjectIdentifier.tcgCapVerifiedTPMResidency,
                TCGObjectIdentifier.tcgCapVerifiedTPMFixed,
                TCGObjectIdentifier.tcgCapVerifiedTPMRestricted));

        Assertions.assertEquals(Optional.empty(), CryptographicAnchorClassifier.detectDevIdCertificate(certificate));
        assertCategory(TCGObjectIdentifier.tcgTrCatGenericCertificate, certificate);
    }

    @Test
    void diceCertificateDetectedFromTcbInfoExtension() throws Exception {
        assertCategory(TCGObjectIdentifier.tcgTrCatDiceCertificate,
                certificate(TCGObjectIdentifier.tcgDiceTcbInfo, new DERSequence()));
        assertCategory(TCGObjectIdentifier.tcgTrCatDiceCertificate,
                certificate(TCGObjectIdentifier.tcgDiceMultiTcbInfo, new DERSequence()));
    }

    @Test
    void diceCertificateDetectedFromKeyPurpose() throws Exception {
        assertCategory(TCGObjectIdentifier.tcgTrCatDiceCertificate,
                certificate(Extension.extendedKeyUsage,
                        new ExtendedKeyUsage(KeyPurposeId.getInstance(TCGObjectIdentifier.tcgDiceKpIdentityInit))));
        assertCategory(TCGObjectIdentifier.tcgTrCatDiceCertificate,
                certificate(Extension.certificatePolicies, policies(TCGObjectIdentifier.tcgDiceKpAttestLoc)));
    }

    @Test
    void spdmCertificateDetectedFromExtendedKeyUsage() throws Exception {
        assertCategory(TCGObjectIdentifier.tcgTrCatSpdmCertificate,
                certificate(Extension.extendedKeyUsage,
                        new ExtendedKeyUsage(KeyPurposeId.getInstance(SPDM_EKU_RESPONDER_AUTH))));
        assertCategory(TCGObjectIdentifier.tcgTrCatSpdmCertificate,
                certificate(Extension.extendedKeyUsage,
                        new ExtendedKeyUsage(KeyPurposeId.getInstance(SPDM_EKU_REQUESTER_AUTH))));
    }

    @Test
    void spdmCertificateDetectedFromSpdmExtension() throws Exception {
        assertCategory(TCGObjectIdentifier.tcgTrCatSpdmCertificate,
                certificate(SPDM_EXTENSION, new DERSequence()));
    }

    private static void assertCategory(ASN1ObjectIdentifier expected, PlatformCertificate certificate) {
        Assertions.assertNotNull(certificate);
        CertificateReference reference = CryptographicAnchorClassifier.toReference(certificate);
        Assertions.assertEquals(expected, reference.traitCategory());
    }

    private static CertificatePolicies policies(ASN1ObjectIdentifier... oids) {
        PolicyInformation[] info = new PolicyInformation[oids.length];
        for (int i = 0; i < oids.length; i++) {
            info[i] = new PolicyInformation(oids[i]);
        }
        return new CertificatePolicies(info);
    }

    private static PlatformCertificate certificate(ASN1ObjectIdentifier extension, ASN1Encodable value) throws Exception {
        X509CertificateHolder ca = CliHelper.loadCert(CA_CERT, CliHelper.x509type.CERTIFICATE);
        PrivateKeyInfo keyInfo = CliHelper.loadCert(CA_KEY, CliHelper.x509type.PRIVATE_KEY);
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(ca.getSubject(), BigInteger.TWO,
                Date.from(Instant.parse("2026-01-01T00:00:00Z")), Date.from(Instant.parse("2036-01-01T00:00:00Z")),
                ca.getSubject(), ca.getSubjectPublicKeyInfo());
        builder.addExtension(extension, false, value);
        X509CertificateHolder holder = builder.build(new JcaContentSignerBuilder("SHA256withRSA")
                .build(new JcaPEMKeyConverter().getPrivateKey(keyInfo)));
        return PlatformCertificate.load(holder.getEncoded());
    }
}
