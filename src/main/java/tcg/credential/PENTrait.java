package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * PENTrait TRAIT ::= {
 *      SYNTAX PrivateEnterpriseNumber
 *      IDENTIFIED BY tcg-tr-ID-PEN }
 *
 * A Trait that contains an IANA Private Enterprise Number using the PENTrait SHALL use tcg-tr-cat-PEN in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PENTrait extends ASN1ObjectIdentifierTrait {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PENTrait
     */
    public static PENTrait getInstance(Object obj) {
        return (PENTrait)ASN1ObjectIdentifierTrait.getInstance(obj);
    }

    @Override
    public PENTrait createInstance(Object obj) {
        return PENTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return PENTrait
     */
    public static PENTrait fromASN1Sequence(ASN1Sequence seq) {
        return (PENTrait)ASN1ObjectIdentifierTrait.fromASN1Sequence(seq);
    }

    /**
     * Initializes a builder with expected PENTrait metadata.
     * @return PENTrait Builder
     */
    public static PENTraitBuilder<?, ?> builder() {
        return new PENTraitBuilderImpl()
                .traitId(TCGObjectIdentifier.tcgTrIdPen)
                .traitCategory(TCGObjectIdentifier.tcgTrCatPen);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class PENTraitBuilder<C extends PENTrait, B extends PENTraitBuilder<C, B>> extends ASN1ObjectIdentifierTraitBuilder<C, B> {
    }
}
