package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.DERIA5String;

/**
 * Trait that holds an OID.
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ASN1IA5StringTrait extends Trait<ASN1IA5String, ASN1IA5StringTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return ASN1IA5StringTrait
     */
    public static ASN1IA5StringTrait getInstance(Object obj) {
        return Trait.getInstance(obj, ASN1IA5StringTrait.class, ASN1IA5StringTrait::fromASN1Sequence);
    }

    @Override
    public ASN1IA5StringTrait createInstance(Object obj) {
        return ASN1IA5StringTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return ASN1IA5StringTrait
     */
    public static ASN1IA5StringTrait fromASN1Sequence(ASN1Sequence seq) {
        return ASN1IA5StringTrait.builder()
                .traitType(ASN1IA5StringTrait.class)
                .fromASN1Sequence(seq, DERIA5String::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected ASN1IA5StringTrait metadata.
     * @return ASN1IA5StringTrait Builder
     */
    public static ASN1IA5StringTraitBuilder<?, ?> builder() {
        return new ASN1IA5StringTraitBuilderImpl()
                .traitType(ASN1IA5StringTrait.class);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class ASN1IA5StringTraitBuilder<C extends ASN1IA5StringTrait, B extends ASN1IA5StringTraitBuilder<C, B>> extends TraitBuilder<ASN1IA5String, ASN1IA5StringTrait, C, B> {
    }
}
