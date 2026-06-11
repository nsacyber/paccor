package paccor.tcg.credential;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.DERUTF8String;

public class BooleanTraitTest {
    public static final String BOOL_1_TRAIT_DESC = "Boolean Trait Test 1";
    public static final boolean BOOL_1_VALUE = true;
    public static final ASN1Boolean BOOL_1 = ASN1Boolean.getInstance(BOOL_1_VALUE);

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test BooleanTrait
     */
    public static final BooleanTrait sampleBooleanTrait1() {
        return BooleanTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(BooleanTraitTest.BOOL_1_TRAIT_DESC))
                .traitValue(BOOL_1)
                .build();
    }
}
