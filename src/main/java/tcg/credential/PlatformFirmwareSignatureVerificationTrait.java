package tcg.credential;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * PlatformFirmwareSignatureVerificationTrait TRAIT ::= {
 *      SYNTAX PlatformFirmwareSignatureVerification
 *      IDENTIFIED BY tcg-tr-ID-platformFirmwareSignatureVerification }
 *
 * A Trait that identifies the platform firmware signature verification mechanism using the PlatformFirmwareSignatureVerificationTrait SHALL use tcg-tr-cat-platformFirmwareSignatureVerification in its traitCategory field.
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PlatformFirmwareSignatureVerificationTrait extends Trait<PlatformFirmwareSignatureVerification, PlatformFirmwareSignatureVerificationTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PlatformFirmwareSignatureVerificationTrait
     */
    public static PlatformFirmwareSignatureVerificationTrait getInstance(Object obj) {
        return Trait.getInstance(obj, PlatformFirmwareSignatureVerificationTrait.class, PlatformFirmwareSignatureVerificationTrait::fromASN1Sequence, PlatformFirmwareSignatureVerificationTrait::fromJsonNode);
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
     * Attempts to read the provided JSON node into an instance of this trait.
     * @param node JSON node to parse
     * @return PlatformFirmwareSignatureVerificationTrait
     */
    public static PlatformFirmwareSignatureVerificationTrait fromJsonNode(JsonNode node) {
        return PlatformFirmwareSignatureVerificationTrait.builder()
                .traitType(PlatformFirmwareSignatureVerificationTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, PlatformFirmwareSignatureVerification.class)
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
                .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformFirmwareSignatureVerification)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone);
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
