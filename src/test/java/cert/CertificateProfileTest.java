package cert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CertificateProfileTest {

    @Test
    void testNamedConstructors() {
        CertificateProfile v1_0Ac = CertificateProfile.platformV1_0Ac();
        Assertions.assertEquals(CertSpecVersion.V1_0, v1_0Ac.specVersion());
        Assertions.assertEquals(CertKind.AC, v1_0Ac.outputType());

        CertificateProfile v1_0Pkc = CertificateProfile.platformV1_0Pkc();
        Assertions.assertEquals(CertSpecVersion.V1_0, v1_0Pkc.specVersion());
        Assertions.assertEquals(CertKind.PKC, v1_0Pkc.outputType());

        CertificateProfile v1_1 = CertificateProfile.platformV1_1();
        Assertions.assertEquals(CertSpecVersion.V1_1, v1_1.specVersion());
        Assertions.assertEquals(CertKind.AC, v1_1.outputType());

        CertificateProfile v2_0Ac = CertificateProfile.platformV2_0Ac();
        Assertions.assertEquals(CertSpecVersion.V2_0, v2_0Ac.specVersion());
        Assertions.assertEquals(CertKind.AC, v2_0Ac.outputType());

        CertificateProfile v2_0Pkc = CertificateProfile.platformV2_0Pkc();
        Assertions.assertEquals(CertSpecVersion.V2_0, v2_0Pkc.specVersion());
        Assertions.assertEquals(CertKind.PKC, v2_0Pkc.outputType());

        CertificateProfile defaultProf = CertificateProfile.defaultProfile();
        Assertions.assertEquals(v2_0Ac, defaultProf);
    }

    @Test
    void testValidation() {
        // Valid combinations
        Assertions.assertDoesNotThrow(() -> CertificateProfile.platformV1_0Ac().validate());
        Assertions.assertDoesNotThrow(() -> CertificateProfile.platformV1_0Pkc().validate());
        Assertions.assertDoesNotThrow(() -> CertificateProfile.platformV1_1().validate());
        Assertions.assertDoesNotThrow(() -> CertificateProfile.platformV2_0Ac().validate());
        Assertions.assertDoesNotThrow(() -> CertificateProfile.platformV2_0Pkc().validate());

        // Invalid combination: V1.1 does not support PKC
        CertificateProfile invalid = new CertificateProfile(CertSpecVersion.V1_1, CertKind.PKC);
        Assertions.assertFalse(invalid.isValid());
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class, invalid::validate);
        Assertions.assertTrue(ex.getMessage().contains("1.1 does not support PKC"));
    }

    @Test
    void testOfWithDefaults() {
        CertificateProfile p1 = CertificateProfile.ofWithDefaults(null, null);
        Assertions.assertEquals(CertSpecVersion.V2_0, p1.specVersion());
        Assertions.assertEquals(CertKind.AC, p1.outputType());

        CertificateProfile p2 = CertificateProfile.ofWithDefaults(CertSpecVersion.V1_0, null);
        Assertions.assertEquals(CertSpecVersion.V1_0, p2.specVersion());
        Assertions.assertEquals(CertKind.AC, p2.outputType());

        CertificateProfile p3 = CertificateProfile.ofWithDefaults(null, CertKind.PKC);
        Assertions.assertEquals(CertSpecVersion.V2_0, p3.specVersion());
        Assertions.assertEquals(CertKind.PKC, p3.outputType());
    }

    @Test
    void testGetDescription() {
        Assertions.assertEquals("Platform Certificate v1.0 (PlatformConfigurationV1, AC or PKC) Public Key Certificate",
                CertificateProfile.platformV1_0Pkc().getDescription());
        Assertions.assertEquals("Platform Certificate v1.1 (PlatformConfigurationV2, AC only) Attribute Certificate",
                CertificateProfile.platformV1_1().getDescription());
        Assertions.assertEquals("Platform Certificate v2.x (PlatformConfigurationV3, AC or PKC) Public Key Certificate",
                CertificateProfile.platformV2_0Pkc().getDescription());
    }
}
