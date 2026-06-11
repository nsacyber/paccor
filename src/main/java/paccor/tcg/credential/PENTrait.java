package paccor.tcg.credential;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * PENTrait TRAIT ::= {
 *      SYNTAX OBJECT IDENTIFIER
 *      IDENTIFIED BY tcg-tr-ID-PEN }
 *
 * A Trait that contains an PEN using the PENTrait SHALL use tcg-tr-cat-PEN in its traitCategory field.
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PENTrait extends Trait<ASN1ObjectIdentifier, PENTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PENTrait
     */
    public static PENTrait getInstance(Object obj) {
        return Trait.getInstance(obj, PENTrait.class, PENTrait::fromASN1Sequence, PENTrait::fromJsonNode);
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
        return PENTrait.builder()
                .traitType(PENTrait.class)
                .fromASN1Sequence(seq, ASN1Utils::getOID)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return PENTrait
     */
    public static PENTrait fromJsonNode(JsonNode node) {
        return PENTrait.builder()
                .traitType(PENTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, ASN1ObjectIdentifier.class)
                .build();
    }

    /**
     * Initializes a builder with expected PENTrait metadata.
     * @return PENTrait Builder
     */
    public static PENTraitBuilder<?, ?> builder() {
        return new PENTraitBuilderImpl()
                .traitType(PENTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdPen)
                .traitCategory(TCGObjectIdentifier.tcgTrCatPen)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class PENTraitBuilder<C extends PENTrait, B extends PENTraitBuilder<C, B>> extends TraitBuilder<ASN1ObjectIdentifier, PENTrait, C, B> {
    }
}
