package tcg.credential;

import org.bouncycastle.asn1.DERUTF8String;

public class PlatformHardwareCapabilitiesTraitTest {
    public static final PlatformHardwareCapabilities.Enumerated PLATFORM_HW_CAPS_1_ENUM = PlatformHardwareCapabilities.Enumerated.trustedExecutionEnvironment;
    public static final String PLATFORM_HW_CAPS_1_TRAIT_DESC = "PlatformHardwareCapabilities Trait Test 1";
    public static final PlatformHardwareCapabilities PLATFORM_HW_CAPS_1 = new PlatformHardwareCapabilities(PLATFORM_HW_CAPS_1_ENUM.getValue());

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test PlatformHardwareCapabilitiesTrait
     */
    public static final PlatformHardwareCapabilitiesTrait samplePlatformHardwareCapabilitiesTrait1() {
        return PlatformHardwareCapabilitiesTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(PlatformHardwareCapabilitiesTraitTest.PLATFORM_HW_CAPS_1_TRAIT_DESC))
                .traitValue(PlatformHardwareCapabilitiesTraitTest.PLATFORM_HW_CAPS_1)
                .build();
    }
}
