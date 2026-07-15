package paccor.cert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.model.PlatformCertificateInformationModel;
import paccor.tcg.credential.*;
import java.util.List;
import org.bouncycastle.asn1.ASN1Boolean;

public class TbbSecurityAssertionsValidationTest {

    @Test
    void validateTbbSecurityAssertions_v20_flagsUnsupportedTraits() {
        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        
        // Add an unsupported trait to TBB Security Assertions (BooleanTrait is not allowed)
        TraitMap traits = TraitMap.builder()
                .trait(FIPSLevelTrait.builder()
                        .traitValue(FIPSLevel.builder()
                                .version(new org.bouncycastle.asn1.DERIA5String("140-2"))
                                .level(new SecurityLevel(1))
                                .build())
                        .build())
                .trait(BooleanTrait.builder()
                        .traitId(TCGObjectIdentifier.tcgTrIdIso9000Level) // reusing an OID for simplicity
                        .traitValue(ASN1Boolean.TRUE)
                        .build())
                .build();
        
        pi.setTbbSecurityAssertions(TBBSecurityAssertions.builder().traits(traits).build());

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV2_0Ac(), pi);
        
        Assertions.assertTrue(issues.stream().anyMatch(s -> s.contains("TBBSecurityAssertions contains unsupported trait type: BooleanTrait")),
                "Should have flagged unsupported trait type, but got: " + issues);
    }

    @Test
    void validateTbbSecurityAssertions_v20_acceptsAllowedTraits() {
        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        
        TraitMap traits = TraitMap.builder()
                .trait(FIPSLevelTrait.builder()
                        .traitValue(FIPSLevel.builder()
                                .version(new org.bouncycastle.asn1.DERIA5String("140-2"))
                                .level(new SecurityLevel(1))
                                .build())
                        .build())
                .trait(RTMTrait.builder().traitValue(new RTMTypes(1)).build())
                .build();
        
        pi.setTbbSecurityAssertions(TBBSecurityAssertions.builder().traits(traits).build());

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV2_0Ac(), pi);
        
        Assertions.assertFalse(issues.stream().anyMatch(s -> s.contains("TBBSecurityAssertions contains unsupported trait type")),
                "Should have accepted allowed trait types, but got: " + issues);
    }
    
    @Test
    void validateTbbSecurityAssertions_v10_doesNotFlagTraits() {
        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        
        // V1.0 doesn't use TraitMap for TBBSecurityAssertions in our validation logic (it uses the legacy structure)
        // Even if we provide traits, the current validation for V1.0 doesn't check them as a TraitMap.
        TraitMap traits = TraitMap.builder()
                .trait(BooleanTrait.builder().traitValue(ASN1Boolean.TRUE).build())
                .build();
        
        pi.setTbbSecurityAssertions(TBBSecurityAssertions.builder().traits(traits).build());

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV1_0Ac(), pi);
        
        Assertions.assertFalse(issues.stream().anyMatch(s -> s.contains("TBBSecurityAssertions contains unsupported trait type")),
                "Should not have flagged traits for V1.0, but got: " + issues);
    }

    @Test
    void testToTraitMapConversion() {
        TBBSecurityAssertions v1 = TBBSecurityAssertions.builder()
                .fipsLevel(FIPSLevel.builder()
                        .version(new org.bouncycastle.asn1.DERIA5String("140-2"))
                        .level(new SecurityLevel(1))
                        .build())
                .build();

        TraitMap traits = v1.toTraitMap();
        Assertions.assertTrue(traits.containsKey(FIPSLevelTrait.class));
        Assertions.assertEquals(1, traits.get(FIPSLevelTrait.class).size());
    }
}
