package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * ComponentIdentifierV11Trait TRAIT ::= {
 *      SYNTAX ComponentIdentifierV11
 *      IDENTIFIED BY tcg-tr-ID-componentIdentifierV11 }
 *
 * A Trait that identifies a component using the ComponentIdentifierV11Trait SHALL use tcg-tr-cat-componentIdentifierV11 in its traitCategory field.
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
public class ComponentIdentifierV11Trait extends Trait<ComponentIdentifierV2, ComponentIdentifierV11Trait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return ComponentIdentifierV11Trait
     */
    public static final ComponentIdentifierV11Trait getInstance(Object obj) {
        return Trait.getInstance(obj, ComponentIdentifierV11Trait.class, ComponentIdentifierV11Trait::fromASN1Sequence, ComponentIdentifierV11Trait::fromJsonNode);
    }

    @Override
    public ComponentIdentifierV11Trait createInstance(Object obj) {
        return ComponentIdentifierV11Trait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return ComponentIdentifierV11Trait
     */
    public static final ComponentIdentifierV11Trait fromASN1Sequence(ASN1Sequence seq) {
        return ComponentIdentifierV11Trait.builder()
                .traitType(ComponentIdentifierV11Trait.class)
                .fromASN1Sequence(seq, ComponentIdentifierV2::getInstance)
                .build();
    }

    /**
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return ComponentIdentifierV11Trait
     */
    public static ComponentIdentifierV11Trait fromJsonNode(JsonNode node) {
        return ComponentIdentifierV11Trait.builder()
                .traitType(ComponentIdentifierV11Trait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, ComponentIdentifierV2.class)
                .build();
    }

    public static final ComponentIdentifierV11Trait fromComponentIdentifierV2(ComponentIdentifierV2 v2) {
        return ComponentIdentifierV11Trait.builder()
                .traitValue(v2)
                .traitRegistry(v2.getComponentClass().getComponentClassRegistry())
                .build();
    }

    /**
     * Initializes a builder with expected ComponentIdentifierV11Trait metadata.
     * @return ComponentIdentifierV11Trait Builder
     */
    public static final ComponentIdentifierV11TraitBuilder<?, ?> builder() {
        return new ComponentIdentifierV11TraitBuilderImpl()
                .traitType(ComponentIdentifierV11Trait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdComponentIdentifierV11)
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentIdentifierV11)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class ComponentIdentifierV11TraitBuilder<C extends ComponentIdentifierV11Trait, B extends ComponentIdentifierV11TraitBuilder<C, B>> extends TraitBuilder<ComponentIdentifierV2, ComponentIdentifierV11Trait, C, B> {
    }
}
