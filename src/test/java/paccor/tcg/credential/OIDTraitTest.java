package paccor.tcg.credential;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;

public class OIDTraitTest {
    public static final String OID_1_TRAIT_DESC = "OID Trait Test 1";
    public static final ASN1ObjectIdentifier OID_1 = TCGObjectIdentifier.tcgRegistryComponentClassDisk;

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test OIDTrait
     */
    public static final OIDTrait sampleOIDTrait1() {
        return OIDTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(OIDTraitTest.OID_1_TRAIT_DESC))
                .traitValue(OIDTraitTest.OID_1)
                .build();
    }
}
