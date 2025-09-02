package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * PlatformFirmwareSignatureVerificationTrait TRAIT ::= {
 *      SYNTAX PlatformFirmwareSignatureVerification
 *      IDENTIFIED BY tcg-tr-ID-platformFirmwareSignatureVerification }
 *
 * A Trait that identifies the platform firmware signature verification mechanism using the PlatformFirmwareSignatureVerificationTrait SHALL use tcg-tr-cat-platformFirmwareSignatureVerification in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PlatformFirmwareSignatureVerificationTrait extends Trait<PlatformFirmwareSignatureVerification, PlatformFirmwareSignatureVerificationTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PlatformFirmwareSignatureVerificationTrait
     */
    public static PlatformFirmwareSignatureVerificationTrait getInstance(Object obj) {
        return Trait.getInstance(obj, PlatformFirmwareSignatureVerificationTrait.class, PlatformFirmwareSignatureVerificationTrait::fromASN1Sequence);
    }

    @Override
    public PlatformFirmwareSignatureVerificationTrait createInstance(Object obj) {
        return PlatformFirmwareSignatureVerificationTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return PlatformFirmwareSignatureVerificationTrait
     */
    public static PlatformFirmwareSignatureVerificationTrait fromASN1Sequence(ASN1Sequence seq) {
        return PlatformFirmwareSignatureVerificationTrait.builder()
                .traitType(PlatformFirmwareSignatureVerificationTrait.class)
                .fromASN1Sequence(seq, PlatformFirmwareSignatureVerification::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected PlatformFirmwareSignatureVerificationTrait metadata.
     * @return PlatformFirmwareSignatureVerificationTrait Builder
     */
    public static PlatformFirmwareSignatureVerificationTraitBuilder<?, ?> builder() {
        return new PlatformFirmwareSignatureVerificationTraitBuilderImpl()
                .traitType(PlatformFirmwareSignatureVerificationTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdPlatformFirmwareSignatureVerification)
                .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformFirmwareSignatureVerification);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class PlatformFirmwareSignatureVerificationTraitBuilder<C extends PlatformFirmwareSignatureVerificationTrait, B extends PlatformFirmwareSignatureVerificationTrait.PlatformFirmwareSignatureVerificationTraitBuilder<C, B>> extends TraitBuilder<PlatformFirmwareSignatureVerification, PlatformFirmwareSignatureVerificationTrait, C, B> {
    }
}
