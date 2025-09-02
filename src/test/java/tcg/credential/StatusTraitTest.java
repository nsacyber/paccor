package tcg.credential;

import org.bouncycastle.asn1.DERUTF8String;

public class StatusTraitTest {
    public static final AttributeStatus.Enumerated STATUS_1_ENUM = AttributeStatus.Enumerated.modified;
    public static final String STATUS_1_TRAIT_DESC = "Status Trait Test 1";
    public static final AttributeStatus STATUS_1 = new AttributeStatus(STATUS_1_ENUM.getValue());

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test StatusTrait
     */
    public static final StatusTrait sampleStatusTrait1() {
        return StatusTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(StatusTraitTest.STATUS_1_TRAIT_DESC))
                .traitValue(StatusTraitTest.STATUS_1)
                .build();
    }
}
