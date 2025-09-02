package validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import normalization.ComponentIdentifierV2Converter;
import normalization.PlatformConfigurationNormalizer;
import normalization.StringSynonymTranslator;
import normalization.TraitValueTranslator;
import normalization.pci.PciFieldTranslator;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import tcg.credential.ComponentIdentifierV2;
import tcg.credential.PlatformConfigurationV2;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.Trait;
import tcg.credential.TraitCollection;
import tcg.credential.TraitMap;

/**
 * Translator-driven component matcher for both V2 and V3 configurations.
 * Matching always uses the same subset contract: expected components and traits
 * must be present in actual data, while actual data may contain additional detail.
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

    private static final List<TraitValueTranslator> STANDARD_TRANSLATORS = List.of(
            new StringSynonymTranslator(STRING_SYNONYM_CATEGORIES),
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

    public boolean matchV3(List<TraitMap> expected, List<TraitMap> actual) {
        List<TraitMap> exp = Optional.ofNullable(expected).orElse(List.of());
        List<TraitMap> act = Optional.ofNullable(actual).orElse(List.of());
        if (exp.size() > act.size()) {
            return false;
        }
        for (TraitMap component : exp) {
            if (!containsMatchingTraitMap(act, component)) {
                return false;
            }
        }
        return true;
    }

    private boolean containsMatchingTraitMap(List<TraitMap> haystack, TraitMap needle) {
        TraitCollection expected = TraitCollection.from(normalizeTraitMap(needle));
        for (TraitMap actual : haystack) {
            TraitCollection actualTraits = TraitCollection.from(normalizeTraitMap(actual));
            if (traitCollectionsMatch(actualTraits, expected)) {
                return true;
            }
        }
        return false;
    }

    private TraitMap normalizeTraitMap(TraitMap traits) {
        return ComponentIdentifierV2Converter.normalizeTraitMap(traits);
    }

    private boolean traitCollectionsMatch(TraitCollection actual, TraitCollection expected) {
        return multisetContains(canonicalTraits(actual), canonicalTraits(expected));
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

    private record CanonicalTrait(
            ASN1ObjectIdentifier traitId,
            ASN1ObjectIdentifier traitCategory,
            ASN1ObjectIdentifier traitRegistry,
            ASN1Object traitValue) {}
}
