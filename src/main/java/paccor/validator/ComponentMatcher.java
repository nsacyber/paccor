package paccor.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.normalization.CanonicalComponent;
import paccor.normalization.ComponentCanonicalizer;
import paccor.normalization.PlatformConfigurationNormalizer;
import paccor.normalization.TraitValueTranslator;
import paccor.tcg.credential.ComponentIdentifierV2;
import paccor.tcg.credential.PlatformConfigurationV2;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitMap;

/**
 * Matches components. Two components pair when their hardware traits are equal after
 * normalization and the {@link ComponentReferenceCheck} accepts their certificate references.
 */
public final class ComponentMatcher {
    public static final ASN1ObjectIdentifier PCI_REGISTRY_OID = TCGObjectIdentifier.tcgRegistryComponentClassPcie.intern();

    public static final ComponentMatcher RAW = new ComponentMatcher(ComponentCanonicalizer.RAW, ComponentReferenceCheck.REPORTED_MUST_MATCH);
    public static final ComponentMatcher NORMALIZED = new ComponentMatcher(ComponentCanonicalizer.NORMALIZED, ComponentReferenceCheck.REPORTED_MUST_MATCH);

    private final ComponentCanonicalizer canonicalizer;
    private final ComponentReferenceCheck referenceCheck;

    public ComponentMatcher(List<TraitValueTranslator> translators) {
        this(new ComponentCanonicalizer(translators), ComponentReferenceCheck.REPORTED_MUST_MATCH);
    }

    public ComponentMatcher(ComponentCanonicalizer canonicalizer, ComponentReferenceCheck referenceCheck) {
        this.canonicalizer = canonicalizer;
        this.referenceCheck = referenceCheck;
    }

    public boolean matchV2(List<ComponentIdentifierV2> expected, List<ComponentIdentifierV2> actual) {
        return matchV3(componentsOf(expected), componentsOf(actual));
    }

    /**
     * Check that every expected component pairs with an actual component. Actual may contain additional components.
     * @param expected Expected components.
     * @param actual Actual components.
     * @return true if every expected component is matched.
     */
    public boolean matchV3(List<TraitMap> expected, List<TraitMap> actual) {
        return match(expected, actual).unmatchedExpected().isEmpty();
    }

    /**
     * Pair expected components with actual components one-to-one.
     * @param expected Expected components (as the platform reports them).
     * @param actual Actual components (as the certificate records them).
     * @return The components left unpaired on either side.
     */
    public MatchResult match(List<TraitMap> expected, List<TraitMap> actual) {
        List<CanonicalComponent> remaining = new ArrayList<>(canonicalize(actual));
        List<TraitMap> unmatchedExpected = canonicalize(expected).stream()
                .filter(component -> !claim(component, remaining))
                .map(CanonicalComponent::source)
                .toList();
        return new MatchResult(unmatchedExpected, remaining.stream().map(CanonicalComponent::source).toList());
    }

    /**
     * Check that every trait the reported identifiers carry is present in the certified identifiers.
     * Used for platform identifiers, where the certificate may record more than the platform reports.
     * @param reported identifiers as the platform reports them
     * @param certified identifiers as the certificate records them
     * @return true if the certified identifiers include all reported ones
     */
    public boolean covers(TraitMap reported, TraitMap certified) {
        return CanonicalComponent.includes(
                CanonicalComponent.count(canonicalizer.canonicalTraits(certified)),
                CanonicalComponent.count(canonicalizer.canonicalTraits(reported)));
    }

    /**
     * @param component Component traits.
     * @return true if the component has the class, manufacturer, and model needed to identify it.
     */
    public boolean hasIdentity(TraitMap component) {
        return canonicalizer.canonicalize(component).hasIdentity();
    }

    /**
     * @param left Component traits.
     * @param right Component traits.
     * @return true if both components can be identified and their class, manufacturer, model, and
     *         serial are equal after this matcher's normalization.
     */
    public boolean sameIdentity(TraitMap left, TraitMap right) {
        return canonicalizer.canonicalize(left).sameIdentity(canonicalizer.canonicalize(right));
    }

    private boolean claim(CanonicalComponent expected, List<CanonicalComponent> remaining) {
        return remaining.stream()
                .filter(candidate -> candidate.sameHardware(expected) && referenceCheck.accepts(expected, candidate))
                .findFirst()
                .map(remaining::remove)
                .orElse(false);
    }

    private List<CanonicalComponent> canonicalize(List<TraitMap> components) {
        return Optional.ofNullable(components).orElse(List.of()).stream()
                .map(canonicalizer::canonicalize)
                .toList();
    }

    private static List<TraitMap> componentsOf(List<ComponentIdentifierV2> components) {
        return PlatformConfigurationNormalizer.componentsForValidation(
                PlatformConfigurationV2.builder().componentIdentifiers(components).build());
    }

    /**
     * Components left unpaired by {@link #match(List, List)}.
     * @param unmatchedExpected Expected components with no distinct matching actual component.
     * @param unmatchedActual Actual components not paired with any expected component.
     */
    public record MatchResult(List<TraitMap> unmatchedExpected, List<TraitMap> unmatchedActual) {
        public MatchResult {
            unmatchedExpected = List.copyOf(unmatchedExpected);
            unmatchedActual = List.copyOf(unmatchedActual);
        }

        public boolean complete() {
            return unmatchedExpected.isEmpty() && unmatchedActual.isEmpty();
        }
    }
}
