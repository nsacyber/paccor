package paccor.cert;

import java.io.File;
import java.nio.file.Files;
import paccor.model.PlatformCertificateInformationModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlatformCertificateTest {
    private static final File ATTRIBUTE_CERT =
            new File("src/test/resources/sample_testgen1/platform_cert.20250909102720.crt");
    private static final File PUBLIC_KEY_CERT =
            new File("src/test/resources/TestCA.cert.example.pem");

    @Test
    void testLoadAttributeCertificate() {
        PlatformCertificate certificate = PlatformCertificate.load(ATTRIBUTE_CERT);

        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.isAttributeCertificate());
        Assertions.assertFalse(certificate.isPublicKeyCertificate());
        Assertions.assertEquals(CertKind.AC, certificate.certKind());
        Assertions.assertEquals(CertKind.AC, certificate.toReference().certKind());
        Assertions.assertNotNull(certificate.serialNumber());
        Assertions.assertNotNull(certificate.subjectAlternativeNames());
        Assertions.assertNotNull(certificate.platformSpecification());
        Assertions.assertNotNull(certificate.canonicalizedPlatformConfigurationV3());
        Assertions.assertNotNull(certificate.declaredSpecification());
        Assertions.assertNotNull(SubjectAlternativeNameHelper.extractPlatformTraits(
                certificate.subjectAlternativeNames(),
                certificate.resolvedSpecVersion()));

        PlatformCertificateInformationModel info = PlatformCertificateInformationModel.from(certificate);
        Assertions.assertNotNull(info.describeIssuer());
        Assertions.assertNotNull(info.describeSubject());
    }

    @Test
    void testLoadPublicKeyCertificate() {
        PlatformCertificate certificate = PlatformCertificate.load(PUBLIC_KEY_CERT);

        Assertions.assertNotNull(certificate);
        Assertions.assertFalse(certificate.isAttributeCertificate());
        Assertions.assertTrue(certificate.isPublicKeyCertificate());
        Assertions.assertEquals(CertKind.PKC, certificate.certKind());
        Assertions.assertEquals(CertKind.PKC, certificate.toReference().certKind());
        Assertions.assertNotNull(certificate.serialNumber());
        Assertions.assertNull(certificate.subjectAlternativeNames());
        Assertions.assertNull(certificate.platformSpecification());
        Assertions.assertNull(certificate.canonicalizedPlatformConfigurationV3());
        Assertions.assertFalse(certificate.requiresPreviousPlatformCertificates());

        PlatformCertificateInformationModel info = PlatformCertificateInformationModel.from(certificate);
        Assertions.assertFalse(info.describeSubject().isBlank());
        Assertions.assertFalse(info.describeIssuer().isBlank());
    }

    @Test
    void testLoadPemAttributeCertificateFromBytes() throws Exception {
        byte[] bytes = Files.readAllBytes(ATTRIBUTE_CERT.toPath());

        PlatformCertificate certificate = PlatformCertificate.load(bytes);

        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.isAttributeCertificate());
        Assertions.assertFalse(certificate.isPublicKeyCertificate());
        Assertions.assertNull(certificate.getFile());
        Assertions.assertEquals(CertKind.AC, certificate.certKind());
        Assertions.assertNull(certificate.toReference().file());
        Assertions.assertNotNull(certificate.serialNumber());
        Assertions.assertNotNull(certificate.subjectAlternativeNames());
        Assertions.assertNotNull(certificate.platformSpecification());
        Assertions.assertNotNull(certificate.canonicalizedPlatformConfigurationV3());
    }

    @Test
    void testLoadDerAttributeCertificateFromBytes() throws Exception {
        PlatformCertificate cert = PlatformCertificate.load(ATTRIBUTE_CERT);
        Assertions.assertNotNull(cert);
        Assertions.assertTrue(cert.isAttributeCertificate());
        byte[] bytes = cert.getAttributeCertificate().getEncoded();

        PlatformCertificate certificate = PlatformCertificate.load(bytes);

        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.isAttributeCertificate());
        Assertions.assertEquals(CertKind.AC, certificate.certKind());
        Assertions.assertNotNull(certificate.getCertificateIdentifier());
        Assertions.assertNotNull(certificate.canonicalizedPlatformConfigurationV3());
        Assertions.assertArrayEquals(bytes, certificate.getAttributeCertificate().getEncoded());
    }

    @Test
    void testLoadPemPublicKeyCertificateFromBytes() throws Exception {
        byte[] bytes = Files.readAllBytes(PUBLIC_KEY_CERT.toPath());

        PlatformCertificate certificate = PlatformCertificate.load(bytes);

        Assertions.assertNotNull(certificate);
        Assertions.assertFalse(certificate.isAttributeCertificate());
        Assertions.assertTrue(certificate.isPublicKeyCertificate());
        Assertions.assertNull(certificate.getFile());
        Assertions.assertEquals(CertKind.PKC, certificate.certKind());
        Assertions.assertNull(certificate.toReference().file());
        Assertions.assertNotNull(certificate.serialNumber());
    }

    @Test
    void testLoadDerPublicKeyCertificateFromBytes() throws Exception {
        PlatformCertificate cert = PlatformCertificate.load(PUBLIC_KEY_CERT);
        Assertions.assertNotNull(cert);
        Assertions.assertTrue(cert.isPublicKeyCertificate());
        byte[] bytes = cert.getPublicKeyCertificate().getEncoded();

        PlatformCertificate certificate = PlatformCertificate.load(bytes);

        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.isPublicKeyCertificate());
        Assertions.assertEquals(CertKind.PKC, certificate.certKind());
        Assertions.assertNotNull(certificate.getCertificateIdentifier());
        Assertions.assertArrayEquals(bytes, certificate.getPublicKeyCertificate().getEncoded());
    }

    @Test
    void testCertKindProperlySetFromBytes() throws Exception {
        byte[] acBytes = Files.readAllBytes(ATTRIBUTE_CERT.toPath());
        byte[] pkcBytes = Files.readAllBytes(PUBLIC_KEY_CERT.toPath());

        Assertions.assertNotNull(PlatformCertificate.fromAttributeCertificate(acBytes));
        Assertions.assertNull(PlatformCertificate.fromAttributeCertificate(pkcBytes));
        Assertions.assertNotNull(PlatformCertificate.fromPublicKeyCertificate(pkcBytes));
        Assertions.assertNull(PlatformCertificate.fromPublicKeyCertificate(acBytes));
    }

    @Test
    void testInvalidBytesReturnsNull() {
        byte[] bytes = "not a certificate".getBytes();

        Assertions.assertNull(PlatformCertificate.load(bytes));
        Assertions.assertNull(PlatformCertificate.loadSafe(bytes));
    }
}
