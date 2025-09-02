package json.schema;

import lombok.Getter;

public final class HardwareManifestSchema {
    public static final String PLATFORM = "platform";
    public static final String COMPONENTS = "components";
    public static final String COMPONENTS_URI = "componentsUri";
    public static final String PROPERTIES = "properties";
    public static final String PROPERTIES_URI = "propertiesUri";

    private HardwareManifestSchema() {}

    @Getter
    public enum Field implements JsonSchemaField {
        PLATFORM_FIELD(HardwareManifestSchema.PLATFORM,
                "Platform identifiers accepted under the manifest PLATFORM object. Can accept fields or traits."),
        COMPONENTS_FIELD(HardwareManifestSchema.COMPONENTS,
                "Components Manifest. Each item may be a legacy component object or a trait-based component collection."),
        COMPONENTS_URI_FIELD(HardwareManifestSchema.COMPONENTS_URI,
                "URI reference for information about the components in the manifest."),
        PROPERTIES_FIELD(HardwareManifestSchema.PROPERTIES,
                "Manifest property entries."),
        PROPERTIES_URI_FIELD(HardwareManifestSchema.PROPERTIES_URI,
                "URI reference for information about the properties in the manifest.");

        private final String jsonName;
        private final String descriptionText;

        Field(String jsonName, String descriptionText) {
            this.jsonName = jsonName;
            this.descriptionText = descriptionText;
        }

        @Override
        public String description() {
            return descriptionText;
        }
    }
}
