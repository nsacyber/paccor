package paccor.cert;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.tcg.credential.CertificateIdentifier;
import paccor.tcg.credential.HashedCertificateIdentifier;

/**
 * Unit tests for {@link PlatformCertificate#identifies(CertificateIdentifier)}.
 */
class CertificateIdentifierMatchTest {
    private static final File ATTRIBUTE_CERT =
            new File("src/test/resources/sample_testgen1/platform_cert.20250909102720.crt");

    private final PlatformCertificate certificate = PlatformCertificate.load(ATTRIBUTE_CERT);

    private CertificateIdentifier hashed(AlgorithmIdentifier algorithm, byte[] digest) {
        return CertificateIdentifier.builder()
                .hashedCertIdentifier(HashedCertificateIdentifier.builder()
                        .hashAlgorithm(algorithm)
                        .hashOverSignatureValue(new DEROctetString(digest))
                        .build())
                .build();
    }

    private byte[] digest(String jcaName) throws Exception {
        Assertions.assertNotNull(certificate);
        return MessageDigest.getInstance(jcaName).digest(certificate.signatureBytes());
    }

    @Test
    void hashedIdentifier_sha256_matches() throws Exception {
        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.identifies(
                hashed(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), digest("SHA-256"))));
    }

    @Test
    void hashedIdentifier_wrongDigest_doesNotMatch() throws Exception {
        Assertions.assertNotNull(certificate);
        byte[] wrong = digest("SHA-256");
        wrong[0] ^= 1;
        Assertions.assertFalse(certificate.identifies(
                hashed(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), wrong)));
    }

    @Test
    void hashedIdentifier_sha1_isAcceptedForLegacyCompatibility() throws Exception {
        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.identifies(
                hashed(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1), digest("SHA-1"))));
    }

    @Test
    void hashedIdentifier_md5_isRejected() throws Exception {
        Assertions.assertNotNull(certificate);
        Assertions.assertFalse(certificate.identifies(
                hashed(new AlgorithmIdentifier(PKCSObjectIdentifiers.md5), digest("MD5"))));
    }

    @Test
    void genericIdentifier_requiresSerialAndIssuer() {
        Assertions.assertNotNull(certificate);
        GeneralNames issuer = certificate.getAttributeCertificate().getIssuer().getNames().length > 0
                ? new GeneralNames(new GeneralName(certificate.getAttributeCertificate().getIssuer().getNames()[0]))
                : null;
        Assertions.assertNotNull(issuer);

        Assertions.assertTrue(certificate.identifies(CertificateIdentifier.builder()
                .genericCertIdentifier(new IssuerSerial(issuer, certificate.serialNumber()))
                .build()));
        Assertions.assertFalse(certificate.identifies(CertificateIdentifier.builder()
                .genericCertIdentifier(new IssuerSerial(issuer, certificate.serialNumber().add(BigInteger.ONE)))
                .build()));
    }

    @Test
    void emptyIdentifier_matchesNothing() {
        Assertions.assertNotNull(certificate);
        Assertions.assertFalse(certificate.identifies(CertificateIdentifier.builder().build()));
        Assertions.assertFalse(certificate.identifies(null));
    }
}
