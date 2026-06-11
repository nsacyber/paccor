package paccor.tcg.credential;

import org.bouncycastle.asn1.DERUTF8String;

public class PlatformFirmwareUpdateComplianceTraitTest {
    public static final PlatformFirmwareUpdateCompliance.Enumerated PLATFORM_FW_UPDATE_COMP_1_ENUM = PlatformFirmwareUpdateCompliance.Enumerated.sp800_147;
    public static final String PLATFORM_FW_UPDATE_COMP_1_TRAIT_DESC = "PlatformFirmwareUpdateCompliance Trait Test 1";
    public static final PlatformFirmwareUpdateCompliance PLATFORM_FW_UPDATE_COMP_1 = new PlatformFirmwareUpdateCompliance(PLATFORM_FW_UPDATE_COMP_1_ENUM.getValue());

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test PlatformFirmwareUpdateComplianceTrait
     */
    public static final PlatformFirmwareUpdateComplianceTrait samplePlatformFirmwareUpdateComplianceTrait1() {
        return PlatformFirmwareUpdateComplianceTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(PlatformFirmwareUpdateComplianceTraitTest.PLATFORM_FW_UPDATE_COMP_1_TRAIT_DESC))
                .traitValue(PlatformFirmwareUpdateComplianceTraitTest.PLATFORM_FW_UPDATE_COMP_1)
                .build();
    }
}
