package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * OIDTrait TRAIT ::= {
 *      SYNTAX OBJECT IDENTIFIER
 *      IDENTIFIED BY tcg-tr-ID-OID }
 *
 * A Trait that contains an OID using the OIDTrait SHALL use tcg-tr-cat-OID in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class OIDTrait extends ASN1ObjectIdentifierTrait {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return OIDTrait
     */
    public static OIDTrait getInstance(Object obj) {
        return (OIDTrait)ASN1ObjectIdentifierTrait.getInstance(obj);
    }

    @Override
    public OIDTrait createInstance(Object obj) {
        return OIDTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return OIDTrait
     */
    public static OIDTrait fromASN1Sequence(ASN1Sequence seq) {
        return (OIDTrait)ASN1ObjectIdentifierTrait.fromASN1Sequence(seq);
    }

    /**
     * Initializes a builder with expected OIDTrait metadata.
     * @return OIDTrait Builder
     */
    public static OIDTraitBuilder<?, ?> builder() {
        return new OIDTraitBuilderImpl()
                .traitId(TCGObjectIdentifier.tcgTrIdOid)
                .traitCategory(TCGObjectIdentifier.tcgTrCatOid);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class OIDTraitBuilder<C extends OIDTrait, B extends OIDTraitBuilder<C, B>> extends ASN1ObjectIdentifierTraitBuilder<C, B> {
    }
}
