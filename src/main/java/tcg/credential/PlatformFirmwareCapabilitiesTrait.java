package tcg.credential;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bouncycastle.asn1.ASN1Sequence;
import tools.jackson.databind.JsonNode;

/**
 * <pre>{@code
 * PlatformFirmwareCapabilitiesTrait TRAIT ::= {
 *      SYNTAX PlatformFirmwareCapabilities
 *      IDENTIFIED BY tcg-tr-ID-platformFirmwareCapabilities }
 *
 * A Trait that indicates security capabilities provided by the firmware using the PlatformFirmwareCapabilitiesTrait SHALL use tcg-tr-cat-platformFirmwareCapabilities in its traitCategory field.
 * }</pre>
 */
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PlatformFirmwareCapabilitiesTrait extends Trait<PlatformFirmwareCapabilities, PlatformFirmwareCapabilitiesTrait> {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PlatformFirmwareCapabilitiesTrait
     */
    public static PlatformFirmwareCapabilitiesTrait getInstance(Object obj) {
        return Trait.getInstance(obj, PlatformFirmwareCapabilitiesTrait.class, PlatformFirmwareCapabilitiesTrait::fromASN1Sequence, PlatformFirmwareCapabilitiesTrait::fromJsonNode);
    }

    @Override
    public PlatformFirmwareCapabilitiesTrait createInstance(Object obj) {
        return PlatformFirmwareCapabilitiesTrait.getInstance(obj);
    }

    /**
     * Attempts to read the provided sequence into an instance of this trait.
     * @param seq ASN1Sequence to parse
     * @return PlatformFirmwareCapabilitiesTrait
     */
    public static PlatformFirmwareCapabilitiesTrait fromASN1Sequence(ASN1Sequence seq) {
        return PlatformFirmwareCapabilitiesTrait.builder()
                .traitType(PlatformFirmwareCapabilitiesTrait.class)
                .fromASN1Sequence(seq, PlatformFirmwareCapabilities::getInstance)
                .build();
    }

    /**
     * Attempts to read the provided JsonNode into an instance of this trait.
     * @param node JSON node to parse
     * @return PlatformFirmwareCapabilitiesTrait
     */
    public static PlatformFirmwareCapabilitiesTrait fromJsonNode(JsonNode node) {
        return PlatformFirmwareCapabilitiesTrait.builder()
                .traitType(PlatformFirmwareCapabilitiesTrait.class)
                .deserializeTraitDescriptors(node)
                .traitValue(node, PlatformFirmwareCapabilities.class)
                .build();
    }

    /**
     * Initializes a builder with expected PlatformFirmwareCapabilitiesTrait metadata.
     * @return PlatformFirmwareCapabilitiesTrait Builder
     */
    public static PlatformFirmwareCapabilitiesTraitBuilder<?, ?> builder() {
        return new PlatformFirmwareCapabilitiesTraitBuilderImpl()
                .traitType(PlatformFirmwareCapabilitiesTrait.class)
                .traitId(TCGObjectIdentifier.tcgTrIdPlatformFirmwareCapabilities)
                .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformFirmwareCapabilities)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone);
    }

    /**
     * Needed to include this to satisfy Javadoc compiling.
     * A future version of SuperBuilder could fix it.
     * @param <C> Trait type
     * @param <B> Builder type
     */
    public static abstract class PlatformFirmwareCapabilitiesTraitBuilder<C extends PlatformFirmwareCapabilitiesTrait, B extends PlatformFirmwareCapabilitiesTrait.PlatformFirmwareCapabilitiesTraitBuilder<C, B>> extends TraitBuilder<PlatformFirmwareCapabilities, PlatformFirmwareCapabilitiesTrait, C, B> {
    }
}
