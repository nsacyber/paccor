package cert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import json.schema.SubjectAlternativeNameSchema;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import tcg.credential.ASN1Utils;
import tcg.credential.EnumWithStringValue;
import tcg.credential.ManufacturerId;
import tcg.credential.PENTrait;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.Trait;
import tcg.credential.TraitCollection;
import tcg.credential.TraitMap;
import tcg.credential.UTF8StringTrait;

public final class SubjectAlternativeNameHelper {
    private SubjectAlternativeNameHelper() {}

    public static Builder builder() {
        return new Builder();
    }

    public static GeneralNames buildLegacy(TraitMap traits) {
        return builder().platformTraits(traits).buildLegacy();
    }

    public static GeneralNames buildPlatformIdentifier(TraitMap traits) {
        return builder().platformTraits(traits).buildPlatformIdentifier();
    }

    public static TraitMap extractPlatformTraits(GeneralNames san, CertSpecVersion certSpec) {
        if (san == null || san.getNames() == null) {
            return null;
        }
        TraitMap traits = certSpec == CertSpecVersion.V2_0 ? readOtherNameTraits(san) : convertRdnToTraits(san);
        if (traits == null || traits.isEmpty()) {
            traits = convertRdnToTraits(san);
        }
        if (traits == null || traits.isEmpty()) {
            traits = readOtherNameTraits(san);
        }
        return traits == null || traits.isEmpty() ? null : traits;
    }

    private static TraitMap convertRdnToTraits(GeneralNames san) {
        Map<SubjectAlternativeNameSchema.PlatformField, ASN1Encodable> values =
                new EnumMap<>(SubjectAlternativeNameSchema.PlatformField.class);
        Arrays.stream(san.getNames())
                .filter(generalName -> generalName.getTagNo() == GeneralName.directoryName)
                .flatMap(generalName -> Arrays.stream(X500Name.getInstance(generalName.getName()).getRDNs())
                        .filter(rdn -> rdn != null && rdn.getFirst() != null)
                        .map(RDN::getFirst))
                .forEach(atv -> {
                    SubjectAlternativeNameSchema.PlatformField field = EnumWithStringValue.lookupValue(
                            atv.getType().getId(), SubjectAlternativeNameSchema.PlatformField.class);
                    values.putIfAbsent(field, atv.getValue());
                });
        TraitMap.TraitMapBuilder builder = TraitMap.builder();
        for (SubjectAlternativeNameSchema.PlatformField field : new SubjectAlternativeNameSchema.PlatformField[] {
                SubjectAlternativeNameSchema.PlatformField.PLATFORM_MANUFACTURER_FIELD,
                SubjectAlternativeNameSchema.PlatformField.PLATFORM_MODEL_FIELD,
                SubjectAlternativeNameSchema.PlatformField.PLATFORM_SERIAL_FIELD,
                SubjectAlternativeNameSchema.PlatformField.PLATFORM_VERSION_FIELD,
                SubjectAlternativeNameSchema.PlatformField.PLATFORM_MANUFACTURER_ID_FIELD }) {
            ASN1Encodable value = values.get(field);
            if (value != null) {
                builder.trait(traitFromAsn1Value(value, field));
            }
        }
        return builder.build();
    }

    private static TraitMap readOtherNameTraits(GeneralNames san) {
        return Arrays.stream(san.getNames())
                .filter(generalName -> generalName.getTagNo() == GeneralName.otherName)
                .findFirst()
                .map(generalName -> ASN1Sequence.getInstance(generalName.getName()))
                .filter(otherName -> otherName.size() == 2)
                .filter(otherName -> TCGObjectIdentifier.tcgAtPlatformIdentifier.equals(ASN1Utils.getOID(otherName.getObjectAt(0))))
                .map(otherName -> ASN1TaggedObject.getInstance(otherName.getObjectAt(1)))
                .map(tagged -> TraitMap.getInstance(tagged.getBaseObject()))
                .orElse(null);
    }

    private static Trait<?, ?> traitFromAsn1Value(ASN1Encodable value, SubjectAlternativeNameSchema.PlatformField field) {
        return switch (field) {
            case PLATFORM_MANUFACTURER_ID_FIELD -> PENTrait.builder()
                    .traitCategory(field.getTraitCategoryOid())
                    .traitValue(ManufacturerId.getInstance(value).getManufacturerIdentifier())
                    .build();
            case PLATFORM_MANUFACTURER_FIELD, PLATFORM_MODEL_FIELD, PLATFORM_SERIAL_FIELD, PLATFORM_VERSION_FIELD ->
                    UTF8StringTrait.builder()
                            .traitCategory(field.getTraitCategoryOid())
                            .traitValue(ASN1Utils.getUTF8String(value))
                            .build();
        };
    }

    public static final class Builder {
        private final List<RDN> names = new ArrayList<>();
        private TraitMap platformTraits = TraitMap.builder().build();

        public Builder addRdn(RDN name) {
            if (name != null) {
                names.add(name);
            }
            return this;
        }

        public Builder addRdn(ASN1ObjectIdentifier oid, ASN1UTF8String name) {
            if (oid != null && name != null) {
                addRdn(new RDN(oid, name));
            }
            return this;
        }

        public Builder addRdn(ASN1ObjectIdentifier oid, ManufacturerId name) {
            if (oid != null && name != null) {
                addRdn(new RDN(oid, name));
            }
            return this;
        }

        public Builder platformTraits(TraitMap traits) {
            this.platformTraits = traits != null ? traits : TraitMap.builder().build();
            return this;
        }

        public TraitMap platformTraits() {
            return platformTraits;
        }

        public GeneralNames buildLegacy() {
            convertTraitsToRdn();
            if (names.isEmpty()) {
                return null;
            }
            return new GeneralNames(new GeneralName(new X500Name(names.toArray(RDN[]::new))));
        }

        public GeneralNames buildPlatformIdentifier() {
            if (!hasRequiredPlatformTraits(platformTraits)) {
                return null;
            }
            ASN1Sequence otherName = new DERSequence(new ASN1Encodable[] {
                    TCGObjectIdentifier.tcgAtPlatformIdentifier,
                    new DERTaggedObject(true, 0, platformTraits.toASN1Primitive())
            });
            return new GeneralNames(new GeneralName(GeneralName.otherName, otherName));
        }

        private void convertTraitsToRdn() {
            if (!names.isEmpty()) {
                return;
            }
            TraitCollection traits = TraitCollection.from(platformTraits);
            addRdnIfPresent(traits, SubjectAlternativeNameSchema.PlatformField.PLATFORM_MANUFACTURER_FIELD, true);
            addRdnIfPresent(traits, SubjectAlternativeNameSchema.PlatformField.PLATFORM_MODEL_FIELD, true);
            addRdnIfPresent(traits, SubjectAlternativeNameSchema.PlatformField.PLATFORM_VERSION_FIELD, true);
            addRdnIfPresent(traits, SubjectAlternativeNameSchema.PlatformField.PLATFORM_SERIAL_FIELD, true);
            traits.firstStringWithCategory(SubjectAlternativeNameSchema.PlatformField.PLATFORM_MANUFACTURER_ID_FIELD.getTraitCategoryOid())
                    .ifPresent(str -> addRdn(SubjectAlternativeNameSchema.PlatformField.PLATFORM_MANUFACTURER_ID_FIELD.attributeOid(),
                            new ManufacturerId(ASN1Utils.getOID(str))));
        }

        private void addRdnIfPresent(TraitCollection traits, SubjectAlternativeNameSchema.PlatformField field, boolean utf8) {
            traits.firstStringWithCategory(field.getTraitCategoryOid())
                    .ifPresent(str -> addRdn(field.attributeOid(), ASN1Utils.getUTF8String(str)));
        }

        private boolean hasRequiredPlatformTraits(TraitMap traits) {
            TraitCollection flatTraits = TraitCollection.from(traits);
            return flatTraits.containsCategory(TCGObjectIdentifier.tcgTrCatPlatformManufacturer)
                    && flatTraits.containsCategory(TCGObjectIdentifier.tcgTrCatPlatformModel)
                    && flatTraits.containsCategory(TCGObjectIdentifier.tcgTrCatPlatformVersion);
        }
    }
}
