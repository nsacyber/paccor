package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * ComponentIdentifierV11Trait TRAIT ::= {
 *      SYNTAX ComponentIdentifierV11
 *      IDENTIFIED BY tcg-tr-ID-componentIdentifierV11 }
 *
 * A Trait that identifies a component using the ComponentIdentifierV11Trait SHALL use tcg-tr-cat-componentIdentifierV11 in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ComponentIdentifierV11Trait extends Trait<ComponentIdentifierV2, ComponentIdentifierV11Trait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return ComponentIdentifierV11Trait
     */
    public static ComponentIdentifierV11Trait getInstance(Object obj) {
        return Trait.getInstance(obj, ComponentIdentifierV11Trait.class, ComponentIdentifierV11Trait::fromASN1Sequence);
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
    public static ComponentIdentifierV11Trait fromASN1Sequence(ASN1Sequence seq) {
        return ComponentIdentifierV11Trait.builder()
                .traitType(ComponentIdentifierV11Trait.class)
                .fromASN1Sequence(seq, ComponentIdentifierV2::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected ComponentIdentifierV11Trait metadata.
     * @return ComponentIdentifierV11Trait Builder
     */
    public static ComponentIdentifierV11TraitBuilder<?, ?> builder() {
        return new ComponentIdentifierV11TraitBuilderImpl()
                .traitType(ComponentIdentifierV11Trait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdComponentIdentifierV11)
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentIdentifierV11);
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
