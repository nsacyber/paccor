package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * FIPSLevelTrait TRAIT ::= {
 *      SYNTAX FIPSLevel
 *      IDENTIFIED BY tcg-tr-ID-FIPSLevel }
 *
 * A Trait that indicates a FIPS classification level using the FIPSLevelTrait SHALL use tcg-tr-cat-FIPSLevel in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class FIPSLevelTrait extends Trait<FIPSLevel, FIPSLevelTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return FIPSLevelTrait
     */
    public static FIPSLevelTrait getInstance(Object obj) {
        return Trait.getInstance(obj, FIPSLevelTrait.class, FIPSLevelTrait::fromASN1Sequence);
    }

    @Override
    public FIPSLevelTrait createInstance(Object obj) {
        return FIPSLevelTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return FIPSLevelTrait
     */
    public static FIPSLevelTrait fromASN1Sequence(ASN1Sequence seq) {
        return FIPSLevelTrait.builder()
                .traitType(FIPSLevelTrait.class)
                .fromASN1Sequence(seq, FIPSLevel::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected FIPSLevelTrait metadata.
     * @return FIPSLevelTrait Builder
     */
    public static FIPSLevelTraitBuilder<?, ?> builder() {
        return new FIPSLevelTraitBuilderImpl()
                .traitType(FIPSLevelTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdFipsLevel)
                .traitCategory(TCGObjectIdentifier.tcgTrCatFipsLevel);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class FIPSLevelTraitBuilder<C extends FIPSLevelTrait, B extends FIPSLevelTrait.FIPSLevelTraitBuilder<C, B>> extends TraitBuilder<FIPSLevel, FIPSLevelTrait, C, B> {
    }
}
