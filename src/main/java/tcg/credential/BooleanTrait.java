package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * BooleanTrait TRAIT ::= {
 *      SYNTAX BOOLEAN
 *      IDENTIFIED BY tcg-tr-ID-Boolean }
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class BooleanTrait extends ASN1BooleanTrait {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return BooleanTrait
     */
    public static BooleanTrait getInstance(Object obj) {
        return (BooleanTrait)ASN1BooleanTrait.getInstance(obj);
    }

    @Override
    public BooleanTrait createInstance(Object obj) {
        return BooleanTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return BooleanTrait
     */
    public static BooleanTrait fromASN1Sequence(ASN1Sequence seq) {
        return (BooleanTrait)ASN1BooleanTrait.fromASN1Sequence(seq);
    }

    /**
     * Initializes a builder with expected BooleanTrait metadata.
     * @return BooleanTrait Builder
     */
    public static BooleanTraitBuilder<?, ?> builder() {
        return new BooleanTraitBuilderImpl()
                .traitId(TCGObjectIdentifier.tcgTrIdBoolean);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class BooleanTraitBuilder<C extends BooleanTrait, B extends BooleanTraitBuilder<C, B>> extends ASN1BooleanTraitBuilder<C, B> {
    }
}
