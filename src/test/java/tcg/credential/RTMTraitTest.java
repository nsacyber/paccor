package tcg.credential;

import org.bouncycastle.asn1.DERUTF8String;

public class RTMTraitTest {
    public static final RTMTypes.Enumerated RTM_1_ENUM = RTMTypes.Enumerated.hardwareStatic;
    public static final String RTM_1_TRAIT_DESC = "RTM Trait Test 1";
    public static final RTMTypes RTM_1 = new RTMTypes(RTM_1_ENUM.getValue());

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test RTMTrait
     */
    public static final RTMTrait sampleRTMTrait1() {
        return RTMTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(RTMTraitTest.RTM_1_TRAIT_DESC))
                .traitValue(RTMTraitTest.RTM_1)
                .build();
    }
}
