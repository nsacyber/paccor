package paccor.tcg.credential;

import org.bouncycastle.asn1.DERUTF8String;

public class CountryOfOriginTraitTest {
    public static final EntityGeoLocation COUNTRY_ORIGIN_1_ENTITY = EntityGeoLocationTraitTest.ENTITY_GEO_LOC_1;
    public static final boolean COUNTRY_ORIGIN_1_HAS_COMPONENTS = false;
    public static final String COUNTRY_ORIGIN_1_TRAIT_DESC = "Country of Origin Trait Test 1";
    public static final OriginComposition COUNTRY_ORIGIN_1 = OriginComposition.builder()
            .location(COUNTRY_ORIGIN_1_ENTITY)
            .hasComponents(ASN1Utils.getBoolean(COUNTRY_ORIGIN_1_HAS_COMPONENTS))
            .build();

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test CountryOfOriginTrait
     */
    public static final CountryOfOriginTrait sampleCountryOfOriginTrait1() {
        return CountryOfOriginTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(CountryOfOriginTraitTest.COUNTRY_ORIGIN_1_TRAIT_DESC))
                .traitValue(CountryOfOriginTraitTest.COUNTRY_ORIGIN_1)
                .build();
    }
}
