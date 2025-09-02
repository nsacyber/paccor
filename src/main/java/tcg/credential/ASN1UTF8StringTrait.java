package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * Trait that holds an OID.
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ASN1UTF8StringTrait extends Trait<ASN1UTF8String, ASN1UTF8StringTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return ASN1UTF8StringTrait
     */
    public static ASN1UTF8StringTrait getInstance(Object obj) {
        return Trait.getInstance(obj, ASN1UTF8StringTrait.class, ASN1UTF8StringTrait::fromASN1Sequence);
    }

    @Override
    public ASN1UTF8StringTrait createInstance(Object obj) {
        return ASN1UTF8StringTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return ASN1UTF8StringTrait
     */
    public static ASN1UTF8StringTrait fromASN1Sequence(ASN1Sequence seq) {
        return ASN1UTF8StringTrait.builder()
                .traitType(ASN1UTF8StringTrait.class)
                .fromASN1Sequence(seq, DERUTF8String::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected ASN1UTF8StringTrait metadata.
     * @return ASN1UTF8StringTrait Builder
     */
    public static ASN1UTF8StringTraitBuilder<?, ?> builder() {
        return new ASN1UTF8StringTraitBuilderImpl()
                .traitType(ASN1UTF8StringTrait.class);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class ASN1UTF8StringTraitBuilder<C extends ASN1UTF8StringTrait, B extends ASN1UTF8StringTraitBuilder<C, B>> extends TraitBuilder<ASN1UTF8String, ASN1UTF8StringTrait, C, B> {
    }
}
