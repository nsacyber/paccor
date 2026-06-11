package paccor.validator;

import paccor.cert.CertSpecVersion;
import paccor.cert.PlatformCertificate;
import java.io.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.tcg.credential.ASN1Utils;
import paccor.tcg.credential.TCGSpecificationVersion;
import paccor.validator.SpecificationValidationReport;
import paccor.validator.SpecificationValidator;

public class SpecificationValidatorTest {
    private static final File ATTRIBUTE_CERT =
            new File("src/test/resources/sample_testgen1/platform_cert.20250909102720.crt");
    private static final File PUBLIC_KEY_CERT =
            new File("src/test/resources/TestCA.cert.example.pem");

    @Test
    void inferExpectedSpecVersion_mapsSupportedFamilies() {
        Assertions.assertEquals(
                CertSpecVersion.V1_0,
                CertSpecVersion.fromTcgSpecVersion(
                        TCGSpecificationVersion.builder()
                                .majorVersion(ASN1Utils.getInteger(1))
                                .minorVersion(ASN1Utils.getInteger(0))
                                .revision(ASN1Utils.getInteger(2))
                                .build()));
        Assertions.assertEquals(
                CertSpecVersion.V1_1,
                CertSpecVersion.fromTcgSpecVersion(
                        TCGSpecificationVersion.builder()
                                .majorVersion(ASN1Utils.getInteger(1))
                                .minorVersion(ASN1Utils.getInteger(1))
                                .revision(ASN1Utils.getInteger(17))
                                .build()));
        Assertions.assertEquals(
                CertSpecVersion.V2_0,
                CertSpecVersion.fromTcgSpecVersion(TCGSpecificationVersion.builder()
                        .majorVersion(ASN1Utils.getInteger(2))
                        .minorVersion(ASN1Utils.getInteger(0))
                        .revision(ASN1Utils.getInteger(43))
                        .build()));
        Assertions.assertEquals(
                CertSpecVersion.V2_0,
                CertSpecVersion.fromTcgSpecVersion(TCGSpecificationVersion.builder()
                        .majorVersion(ASN1Utils.getInteger(2))
                        .minorVersion(ASN1Utils.getInteger(1))
                        .revision(ASN1Utils.getInteger(0))
                        .build()));
    }

    @Test
    void validate_readsDeclaredSpecificationFromAttributeCertificate() {
        SpecificationValidationReport report = SpecificationValidator.validate(PlatformCertificate.load(ATTRIBUTE_CERT));
        Assertions.assertEquals(CertSpecVersion.V1_1, report.expectedSpecVersion());
        Assertions.assertNotNull(report.actualSpecVersion());
    }

    @Test
    void validate_failsWhenCertificateLacksSpecificationMaterial() {
        SpecificationValidationReport report = SpecificationValidator.validate(PlatformCertificate.load(PUBLIC_KEY_CERT));
        Assertions.assertFalse(report.ok());
        Assertions.assertTrue(report.issues().stream().anyMatch(issue -> issue.contains("missing required")));
        Assertions.assertTrue(report.issues().stream().anyMatch(issue -> issue.contains("Could not infer")));
    }
}
