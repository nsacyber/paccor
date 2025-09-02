package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * Trait that holds a Boolean.
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ASN1BooleanTrait extends Trait<ASN1Boolean, ASN1BooleanTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return ASN1BooleanTrait
     */
    public static ASN1BooleanTrait getInstance(Object obj) {
        return Trait.getInstance(obj, ASN1BooleanTrait.class, ASN1BooleanTrait::fromASN1Sequence);
    }

    @Override
    public ASN1BooleanTrait createInstance(Object obj) {
        return ASN1BooleanTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return ASN1BooleanTrait
     */
    public static ASN1BooleanTrait fromASN1Sequence(ASN1Sequence seq) {
        return ASN1BooleanTrait.builder()
                .traitType(ASN1BooleanTrait.class)
                .fromASN1Sequence(seq, ASN1Boolean::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected ASN1BooleanTrait metadata.
     * @return ASN1BooleanTrait Builder
     */
    public static ASN1BooleanTraitBuilder<?, ?> builder() {
        return new ASN1BooleanTraitBuilderImpl()
                .traitType(ASN1BooleanTrait.class);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class ASN1BooleanTraitBuilder<C extends ASN1BooleanTrait, B extends ASN1BooleanTraitBuilder<C, B>> extends TraitBuilder<ASN1Boolean, ASN1BooleanTrait, C, B> {
    }
}
