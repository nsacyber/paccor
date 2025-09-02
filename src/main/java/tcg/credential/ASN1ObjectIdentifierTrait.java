package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * Trait that holds an OID.
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ASN1ObjectIdentifierTrait extends Trait<ASN1ObjectIdentifier, ASN1ObjectIdentifierTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return ASN1ObjectIdentifierTrait
     */
    public static ASN1ObjectIdentifierTrait getInstance(Object obj) {
        return Trait.getInstance(obj, ASN1ObjectIdentifierTrait.class, ASN1ObjectIdentifierTrait::fromASN1Sequence);
    }

    @Override
    public ASN1ObjectIdentifierTrait createInstance(Object obj) {
        return ASN1ObjectIdentifierTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return ASN1ObjectIdentifierTrait
     */
    public static ASN1ObjectIdentifierTrait fromASN1Sequence(ASN1Sequence seq) {
        return ASN1ObjectIdentifierTrait.builder()
                .traitType(ASN1ObjectIdentifierTrait.class)
                .fromASN1Sequence(seq, ASN1ObjectIdentifier::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected ASN1ObjectIdentifierTrait metadata.
     * @return ASN1ObjectIdentifierTrait Builder
     */
    public static ASN1ObjectIdentifierTraitBuilder<?, ?> builder() {
        return new ASN1ObjectIdentifierTraitBuilderImpl()
                .traitType(ASN1ObjectIdentifierTrait.class);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class ASN1ObjectIdentifierTraitBuilder<C extends ASN1ObjectIdentifierTrait, B extends ASN1ObjectIdentifierTraitBuilder<C, B>> extends TraitBuilder<ASN1ObjectIdentifier, ASN1ObjectIdentifierTrait, C, B> {
    }
}
