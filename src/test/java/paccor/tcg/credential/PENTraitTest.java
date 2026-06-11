package paccor.tcg.credential;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;

public class PENTraitTest {
    public static final String PEN_1_TRAIT_DESC = "PEN Trait Test 1";
    public static final ASN1ObjectIdentifier PEN_1 = ASN1Utils.getOID("1.3.6.1.4.1.0");

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test PENTrait
     */
    public static final PENTrait samplePENTrait1() {
        return PENTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(PENTraitTest.PEN_1_TRAIT_DESC))
                .traitValue(PENTraitTest.PEN_1)
                .build();
    }
}
