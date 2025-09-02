package tcg.credential;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * BooleanTrait TRAIT ::= {
 *      SYNTAX BOOLEAN
 *      IDENTIFIED BY tcg-tr-ID-Boolean }
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class BooleanTrait extends Trait<ASN1Boolean, BooleanTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return BooleanTrait
     */
    public static BooleanTrait getInstance(Object obj) {
        return Trait.getInstance(obj, BooleanTrait.class, BooleanTrait::fromASN1Sequence, BooleanTrait::fromJsonNode);
    }

    @Override
    public BooleanTrait createInstance(Object obj) {
        return BooleanTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return BooleanTrait
     */
    public static BooleanTrait fromASN1Sequence(ASN1Sequence seq) {
        return BooleanTrait.builder()
                .traitType(BooleanTrait.class)
                .fromASN1Sequence(seq, ASN1Utils::getBoolean)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return BooleanTrait
     */
    public static BooleanTrait fromJsonNode(JsonNode node) {
        return BooleanTrait.builder()
                .traitType(BooleanTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, ASN1Boolean.class)
                .build();
    }

    /**
     * Initializes a builder with expected BooleanTrait metadata.
     * @return BooleanTrait Builder
     */
    public static BooleanTraitBuilder<?, ?> builder() {
        return new BooleanTraitBuilderImpl()
                .traitType(BooleanTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdBoolean)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class BooleanTraitBuilder<C extends BooleanTrait, B extends BooleanTraitBuilder<C, B>> extends TraitBuilder<ASN1Boolean, BooleanTrait, C, B> {
    }
}
