package paccor.cert;

import java.io.File;
import paccor.cert.CertKind;
import paccor.cert.PlatformCertificate;
import paccor.cert.SubjectAlternativeNameHelper;
import paccor.model.PlatformCertificateInformationModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PlatformCertificateTest {
    private static final File ATTRIBUTE_CERT =
            new File("src/test/resources/sample_testgen1/platform_cert.20250909102720.crt");
    private static final File PUBLIC_KEY_CERT =
            new File("src/test/resources/TestCA.cert.example.pem");

    @Test
    void loadAttributeCertificateExposesUnifiedMetadata() {
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
    void loadPublicKeyCertificateExposesUnifiedMetadata() {
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
}
