package paccor.normalization;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1UTF8String;
import paccor.tcg.credential.AttributeStatus;
import paccor.tcg.credential.BooleanTrait;
import paccor.tcg.credential.CertificateIdentifier;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.ComponentClass;
import paccor.tcg.credential.ComponentAddress;
import paccor.tcg.credential.ComponentClassTrait;
import paccor.tcg.credential.ComponentIdentifierV11Trait;
import paccor.tcg.credential.ComponentIdentifierV2;
import paccor.tcg.credential.NetworkMACTrait;
import paccor.tcg.credential.PENTrait;
import paccor.tcg.credential.StatusTrait;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.Trait;
import paccor.tcg.credential.TraitCollection;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.URIReference;
import paccor.tcg.credential.URITrait;
import paccor.tcg.credential.UTF8StringTrait;

/**
 * Utility for converting ComponentIdentifierV2 to V3 TraitMap format.
 * Enables unified V3 comparison logic for both V2 and V3 components.
 * Each V2 field becomes an appropriate Trait.
 * The componentClassRegistry from V2 is used as the traitRegistry for each field trait,
 * enabling registry-aware translators (e.g., PciFieldTranslator) to activate.
 */
public final class ComponentIdentifierV2Converter {

    private ComponentIdentifierV2Converter() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Convert a ComponentIdentifierV2 to a TraitMap with decomposed field traits.
     * Each field (manufacturer, model, serial, etc.) becomes a separate trait.
     * The componentClassRegistry is used as the traitRegistry for all field traits.
     *
     * @param v2 ComponentIdentifierV2 to convert
     * @return TraitMap containing decomposed traits
     */
    public static TraitMap toTraitMap(ComponentIdentifierV2 v2) {
        if (v2 == null) {
            return null;
        }

        ASN1ObjectIdentifier registry = v2.getComponentClass().getComponentClassRegistry();
        TraitMap.TraitMapBuilder builder = TraitMap.builder();

        builder.trait(componentClassTrait(v2, registry));
        addUtf8Trait(builder, registry, TCGObjectIdentifier.tcgTrCatComponentManufacturer, v2.getComponentManufacturer());
        addUtf8Trait(builder, registry, TCGObjectIdentifier.tcgTrCatComponentModel, v2.getComponentModel());
        addUtf8Trait(builder, registry, TCGObjectIdentifier.tcgTrCatComponentSerial, v2.getComponentSerial());
        addUtf8Trait(builder, registry, TCGObjectIdentifier.tcgTrCatComponentRevision, v2.getComponentRevision());
        addPenTrait(builder, registry, v2.getComponentManufacturerId());
        addBooleanTrait(builder, registry, v2.getFieldReplaceable());
        addAddresses(builder, registry, v2.getComponentAddresses());
        addCertificateTrait(builder, registry, v2.getComponentPlatformCert());
        addUriTrait(builder, registry, v2.getComponentPlatformCertUri());
        addStatusTrait(builder, registry, v2.getStatus());

        return builder.build();
    }

    /**
     * Wrap a ComponentIdentifierV2 as a single V11 trait.
     * This preserves legacy V3 encoding that embeds the full V2 component as a single trait.
     *
     * @param v2 ComponentIdentifierV2 to wrap
     * @return TraitMap containing a single ComponentIdentifierV11Trait
     */
    public static TraitMap toV11TraitMap(ComponentIdentifierV2 v2) {
        if (v2 == null) {
            return null;
        }
        return TraitMap.builder()
                .trait(ComponentIdentifierV11Trait.fromComponentIdentifierV2(v2))
                .build();
    }

    /**
     * Decompose a ComponentIdentifierV11Trait into separate field traits.
     * Extracts the ComponentIdentifierV2 from the V11 trait wrapper and converts it.
     *
     * @param v11Trait ComponentIdentifierV11Trait containing a V2 component
     * @return TraitMap with decomposed field traits
     */
    public static TraitMap decomposeV11Trait(ComponentIdentifierV11Trait v11Trait) {
        if (v11Trait == null) {
            return null;
        }

        ComponentIdentifierV2 v2 = v11Trait.getTraitValue();
        return toTraitMap(v2);
    }

    /**
     * Normalize a TraitMap by expanding any ComponentIdentifierV11Trait into decomposed field traits.
     * Preserves any other traits present in the map.
     *
     * @param ts TraitMap to normalize
     * @return Normalized TraitMap (or original if no V11 trait found)
     */
    public static TraitMap normalizeTraitMap(TraitMap ts) {
        if (ts == null) {
            return null;
        }

        TraitCollection traits = TraitCollection.from(ts);
        boolean hasV11 = traits.stream().anyMatch(t -> t instanceof ComponentIdentifierV11Trait);
        if (!hasV11) {
            return ts;
        }

        TraitMap.TraitMapBuilder builder = TraitMap.builder();
        for (Trait<?, ?> t : traits) {
            if (t instanceof ComponentIdentifierV11Trait v11) {
                TraitMap decomposed = decomposeV11Trait(v11);
                TraitCollection.from(decomposed).forEach(builder::trait);
            } else {
                builder.trait(t);
            }
        }
        return builder.build();
    }

    /**
     * Convert a decomposed TraitMap into a single ComponentIdentifierV11Trait when all traits
     * use the same non-null registry and the component can be reconstructed as V2.
     *
     * @param ts TraitMap to encode for certificate output
     * @return V11-wrapped TraitMap when possible, otherwise the original TraitMap
     */
    public static TraitMap toCertificateTraitMap(TraitMap ts) {
        if (ts == null) {
            return null;
        }

        TraitCollection traits = TraitCollection.from(ts);
        if (traits.isEmpty()) {
            return ts;
        }

        boolean hasV11 = traits.stream().anyMatch(t -> t instanceof ComponentIdentifierV11Trait);
        if (hasV11) {
            return ts;
        }

        List<ASN1ObjectIdentifier> registries = traits.stream()
                .filter(Objects::nonNull)
                .map(Trait::getTraitRegistry)
                .distinct()
                .toList();
        if (registries.size() != 1) {
            return ts;
        }

        ComponentIdentifierV2 component = fromTraitMap(ts);
        if (component == null) {
            return ts;
        }

        ASN1ObjectIdentifier sharedRegistry = registries.getFirst();
        ComponentIdentifierV2 encodedComponent = component.toBuilder()
                .componentClass(new ComponentClass(
                        sharedRegistry,
                        component.getComponentClass().getComponentClassValue()))
                .build();
        return toV11TraitMap(encodedComponent);
    }

    /**
     * Convert a TraitMap (decomposed or V11-wrapped) to ComponentIdentifierV2.
     * If a ComponentIdentifierV11Trait is present, it is preferred.
     *
     * @param ts TraitMap instance
     * @return ComponentIdentifierV2 or null if required fields are missing
     */
    public static ComponentIdentifierV2 fromTraitMap(TraitMap ts) {
        if (ts == null) {
            return null;
        }

        TraitCollection traits = TraitCollection.from(ts);
        ComponentIdentifierV2 embedded = findEmbeddedV11Trait(traits);
        if (embedded != null) {
            return embedded;
        }

        ComponentIdentifierV2.ComponentIdentifierV2Builder builder = ComponentIdentifierV2.builder();
        traits.forEach(trait -> applyTrait(builder, trait));

        try {
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    private static ComponentIdentifierV2 findEmbeddedV11Trait(TraitCollection traits) {
        return traits.stream()
                .filter(ComponentIdentifierV11Trait.class::isInstance)
                .map(ComponentIdentifierV11Trait.class::cast)
                .map(ComponentIdentifierV11Trait::getTraitValue)
                .findFirst()
                .orElse(null);
    }

    private static void applyTrait(ComponentIdentifierV2.ComponentIdentifierV2Builder builder, Trait<?, ?> trait) {
        if (trait == null) {
            return;
        }
        if (applyObjectTrait(builder, trait)) {
            return;
        }
        applyCategoryTrait(builder, trait);
    }

    private static boolean applyObjectTrait(
            ComponentIdentifierV2.ComponentIdentifierV2Builder builder,
            Trait<?, ?> trait) {
        if (trait instanceof CertificateIdentifierTrait cert) {
            builder.componentPlatformCert(cert.getTraitValue());
            return true;
        }
        if (trait instanceof URITrait uri) {
            builder.componentPlatformCertUri(uri.getTraitValue());
            return true;
        }
        return false;
    }

    private static void applyCategoryTrait(ComponentIdentifierV2.ComponentIdentifierV2Builder builder, Trait<?, ?> trait) {
        ASN1ObjectIdentifier cat = trait.getTraitCategory();
        if (TCGObjectIdentifier.tcgTrCatComponentClass.equals(cat)) {
            applyComponentClass(builder, trait);
        } else if (TCGObjectIdentifier.tcgTrCatComponentManufacturer.equals(cat)) {
            builder.componentManufacturer(utf8Value(trait));
        } else if (TCGObjectIdentifier.tcgTrCatComponentModel.equals(cat)) {
            builder.componentModel(utf8Value(trait));
        } else if (TCGObjectIdentifier.tcgTrCatComponentSerial.equals(cat)) {
            builder.componentSerial(utf8Value(trait));
        } else if (TCGObjectIdentifier.tcgTrCatComponentRevision.equals(cat)) {
            builder.componentRevision(utf8Value(trait));
        } else if (TCGObjectIdentifier.tcgTrCatPen.equals(cat)) {
            builder.componentManufacturerId(PENTrait.getInstance(trait).getTraitValue());
        } else if (TCGObjectIdentifier.tcgTrCatComponentFieldReplaceable.equals(cat)) {
            builder.fieldReplaceable(BooleanTrait.getInstance(trait).getTraitValue());
        } else if (TCGObjectIdentifier.tcgTrCatNetworkMac.equals(cat)) {
            builder.componentAddress(NetworkMACTrait.getInstance(trait).getTraitValue());
        } else if (TCGObjectIdentifier.tcgTrCatComponentStatus.equals(cat)) {
            builder.status(StatusTrait.getInstance(trait).getTraitValue());
        }
    }

    private static void applyComponentClass(
            ComponentIdentifierV2.ComponentIdentifierV2Builder builder,
            Trait<?, ?> trait) {
        ComponentClassTrait cct = ComponentClassTrait.getInstance(trait);
        ASN1OctetString value = cct.getTraitValue();
        ASN1ObjectIdentifier reg = cct.getTraitRegistry();
        builder.componentClass(new ComponentClass(reg, value));
    }

    private static ComponentClassTrait componentClassTrait(ComponentIdentifierV2 v2, ASN1ObjectIdentifier registry) {
        return ComponentClassTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentClass)
                .traitRegistry(registry)
                .traitValue(v2.getComponentClass().getComponentClassValue())
                .build();
    }

    private static void addUtf8Trait(
            TraitMap.TraitMapBuilder builder,
            ASN1ObjectIdentifier registry,
            ASN1ObjectIdentifier category,
            ASN1UTF8String value) {
        Optional.ofNullable(value)
                .map(v -> UTF8StringTrait.builder()
                        .traitCategory(category)
                        .traitRegistry(registry)
                        .traitValue(v)
                        .build())
                .ifPresent(builder::trait);
    }

    private static void addPenTrait(TraitMap.TraitMapBuilder builder, ASN1ObjectIdentifier registry, ASN1ObjectIdentifier value) {
        Optional.ofNullable(value)
                .map(v -> PENTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatPen)
                        .traitRegistry(registry)
                        .traitValue(v)
                        .build())
                .ifPresent(builder::trait);
    }

    private static void addBooleanTrait(TraitMap.TraitMapBuilder builder, ASN1ObjectIdentifier registry, ASN1Boolean value) {
        Optional.ofNullable(value)
                .map(v -> BooleanTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentFieldReplaceable)
                        .traitRegistry(registry)
                        .traitValue(v)
                        .build())
                .ifPresent(builder::trait);
    }

    private static void addAddresses(TraitMap.TraitMapBuilder builder, ASN1ObjectIdentifier registry, List<ComponentAddress> addresses) {
        Optional.ofNullable(addresses)
                .ifPresent(values -> values.forEach(value -> builder.trait(NetworkMACTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatNetworkMac)
                        .traitRegistry(registry)
                        .traitValue(value)
                        .build())));
    }

    private static void addCertificateTrait(TraitMap.TraitMapBuilder builder, ASN1ObjectIdentifier registry, CertificateIdentifier value) {
        Optional.ofNullable(value)
                .map(v -> CertificateIdentifierTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatGenericCertificate)
                        .traitRegistry(registry)
                        .traitValue(v)
                        .build())
                .ifPresent(builder::trait);
    }

    private static void addUriTrait(TraitMap.TraitMapBuilder builder, ASN1ObjectIdentifier registry, URIReference value) {
        Optional.ofNullable(value)
                .map(v -> URITrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformCertificate)
                        .traitRegistry(registry)
                        .traitValue(v)
                        .build())
                .ifPresent(builder::trait);
    }

    private static void addStatusTrait(TraitMap.TraitMapBuilder builder, ASN1ObjectIdentifier registry, AttributeStatus value) {
        Optional.ofNullable(value)
                .map(v -> StatusTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentStatus)
                        .traitRegistry(registry)
                        .traitValue(v)
                        .build())
                .ifPresent(builder::trait);
    }

    private static ASN1UTF8String utf8Value(Trait<?, ?> trait) {
        return UTF8StringTrait.getInstance(trait).getTraitValue();
    }
}
