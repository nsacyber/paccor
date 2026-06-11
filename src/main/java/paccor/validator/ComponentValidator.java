package paccor.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import paccor.normalization.HexNormalizer;
import paccor.normalization.PlatformConfigurationNormalizer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.tcg.credential.AttributeStatus;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.ComponentClassTrait;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.StatusTrait;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.Trait;
import paccor.tcg.credential.TraitCollection;
import paccor.tcg.credential.TraitMap;

/**
 * Validator for Platform Configuration V3 components.
 */
public final class ComponentValidator {
    private ComponentValidator() {}

    /**
     * Compare the expected and actual components.
     * @param expected Expected components.
     * @param actual Actual components.
     * @param matcher Component matcher.
     * @return ComponentValidationReport
     */
    public static ComponentValidationReport compareComponents(List<TraitMap> expected, List<TraitMap> actual, ComponentMatcher matcher) {
        List<TraitMap> exp = Optional.ofNullable(expected).orElse(List.of());
        List<TraitMap> act = Optional.ofNullable(actual).orElse(List.of());
        List<String> issues = new ArrayList<>();
        if (exp.size() > act.size()) {
            issues.add("Expected " + exp.size() + " component(s) but certificate materialized to " + act.size() + ".");
        }
        for (TraitMap component : exp) {
            if (!matcher.matchV3(List.of(component), act)) {
                issues.add("Missing matching component: " + summarize(component));
            }
        }
        return new ComponentValidationReport(issues.isEmpty(), issues);
    }

    /**
     * Materialize the components from the base and deltas.
     * @param base Base platform configuration.
     * @param deltas List of delta platform configurations.
     * @return Materialized platform configuration with components.
     */
    public static PlatformConfigurationV3 materializeComponents(PlatformConfigurationV3 base, List<PlatformConfigurationV3> deltas) {
        List<TraitMap> current = PlatformConfigurationNormalizer.componentsForValidation(base);
        for (PlatformConfigurationV3 delta : Optional.ofNullable(deltas).orElse(List.of())) {
            current = applyDelta(current, PlatformConfigurationNormalizer.componentsForValidation(delta));
        }
        return PlatformConfigurationV3.builder().platformComponents(current).build();
    }

    /**
     * Check if the CertificateIdentifierTrait contains a base platform certificate.
     * @param trait CertificateIdentifierTrait
     * @return true if the trait represents a base platform certificate. Otherwise, false.
     */
    public static boolean isBaseTrait(CertificateIdentifierTrait trait) {
        if (trait == null) {
            return false;
        }
        return TCGObjectIdentifier.tcgTrCatPlatformCertificate.equals(trait.getTraitCategory())
                || TCGObjectIdentifier.tcgKpPlatformAttributeCertificate.equals(trait.getTraitCategory())
                || TCGObjectIdentifier.tcgKpPlatformKeyCertificate.equals(trait.getTraitCategory());
    }

    /**
     * Check if the CertificateIdentifierTrait contains a delta platform certificate.
     * @param trait CertificateIdentifierTrait
     * @return true if the trait represents a delta platform certificate. Otherwise, false.
     */
    public static boolean isDeltaTrait(CertificateIdentifierTrait trait) {
        if (trait == null) {
            return false;
        }
        return TCGObjectIdentifier.tcgTrCatDeltaPlatformCertificate.equals(trait.getTraitCategory())
                || TCGObjectIdentifier.tcgKpDeltaPlatformAttributeCertificate.equals(trait.getTraitCategory())
                || TCGObjectIdentifier.tcgKpDeltaPlatformKeyCertificate.equals(trait.getTraitCategory());
    }

    /**
     * Check if the CertificateIdentifierTrait contains a rebase platform certificate.
     * @param trait CertificateIdentifierTrait
     * @return true if the trait represents a rebase platform certificate. Otherwise, false.
     */
    public static boolean isRebaseTrait(CertificateIdentifierTrait trait) {
        if (trait == null) {
            return false;
        }
        return TCGObjectIdentifier.tcgTrCatRebasePlatformCertificate.equals(trait.getTraitCategory())
                || TCGObjectIdentifier.tcgKpAdditionalPlatformAttributeCertificate.equals(trait.getTraitCategory())
                || TCGObjectIdentifier.tcgKpAdditionalPlatformKeyCertificate.equals(trait.getTraitCategory());
    }

    private static List<TraitMap> applyDelta(List<TraitMap> base, List<TraitMap> delta) {
        List<TraitMap> current = new ArrayList<>(Optional.ofNullable(base).orElse(List.of()));
        for (TraitMap component : Optional.ofNullable(delta).orElse(List.of())) {
            TraitMap stripped = stripStatusTrait(component);
            ComponentKey key = ComponentKey.from(stripped);
            int index = (key != null) ? findIndex(current, key) : -1;
            applyDeltaComponent(current, stripped, index, component.firstValueOfType(StatusTrait.class));
        }
        return current;
    }

    private static void applyDeltaComponent(
            List<TraitMap> current,
            TraitMap stripped,
            int index,
            AttributeStatus status) {
        AttributeStatus.Enumerated operation = status != null ? status.getEnum() : null;
        if (operation == AttributeStatus.Enumerated.removed) {
            removeAt(current, index);
            return;
        }
        if (operation == AttributeStatus.Enumerated.modified) {
            replaceAt(current, index, stripped);
            return;
        }
        if (operation == AttributeStatus.Enumerated.added) {
            addIfMissing(current, index, stripped);
            return;
        }
        addIfMissing(current, index, stripped);
    }

    private static void removeAt(List<TraitMap> current, int index) {
        if (index >= 0) {
            current.remove(index);
        }
    }

    private static void replaceAt(List<TraitMap> current, int index, TraitMap stripped) {
        removeAt(current, index);
        current.add(stripped);
    }

    private static void addIfMissing(List<TraitMap> current, int index, TraitMap stripped) {
        if (index < 0) {
            current.add(stripped);
        }
    }

    private static TraitMap stripStatusTrait(TraitMap traits) {
        TraitMap.TraitMapBuilder builder = TraitMap.builder();
        for (Trait<?, ?> trait : TraitCollection.from(traits)) {
            if (!(trait instanceof StatusTrait)) {
                builder.trait(trait);
            }
        }
        return builder.build();
    }

    private static int findIndex(List<TraitMap> haystack, ComponentKey key) {
        for (int i = 0; i < haystack.size(); i++) {
            if (key.equals(ComponentKey.from(haystack.get(i)))) {
                return i;
            }
        }
        return -1;
    }

    private record ComponentKey(String registryOid, String classValueHex, String manufacturer, String model) {
        static ComponentKey from(TraitMap traits) {
            String registry = componentRegistryOid(traits);
            String classValue = componentClassValueHex(traits);
            String manufacturer = componentManufacturer(traits);
            String model = componentModel(traits);
            if (registry == null || classValue == null || manufacturer == null || model == null) {
                return null;
            }
            return new ComponentKey(registry, classValue, manufacturer, model);
        }
    }

    private static String summarize(TraitMap traits) {
        return "registry=" + Optional.ofNullable(componentRegistryOid(traits)).orElse("?")
                + ", class=" + Optional.ofNullable(componentClassValueHex(traits)).orElse("?")
                + ", manufacturer=" + Optional.ofNullable(componentManufacturer(traits)).orElse("?")
                + ", model=" + Optional.ofNullable(componentModel(traits)).orElse("?");
    }

    private static String componentManufacturer(TraitMap traits) {
        return TraitCollection.from(traits)
                .firstStringWithCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                .orElse(null);
    }

    private static String componentModel(TraitMap traits) {
        return TraitCollection.from(traits)
                .firstStringWithCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                .orElse(null);
    }

    private static String componentRegistryOid(TraitMap traits) {
        return TraitCollection.from(traits).stream()
                .filter(ComponentClassTrait.class::isInstance)
                .map(ComponentClassTrait.class::cast)
                .map(ComponentClassTrait::getTraitRegistry)
                .map(ASN1ObjectIdentifier::getId)
                .findFirst()
                .orElse(null);
    }

    private static String componentClassValueHex(TraitMap traits) {
        return Optional.ofNullable(traits.firstValueOfType(ComponentClassTrait.class))
                .map(value -> HexNormalizer.toHexString(value.getOctets()))
                .orElse(null);
    }
}
