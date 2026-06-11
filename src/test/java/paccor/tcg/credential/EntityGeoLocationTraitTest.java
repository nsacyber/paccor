package paccor.tcg.credential;

import org.bouncycastle.asn1.DERUTF8String;

public class EntityGeoLocationTraitTest {
    public static final String ENTITY_GEO_LOC_1_COUNTRY_CODE = "US";
    public static final String ENTITY_GEO_LOC_1_STATE = "US-MD";
    public static final EntityGeoLocation ENTITY_GEO_LOC_1 = EntityGeoLocation.builder()
            .countryCode(ASN1Utils.getPrintableString(ENTITY_GEO_LOC_1_COUNTRY_CODE))
            .stateOrProvince(ASN1Utils.getPrintableString(ENTITY_GEO_LOC_1_STATE))
            .build();
    public static final String ENTITY_GEO_LOC_1_TRAIT_DESC = "Entity Geo Location Trait Test 1";

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test EntityGeoLocationTraitTest
     */
    public static final EntityGeoLocationTrait sampleEntityGeoLocationTrait1() {
        return EntityGeoLocationTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(EntityGeoLocationTraitTest.ENTITY_GEO_LOC_1_TRAIT_DESC))
                .traitValue(EntityGeoLocationTraitTest.ENTITY_GEO_LOC_1)
                .build();
    }
}
