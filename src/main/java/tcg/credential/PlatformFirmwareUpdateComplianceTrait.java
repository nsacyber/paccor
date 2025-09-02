package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * PlatformFirmwareUpdateComplianceTrait TRAIT ::= {
 *      SYNTAX PlatformFirmwareUpdateCompliance
 *      IDENTIFIED BY tcg-tr-ID-platformFirmwareUpdateCompliance }
 *
 * A Trait that identifies the platform firmware update mechanism using the PlatformFirmwareUpdateComplianceTrait SHALL use tcg-tr-cat-platformFirmwareUpdateCompliance in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PlatformFirmwareUpdateComplianceTrait extends Trait<PlatformFirmwareUpdateCompliance, PlatformFirmwareUpdateComplianceTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PlatformFirmwareUpdateComplianceTrait
     */
    public static PlatformFirmwareUpdateComplianceTrait getInstance(Object obj) {
        return Trait.getInstance(obj, PlatformFirmwareUpdateComplianceTrait.class, PlatformFirmwareUpdateComplianceTrait::fromASN1Sequence);
    }

    @Override
    public PlatformFirmwareUpdateComplianceTrait createInstance(Object obj) {
        return PlatformFirmwareUpdateComplianceTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return PlatformFirmwareUpdateComplianceTrait
     */
    public static PlatformFirmwareUpdateComplianceTrait fromASN1Sequence(ASN1Sequence seq) {
        return PlatformFirmwareUpdateComplianceTrait.builder()
                .traitType(PlatformFirmwareUpdateComplianceTrait.class)
                .fromASN1Sequence(seq, PlatformFirmwareUpdateCompliance::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected PlatformFirmwareUpdateComplianceTrait metadata.
     * @return PlatformFirmwareUpdateComplianceTrait Builder
     */
    public static PlatformFirmwareUpdateComplianceTraitBuilder<?, ?> builder() {
        return new PlatformFirmwareUpdateComplianceTraitBuilderImpl()
                .traitType(PlatformFirmwareUpdateComplianceTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdPlatformFirmwareUpdateCompliance)
                .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformFirmwareUpdateCompliance);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class PlatformFirmwareUpdateComplianceTraitBuilder<C extends PlatformFirmwareUpdateComplianceTrait, B extends PlatformFirmwareUpdateComplianceTrait.PlatformFirmwareUpdateComplianceTraitBuilder<C, B>> extends TraitBuilder<PlatformFirmwareUpdateCompliance, PlatformFirmwareUpdateComplianceTrait, C, B> {
    }
}
