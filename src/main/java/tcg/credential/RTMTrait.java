package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * RTMTrait TRAIT ::= {
 *      SYNTAX RTMTypes
 *      IDENTIFIED BY tcg-tr-ID-RTM }
 *
 * A Trait that identifies the Root of Trust for Measurement using the RTMTrait SHALL use tcg-tr-cat-RTM in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RTMTrait extends Trait<RTMTypes, RTMTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return RTMTrait
     */
    public static RTMTrait getInstance(Object obj) {
        return Trait.getInstance(obj, RTMTrait.class, RTMTrait::fromASN1Sequence);
    }

    @Override
    public RTMTrait createInstance(Object obj) {
        return RTMTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return RTMTrait
     */
    public static RTMTrait fromASN1Sequence(ASN1Sequence seq) {
        return RTMTrait.builder()
                .traitType(RTMTrait.class)
                .fromASN1Sequence(seq, RTMTypes::getInstance).build();
    }

    /**
     * Initializes a builder with expected RTMTrait metadata.
     * @return RTMTrait Builder
     */
    public static RTMTraitBuilder<?, ?> builder() {
        return new RTMTraitBuilderImpl()
                .traitType(RTMTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdRtm)
                .traitCategory(TCGObjectIdentifier.tcgTrCatRtm);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class RTMTraitBuilder<C extends RTMTrait, B extends RTMTrait.RTMTraitBuilder<C, B>> extends TraitBuilder<RTMTypes, RTMTrait, C, B> {
    }
}
