package json.schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import tcg.credential.EnumWithStringValue;
import tcg.credential.TCGObjectIdentifier;

public final class SubjectAlternativeNameSchema {
    public static final String MANUFACTURER_ID = "manufacturerId";
    public static final String PLATFORM_MANUFACTURER_ID = "platformManufacturerId";
    public static final String MANUFACTURER = "manufacturer";
    public static final String PLATFORM_MANUFACTURER = "platformManufacturerStr";
    public static final String MODEL = "model";
    public static final String PLATFORM_MODEL = "platformModel";
    public static final String SERIAL = "serial";
    public static final String PLATFORM_SERIAL = "platformSerial";
    public static final String VERSION = "version";
    public static final String PLATFORM_VERSION = "platformVersion";

    private SubjectAlternativeNameSchema() {}

    @AllArgsConstructor
    @Getter
    public enum PlatformField implements JsonSchemaField, EnumWithStringValue, JsonSchemaValue {
        PLATFORM_MANUFACTURER_FIELD(
                SubjectAlternativeNameSchema.PLATFORM_MANUFACTURER,
                TCGObjectIdentifier.tcgAtPlatformManufacturerStr.getId(),
                TCGObjectIdentifier.tcgTrCatPlatformManufacturer,
                "Platform manufacturer string.",
                List.of(SubjectAlternativeNameSchema.MANUFACTURER)),
        PLATFORM_MANUFACTURER_ID_FIELD(
                SubjectAlternativeNameSchema.PLATFORM_MANUFACTURER_ID,
                TCGObjectIdentifier.tcgAtPlatformManufacturerId.getId(),
                TCGObjectIdentifier.tcgTrCatPlatformManufactureridentifier,
                "Platform manufacturer private enterprise number.",
                List.of(SubjectAlternativeNameSchema.MANUFACTURER_ID)),
        PLATFORM_MODEL_FIELD(
                SubjectAlternativeNameSchema.PLATFORM_MODEL,
                TCGObjectIdentifier.tcgAtPlatformModel.getId(),
                TCGObjectIdentifier.tcgTrCatPlatformModel,
                "Platform model string.",
                List.of(SubjectAlternativeNameSchema.MODEL)),
        PLATFORM_SERIAL_FIELD(
                SubjectAlternativeNameSchema.PLATFORM_SERIAL,
                TCGObjectIdentifier.tcgAtPlatformSerial.getId(),
                TCGObjectIdentifier.tcgTrCatPlatformSerial,
                "Platform serial string.",
                List.of(SubjectAlternativeNameSchema.SERIAL)),
        PLATFORM_VERSION_FIELD(
                SubjectAlternativeNameSchema.PLATFORM_VERSION,
                TCGObjectIdentifier.tcgAtPlatformVersion.getId(),
                TCGObjectIdentifier.tcgTrCatPlatformVersion,
                "Platform version string.",
                List.of(SubjectAlternativeNameSchema.VERSION));

        private final String jsonName;
        private final String value;
        private final ASN1ObjectIdentifier traitCategoryOid;
        private final String description;
        private final List<String> aliases;

        public ASN1ObjectIdentifier attributeOid() {
            return new ASN1ObjectIdentifier(value);
        }

        @Override
        public String getJsonValue() {
            return jsonName;
        }

        @Override
        public List<String> aliases() {
            return JsonSchemaField.super.aliases();
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String asn1Value() {
            return value;
        }
    }
}
