package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * ISO9000Trait TRAIT ::= {
 *      SYNTAX ISO9000Certification
 *      IDENTIFIED BY tcg-tr-ID-ISO9000 }
 *
 * A Trait that contains an ISO9000 certification assertion using the ISO9000Trait SHALL use tcg-tr-cat-ISO9000 in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class ISO9000Trait extends Trait<ISO9000Certification, ISO9000Trait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return ISO9000Trait
     */
    public static ISO9000Trait getInstance(Object obj) {
        return Trait.getInstance(obj, ISO9000Trait.class, ISO9000Trait::fromASN1Sequence);
    }

    @Override
    public ISO9000Trait createInstance(Object obj) {
        return ISO9000Trait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return ISO9000Trait
     */
    public static ISO9000Trait fromASN1Sequence(ASN1Sequence seq) {
        return ISO9000Trait.builder()
                .traitType(ISO9000Trait.class)
                .fromASN1Sequence(seq, ISO9000Certification::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected ISO9000Trait metadata.
     * @return ISO9000Trait Builder
     */
    public static ISO9000TraitBuilder<?, ?> builder() {
        return new ISO9000TraitBuilderImpl()
                .traitType(ISO9000Trait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdIso9000Level)
                .traitCategory(TCGObjectIdentifier.tcgTrCatIso9000);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class ISO9000TraitBuilder<C extends ISO9000Trait, B extends ISO9000Trait.ISO9000TraitBuilder<C, B>> extends TraitBuilder<ISO9000Certification, ISO9000Trait, C, B> {
    }
}
