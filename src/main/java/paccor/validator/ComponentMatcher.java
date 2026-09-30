package paccor.validator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import paccor.normalization.ComponentIdentifierV2Converter;
import paccor.normalization.PlatformConfigurationNormalizer;
import paccor.normalization.StringSynonymTranslator;
import paccor.normalization.TraitValueTranslator;
import paccor.normalization.pci.PciFieldTranslator;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.tcg.credential.ComponentClassTrait;
import paccor.tcg.credential.ComponentIdentifierV2;
import paccor.tcg.credential.PlatformConfigurationV2;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.Trait;
import paccor.tcg.credential.TraitCollection;
import paccor.tcg.credential.TraitMap;

/**
 * Translator-driven component matcher for both V2 and V3 configurations.
 * Each expected component is paired with a distinct actual component whose traits contain the
 * expected component's traits. Callers decide whether unpaired actual components are acceptable.
 */
public final class ComponentMatcher {
    public static final ASN1ObjectIdentifier PCI_REGISTRY_OID = TCGObjectIdentifier.tcgRegistryComponentClassPcie.intern();

    private static final Set<ASN1ObjectIdentifier> STRING_SYNONYM_CATEGORIES = Set.of(
            TCGObjectIdentifier.tcgTrCatPlatformManufacturer,
            TCGObjectIdentifier.tcgTrCatPlatformModel,
            TCGObjectIdentifier.tcgTrCatPlatformSerial,
            TCGObjectIdentifier.tcgTrCatComponentManufacturer,
            TCGObjectIdentifier.tcgTrCatComponentModel,
            TCGObjectIdentifier.tcgTrCatComponentSerial,
            TCGObjectIdentifier.tcgTrCatComponentRevision
    );

    private static final Set<ASN1ObjectIdentifier> CASE_INSENSITIVE_CATEGORIES = Set.of(
            TCGObjectIdentifier.tcgTrCatPlatformManufacturer,
            TCGObjectIdentifier.tcgTrCatPlatformModel,
            TCGObjectIdentifier.tcgTrCatComponentManufacturer,
            TCGObjectIdentifier.tcgTrCatComponentModel
    );

    private static final Set<ASN1ObjectIdentifier> IDENTITY_CATEGORIES = Set.of(
            TCGObjectIdentifier.tcgTrCatComponentClass,
            TCGObjectIdentifier.tcgTrCatComponentManufacturer,
            TCGObjectIdentifier.tcgTrCatComponentModel,
            TCGObjectIdentifier.tcgTrCatComponentSerial
    );

    private static final List<TraitValueTranslator> STANDARD_TRANSLATORS = List.of(
            new StringSynonymTranslator(STRING_SYNONYM_CATEGORIES, CASE_INSENSITIVE_CATEGORIES),
            new PciFieldTranslator()
    );

    public static final ComponentMatcher RAW = new ComponentMatcher(List.of());
    public static final ComponentMatcher NORMALIZED = new ComponentMatcher(STANDARD_TRANSLATORS);

    private final List<TraitValueTranslator> translators;

    public ComponentMatcher(List<TraitValueTranslator> translators) {
        this.translators = List.copyOf(Optional.ofNullable(translators).orElse(List.of()));
    }

    public boolean matchV2(List<ComponentIdentifierV2> expected, List<ComponentIdentifierV2> actual) {
        List<TraitMap> expectedTraits = PlatformConfigurationNormalizer.componentsForValidation(
                PlatformConfigurationV2.builder().componentIdentifiers(expected).build());
        List<TraitMap> actualTraits = PlatformConfigurationNormalizer.componentsForValidation(
                PlatformConfigurationV2.builder().componentIdentifiers(actual).build());
        return matchV3(expectedTraits, actualTraits);
    }

    /**
     * Check that every expected component is matched by a distinct actual component.
     * Actual may contain additional components.
     * @param expected Expected components.
     * @param actual Actual components.
     * @return true if every expected component has its own matching actual component.
     */
    public boolean matchV3(List<TraitMap> expected, List<TraitMap> actual) {
        return match(expected, actual).unmatchedExpected().isEmpty();
    }

    /**
     * Pair expected components with actual components one-to-one. Each expected component's traits
     * must be contained in the traits of the actual component it is paired with, and no actual
     * component is paired more than once. A maximum matching is computed so that a less specific
     * expected component cannot claim an actual component that a more specific one needs.
     * @param expected Expected components.
     * @param actual Actual components.
     * @return The components left unpaired on either side.
     */
    public MatchResult match(List<TraitMap> expected, List<TraitMap> actual) {
        List<TraitMap> exp = Optional.ofNullable(expected).orElse(List.of());
        List<TraitMap> act = Optional.ofNullable(actual).orElse(List.of());
        List<List<CanonicalTrait>> expCanonical = exp.stream().map(this::canonicalComponent).toList();
        List<List<CanonicalTrait>> actCanonical = act.stream().map(this::canonicalComponent).toList();

        List<List<Integer>> candidates = new ArrayList<>();
        for (List<CanonicalTrait> required : expCanonical) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < actCanonical.size(); j++) {
                if (multisetContains(actCanonical.get(j), required)) {
                    row.add(j);
                }
            }
            candidates.add(row);
        }

        int[] actualOwner = new int[act.size()];
        Arrays.fill(actualOwner, -1);
        List<TraitMap> unmatchedExpected = new ArrayList<>();
        for (int i = 0; i < exp.size(); i++) {
            if (!assign(i, candidates, actualOwner, new boolean[act.size()])) {
                unmatchedExpected.add(exp.get(i));
            }
        }
        List<TraitMap> unmatchedActual = new ArrayList<>();
        for (int j = 0; j < act.size(); j++) {
            if (actualOwner[j] < 0) {
                unmatchedActual.add(act.get(j));
            }
        }
        return new MatchResult(unmatchedExpected, unmatchedActual);
    }

    /**
     * Check whether a component carries the identifying fields needed to locate it in another
     * configuration: a component class, manufacturer, and model.
     * @param component Component traits.
     * @return true if the component can be identified.
     */
    public boolean hasIdentity(TraitMap component) {
        TraitCollection traits = TraitCollection.from(normalizeTraitMap(component));
        return traits.firstTrait(ComponentClassTrait.class).isPresent()
                && traits.containsCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                && traits.containsCategory(TCGObjectIdentifier.tcgTrCatComponentModel);
    }

    /**
     * Check whether two components have the same identity (component class, manufacturer, model,
     * and serial) after this matcher's normalization.
     * @param left Component traits.
     * @param right Component traits.
     * @return true if both components can be identified and their identities are equal.
     */
    public boolean sameIdentity(TraitMap left, TraitMap right) {
        if (!hasIdentity(left) || !hasIdentity(right)) {
            return false;
        }
        List<CanonicalTrait> a = identityTraits(left);
        List<CanonicalTrait> b = identityTraits(right);
        return a.size() == b.size() && multisetContains(a, b);
    }

    private boolean assign(int expectedIndex, List<List<Integer>> candidates, int[] actualOwner, boolean[] visited) {
        for (int actualIndex : candidates.get(expectedIndex)) {
            if (visited[actualIndex]) {
                continue;
            }
            visited[actualIndex] = true;
            if (actualOwner[actualIndex] < 0
                    || assign(actualOwner[actualIndex], candidates, actualOwner, visited)) {
                actualOwner[actualIndex] = expectedIndex;
                return true;
            }
        }
        return false;
    }

    private List<CanonicalTrait> canonicalComponent(TraitMap component) {
        return canonicalTraits(TraitCollection.from(normalizeTraitMap(component)));
    }

    private List<CanonicalTrait> identityTraits(TraitMap component) {
        return canonicalComponent(component).stream()
                .filter(trait -> IDENTITY_CATEGORIES.contains(trait.traitCategory()))
                .toList();
    }

    private TraitMap normalizeTraitMap(TraitMap traits) {
        return ComponentIdentifierV2Converter.normalizeTraitMap(traits);
    }

    private List<CanonicalTrait> canonicalTraits(TraitCollection traits) {
        List<CanonicalTrait> out = new ArrayList<>();
        for (Trait<?, ?> trait : traits) {
            if (trait == null) {
                continue;
            }
            ASN1ObjectIdentifier traitId = trait.getTraitId();
            ASN1ObjectIdentifier category = trait.getTraitCategory();
            ASN1ObjectIdentifier registry = trait.getTraitRegistry();
            ASN1Object value = applyTranslators(traitId, category, registry, trait.getTraitValue());
            out.add(new CanonicalTrait(traitId, category, registry, value));
        }
        return out;
    }

    private ASN1Object applyTranslators(
            ASN1ObjectIdentifier traitId,
            ASN1ObjectIdentifier traitCategory,
            ASN1ObjectIdentifier traitRegistry,
            ASN1Object rawValue) {
        ASN1Object current = rawValue;
        for (TraitValueTranslator translator : translators) {
            try {
                if (translator.supports(traitId, traitCategory, traitRegistry)) {
                    ASN1Object next = translator.translate(traitId, traitCategory, traitRegistry, current);
                    if (next != null) {
                        current = next;
                    }
                }
            } catch (Throwable ignored) {
                // Translators are best-effort normalization only.
            }
        }
        return current;
    }

    private boolean multisetContains(List<CanonicalTrait> actual, List<CanonicalTrait> expected) {
        List<CanonicalTrait> copy = new ArrayList<>(actual);
        for (CanonicalTrait required : expected) {
            boolean matched = false;
            for (int i = 0; i < copy.size(); i++) {
                if (Objects.equals(required, copy.get(i))) {
                    copy.remove(i);
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return true;
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

    private record CanonicalTrait(
            ASN1ObjectIdentifier traitId,
            ASN1ObjectIdentifier traitCategory,
            ASN1ObjectIdentifier traitRegistry,
            ASN1Object traitValue) {}
}
