package tcg.credential;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * IA5StringTrait TRAIT ::= {
 *      SYNTAX IA5String(SIZE (1..STRMAX))
 *      IDENTIFIED BY tcg-tr-ID-IA5String }
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class IA5StringTrait extends Trait<ASN1IA5String, IA5StringTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return IA5StringTrait
     */
    public static IA5StringTrait getInstance(Object obj) {
        return Trait.getInstance(obj, IA5StringTrait.class, IA5StringTrait::fromASN1Sequence, IA5StringTrait::fromJsonNode);
    }

    @Override
    public IA5StringTrait createInstance(Object obj) {
        return IA5StringTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return IA5StringTrait
     */
    public static IA5StringTrait fromASN1Sequence(ASN1Sequence seq) {
        return IA5StringTrait.builder()
                .traitType(IA5StringTrait.class)
                .fromASN1Sequence(seq, ASN1Utils::getIA5String)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return IA5StringTrait
     */
    public static IA5StringTrait fromJsonNode(JsonNode node) {
        return IA5StringTrait.builder()
                .traitType(IA5StringTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, ASN1IA5String.class)
                .build();
    }

    /**
     * Initializes a builder with expected IA5StringTrait metadata.
     * @return IA5StringTrait Builder
     */
    public static IA5StringTraitBuilder<?, ?> builder() {
        return new IA5StringTraitBuilderImpl()
                .traitType(IA5StringTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdIa5String);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class IA5StringTraitBuilder<C extends IA5StringTrait, B extends IA5StringTraitBuilder<C, B>> extends TraitBuilder<ASN1IA5String, IA5StringTrait, C, B> {
    }
}
