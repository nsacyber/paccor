package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import tools.jackson.databind.JsonNode;

//import json.ComponentClassTraitDeserializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;

/**
 * <pre>{@code
 * ComponentClassTrait TRAIT ::= {
 *      SYNTAX OCTET STRING (SIZE(4))
 *      IDENTIFIED BY tcg-tr-ID-componentClass }
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@ToString
public class ComponentClassTrait extends Trait<ASN1OctetString, ComponentClassTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return ComponentClassTrait
     */
    public static ComponentClassTrait getInstance(Object obj) {
        return Trait.getInstance(obj, ComponentClassTrait.class, ComponentClassTrait::fromASN1Sequence, ComponentClassTrait::fromJsonNode);
    }

    @Override
    public ComponentClassTrait createInstance(Object obj) {
        return ComponentClassTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return ComponentClassTrait
     */
    public static ComponentClassTrait fromASN1Sequence(ASN1Sequence seq) {
        return ComponentClassTrait.builder()
                .traitType(ComponentClassTrait.class)
                .fromASN1Sequence(seq, DEROctetString::getInstance)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return ComponentClassTrait
     */
    public static ComponentClassTrait fromJsonNode(JsonNode node) {
        return ComponentClassTrait.builder()
                .traitType(ComponentClassTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, ASN1OctetString.class)
                .build();
    }

    /**
     * Initializes a builder with expected ComponentClassTrait metadata.
     * @return ComponentClassTrait Builder
     */
    public static ComponentClassTraitBuilder<?, ?> builder() {
        return new ComponentClassTraitBuilderImpl()
                .traitType(ComponentClassTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdComponentClass)
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentClass)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class ComponentClassTraitBuilder<C extends ComponentClassTrait, B extends ComponentClassTraitBuilder<C, B>> extends TraitBuilder<ASN1OctetString, ComponentClassTrait, C, B> {
    }
}
