package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * PEMCertStringTrait TRAIT ::= {
 *      SYNTAX UTF8String (SIZE (1..CERTSTRMAX))
 *      IDENTIFIED BY tcg-tr-ID-PEMCertString }
 *
 * A Trait that contains a PEM-encoded certificate using the PEMCertStringTrait SHALL use tcg-tr-cat-PEMCertificate in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PEMCertStringTrait extends ASN1UTF8StringTrait {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PEMCertStringTrait
     */
    public static PEMCertStringTrait getInstance(Object obj) {
        return (PEMCertStringTrait)ASN1UTF8StringTrait.getInstance(obj);
    }

    @Override
    public PEMCertStringTrait createInstance(Object obj) {
        return PEMCertStringTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return PEMCertStringTrait
     */
    public static PEMCertStringTrait fromASN1Sequence(ASN1Sequence seq) {
        return (PEMCertStringTrait)ASN1UTF8StringTrait.fromASN1Sequence(seq);
    }

    /**
     * Initializes a builder with expected PEMCertStringTrait metadata.
     * @return trait
     */
    public static PEMCertStringTraitBuilder<?, ?> builder() {
        return new PEMCertStringTraitBuilderImpl()
                .traitId(TCGObjectIdentifier.tcgTrIdPemCertString)
                .traitCategory(TCGObjectIdentifier.tcgTrCatPemCertificate);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class PEMCertStringTraitBuilder<C extends PEMCertStringTrait, B extends PEMCertStringTraitBuilder<C, B>> extends ASN1UTF8StringTraitBuilder<C, B> {
    }
}
