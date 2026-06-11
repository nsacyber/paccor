package paccor.json.schema;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.tcg.credential.ComponentAddressType;
import paccor.tcg.credential.EnumWithStringValue;

public final class ComponentSchema {
    public static final String COMPONENT_CLASS = "componentClass";
    public static final String COMPONENT_CLASS_REGISTRY = "componentClassRegistry";
    public static final String COMPONENT_CLASS_VALUE = "componentClassValue";
    public static final String MANUFACTURER = "manufacturer";
    public static final String COMPONENT_MANUFACTURER = "componentManufacturer";
    public static final String MODEL = "model";
    public static final String COMPONENT_MODEL = "componentModel";
    public static final String SERIAL = "serial";
    public static final String COMPONENT_SERIAL = "componentSerial";
    public static final String REVISION = "revision";
    public static final String COMPONENT_REVISION = "componentRevision";
    public static final String MANUFACTURER_ID = "manufacturerId";
    public static final String COMPONENT_MANUFACTURER_ID = "componentManufacturerId";
    public static final String FIELD_REPLACEABLE = "fieldReplaceable";
    public static final String ADDRESSES = "addresses";
    public static final String COMPONENT_ADDRESSES = "componentAddresses";
    public static final String PLATFORM_CERT = "platformCert";
    public static final String COMPONENT_PLATFORM_CERT = "componentPlatformCert";
    public static final String PLATFORM_CERT_URI = "platformCertUri";
    public static final String COMPONENT_PLATFORM_CERT_URI = "componentPlatformCertUri";
    public static final String TRAITS = "traits";
    public static final String STATUS = "status";
    public static final String ADDRESS_TYPE = "addressType";
    public static final String ADDRESS_VALUE = "addressValue";
    public static final String PROPERTY_NAME = "propertyName";
    public static final String PROPERTY_VALUE = "propertyValue";
    public static final String PROPERTY_STATUS = "propertyStatus";
    public static final String UNIFORM_RESOURCE_IDENTIFIER = "uniformResourceIdentifier";
    public static final String HASH_VALUE = "hashValue";
    public static final String HASH_ALGORITHM = "hashAlgorithm";
    public static final String HASH_ALG = "hashAlg";
    public static final String HASH = "hash";
    public static final String HASH_OVER_SIGNATURE_VALUE = "hashOverSignatureValue";
    public static final String HASHED_CERT_IDENTIFIER = "hashedCertIdentifier";
    public static final String ATTRIBUTE_CERT_IDENTIFIER = "attributeCertIdentifier";
    public static final String GENERIC_CERT_IDENTIFIER = "genericCertIdentifier";
    public static final String ISSUER = "issuer";

    private ComponentSchema() {}

    @Getter
    public enum Field implements JsonSchemaField {
        COMPONENT_CLASS_FIELD(ComponentSchema.COMPONENT_CLASS, List.of(),
                "Component class identifying the registry and class value for this component."),
        MANUFACTURER_FIELD(ComponentSchema.MANUFACTURER, List.of(ComponentSchema.COMPONENT_MANUFACTURER),
                "Component manufacturer."),
        MODEL_FIELD(ComponentSchema.MODEL, List.of(ComponentSchema.COMPONENT_MODEL),
                "Component model."),
        SERIAL_FIELD(ComponentSchema.SERIAL, List.of(ComponentSchema.COMPONENT_SERIAL),
                "Component serial number."),
        REVISION_FIELD(ComponentSchema.REVISION, List.of(ComponentSchema.COMPONENT_REVISION),
                "Component revision."),
        MANUFACTURER_ID_FIELD(ComponentSchema.MANUFACTURER_ID, List.of(ComponentSchema.COMPONENT_MANUFACTURER_ID),
                "Component manufacturer Private Enterprise Number."),
        FIELD_REPLACEABLE_FIELD(ComponentSchema.FIELD_REPLACEABLE, List.of(),
                "Boolean indicating whether the component is field-replaceable."),
        ADDRESSES_FIELD(ComponentSchema.ADDRESSES, List.of(ComponentSchema.COMPONENT_ADDRESSES),
                "List of component addresses (e.g., MAC addresses)."),
        PLATFORM_CERT_FIELD(ComponentSchema.PLATFORM_CERT, List.of(ComponentSchema.COMPONENT_PLATFORM_CERT),
                "Component certificate identifier."),
        PLATFORM_CERT_URI_FIELD(ComponentSchema.PLATFORM_CERT_URI, List.of(ComponentSchema.COMPONENT_PLATFORM_CERT_URI),
                "Platform configuration URI."),
        TRAITS_FIELD(ComponentSchema.TRAITS, List.of(),
                "Trait collection accepted for explicit or supplemental component traits."),
        STATUS_FIELD(ComponentSchema.STATUS, List.of(),
                "Status: ADDED, MODIFIED, or REMOVED. For delta credentials.");

        private final String jsonName;
        private final List<String> aliases;
        private final String descriptionText;

        Field(String jsonName, List<String> aliases, String descriptionText) {
            this.jsonName = jsonName;
            this.aliases = aliases;
            this.descriptionText = descriptionText;
        }

        @Override
        public String description() {
            return descriptionText;
        }
    }

    @Getter
    public enum ComponentClassField implements JsonSchemaField {
        COMPONENT_CLASS_REGISTRY_FIELD(ComponentSchema.COMPONENT_CLASS_REGISTRY,
                "OID identifying the component class registry."),
        COMPONENT_CLASS_VALUE_FIELD(ComponentSchema.COMPONENT_CLASS_VALUE,
                "Registry-defined component class value.");

        private final String jsonName;
        private final String descriptionText;

        ComponentClassField(String jsonName, String descriptionText) {
            this.jsonName = jsonName;
            this.descriptionText = descriptionText;
        }

        @Override
        public String description() {
            return descriptionText;
        }
    }

    @Getter
    public enum AddressField implements JsonSchemaField {
        ADDRESS_TYPE_FIELD(ComponentSchema.ADDRESS_TYPE,
                "Address type selector for the canonical address object."),
        ADDRESS_VALUE_FIELD(ComponentSchema.ADDRESS_VALUE,
                "Normalized address value.");

        private final String jsonName;
        private final String descriptionText;

        AddressField(String jsonName, String descriptionText) {
            this.jsonName = jsonName;
            this.descriptionText = descriptionText;
        }

        @Override
        public String description() {
            return descriptionText;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum AddressTypeValue implements EnumWithStringValue, JsonSchemaValue {
        ETHERNET_MAC("ethernetMac", ComponentAddressType.ETHERNETMAC.getValue(), "Ethernet MAC address."),
        WLAN_MAC("wlanMac", ComponentAddressType.WLANMAC.getValue(), "WLAN MAC address."),
        BLUETOOTH_MAC("bluetoothMac", ComponentAddressType.BLUETOOTHMAC.getValue(), "Bluetooth MAC address.");

        private final String jsonValue;
        private final String value;
        private final String description;

        public ASN1ObjectIdentifier oid() {
            return new ASN1ObjectIdentifier(value);
        }

        @Override
        public String asn1Value() {
            return value;
        }
    }

    @Getter
    public enum PropertyField implements JsonSchemaField {
        PROPERTY_NAME_FIELD(ComponentSchema.PROPERTY_NAME,
                "Property name."),
        PROPERTY_VALUE_FIELD(ComponentSchema.PROPERTY_VALUE,
                "Property value."),
        PROPERTY_STATUS_FIELD(ComponentSchema.PROPERTY_STATUS,
                "Optional attribute status indicating whether the property was added, modified, or removed.");

        private final String jsonName;
        private final String descriptionText;

        PropertyField(String jsonName, String descriptionText) {
            this.jsonName = jsonName;
            this.descriptionText = descriptionText;
        }

        @Override
        public String description() {
            return descriptionText;
        }
    }

    @Getter
    public enum UriReferenceField implements JsonSchemaField {
        UNIFORM_RESOURCE_IDENTIFIER_FIELD(ComponentSchema.UNIFORM_RESOURCE_IDENTIFIER),
        HASH_ALGORITHM_FIELD(ComponentSchema.HASH_ALGORITHM, List.of(ComponentSchema.HASH_ALG)),
        HASH_VALUE_FIELD(ComponentSchema.HASH_VALUE);

        private final String jsonName;
        private final List<String> aliases;

        UriReferenceField(String jsonName) {
            this(jsonName, List.of());
        }

        UriReferenceField(String jsonName, List<String> aliases) {
            this.jsonName = jsonName;
            this.aliases = aliases;
        }
    }

    @Getter
    public enum CertificateIdentifierField implements JsonSchemaField {
        HASHED_CERT_IDENTIFIER_FIELD(ComponentSchema.HASHED_CERT_IDENTIFIER,
                List.of(ComponentSchema.ATTRIBUTE_CERT_IDENTIFIER)),
        GENERIC_CERT_IDENTIFIER_FIELD(ComponentSchema.GENERIC_CERT_IDENTIFIER);

        private final String jsonName;
        private final List<String> aliases;

        CertificateIdentifierField(String jsonName) {
            this(jsonName, List.of());
        }

        CertificateIdentifierField(String jsonName, List<String> aliases) {
            this.jsonName = jsonName;
            this.aliases = aliases;
        }
    }

    @Getter
    public enum HashedCertificateField implements JsonSchemaField {
        HASH_ALGORITHM_FIELD(ComponentSchema.HASH_ALGORITHM, List.of(ComponentSchema.HASH_ALG)),
        HASH_FIELD(ComponentSchema.HASH, List.of(ComponentSchema.HASH_OVER_SIGNATURE_VALUE, ComponentSchema.HASH_VALUE));

        private final String jsonName;
        private final List<String> aliases;

        HashedCertificateField(String jsonName) {
            this(jsonName, List.of());
        }

        HashedCertificateField(String jsonName, List<String> aliases) {
            this.jsonName = jsonName;
            this.aliases = aliases;
        }
    }

    @Getter
    public enum IssuerSerialField implements JsonSchemaField {
        ISSUER_FIELD(ComponentSchema.ISSUER),
        SERIAL_FIELD(ComponentSchema.SERIAL);

        private final String jsonName;

        IssuerSerialField(String jsonName) {
            this.jsonName = jsonName;
        }
    }
}
