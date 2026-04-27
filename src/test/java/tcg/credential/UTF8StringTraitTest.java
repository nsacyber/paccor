package tcg.credential;

import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERUTF8String;

public class UTF8StringTraitTest {
    public static final String UTF8_1_TRAIT_DESC = "UTF8String Trait Test 1";
    public static final String UTF8_1_VALUE = "EIEIO";
    public static final ASN1UTF8String UTF8_1 = new DERUTF8String(UTF8_1_VALUE);

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test UTF8StringTrait
     */
    public static final UTF8StringTrait sampleUTF8StringTrait1() {
        return UTF8StringTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(UTF8StringTraitTest.UTF8_1_TRAIT_DESC))
                .traitValue(UTF8StringTraitTest.UTF8_1)
                .build();
    }
}
