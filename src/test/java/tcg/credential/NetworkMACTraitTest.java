package tcg.credential;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;
import normalization.HexNormalizer;

public class NetworkMACTraitTest {
    public static final ASN1ObjectIdentifier COMPONENT_ADDRESS_1_OID = ComponentAddressType.BLUETOOTHMAC.getOid();
    public static final String COMPONENT_ADDRESS_1_VALUE = "00:01:02:03:04:05";
    public static final String COMPONENT_ADDRESS_1_TRAIT_DESC = "Network MAC Trait Test 1";
    public static final ComponentAddress COMPONENT_ADDRESS_1 = ComponentAddress.builder()
            .addressType(COMPONENT_ADDRESS_1_OID)
            .addressValue(ASN1Utils.getUTF8String(HexNormalizer.normalizeMac(COMPONENT_ADDRESS_1_VALUE)))
            .build();

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test NetworkMACTrait
     */
    public static final NetworkMACTrait sampleNetworkMACTrait1() {
        return NetworkMACTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(NetworkMACTraitTest.COMPONENT_ADDRESS_1_TRAIT_DESC))
                .traitValue(NetworkMACTraitTest.COMPONENT_ADDRESS_1)
                .build();
    }
}
