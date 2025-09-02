package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * ComponentClassTrait TRAIT ::= {
 *      SYNTAX OCTET STRING (SIZE(4))
 *      IDENTIFIED BY tcg-tr-ID-componentClass }
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ComponentClassTrait extends Trait<ComponentClass, ComponentClassTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return ComponentClassTrait
     */
    public static ComponentClassTrait getInstance(Object obj) {
        return Trait.getInstance(obj, ComponentClassTrait.class, ComponentClassTrait::fromASN1Sequence);
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
                .fromASN1Sequence(seq, ComponentClass::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected ComponentClassTrait metadata.
     * @return ComponentClassTrait Builder
     */
    public static ComponentClassTraitBuilder<?, ?> builder() {
        return new ComponentClassTraitBuilderImpl()
                .traitType(ComponentClassTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdComponentClass);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class ComponentClassTraitBuilder<C extends ComponentClassTrait, B extends ComponentClassTraitBuilder<C, B>> extends TraitBuilder<ComponentClass, ComponentClassTrait, C, B> {
    }
}
