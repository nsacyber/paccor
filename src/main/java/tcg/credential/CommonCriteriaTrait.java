package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * CommonCriteriaTrait TRAIT ::= {
 *      SYNTAX CommonCriteriaEvaluation
 *      IDENTIFIED BY tcg-tr-ID-CommonCriteria }
 *
 * A Trait that indicates a Common Criteria evaluation using the CommonCriteriaTrait SHALL use tcg-tr-cat-CommonCriteria in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CommonCriteriaTrait extends Trait<CommonCriteriaEvaluation, CommonCriteriaTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return CommonCriteriaTrait
     */
    public static CommonCriteriaTrait getInstance(Object obj) {
        return Trait.getInstance(obj, CommonCriteriaTrait.class, CommonCriteriaTrait::fromASN1Sequence);
    }

    @Override
    public CommonCriteriaTrait createInstance(Object obj) {
        return CommonCriteriaTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return CommonCriteriaTrait
     */
    public static CommonCriteriaTrait fromASN1Sequence(ASN1Sequence seq) {
        return CommonCriteriaTrait.builder()
                .traitType(CommonCriteriaTrait.class)
                .fromASN1Sequence(seq, CommonCriteriaEvaluation::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected CommonCriteriaTrait metadata.
     * @return CommonCriteriaTrait Builder
     */
    public static CommonCriteriaTraitBuilder<?, ?> builder() {
        return new CommonCriteriaTraitBuilderImpl()
                .traitType(CommonCriteriaTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdCommonCriteria)
                .traitCategory(TCGObjectIdentifier.tcgTrCatCommonCriteria);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class CommonCriteriaTraitBuilder<C extends CommonCriteriaTrait, B extends CommonCriteriaTraitBuilder<C, B>> extends TraitBuilder<CommonCriteriaEvaluation, CommonCriteriaTrait, C, B> {
    }
}
