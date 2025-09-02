package tcg.credential;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTF8String;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * UTF8StringTrait TRAIT ::= {
 *      SYNTAX UTF8String(SIZE (1..STRMAX))
 *      IDENTIFIED BY tcg-tr-ID-UTF8String }
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UTF8StringTrait extends Trait<ASN1UTF8String, UTF8StringTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return UTF8StringTrait
     */
    public static final UTF8StringTrait getInstance(Object obj) {
        return Trait.getInstance(obj, UTF8StringTrait.class, UTF8StringTrait::fromASN1Sequence, UTF8StringTrait::fromJsonNode);
    }

    @Override
    public UTF8StringTrait createInstance(Object obj) {
        return UTF8StringTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return UTF8StringTrait
     */
    public static final UTF8StringTrait fromASN1Sequence(ASN1Sequence seq) {
        return UTF8StringTrait.builder()
                .traitType(UTF8StringTrait.class)
                .fromASN1Sequence(seq, ASN1Utils::getUTF8String)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return UTF8StringTrait
     */
    public static final UTF8StringTrait fromJsonNode(JsonNode node) {
        return UTF8StringTrait.builder()
                .traitType(UTF8StringTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, ASN1UTF8String.class)
                .build();
    }

    /**
     * Initializes a builder with expected UTF8StringTrait metadata.
     * @return UTF8StringTrait Builder
     */
    public static final UTF8StringTraitBuilder<?, ?> builder() {
        return new UTF8StringTraitBuilderImpl()
                .traitType(UTF8StringTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdUtf8String)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class UTF8StringTraitBuilder<C extends UTF8StringTrait, B extends UTF8StringTraitBuilder<C, B>> extends TraitBuilder<ASN1UTF8String, UTF8StringTrait, C, B> {
    }
}
