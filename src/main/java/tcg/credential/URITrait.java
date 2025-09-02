package tcg.credential;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * URITrait TRAIT ::= {
 *      SYNTAX URIReference
 *      IDENTIFIED BY tcg-tr-ID-URI }
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class URITrait extends Trait<URIReference, URITrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return URITrait
     */
    public static URITrait getInstance(Object obj) {
        return Trait.getInstance(obj, URITrait.class, URITrait::fromASN1Sequence, URITrait::fromJsonNode);
    }

    @Override
    public URITrait createInstance(Object obj) {
        return URITrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return URITrait
     */
    public static URITrait fromASN1Sequence(ASN1Sequence seq) {
        return URITrait.builder()
                .traitType(URITrait.class)
                .fromASN1Sequence(seq, URIReference::getInstance)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return URITrait
     */
    public static URITrait fromJsonNode(JsonNode node) {
        return URITrait.builder()
                .traitType(URITrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, URIReference.class)
                .build();
    }

    /**
     * Initializes a builder with expected URITrait metadata.
     * @return URITrait Builder
     */
    public static URITraitBuilder<?, ?> builder() {
        return new URITraitBuilderImpl()
                .traitType(URITrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdUri)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class URITraitBuilder<C extends URITrait, B extends URITrait.URITraitBuilder<C, B>> extends TraitBuilder<URIReference, URITrait, C, B> {
    }
}
