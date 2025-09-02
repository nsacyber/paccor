package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * CertificateIdentifierTrait TRAIT ::= {
 *      SYNTAX CertificateIdentifier
 *      IDENTIFIED BY tcg-tr-ID-certificateIdentifier }
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class CertificateIdentifierTrait extends Trait<CertificateIdentifier, CertificateIdentifierTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return CertificateIdentifierTrait
     */
    public static CertificateIdentifierTrait getInstance(Object obj) {
        return Trait.getInstance(obj, CertificateIdentifierTrait.class, CertificateIdentifierTrait::fromASN1Sequence);
    }

    @Override
    public CertificateIdentifierTrait createInstance(Object obj) {
        return CertificateIdentifierTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return CertificateIdentifierTrait
     */
    public static CertificateIdentifierTrait fromASN1Sequence(ASN1Sequence seq) {
        return CertificateIdentifierTrait.builder()
                .traitType(CertificateIdentifierTrait.class)
                .fromASN1Sequence(seq, CertificateIdentifier::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected CertificateIdentifierTrait metadata.
     * @return CertificateIdentifierTrait Builder
     */
    public static CertificateIdentifierTraitBuilder<?, ?> builder() {
        return new CertificateIdentifierTraitBuilderImpl()
                .traitType(CertificateIdentifierTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdCertificateIdentifier);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class CertificateIdentifierTraitBuilder<C extends CertificateIdentifierTrait, B extends CertificateIdentifierTraitBuilder<C, B>> extends TraitBuilder<CertificateIdentifier, CertificateIdentifierTrait, C, B> {
    }
}
