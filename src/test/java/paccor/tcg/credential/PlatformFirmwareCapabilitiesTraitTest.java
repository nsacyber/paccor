package paccor.tcg.credential;

import org.bouncycastle.asn1.DERUTF8String;

public class PlatformFirmwareCapabilitiesTraitTest {
    public static final PlatformFirmwareCapabilities.Enumerated PLATFORM_FW_CAPS_1_ENUM = PlatformFirmwareCapabilities.Enumerated.fwSetupAuthLocal;
    public static final String PLATFORM_FW_CAPS_1_TRAIT_DESC = "PlatformFirmwareCapabilities Trait Test 1";
    public static final PlatformFirmwareCapabilities PLATFORM_FW_CAPS_1 = new PlatformFirmwareCapabilities(PLATFORM_FW_CAPS_1_ENUM.getValue());

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test PlatformFirmwareCapabilitiesTrait
     */
    public static final PlatformFirmwareCapabilitiesTrait samplePlatformFirmwareCapabilitiesTrait1() {
        return PlatformFirmwareCapabilitiesTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(PlatformFirmwareCapabilitiesTraitTest.PLATFORM_FW_CAPS_1_TRAIT_DESC))
                .traitValue(PlatformFirmwareCapabilitiesTraitTest.PLATFORM_FW_CAPS_1)
                .build();
    }
}
