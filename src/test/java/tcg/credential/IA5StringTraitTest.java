package tcg.credential;

import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERUTF8String;

public class IA5StringTraitTest {
    public static final String IA5_1_TRAIT_DESC = "IA5String Trait Test 1";
    public static final String IA5_1_VALUE = "BINGO";
    public static final ASN1IA5String IA5_1 = new DERIA5String(IA5_1_VALUE);

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test IA5StringTraitTest
     */
    public static final IA5StringTrait sampleIA5StringTrait1() {
        return IA5StringTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(IA5StringTraitTest.IA5_1_TRAIT_DESC))
                .traitValue(IA5StringTraitTest.IA5_1)
                .build();
    }
}
