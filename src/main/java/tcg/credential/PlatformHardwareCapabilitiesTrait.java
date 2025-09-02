package tcg.credential;

import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;

/**
 * <pre>
 * PlatformHardwareCapabilitiesTrait TRAIT ::= {
 *      SYNTAX PlatformHardwareCapabilities
 *      IDENTIFIED BY tcg-tr-ID-platformHardwareCapabilities }
 *
 * A Trait that indicates the security capabilities provided by the platform motherboard using the PlatformHardwareCapabilitiesTrait SHALL use tcg-tr-cat-platformHardwareCapabilities in its traitCategory field.
 * </pre>
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PlatformHardwareCapabilitiesTrait extends Trait<PlatformHardwareCapabilities, PlatformHardwareCapabilitiesTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PlatformHardwareCapabilitiesTrait
     */
    public static PlatformHardwareCapabilitiesTrait getInstance(Object obj) {
        return Trait.getInstance(obj, PlatformHardwareCapabilitiesTrait.class, PlatformHardwareCapabilitiesTrait::fromASN1Sequence);
    }

    @Override
    public PlatformHardwareCapabilitiesTrait createInstance(Object obj) {
        return PlatformHardwareCapabilitiesTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return PlatformHardwareCapabilitiesTrait
     */
    public static PlatformHardwareCapabilitiesTrait fromASN1Sequence(ASN1Sequence seq) {
        return PlatformHardwareCapabilitiesTrait.builder()
                .traitType(PlatformHardwareCapabilitiesTrait.class)
                .fromASN1Sequence(seq, PlatformHardwareCapabilities::getInstance)
                .build();
    }

    /**
     * Initializes a builder with expected PlatformHardwareCapabilitiesTrait metadata.
     * @return PlatformHardwareCapabilitiesTrait Builder
     */
    public static PlatformHardwareCapabilitiesTraitBuilder<?, ?> builder() {
        return new PlatformHardwareCapabilitiesTraitBuilderImpl()
                .traitType(PlatformHardwareCapabilitiesTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdPlatformHardwareCapabilities)
                .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformHardwareCapabilities);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class PlatformHardwareCapabilitiesTraitBuilder<C extends PlatformHardwareCapabilitiesTrait, B extends PlatformHardwareCapabilitiesTrait.PlatformHardwareCapabilitiesTraitBuilder<C, B>> extends TraitBuilder<PlatformHardwareCapabilities, PlatformHardwareCapabilitiesTrait, C, B> {
    }
}
