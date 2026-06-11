package paccor.validator;

import paccor.cert.CertKind;
import paccor.cert.PlatformCertificate;
import java.io.File;
import java.util.List;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.normalization.PlatformConfigurationNormalizer;
import paccor.tcg.credential.ComponentClass;
import paccor.tcg.credential.PlatformConfigurationV2;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.UTF8StringTrait;
import paccor.tcg.credential.ComponentIdentifierV2;
import paccor.validator.ComponentMatcher;
import paccor.validator.ComponentValidator;

public class ComponentValidatorTest {
    private static final File ATTRIBUTE_CERT =
            new File("src/test/resources/sample_testgen1/platform_cert.20250909102720.crt");
    private static final File PUBLIC_KEY_CERT =
            new File("src/test/resources/TestCA.cert.example.pem");

    private ComponentIdentifierV2 mkV2(String mfr, String model) {
        return ComponentIdentifierV2.builder()
                .componentClass(new ComponentClass(TCGObjectIdentifier.tcgRegistryComponentClassTcg,
                        new DEROctetString(new byte[]{0,0,0,1})))
                .componentManufacturer(new DERUTF8String(mfr))
                .componentModel(new DERUTF8String(model))
                .build();
    }

    @Test
    void v2_rawAndNormalizedCompare() {
        PlatformConfigurationV2 expected = PlatformConfigurationV2.builder().componentIdentifier(mkV2("Unknown","M")).build();
        PlatformConfigurationV2 actualRawFail = PlatformConfigurationV2.builder().componentIdentifier(mkV2("", "M")).build();
        PlatformConfigurationV2 actualRawOk = PlatformConfigurationV2.builder().componentIdentifier(mkV2("Unknown", "M")).build();
        List<TraitMap> expectedTraits = PlatformConfigurationNormalizer.componentsForValidation(expected);
        List<TraitMap> rawFailTraits = PlatformConfigurationNormalizer.componentsForValidation(actualRawFail);
        List<TraitMap> rawOkTraits = PlatformConfigurationNormalizer.componentsForValidation(actualRawOk);

        // Raw matching treats Unknown != ""
        Assertions.assertFalse(ComponentValidator.compareComponents(expectedTraits, rawFailTraits, ComponentMatcher.RAW).ok());
        Assertions.assertTrue(ComponentValidator.compareComponents(expectedTraits, rawOkTraits, ComponentMatcher.RAW).ok());

        // normalized matching treats Unknown == ""
        Assertions.assertTrue(ComponentValidator.compareComponents(expectedTraits, rawFailTraits, ComponentMatcher.NORMALIZED).ok());
    }

    @Test
    void v3_rawCompareBasic() {
        UTF8StringTrait t1 = UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .traitValue(new DERUTF8String("A"))
                .build();
        PlatformConfigurationV3 expected = PlatformConfigurationV3.builder().platformComponent(TraitMap.builder().trait(t1).build()).build();
        PlatformConfigurationV3 actualOk = PlatformConfigurationV3.builder().platformComponent(TraitMap.builder().trait(t1).build()).build();
        Assertions.assertTrue(ComponentValidator.compareComponents(
                PlatformConfigurationNormalizer.componentsForValidation(expected),
                PlatformConfigurationNormalizer.componentsForValidation(actualOk),
                ComponentMatcher.RAW).ok());

        UTF8StringTrait t2 = UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .traitValue(new DERUTF8String("B"))
                .build();
        PlatformConfigurationV3 actualBad = PlatformConfigurationV3.builder().platformComponent(TraitMap.builder().trait(t2).build()).build();
        Assertions.assertFalse(ComponentValidator.compareComponents(
                PlatformConfigurationNormalizer.componentsForValidation(expected),
                PlatformConfigurationNormalizer.componentsForValidation(actualBad),
                ComponentMatcher.RAW).ok());
    }

    @Test
    void platformCertificateOverloadsExposeCertificateBackedData() {
        PlatformCertificate ac = PlatformCertificate.load(ATTRIBUTE_CERT);
        PlatformCertificate pkc = PlatformCertificate.load(PUBLIC_KEY_CERT);

        Assertions.assertNotNull(ac);
        Assertions.assertNotNull(pkc);
        Assertions.assertEquals(CertKind.AC, ac.certKind());
        Assertions.assertEquals(CertKind.PKC, pkc.certKind());

        Assertions.assertNotNull(ac.getAttribute(TCGObjectIdentifier.tcgAtTcgPlatformSpecification));
        Assertions.assertNotNull(ac.canonicalizedPlatformConfigurationV3());
        Assertions.assertNotNull(ac.previousPlatformCertificateTraits());

        Assertions.assertNull(pkc.getAttribute(TCGObjectIdentifier.tcgAtTcgPlatformSpecification));
        Assertions.assertNull(pkc.canonicalizedPlatformConfigurationV3());
        Assertions.assertTrue(pkc.previousPlatformCertificateTraits().isEmpty());
    }

    @Test
    void platformCertificateValidateComponentsMatchesOwnConfiguration() {
        PlatformCertificate ac = PlatformCertificate.load(ATTRIBUTE_CERT);
        PlatformConfigurationV3 actual = ac.canonicalizedPlatformConfigurationV3();

        Assertions.assertNotNull(actual);
        Assertions.assertTrue(ComponentValidator.compareComponents(
                PlatformConfigurationNormalizer.componentsForValidation(actual),
                PlatformConfigurationNormalizer.componentsForValidation(ac.canonicalizedPlatformConfigurationV3()),
                ComponentMatcher.RAW).ok());
    }
}
