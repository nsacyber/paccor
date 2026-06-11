package paccor.normalization;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import paccor.tcg.credential.ASN1Utils;
import paccor.tcg.credential.BooleanTrait;
import paccor.tcg.credential.ComponentIdentifier;
import paccor.tcg.credential.ComponentIdentifierV2;
import paccor.tcg.credential.NetworkMACTrait;
import paccor.tcg.credential.PENTrait;
import paccor.tcg.credential.PlatformConfiguration;
import paccor.tcg.credential.PlatformConfigurationV2;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.PlatformProperties;
import paccor.tcg.credential.PlatformPropertiesV2;
import paccor.tcg.credential.StatusTrait;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitCollection;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.UTF8StringTrait;

/**
 * Utility for normalizing Platform Configuration data.
 */
public final class PlatformConfigurationNormalizer {
    private PlatformConfigurationNormalizer() {}

    /**
     * Check if not null and has components.
     * @param configuration PlatformConfiguration
     * @return true if not null and has components
     */
    public static final boolean hasComponents(PlatformConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .map(PlatformConfiguration::getComponentIdentifiers)
                .map(list -> !list.isEmpty())
                .orElse(false);
    }

    /**
     * Check if not null and has properties.
     * @param configuration PlatformConfiguration
     * @return true if not null and has properties
     */
    public static final boolean hasProperties(PlatformConfiguration configuration) {
        return Optional.ofNullable(configuration)
                .map(PlatformConfiguration::getPlatformProperties)
                .map(list -> !list.isEmpty())
                .orElse(false);
    }

    /**
     * Check if not null and has components.
     * @param configuration PlatformConfigurationV2
     * @return true if not null and has components
     */
    public static final boolean hasComponents(PlatformConfigurationV2 configuration) {
        return Optional.ofNullable(configuration)
                .map(PlatformConfigurationV2::getComponentIdentifiers)
                .map(list -> !list.isEmpty())
                .orElse(false);
    }

    /**
     * Check if not null and has properties.
     * @param configuration PlatformConfigurationV2
     * @return true if not null and has properties
     */
    public static final boolean hasProperties(PlatformConfigurationV2 configuration) {
        return Optional.ofNullable(configuration)
                .map(PlatformConfigurationV2::getPlatformProperties)
                .map(list -> !list.isEmpty())
                .orElse(false);
    }

    /**
     * Check if not null and has components.
     * @param configuration PlatformConfigurationV3
     * @return true if not null and has components
     */
    public static final boolean hasComponents(PlatformConfigurationV3 configuration) {
        return Optional.ofNullable(configuration)
                .map(PlatformConfigurationV3::getPlatformComponents)
                .map(list -> !list.isEmpty())
                .orElse(false);
    }

    /**
     * Check if not null and has properties.
     * @param configuration PlatformConfigurationV3
     * @return true if not null and has properties
     */
    public static final boolean hasProperties(PlatformConfigurationV3 configuration) {
        return Optional.ofNullable(configuration)
                .map(PlatformConfigurationV3::getPlatformProperties)
                .map(list -> !list.isEmpty())
                .orElse(false);
    }

    /**
     * Check if not null and has component or property data.
     * @param configuration PlatformConfiguration
     * @return true if not null and has component or property data
     */
    public static final boolean hasContent(PlatformConfiguration configuration) {
        return hasComponents(configuration) || hasProperties(configuration);
    }

    /**
     * Check if not null and has component or property data.
     * @param configuration PlatformConfigurationV2
     * @return true if not null and has component or property data
     */
    public static final boolean hasContent(PlatformConfigurationV2 configuration) {
        return hasComponents(configuration) || hasProperties(configuration);
    }

    /**
     * Check if not null and has component or property data.
     * @param configuration PlatformConfigurationV3
     * @return true if not null and has component or property data
     */
    public static final boolean hasContent(PlatformConfigurationV3 configuration) {
        return hasComponents(configuration) || hasProperties(configuration);
    }

    /**
     * Convert to PlatformConfigurationV3.
     * @param configuration PlatformConfiguration
     * @return PlatformConfigurationV3 or null if no content
     */
    public static final PlatformConfigurationV3 canonicalize(PlatformConfiguration configuration) {
        if (!hasContent(configuration)) {
            return null;
        }

        return PlatformConfigurationV3.builder()
                .platformComponents(Optional.ofNullable(configuration.getComponentIdentifiers()).orElse(List.of()).stream()
                        .map(PlatformConfigurationNormalizer::toTraitMap)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .platformProperties(Optional.ofNullable(configuration.getPlatformProperties()).orElse(List.of()).stream()
                        .map(PlatformConfigurationNormalizer::toPlatformPropertiesV2)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Convert to PlatformConfigurationV3.
     * @param configuration PlatformConfigurationV2
     * @return PlatformConfigurationV3 or null if no content
     */
    public static final PlatformConfigurationV3 canonicalize(PlatformConfigurationV2 configuration) {
        if (!hasContent(configuration)) {
            return null;
        }

        return PlatformConfigurationV3.builder()
                .platformComponents(Optional.ofNullable(configuration.getComponentIdentifiers()).orElse(List.of()).stream()
                        .map(ComponentIdentifierV2Converter::toTraitMap)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .platformProperties(Optional.ofNullable(configuration.getPlatformProperties()).orElse(List.of()))
                .build();
    }

    /**
     * Canonicalize PlatformConfigurationV3.
     * @param configuration PlatformConfigurationV3
     * @return PlatformConfigurationV3 or null if no content
     */
    public static final PlatformConfigurationV3 canonicalize(PlatformConfigurationV3 configuration) {
        if (!hasContent(configuration)) {
            return null;
        }

        return PlatformConfigurationV3.builder()
                .platformComponents(Optional.ofNullable(configuration.getPlatformComponents()).orElse(List.of()))
                .platformProperties(Optional.ofNullable(configuration.getPlatformProperties()).orElse(List.of()))
                .build();
    }

    /**
     * Normalize PlatformConfigurationV3 for validation.
     * @param configuration Normalize PlatformConfiguration
     * @return PlatformConfigurationV3 or null if no content
     */
    public static final PlatformConfigurationV3 normalizeForValidation(PlatformConfiguration configuration) {
        return normalizeForValidation(canonicalize(configuration));
    }

    /**
     * Normalize PlatformConfigurationV3 for validation.
     * @param configuration Normalize PlatformConfigurationV2
     * @return PlatformConfigurationV3 or null if no content
     */
    public static final PlatformConfigurationV3 normalizeForValidation(PlatformConfigurationV2 configuration) {
        return normalizeForValidation(canonicalize(configuration));
    }

    /**
     * Normalize PlatformConfigurationV3 for validation.
     * @param configuration Normalize PlatformConfigurationV3
     * @return PlatformConfigurationV3 or null if no content
     */
    public static final PlatformConfigurationV3 normalizeForValidation(PlatformConfigurationV3 configuration) {
        if (!hasContent(configuration)) {
            return null;
        }

        return PlatformConfigurationV3.builder()
                .platformComponents(Optional.ofNullable(configuration.getPlatformComponents()).orElse(List.of()).stream()
                        .map(ComponentIdentifierV2Converter::normalizeTraitMap)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .platformProperties(Optional.ofNullable(configuration.getPlatformProperties()).orElse(List.of()))
                .build();
    }

    /**
     * Normalize PlatformConfigurationV3 for certificate output.
     * @param configuration Normalize PlatformConfigurationV3
     * @return PlatformConfigurationV3 or null if no content
     */
    public static PlatformConfigurationV3 normalizeForCertificateOutput(PlatformConfigurationV3 configuration) {
        if (!hasContent(configuration)) {
            return null;
        }

        return PlatformConfigurationV3.builder()
                .platformComponents(Optional.ofNullable(configuration.getPlatformComponents()).orElse(List.of()).stream()
                        .map(ComponentIdentifierV2Converter::toCertificateTraitMap)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()))
                .platformProperties(Optional.ofNullable(configuration.getPlatformProperties()).orElse(List.of()))
                .build();
    }

    /**
     * Get the components for validation.
     * @param configuration PlatformConfiguration
     * @return List of TraitMap or empty list if no content
     */
    public static final List<TraitMap> componentsForValidation(PlatformConfiguration configuration) {
        PlatformConfigurationV3 normalized = normalizeForValidation(configuration);
        return normalized == null ? List.of() : Optional.ofNullable(normalized.getPlatformComponents()).orElse(List.of());
    }

    /**
     * Get the components for validation.
     * @param configuration PlatformConfigurationV2
     * @return List of TraitMap or empty list if no content
     */
    public static final List<TraitMap> componentsForValidation(PlatformConfigurationV2 configuration) {
        PlatformConfigurationV3 normalized = normalizeForValidation(configuration);
        return normalized == null ? List.of() : Optional.ofNullable(normalized.getPlatformComponents()).orElse(List.of());
    }

    /**
     * Get the components for validation.
     * @param configuration PlatformConfigurationV3
     * @return List of TraitMap or empty list if no content
     */
    public static final List<TraitMap> componentsForValidation(PlatformConfigurationV3 configuration) {
        PlatformConfigurationV3 normalized = normalizeForValidation(configuration);
        return normalized == null ? List.of() : Optional.ofNullable(normalized.getPlatformComponents()).orElse(List.of());
    }

    /**
     * Check if the configuration has a StatusTrait.
     * @param configuration PlatformConfigurationV3
     * @return true if the configuration has a StatusTrait. Otherwise, false.
     */
    public static boolean hasStatusTraits(PlatformConfigurationV3 configuration) {
        return componentsForValidation(configuration).stream()
                .flatMap(component -> component.flattenTraits().stream())
                .anyMatch(StatusTrait.class::isInstance);
    }

    /**
     * Convert from PlatformConfigurationV3 to PlatformConfiguration.
     * @param configuration PlatformConfigurationV3
     * @return PlatformConfiguration or null if no content
     */
    public static final PlatformConfiguration toV1(PlatformConfigurationV3 configuration) {
        if (!hasContent(configuration)) {
            return null;
        }

        List<ComponentIdentifier> components = Optional.ofNullable(configuration.getPlatformComponents()).orElse(List.of()).stream()
                .map(PlatformConfigurationNormalizer::toComponentIdentifier)
                .collect(Collectors.toList());

        if (components.stream().anyMatch(Objects::isNull)) {
            return null;
        }

        return PlatformConfiguration.builder()
                .componentIdentifiers(components)
                .platformProperties(Optional.ofNullable(configuration.getPlatformProperties()).orElse(List.of()).stream()
                        .map(PlatformConfigurationNormalizer::toPlatformProperties)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Convert from PlatformConfigurationV3 to PlatformConfigurationV2.
     * @param configuration PlatformConfigurationV3
     * @return PlatformConfigurationV2 or null if no content
     */
    public static final PlatformConfigurationV2 toV2(PlatformConfigurationV3 configuration) {
        if (!hasContent(configuration)) {
            return null;
        }

        List<ComponentIdentifierV2> components = Optional.ofNullable(configuration.getPlatformComponents()).orElse(List.of()).stream()
                .map(ComponentIdentifierV2Converter::normalizeTraitMap)
                .map(ComponentIdentifierV2Converter::fromTraitMap)
                .collect(Collectors.toList());

        if (components.stream().anyMatch(Objects::isNull)) {
            return null;
        }

        return PlatformConfigurationV2.builder()
                .componentIdentifiers(components)
                .platformProperties(Optional.ofNullable(configuration.getPlatformProperties()).orElse(List.of()))
                .build();
    }

    private static PlatformPropertiesV2 toPlatformPropertiesV2(PlatformProperties property) {
        if (property == null) {
            return null;
        }
        return PlatformPropertiesV2.builder()
                .propertyName(property.getPropertyName())
                .propertyValue(property.getPropertyValue())
                .build();
    }

    private static PlatformProperties toPlatformProperties(PlatformPropertiesV2 property) {
        if (property == null) {
            return null;
        }
        return PlatformProperties.builder()
                .propertyName(property.getPropertyName())
                .propertyValue(property.getPropertyValue())
                .build();
    }

    private static TraitMap toTraitMap(ComponentIdentifier component) {
        if (component == null) {
            return null;
        }

        TraitMap.TraitMapBuilder builder = TraitMap.builder();
        Optional.of(component.getComponentManufacturer())
                .map(str -> UTF8StringTrait.builder()
                                            .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                                            .traitValue(str)
                                            .build())
                .ifPresent(builder::trait);

        Optional.of(component.getComponentModel())
                .map(str -> UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                        .traitValue(str)
                        .build())
                .ifPresent(builder::trait);

        Optional.ofNullable(component.getComponentSerial())
                .map(str -> UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentSerial)
                        .traitValue(str)
                        .build())
                .ifPresent(builder::trait);

        Optional.ofNullable(component.getComponentRevision())
                .map(str -> UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentRevision)
                        .traitValue(str)
                        .build())
                .ifPresent(builder::trait);

        Optional.ofNullable(component.getComponentManufacturerId())
                .map(pen -> PENTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatPen)
                        .traitValue(pen)
                        .build())
                .ifPresent(builder::trait);

        Optional.ofNullable(component.getFieldReplaceable())
                .map(bool -> BooleanTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentFieldReplaceable)
                        .traitValue(bool)
                        .build())
                .ifPresent(builder::trait);

        Optional.ofNullable(component.getComponentAddresses()).orElse(List.of()).forEach(address -> builder.trait(
                NetworkMACTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatNetworkMac)
                        .traitValue(address)
                        .build()));

        return builder.build();
    }

    private static ComponentIdentifier toComponentIdentifier(TraitMap traits) {
        if (traits == null) {
            return null;
        }

        TraitCollection flatTraits = TraitCollection.from(traits);

        ComponentIdentifier.ComponentIdentifierBuilder builder = ComponentIdentifier.builder();

        String manufacturer = flatTraits.firstStringWithCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                                .orElse(null);

        String model = flatTraits.firstStringWithCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                                .orElse(null);

        // Required fields
        if (manufacturer == null || model == null) {
            return null;
        }

        Optional.of(manufacturer)
                .map(ASN1Utils::getUTF8String)
                .ifPresent(builder::componentManufacturer);

        Optional.of(model)
                .map(ASN1Utils::getUTF8String)
                .ifPresent(builder::componentModel);

        flatTraits.firstStringWithCategory(TCGObjectIdentifier.tcgTrCatComponentSerial)
                .map(ASN1Utils::getUTF8String)
                .ifPresent(builder::componentSerial);

        flatTraits.firstStringWithCategory(TCGObjectIdentifier.tcgTrCatComponentRevision)
                .map(ASN1Utils::getUTF8String)
                .ifPresent(builder::componentRevision);

        flatTraits.firstStringWithCategory(TCGObjectIdentifier.tcgTrCatPen)
                .map(ASN1Utils::getOID)
                .ifPresent(builder::componentManufacturerId);

        flatTraits.firstWithCategory(TCGObjectIdentifier.tcgTrCatComponentFieldReplaceable, BooleanTrait.class)
                .map(BooleanTrait::getTraitValue)
                .ifPresent(builder::fieldReplaceable);

        flatTraits.stream()
                .filter(NetworkMACTrait.class::isInstance)
                .map(NetworkMACTrait.class::cast)
                .map(NetworkMACTrait::getTraitValue)
                .forEach(builder::componentAddress);

        return builder.build();
    }
}
