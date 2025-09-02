package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * StatusTrait TRAIT ::= {
 *      SYNTAX AttributeStatus
 *      IDENTIFIED BY tcg-tr-ID-status }
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class StatusTrait extends Trait<AttributeStatus, StatusTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return StatusTrait
     */
    public static StatusTrait getInstance(Object obj) {
        return Trait.getInstance(obj, StatusTrait.class, StatusTrait::fromASN1Sequence);
    }

    @Override
    public StatusTrait createInstance(Object obj) {
        return StatusTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return StatusTrait
     */
    public static StatusTrait fromASN1Sequence(ASN1Sequence seq) {
        return StatusTrait.builder()
                .traitType(StatusTrait.class)
                .fromASN1Sequence(seq, AttributeStatus::getInstance).build();
    }

    /**
     * Initializes a builder with expected StatusTrait metadata.
     * @return StatusTrait Builder
     */
    public static StatusTraitBuilder<?, ?> builder() {
        return new StatusTraitBuilderImpl()
                .traitType(StatusTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdStatus);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class StatusTraitBuilder<C extends StatusTrait, B extends StatusTrait.StatusTraitBuilder<C, B>> extends TraitBuilder<AttributeStatus, StatusTrait, C, B> {
    }
}
