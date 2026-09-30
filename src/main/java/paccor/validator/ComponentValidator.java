package paccor.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import paccor.cert.CertType;
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
    private static final Logger LOGGER = Logger.getLogger(ComponentValidator.class.getName());

    private ComponentValidator() {}

    /**
     * Compare the expected and actual components.
     * @param expected Expected components (hardware manifest).
     * @param actual Actual components (certificate, after delta materialization).
     * @param matcher Component matcher.
     * @return ComponentValidationReport
     */
    public static ComponentValidationReport compareComponents(List<TraitMap> expected, List<TraitMap> actual, ComponentMatcher matcher) {
        List<TraitMap> exp = Optional.ofNullable(expected).orElse(List.of());
        List<TraitMap> act = Optional.ofNullable(actual).orElse(List.of());
        List<String> issues = new ArrayList<>();
        if (exp.size() != act.size()) {
            issues.add("Expected " + exp.size() + " component(s) but certificate materialized to " + act.size() + ".");
        }
        ComponentMatcher.MatchResult result = matcher.match(exp, act);
        result.unmatchedExpected().forEach(component ->
                issues.add("Component not found in certificate: " + summarize(component)));
        result.unmatchedActual().forEach(component ->
                issues.add("Certificate component not found on platform: " + summarize(component)));
        return new ComponentValidationReport(issues.isEmpty(), issues);
    }

    /**
     * Materialize the components from the base and deltas.
     * @param base Base platform configuration.
     * @param deltas List of delta platform configurations.
     * @param matcher Component matcher used to identify the component a delta entry refers to.
     * @return Materialized platform configuration, or empty if a delta could not be applied.
     */
    public static Optional<PlatformConfigurationV3> materializeComponents(
            PlatformConfigurationV3 base,
            List<PlatformConfigurationV3> deltas,
            ComponentMatcher matcher) {
        List<TraitMap> current = new ArrayList<>(PlatformConfigurationNormalizer.componentsForValidation(base));
        for (PlatformConfigurationV3 delta : Optional.ofNullable(deltas).orElse(List.of())) {
            for (TraitMap component : PlatformConfigurationNormalizer.componentsForValidation(delta)) {
                Optional<String> problem = applyDeltaComponent(current, component, matcher);
                if (problem.isPresent()) {
                    LOGGER.warning("Could not apply delta component (" + summarize(component) + "): " + problem.get());
                    return Optional.empty();
                }
            }
        }
        return Optional.of(PlatformConfigurationV3.builder().platformComponents(current).build());
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

    /**
     * Determine the certificate type a PreviousPlatformCertificates entry declares.
     * @param trait CertificateIdentifierTrait
     * @return The declared type, or empty if the category is not a recognized platform certificate category.
     */
    public static Optional<CertType> certTypeOf(CertificateIdentifierTrait trait) {
        if (isBaseTrait(trait)) {
            return Optional.of(CertType.BASE);
        }
        if (isDeltaTrait(trait)) {
            return Optional.of(CertType.DELTA);
        }
        if (isRebaseTrait(trait)) {
            return Optional.of(CertType.REBASE);
        }
        return Optional.empty();
    }

    private static Optional<String> applyDeltaComponent(List<TraitMap> current, TraitMap component, ComponentMatcher matcher) {
        AttributeStatus status = component.firstValueOfType(StatusTrait.class);
        if (status == null || status.getEnum() == null) {
            return Optional.of("delta components must carry a status");
        }
        TraitMap stripped = stripStatusTrait(component);
        if (status.getEnum() == AttributeStatus.Enumerated.added) {
            current.add(stripped);
            return Optional.empty();
        }
        if (!matcher.hasIdentity(stripped)) {
            return Optional.of(status.getEnum() + " component is missing its class, manufacturer, or model");
        }
        int index = findIndex(current, stripped, matcher);
        if (index < 0) {
            return Optional.of(status.getEnum() + " component does not match any component in the previous configuration");
        }
        current.remove(index);
        if (status.getEnum() == AttributeStatus.Enumerated.modified) {
            current.add(stripped);
        }
        return Optional.empty();
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

    private static int findIndex(List<TraitMap> haystack, TraitMap needle, ComponentMatcher matcher) {
        for (int i = 0; i < haystack.size(); i++) {
            if (matcher.sameIdentity(haystack.get(i), needle)) {
                return i;
            }
        }
        return -1;
    }

    private static String summarize(TraitMap traits) {
        return "registry=" + Optional.ofNullable(componentRegistryOid(traits)).orElse("?")
                + ", class=" + Optional.ofNullable(componentClassValueHex(traits)).orElse("?")
                + ", manufacturer=" + Optional.ofNullable(componentManufacturer(traits)).orElse("?")
                + ", model=" + Optional.ofNullable(componentModel(traits)).orElse("?")
                + ", serial=" + Optional.ofNullable(componentSerial(traits)).orElse("?");
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

    private static String componentSerial(TraitMap traits) {
        return TraitCollection.from(traits)
                .firstStringWithCategory(TCGObjectIdentifier.tcgTrCatComponentSerial)
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
