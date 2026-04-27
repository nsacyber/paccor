package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * Trait that holds a generic ASN1Object.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@SuperBuilder(toBuilder = true)
@ToString
public class ASN1ObjectTrait extends Trait<ASN1Object, ASN1ObjectTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return ASN1ObjectTrait
     */
    public static ASN1ObjectTrait getInstance(Object obj) {
        return Trait.getInstance(obj, ASN1ObjectTrait.class, ASN1ObjectTrait::fromASN1Sequence, ASN1ObjectTrait::fromJsonNode);
    }

    @Override
    public ASN1ObjectTrait createInstance(Object obj) {
        return ASN1ObjectTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return ASN1ObjectTrait
     */
    public static ASN1ObjectTrait fromASN1Sequence(ASN1Sequence seq) {
        return ASN1ObjectTrait.builder()
                .traitType(ASN1ObjectTrait.class)
                .fromASN1Sequence(seq, ASN1Object.class::cast)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return BooleanTrait
     */
    public static ASN1ObjectTrait fromJsonNode(JsonNode node) {
        return ASN1ObjectTrait.builder()
                .traitType(ASN1ObjectTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, ASN1OctetString.class, ASN1Object.class::cast)
                .build();
    }

    /**
     * Initializes a builder with expected ASN1ObjectTrait metadata.
     * @return ASN1ObjectTrait Builder
     */
    public static ASN1ObjectTraitBuilder<?, ?> builder() {
        return new ASN1ObjectTraitBuilderImpl()
                .traitType(ASN1ObjectTrait.class);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class ASN1ObjectTraitBuilder<C extends ASN1ObjectTrait, B extends ASN1ObjectTraitBuilder<C, B>> extends TraitBuilder<ASN1Object, ASN1ObjectTrait, C, B> {
    }
}
