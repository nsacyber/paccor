package tcg.credential;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTF8String;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * PEMCertStringTrait TRAIT ::= {
 *      SYNTAX UTF8String (SIZE (1..CERTSTRMAX))
 *      IDENTIFIED BY tcg-tr-ID-PEMCertString }
 *
 * A Trait that contains a PEM-encoded certificate using the PEMCertStringTrait SHALL use tcg-tr-cat-PEMCertificate in its traitCategory field.
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PEMCertStringTrait extends Trait<ASN1UTF8String, PEMCertStringTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PEMCertStringTrait
     */
    public static PEMCertStringTrait getInstance(Object obj) {
        return Trait.getInstance(obj, PEMCertStringTrait.class, PEMCertStringTrait::fromASN1Sequence, PEMCertStringTrait::fromJsonNode);
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
        return PEMCertStringTrait.builder()
                .traitType(PEMCertStringTrait.class)
                .fromASN1Sequence(seq, ASN1Utils::getUTF8String)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return PEMCertStringTrait
     */
    public static PEMCertStringTrait fromJsonNode(JsonNode node) {
        return PEMCertStringTrait.builder()
                .traitType(PEMCertStringTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, ASN1UTF8String.class)
                .build();
    }

    /**
     * Initializes a builder with expected PEMCertStringTrait metadata.
     * @return trait
     */
    public static PEMCertStringTraitBuilder<?, ?> builder() {
        return new PEMCertStringTraitBuilderImpl()
                .traitType(PEMCertStringTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdPemCertString)
                .traitCategory(TCGObjectIdentifier.tcgTrCatPemCertificate);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class PEMCertStringTraitBuilder<C extends PEMCertStringTrait, B extends PEMCertStringTraitBuilder<C, B>> extends TraitBuilder<ASN1UTF8String, PEMCertStringTrait, C, B> {
    }
}
