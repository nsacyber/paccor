package paccor.tcg.credential;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERUTF8String;

public class FIPSLevelTraitTest {
    public static final String FIPS_1_VERSION = "1";
    public static final String FIPS_1_TRAIT_DESC = "FIPS Trait Test 1";
    public static final SecurityLevel FIPS_1_SEC_LEVEL = new SecurityLevel(SecurityLevel.Enumerated.level1.getValue());
    public static final FIPSLevel FIPS_1 = FIPSLevel.builder().version(new DERIA5String(FIPS_1_VERSION)).level(FIPS_1_SEC_LEVEL).build();
    public static final String FIPS_2_VERSION = "140-2";
    public static final String FIPS_2_TRAIT_DESC = "FIPS Trait Test 2";
    public static final SecurityLevel FIPS_2_SEC_LEVEL = SecurityLevel.getInstance("level3");
    public static final boolean FIPS_2_PLUS = true;
    public static final FIPSLevel FIPS_2 = FIPSLevel.builder().version(new DERIA5String(FIPS_2_VERSION)).level(FIPS_2_SEC_LEVEL).plus(ASN1Boolean.getInstance(FIPS_2_PLUS)).build();

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test FIPSLevelTrait
     */
    public static final FIPSLevelTrait sampleFIPSLevelTrait1() {
        return FIPSLevelTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(FIPSLevelTraitTest.FIPS_1_TRAIT_DESC))
                .traitValue(FIPSLevelTraitTest.FIPS_1)
                .build();
    }

    /**
     * If any aspect of this Trait is altered, verify its usage in other tests.
     * @return A test FIPSLevelTrait
     */
    public static final FIPSLevelTrait sampleFIPSLevelTrait2() {
        return FIPSLevelTrait.builder()
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .description(new DERUTF8String(FIPSLevelTraitTest.FIPS_2_TRAIT_DESC))
                .traitValue(FIPSLevelTraitTest.FIPS_2)
                .build();
    }
}
