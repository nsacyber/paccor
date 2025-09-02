package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

/**
 * <pre>
 * PublicKeyTrait TRAIT ::= {
 *      SYNTAX SubjectPublicKeyInfo
 *      IDENTIFIED BY tcg-tr-ID-PublicKey }
 *
 * A Trait that contains a Public Key using the PublicKeyTrait SHALL use tcg-tr-cat-PublicKey in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PublicKeyTrait extends Trait<SubjectPublicKeyInfo, PublicKeyTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PublicKeyTrait
     */
    public static PublicKeyTrait getInstance(Object obj) {
        return Trait.getInstance(obj, PublicKeyTrait.class, PublicKeyTrait::fromASN1Sequence);
    }

    @Override
    public PublicKeyTrait createInstance(Object obj) {
        return PublicKeyTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return PublicKeyTrait
     */
    public static PublicKeyTrait fromASN1Sequence(ASN1Sequence seq) {
        return PublicKeyTrait.builder()
                .traitType(PublicKeyTrait.class)
                .fromASN1Sequence(seq, SubjectPublicKeyInfo::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected PublicKeyTrait metadata.
     * @return PublicKeyTrait Builder
     */
    public static PublicKeyTraitBuilder<?, ?> builder() {
        return new PublicKeyTraitBuilderImpl()
                .traitType(PublicKeyTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdPublicKey)
                .traitCategory(TCGObjectIdentifier.tcgTrCatPublicKey);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class PublicKeyTraitBuilder<C extends PublicKeyTrait, B extends PublicKeyTrait.PublicKeyTraitBuilder<C, B>> extends TraitBuilder<SubjectPublicKeyInfo, PublicKeyTrait, C, B> {
    }
}
