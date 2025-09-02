package tcg.credential;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * OIDTrait TRAIT ::= {
 *      SYNTAX OBJECT IDENTIFIER
 *      IDENTIFIED BY tcg-tr-ID-OID }
 *
 * A Trait that contains an OID using the OIDTrait SHALL use tcg-tr-cat-OID in its traitCategory field.
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class OIDTrait extends Trait<ASN1ObjectIdentifier, OIDTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return OIDTrait
     */
    public static OIDTrait getInstance(Object obj) {
        return Trait.getInstance(obj, OIDTrait.class, OIDTrait::fromASN1Sequence, OIDTrait::fromJsonNode);
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
        return OIDTrait.builder()
                .traitType(OIDTrait.class)
                .fromASN1Sequence(seq, ASN1Utils::getOID)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return OIDTrait
     */
    public static OIDTrait fromJsonNode(JsonNode node) {
        return OIDTrait.builder()
                .traitType(OIDTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, ASN1ObjectIdentifier.class)
                .build();
    }

    /**
     * Initializes a builder with expected OIDTrait metadata.
     * @return OIDTrait Builder
     */
    public static OIDTraitBuilder<?, ?> builder() {
        return new OIDTraitBuilderImpl()
                .traitType(OIDTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdOid)
                .traitCategory(TCGObjectIdentifier.tcgTrCatOid)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class OIDTraitBuilder<C extends OIDTrait, B extends OIDTraitBuilder<C, B>> extends TraitBuilder<ASN1ObjectIdentifier, OIDTrait, C, B> {
    }
}
