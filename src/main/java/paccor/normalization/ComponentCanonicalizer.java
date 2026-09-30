package paccor.normalization;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.normalization.pci.PciFieldTranslator;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.Trait;
import paccor.tcg.credential.TraitCollection;
import paccor.tcg.credential.TraitMap;

/**
 * Converts components to their canonical form by applying value translators to every trait.
 */
public final class ComponentCanonicalizer {
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

    /**
     * Categories whose traits reference another certificate rather than describe the hardware.
     */
    public static final Set<ASN1ObjectIdentifier> REFERENCE_CATEGORIES = Set.of(
            TCGObjectIdentifier.tcgTrCatEkCertificate,
            TCGObjectIdentifier.tcgTrCatIakCertificate,
            TCGObjectIdentifier.tcgTrCatIdevidCertificate,
            TCGObjectIdentifier.tcgTrCatDiceCertificate,
            TCGObjectIdentifier.tcgTrCatSpdmCertificate,
            TCGObjectIdentifier.tcgTrCatPemCertificate,
            TCGObjectIdentifier.tcgTrCatPlatformCertificate,
            TCGObjectIdentifier.tcgTrCatDeltaPlatformCertificate,
            TCGObjectIdentifier.tcgTrCatRebasePlatformCertificate,
            TCGObjectIdentifier.tcgTrCatGenericCertificate
    );

    /** No translation: values compare exactly as encoded. */
    public static final ComponentCanonicalizer RAW = new ComponentCanonicalizer(List.of());

    /** Synonym, case, whitespace, and PCI ID translation. */
    public static final ComponentCanonicalizer NORMALIZED = new ComponentCanonicalizer(List.of(
            new StringSynonymTranslator(STRING_SYNONYM_CATEGORIES, CASE_INSENSITIVE_CATEGORIES),
            new PciFieldTranslator()));

    private final List<TraitValueTranslator> translators;

    public ComponentCanonicalizer(List<TraitValueTranslator> translators) {
        this.translators = List.copyOf(Optional.ofNullable(translators).orElse(List.of()));
    }

    /**
     * @param component component traits
     * @return the component with every trait translated and split into hardware and reference traits
     */
    public CanonicalComponent canonicalize(TraitMap component) {
        Map<Boolean, List<CanonicalTrait>> byReference = canonicalTraits(component).stream()
                .collect(Collectors.partitioningBy(trait -> REFERENCE_CATEGORIES.contains(trait.traitCategory())));
        return new CanonicalComponent(
                component,
                CanonicalComponent.count(byReference.get(false)),
                CanonicalComponent.count(byReference.get(true)));
    }

    /**
     * @param traits trait map
     * @return every trait translated, in order
     */
    public List<CanonicalTrait> canonicalTraits(TraitMap traits) {
        return TraitCollection.from(ComponentIdentifierV2Converter.normalizeTraitMap(traits)).stream()
                .filter(Objects::nonNull)
                .map(this::canonicalTrait)
                .toList();
    }

    private CanonicalTrait canonicalTrait(Trait<?, ?> trait) {
        return new CanonicalTrait(
                trait.getTraitId(),
                trait.getTraitCategory(),
                trait.getTraitRegistry(),
                translate(trait.getTraitId(), trait.getTraitCategory(), trait.getTraitRegistry(), trait.getTraitValue()));
    }

    private ASN1Object translate(ASN1ObjectIdentifier traitId, ASN1ObjectIdentifier category, ASN1ObjectIdentifier registry, ASN1Object rawValue) {
        ASN1Object current = rawValue;
        for (TraitValueTranslator translator : translators) {
            current = translateOne(translator, traitId, category, registry, current);
        }
        return current;
    }

    private static ASN1Object translateOne(TraitValueTranslator translator, ASN1ObjectIdentifier traitId,
                                           ASN1ObjectIdentifier category, ASN1ObjectIdentifier registry, ASN1Object value) {
        try {
            return translator.supports(traitId, category, registry)
                    ? Optional.ofNullable(translator.translate(traitId, category, registry, value)).orElse(value)
                    : value;
        } catch (RuntimeException ignored) {
            // Translators are best-effort normalization only.
            return value;
        }
    }
}
