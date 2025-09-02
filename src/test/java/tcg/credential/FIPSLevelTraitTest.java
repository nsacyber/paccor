package tcg.credential;

import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERUTF8String;

public class FIPSLevelTraitTest {
    public static final String FIPS_1_VERSION = "1";
    public static final String FIPS_1_TRAIT_DESC = "FIPS Trait Test 1";
    public static final SecurityLevel FIPS_1_SEC_LEVEL = new SecurityLevel(SecurityLevel.Enumerated.level1.getValue());
    public static final FIPSLevel FIPS_1 = FIPSLevel.builder().version(new DERIA5String(FIPS_1_VERSION)).level(FIPS_1_SEC_LEVEL).build();

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
}
