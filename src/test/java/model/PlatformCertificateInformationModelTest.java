package model;

import cert.CertKind;
import cert.PlatformCertificate;
import exception.JsonException;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import json.AttributesJsonHelper;
import json.HardwareManifestJsonHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PlatformCertificateInformationModelTest {
    private static final String COMP_JSON_WITH_TRAITS = "src/test/resources/tutorials/v2/componentswithtraits.json";
    private static final String COMP_JSON_BARE_BONES = "src/test/resources/bare-bones-config/base-bare-bones-componentlist.json";
    private static final String COMP_JSON_V3_ADV = "src/test/resources/sample_testgen1/localhost-componentlistv3adv.json";
    private static final String TEST_PLATFORM_CERT = "src/test/resources/sample_testgen1/platform_cert.20250909102720.crt";
    private static final String TEST_PUBLIC_KEY_CERT = "src/test/resources/TestCA.cert.example.pem";

    @Test
    public void applyHardwareManifestV11UsesV3Projection() {
        File compWithTraitsFile = Paths.get(COMP_JSON_WITH_TRAITS).toFile();
        Assertions.assertTrue(compWithTraitsFile.exists(), "Test file should exist");

        HardwareManifestJsonHelper hw = HardwareManifestJsonHelper.readComponents(compWithTraitsFile);
        Assertions.assertNotNull(hw);
        Assertions.assertNotNull(hw.pcV3());

        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        pi.applyHardwareManifest(hw);

        Assertions.assertNotNull(pi.getPlatformConfiguration(), "Canonical platform configuration should be preserved");
        Assertions.assertNotNull(pi.getPlatformConfiguration().getPlatformComponents(), "V3 components should be preserved canonically");
        Assertions.assertEquals(
                hw.pcV3().getPlatformComponents().size(),
                pi.getPlatformConfiguration().getPlatformComponents().size(),
                "Canonical component count should match V3 platform components");
    }

    @Test
    public void applyAttributesCarriesResolvedCertificateObjects() throws JsonException {
        String json = """
                {
                    "previousPlatformCertificates": [
                        { "FILE": "%s" }
                    ],
                    "cryptographicAnchors": [
                        { "FILE": "%s" }
                    ]
                }
                """.formatted(TEST_PLATFORM_CERT, TEST_PUBLIC_KEY_CERT);

        AttributesJsonHelper attributes = AttributesJsonHelper.read(json);
        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        pi.applyAttributes(attributes);

        Assertions.assertNotNull(pi.getPreviousPlatformCertificates());
        Assertions.assertNotNull(pi.getCryptographicAnchors());
        Assertions.assertEquals(1, sizeOf(pi.getPreviousPlatformCertificateObjects()));
        Assertions.assertEquals(1, sizeOf(pi.getCryptographicAnchorObjects()));

        CertificateReference previous = pi.getPreviousPlatformCertificateObjects().getFirst();
        Assertions.assertNotNull(previous.certKind());
        Assertions.assertNotNull(previous.certSpecVersion());
    }

    @Test
    public void applyHardwareManifestV20PopulatesTraitsWithoutDerivingSubjectAltName() {
        File manifestFile = Paths.get(COMP_JSON_BARE_BONES).toFile();
        Assertions.assertTrue(manifestFile.exists(), "Test file should exist");

        HardwareManifestJsonHelper hw = HardwareManifestJsonHelper.readComponents(manifestFile);
        Assertions.assertNotNull(hw);
        Assertions.assertNotNull(hw.platformTraits());

        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        pi.applyHardwareManifest(hw);

        Assertions.assertNotNull(pi.getPlatformTraits());
        Assertions.assertNull(pi.getSubjectAlternativeName());
    }

    @Test
    public void applyHardwareManifestV11PopulatesTraitsWithoutDerivingSubjectAltName() {
        File manifestFile = Paths.get(COMP_JSON_BARE_BONES).toFile();
        Assertions.assertTrue(manifestFile.exists(), "Test file should exist");

        HardwareManifestJsonHelper hw = HardwareManifestJsonHelper.readComponents(manifestFile);
        Assertions.assertNotNull(hw);

        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        pi.applyHardwareManifest(hw);

        Assertions.assertNotNull(pi.getPlatformTraits());
        Assertions.assertNull(pi.getSubjectAlternativeName());
    }

    @Test
    public void applyHardwareManifestV20AdvancedV3PopulatesTraitsWithoutDerivingSubjectAltName() {
        File manifestFile = Paths.get(COMP_JSON_V3_ADV).toFile();
        Assertions.assertTrue(manifestFile.exists(), "Test file should exist");

        HardwareManifestJsonHelper hw = HardwareManifestJsonHelper.readComponents(manifestFile);
        Assertions.assertNotNull(hw);
        Assertions.assertNotNull(hw.platformTraits());

        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        pi.applyHardwareManifest(hw);

        Assertions.assertNotNull(pi.getPlatformTraits());
        Assertions.assertNull(pi.getSubjectAlternativeName());
    }

    @Test
    void mapsAttributeCertificateIntoCanonicalModel() {
        PlatformCertificate certificate = PlatformCertificate.load(new File(TEST_PLATFORM_CERT));

        PlatformCertificateInformationModel model = PlatformCertificateInformationModel.from(certificate);

        Assertions.assertNotNull(model);
        Assertions.assertEquals(certificate.serialNumber(), model.getCertSerialNumber());
        Assertions.assertNotNull(model.getHolder());
        Assertions.assertNull(model.getSubject());
        Assertions.assertNotNull(model.getIssuer());
        Assertions.assertNotNull(model.getTcgCredentialType());
        Assertions.assertNotNull(model.getPlatformConfiguration());
        Assertions.assertNotNull(model.getPlatformTraits());
        Assertions.assertNotNull(model.getExtensions());
        Assertions.assertSame(CertKind.AC, certificate.toReference().certKind());
    }

    @Test
    void mapsPublicKeyCertificateIntoCanonicalModel() {
        PlatformCertificate certificate = PlatformCertificate.load(new File(TEST_PUBLIC_KEY_CERT));

        PlatformCertificateInformationModel model = PlatformCertificateInformationModel.from(certificate);

        Assertions.assertNotNull(model);
        Assertions.assertEquals(certificate.serialNumber(), model.getCertSerialNumber());
        Assertions.assertNull(model.getHolder());
        Assertions.assertNotNull(model.getSubject());
        Assertions.assertNotNull(model.getIssuer());
        Assertions.assertNull(model.getPlatformConfiguration());
    }

    private static int sizeOf(List<?> list) {
        return list != null ? list.size() : 0;
    }
}
