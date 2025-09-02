package tcg.credential;

import org.bouncycastle.asn1.DERUTF8String;

public class ISO9000TraitTest {
    public static final boolean ISO9000_1_CERTIFIED = true;
    public static final String ISO9000_1_URI = "https://no";
    public static final String ISO9000_1_TRAIT_DESC = "IS09000 Trait Test 1";
    public static final ISO9000Certification ISO9000_1 = ISO9000Certification.builder()
            .iso9000Certified(ASN1Utils.getBoolean(ISO9000_1_CERTIFIED))
            .iso9000Uri(ASN1Utils.getIA5String(ISO9000_1_URI))
            .build();

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test ISO9000Trait
     */
    public static final ISO9000Trait sampleISO9000Trait1() {
        return ISO9000Trait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(ISO9000TraitTest.ISO9000_1_TRAIT_DESC))
                .traitValue(ISO9000TraitTest.ISO9000_1)
                .build();
    }
}
