package json;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.github.victools.jsonschema.generator.CustomDefinition;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerationContext;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaKeyword;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.jackson.JacksonSchemaModule;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationOption;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import json.docgen.GlobalAsn1DocsRegistry;
import json.docgen.ProjectDocRenderer;
import json.schema.JsonSchemaValue;
import json.schema.SubjectAlternativeNameSchema;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.KeyUsage;
import tcg.credential.ASN1BitStringEnumBase;
import tcg.credential.ASN1EnumBase;
import tcg.credential.ASN1EnumeratedEnumBase;
import tcg.credential.ComponentAddress;
import tcg.credential.ComponentAddressType;
import tcg.credential.ComponentIdentifierV2;
import tcg.credential.EnumWithIntegerValue;
import tcg.credential.PlatformConfigurationV2;
import tcg.credential.PlatformConfigurationV3;
import tcg.credential.PlatformPropertiesV2;
import tcg.credential.Trait;
import tcg.credential.TraitCollection;
import tcg.credential.TraitId;
import tcg.credential.TraitMap;
import tcg.credential.URIReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class SchemaUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String GLOBAL_DEFINITIONS_FILE = "global-definitions.json";
    private static final List<String> TRAIT_VALUE_ALIASES = TraitId.getRegisteredAliases().stream().toList();

    public static void main(String[] args) throws IOException {
        new SchemaUtils().generateSchemas();
    }

    public void generateSchemas() throws IOException {
        SchemaGenerator generator = createGenerator();
        ObjectNode mergedDefs = MAPPER.createObjectNode();
        mergedDefs.put("$id", GLOBAL_DEFINITIONS_FILE);
        mergedDefs.put("title", "Global ASN.1 Definitions");
        mergedDefs.putObject("$defs");

        Map<String, JsonNode> helperSchemas = new LinkedHashMap<>();
        helperSchemas.put("AttributesJsonHelper", generateAndCollectDefs(generator, AttributesJsonHelper.class, mergedDefs, "Attributes Schema"));
        helperSchemas.put("ExtensionsJsonHelper", generateAndCollectDefs(generator, ExtensionsJsonHelper.class, mergedDefs, "Extensions Schema"));
        collectStandaloneDefinition(generator, mergedDefs, ComponentIdentifierV2.class);
        collectStandaloneDefinition(generator, mergedDefs, PlatformConfigurationV2.class);
        collectStandaloneDefinition(generator, mergedDefs, PlatformConfigurationV3.class);
        collectStandaloneDefinition(generator, mergedDefs, PlatformPropertiesV2.class);
        collectStandaloneDefinition(generator, mergedDefs, URIReference.class);
        helperSchemas.put("HardwareManifestJsonHelper", createHardwareManifestInputSchema());

        ObjectNode hardwareManifestSchema = (ObjectNode) helperSchemas.get("HardwareManifestJsonHelper");
        ensure(hardwareManifestSchema.path("properties").has("PLATFORM"), "Hardware manifest schema must expose PLATFORM.");
        ensure(!hardwareManifestSchema.path("properties").has("pcV1"), "Hardware manifest schema should not expose legacy pcV1.");
        ensure(mergedDefs.path("$defs").path("ASN1ObjectIdentifier").has("oneOf"), "ASN1ObjectIdentifier must be rendered as a union schema.");
        ensure(mergedDefs.path("$defs").path("ASN1Boolean").has("oneOf"), "ASN1Boolean must be rendered as a union schema.");
        ensure(mergedDefs.path("$defs").path("ASN1OctetString").has("oneOf"), "ASN1OctetString must be rendered as a union schema.");

        writeSchemasIfRequested(mergedDefs, helperSchemas);
    }

    private static void ensure(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private SchemaGenerator createGenerator() {
        JacksonSchemaModule jacksonModule = new JacksonSchemaModule(
                JacksonOption.RESPECT_JSONPROPERTY_REQUIRED);
        JakartaValidationModule validationModule = new JakartaValidationModule(
                JakartaValidationOption.NOT_NULLABLE_FIELD_IS_REQUIRED,
                JakartaValidationOption.NOT_NULLABLE_METHOD_IS_REQUIRED);

        SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
                .with(jacksonModule)
                .with(validationModule)
                .with(Option.DEFINITIONS_FOR_ALL_OBJECTS);

        configBuilder.forFields().withDescriptionResolver(field -> {
            JsonPropertyDescription annotation = field.getAnnotationConsideringFieldAndGetter(JsonPropertyDescription.class);
            return annotation != null ? annotation.value() : null;
        });
        configBuilder.forFields().withDefaultResolver(field -> {
            JsonProperty annotation = field.getAnnotationConsideringFieldAndGetter(JsonProperty.class);
            if (annotation == null || annotation.defaultValue() == null || annotation.defaultValue().isBlank()) {
                return null;
            }
            return parseDefaultValue(field.getType().getErasedType(), annotation.defaultValue());
        });

        configBuilder.forTypesInGeneral().withDescriptionResolver(scope -> {
            Class<?> fieldType = scope.getType().getErasedType();
            String description = GlobalAsn1DocsRegistry.descriptionFor(fieldType);
            if (description == null && ASN1EnumBase.class.isAssignableFrom(fieldType)) {
                description = getEnumDescription(fieldType);
            }
            return description;
        });

        configBuilder.forTypesInGeneral().withTitleResolver(scope -> {
            Class<?> type = scope.getType().getErasedType();
            if (GlobalAsn1DocsRegistry.ENTRIES.containsKey(type) || ASN1EnumBase.class.isAssignableFrom(type)) {
                return type.getSimpleName();
            }
            return null;
        });

        configBuilder.forTypesInGeneral().withCustomDefinitionProvider(this::provideCustomSchema);
        return new SchemaGenerator(configBuilder.build());
    }

    private JsonNode generateAndCollectDefs(SchemaGenerator generator, Class<?> clazz, ObjectNode mergedDefs, String title) {
        ObjectNode jsonSchema = generator.generateSchema(clazz);
        jsonSchema.put("title", title);
        jsonSchema.put("$id", clazz.getSimpleName().toLowerCase().replace("jsonhelper", "") + "-schema.json");

        if (jsonSchema.has(getTag(SchemaKeyword.TAG_DEFINITIONS))) {
            ObjectNode defs = (ObjectNode) jsonSchema.get(getTag(SchemaKeyword.TAG_DEFINITIONS));
            ObjectNode mergedDefsContainer = (ObjectNode) mergedDefs.get(getTag(SchemaKeyword.TAG_DEFINITIONS));
            for (String name : defs.propertyNames()) {
                if (!mergedDefsContainer.has(name)) {
                    mergedDefsContainer.set(name, defs.get(name));
                }
            }
            jsonSchema.remove(getTag(SchemaKeyword.TAG_DEFINITIONS));
        }

        rewriteDefinitionRefs(jsonSchema, true);
        return jsonSchema;
    }

    private void collectStandaloneDefinition(SchemaGenerator generator, ObjectNode mergedDefs, Class<?> clazz) {
        ObjectNode jsonSchema = generator.generateSchema(clazz);
        if (jsonSchema.has(getTag(SchemaKeyword.TAG_DEFINITIONS))) {
            ObjectNode defs = (ObjectNode) jsonSchema.get(getTag(SchemaKeyword.TAG_DEFINITIONS));
            ObjectNode mergedDefsContainer = (ObjectNode) mergedDefs.get(getTag(SchemaKeyword.TAG_DEFINITIONS));
            for (String name : defs.propertyNames()) {
                if (!mergedDefsContainer.has(name)) {
                    mergedDefsContainer.set(name, defs.get(name));
                }
            }
            jsonSchema.remove(getTag(SchemaKeyword.TAG_DEFINITIONS));
        }
        ObjectNode mergedDefsContainer = (ObjectNode) mergedDefs.get(getTag(SchemaKeyword.TAG_DEFINITIONS));
        if (!mergedDefsContainer.has(clazz.getSimpleName())) {
            mergedDefsContainer.set(clazz.getSimpleName(), jsonSchema);
        }
    }

    private ObjectNode createHardwareManifestInputSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("$id", "hardwaremanifest-schema.json");
        schema.put("title", "Hardware Manifest");
        schema.put("description",
                "Hardware Manifest input. PLATFORM fields are platform identifiers intended for SubjectAltName.");
        schema.put("type", "object");

        ObjectNode properties = schema.putObject("properties");
        properties.set("PLATFORM", createPlatformIdentifiersSchema());
        properties.set("COMPONENTS", createComponentsArraySchema());
        properties.set("PROPERTIES", createPropertiesArraySchema());
        properties.set("COMPONENTSURI", createRefSchema("URIReference",
                "v1.1 specific option. Optional URI reference for externally hosted component lists."));
        properties.set("PROPERTIESURI", createRefSchema("URIReference",
                "v1.1 specific option. Optional URI reference for externally hosted property lists."));

        schema.put("additionalProperties", true);
        return schema;
    }

    private ObjectNode createPlatformIdentifiersSchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.put("description",
                "Platform identifiers accepted under the manifest PLATFORM object. Can accept fields or traits.");
        ObjectNode properties = schema.putObject("properties");
        properties.set("PLATFORMMANUFACTURERSTR", createRefSchema("ASN1UTF8String",
                "Platform manufacturer string."));
        properties.set("PLATFORMMANUFACTURERID", createRefSchema("ASN1ObjectIdentifier",
                "Platform manufacturer private enterprise number."));
        properties.set("PLATFORMMODEL", createRefSchema("ASN1UTF8String",
                "Platform model string."));
        properties.set("PLATFORMVERSION", createRefSchema("ASN1UTF8String",
                "Platform version string."));
        properties.set("PLATFORMSERIAL", createRefSchema("ASN1UTF8String",
                "Platform serial string."));
        properties.set("TRAITS", createRefSchema("TraitMap",
                "Platform identifier traits. These may supplement or replace the direct PLATFORM identifier fields."));
        addAliases(properties, "PLATFORMMANUFACTURERSTR", SubjectAlternativeNameSchema.PLATFORM_MANUFACTURER);
        addAliases(properties, "PLATFORMMANUFACTURERID", SubjectAlternativeNameSchema.PLATFORM_MANUFACTURER_ID);
        addAliases(properties, "PLATFORMMODEL", SubjectAlternativeNameSchema.PLATFORM_MODEL);
        addAliases(properties, "PLATFORMVERSION", SubjectAlternativeNameSchema.PLATFORM_VERSION);
        addAliases(properties, "PLATFORMSERIAL", SubjectAlternativeNameSchema.PLATFORM_SERIAL);
        schema.put("additionalProperties", true);
        return schema;
    }

    private ObjectNode createComponentsArraySchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "array");
        schema.put("description",
                "Components Manifest. Each item may be a legacy component object or a trait-based component collection.");
        ArrayNode oneOf = schema.putObject("items").putArray("oneOf");
        oneOf.add(createRefSchema("ComponentIdentifierV2",
                "Component object. Results in a ComponentIdentifierV11Trait for v2.0+."));
        oneOf.add(createRefSchema("TraitMap",
                "Trait-based component representation."));
        return schema;
    }

    private ObjectNode createPropertiesArraySchema() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "array");
        schema.put("description", "Manifest property entries.");
        schema.set("items", createRefSchema("PlatformPropertiesV2", "Property entry."));
        return schema;
    }

    private ObjectNode createRefSchema(String definitionName, String description) {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("$ref", GLOBAL_DEFINITIONS_FILE + "#/$defs/" + definitionName);
        if (description != null && !description.isBlank()) {
            schema.put("description", description);
        }
        return schema;
    }

    private void addAliases(ObjectNode properties, String propertyName, String... aliases) {
        JsonNode property = properties.get(propertyName);
        if (!(property instanceof ObjectNode objectNode) || aliases == null || aliases.length == 0) {
            return;
        }
        ArrayNode aliasArray = objectNode.putArray("x-aliases");
        Arrays.stream(aliases)
                .filter(alias -> alias != null && !alias.isBlank())
                .forEach(aliasArray::add);
    }

    private void writeSchemasIfRequested(ObjectNode mergedDefs, Map<String, JsonNode> helperSchemas) throws IOException {
        Path outputPath = resolveOutputPath();
        if (outputPath == null) {
            return;
        }

        Files.createDirectories(outputPath);

        Files.writeString(outputPath.resolve(GLOBAL_DEFINITIONS_FILE), mergedDefs.toPrettyString());
        Map<String, JsonNode> publishedSchemas = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : helperSchemas.entrySet()) {
            String fileName = entry.getKey().toLowerCase().replace("jsonhelper", "") + "-schema.json";
            Files.writeString(outputPath.resolve(fileName), entry.getValue().toPrettyString());
            publishedSchemas.put(fileName, entry.getValue());
        }
        publishedSchemas.put(GLOBAL_DEFINITIONS_FILE, mergedDefs);
        ProjectDocRenderer renderer = new ProjectDocRenderer();
        renderer.writeArtifacts(outputPath);
        renderer.writeSchemaDocs(outputPath, publishedSchemas);
    }

    private Path resolveOutputPath() {
        String outputDir = System.getProperty("schema.output.dir");
        if (outputDir == null || outputDir.isBlank()) {
            outputDir = System.getenv("SCHEMA_OUTPUT_DIR");
        }
        if (outputDir == null || outputDir.isBlank()) {
            return null;
        }
        return Paths.get(outputDir);
    }

    private CustomDefinition provideCustomSchema(ResolvedType javaType, SchemaGenerationContext context) {
        Class<?> erasedType = javaType.getErasedType();
        if (ASN1ObjectIdentifier.class.equals(erasedType)) {
            return new CustomDefinition(createObjectIdentifierSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (ASN1Boolean.class.equals(erasedType)) {
            return new CustomDefinition(createBooleanSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (ASN1Integer.class.equals(erasedType)) {
            return new CustomDefinition(createIntegerSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (ASN1OctetString.class.equals(erasedType)) {
            return new CustomDefinition(createBinaryStringSchema(context, "Binary octet string value."), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (ASN1BitString.class.equals(erasedType)) {
            return new CustomDefinition(createBinaryStringSchema(context, "Binary bit string value."), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (ASN1UTF8String.class.equals(erasedType) || ASN1IA5String.class.equals(erasedType)) {
            return new CustomDefinition(createAsn1StringSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (ASN1GeneralizedTime.class.equals(erasedType)) {
            return new CustomDefinition(createTimeSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (AlgorithmIdentifier.class.equals(erasedType)) {
            return new CustomDefinition(createAlgorithmIdentifierSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (KeyUsage.class.equals(erasedType)) {
            return new CustomDefinition(createKeyUsageSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (TraitCollection.class.equals(erasedType)) {
            return new CustomDefinition(createTraitSequenceSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (TraitMap.class.equals(erasedType)) {
            return new CustomDefinition(createTraitSequenceSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (Trait.class.equals(erasedType)) {
            return new CustomDefinition(createTraitSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (ComponentAddress.class.equals(erasedType)) {
            return new CustomDefinition(createComponentAddressSchema(context), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        if (ASN1EnumBase.class.isAssignableFrom(erasedType)) {
            return new CustomDefinition(createEnumSchema(context, erasedType), CustomDefinition.DefinitionType.STANDARD, CustomDefinition.AttributeInclusion.YES);
        }
        return null;
    }

    private ObjectNode createObjectIdentifierSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        oneOf.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null));

        ObjectNode objectOption = context.getGeneratorConfig().createObjectNode();
        objectOption.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_OBJECT));
        ObjectNode properties = objectOption.putObject(getTag(context, SchemaKeyword.TAG_PROPERTIES));
        properties.set("oid", createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null));
        ArrayNode required = objectOption.putArray(getTag(context, SchemaKeyword.TAG_REQUIRED));
        required.add("oid");
        objectOption.put(getTag(context, SchemaKeyword.TAG_ADDITIONAL_PROPERTIES), false);
        oneOf.add(objectOption);
        return schema;
    }

    private ObjectNode createAsn1StringSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        oneOf.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null));

        ObjectNode objectOption = context.getGeneratorConfig().createObjectNode();
        objectOption.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_OBJECT));
        ObjectNode properties = objectOption.putObject(getTag(context, SchemaKeyword.TAG_PROPERTIES));
        properties.set("string", createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null));
        ArrayNode required = objectOption.putArray(getTag(context, SchemaKeyword.TAG_REQUIRED));
        required.add("string");
        objectOption.put(getTag(context, SchemaKeyword.TAG_ADDITIONAL_PROPERTIES), false);
        oneOf.add(objectOption);
        return schema;
    }

    private ObjectNode createBooleanSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        oneOf.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_BOOLEAN, null));
        ObjectNode stringOption = createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null);
        stringOption.putArray(getTag(context, SchemaKeyword.TAG_ENUM))
                .add("true")
                .add("false");
        oneOf.add(stringOption);
        return schema;
    }

    private ObjectNode createIntegerSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        oneOf.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_INTEGER, null));
        ObjectNode stringOption = createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null);
        stringOption.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION),
                "Decimal string, or a binary value expressed as hex/base64.");
        oneOf.add(stringOption);
        oneOf.add(createByteArraySchema(context));
        oneOf.add(createBinaryObjectSchema(context));
        return schema;
    }

    private ObjectNode createBinaryStringSchema(SchemaGenerationContext context, String description) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        ObjectNode stringOption = createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null);
        stringOption.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION),
                "Hex string or Base64 string.");
        oneOf.add(stringOption);
        oneOf.add(createByteArraySchema(context));
        oneOf.add(createBinaryObjectSchema(context));
        schema.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION), description);
        return schema;
    }

    private ObjectNode createTimeSchema(SchemaGenerationContext context) {
        ObjectNode schema = createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null);
        schema.put(getTag(context, SchemaKeyword.TAG_FORMAT), "date-time");
        return schema;
    }

    private ObjectNode createAlgorithmIdentifierSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        oneOf.add(createRefWithDescription(context, ASN1ObjectIdentifier.class,
                "Algorithm OID string or { oid } object."));

        ObjectNode objectOption = context.getGeneratorConfig().createObjectNode();
        objectOption.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_OBJECT));
        ObjectNode properties = objectOption.putObject(getTag(context, SchemaKeyword.TAG_PROPERTIES));
        properties.set("algorithm", createRefWithDescription(context, ASN1ObjectIdentifier.class,
                "Algorithm OID."));
        properties.set("parameters", createRefWithDescription(context, ASN1OctetString.class,
                "Optional DER parameters encoded as hex/base64."));
        ArrayNode required = objectOption.putArray(getTag(context, SchemaKeyword.TAG_REQUIRED));
        required.add("algorithm");
        objectOption.put(getTag(context, SchemaKeyword.TAG_ADDITIONAL_PROPERTIES), false);
        oneOf.add(objectOption);
        return schema;
    }

    private ObjectNode createKeyUsageSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        oneOf.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_INTEGER, null));
        oneOf.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null));

        ObjectNode arrayOption = context.getGeneratorConfig().createObjectNode();
        arrayOption.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_ARRAY));
        ObjectNode itemSchema = arrayOption.putObject(getTag(context, SchemaKeyword.TAG_ITEMS));
        ArrayNode itemAlternatives = itemSchema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        itemAlternatives.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null));
        itemAlternatives.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_INTEGER, null));
        oneOf.add(arrayOption);
        return schema;
    }

    private ObjectNode createBinaryObjectSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_OBJECT));
        schema.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION),
                "Binary wrapper object with either base64 or hex.");
        ObjectNode properties = schema.putObject(getTag(context, SchemaKeyword.TAG_PROPERTIES));
        properties.set("base64", createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null));
        properties.set("hex", createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, null));
        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        ObjectNode base64Required = context.getGeneratorConfig().createObjectNode();
        base64Required.putArray(getTag(context, SchemaKeyword.TAG_REQUIRED)).add("base64");
        ObjectNode hexRequired = context.getGeneratorConfig().createObjectNode();
        hexRequired.putArray(getTag(context, SchemaKeyword.TAG_REQUIRED)).add("hex");
        oneOf.add(base64Required);
        oneOf.add(hexRequired);
        schema.put(getTag(context, SchemaKeyword.TAG_ADDITIONAL_PROPERTIES), false);
        return schema;
    }

    private ObjectNode createByteArraySchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_ARRAY));
        ObjectNode items = schema.putObject(getTag(context, SchemaKeyword.TAG_ITEMS));
        items.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_INTEGER));
        return schema;
    }

    private ObjectNode createTraitSequenceSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_ARRAY));
        schema.set(getTag(context, SchemaKeyword.TAG_ITEMS), createTraitSchema(context));
        return schema;
    }

    private ObjectNode createTraitSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_OBJECT));

        ObjectNode properties = schema.putObject(getTag(context, SchemaKeyword.TAG_PROPERTIES));
        properties.set("traitId", createRefWithDescription(context, ASN1ObjectIdentifier.class, "OID Specifies the traitValue encoding"));
        properties.set("traitCategory", createRefWithDescription(context, ASN1ObjectIdentifier.class, "OID Identifies the information category contained in traitValue"));
        properties.set("traitRegistry", createRefWithDescription(context, ASN1ObjectIdentifier.class, "OID Identifies the registry used to match against the traitValue"));
        properties.set("description", createRefWithDescription(context, ASN1UTF8String.class, "Optional description."));
        properties.set("descriptionURI", createRefWithDescription(context, ASN1IA5String.class, "Optional URI for a description."));
        properties.set("traitValue", createUnconstrainedValueSchema(context,
                "Generic DER-encoded octet string given meaning by context information. paccor attempts to resolve the data to a specific type using trait metadata."));

        for (String alias : TRAIT_VALUE_ALIASES) {
            properties.set(alias, createRefWithDescription(context, TraitId.getTraitClassByAlias(alias), "Click the reference to see JSON format. It should be similar to the spec."));
        }

        ArrayNode required = schema.putArray(getTag(context, SchemaKeyword.TAG_REQUIRED));
        required.add("traitId");
        required.add("traitCategory");
        required.add("traitRegistry");
        required.add("traitValue");

        return schema;
    }

    private ObjectNode createComponentAddressSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION),
                "Accepts either the addressType/addressValue form or a shorthand single-property object keyed by ComponentAddressType.");

        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        oneOf.add(createCanonicalComponentAddressSchema(context));
        oneOf.add(createShorthandComponentAddressSchema(context));
        return schema;
    }

    private ObjectNode createCanonicalComponentAddressSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_OBJECT));
        schema.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION),
                "Canonical form: provide both addressType and addressValue.");
        ObjectNode properties = schema.putObject(getTag(context, SchemaKeyword.TAG_PROPERTIES));
        properties.set("addressType", createRefWithDescription(context, ASN1ObjectIdentifier.class,
                "Address type OID."));
        ObjectNode valueSchema = properties.putObject("addressValue");
        valueSchema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_STRING));
        valueSchema.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION), "Address value, such as a MAC address string.");
        ArrayNode required = schema.putArray(getTag(context, SchemaKeyword.TAG_REQUIRED));
        required.add("addressType");
        required.add("addressValue");
        schema.put(getTag(context, SchemaKeyword.TAG_ADDITIONAL_PROPERTIES), false);
        return schema;
    }

    private ObjectNode createShorthandComponentAddressSchema(SchemaGenerationContext context) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_OBJECT));
        schema.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION),
                "Shorthand form: a single property where the property name is the ComponentAddressType enum name and the value is the address string.");
        ObjectNode properties = schema.putObject(getTag(context, SchemaKeyword.TAG_PROPERTIES));
        for (ComponentAddressType value : ComponentAddressType.values()) {
            ObjectNode valueSchema = properties.putObject(value.name());
            valueSchema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_STRING));
            valueSchema.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION), "Address value for " + value.name() + ".");
        }
        schema.put(getTag(context, SchemaKeyword.TAG_PROPERTIES_MIN), 1);
        schema.put(getTag(context, SchemaKeyword.TAG_PROPERTIES_MAX), 1);
        schema.put(getTag(context, SchemaKeyword.TAG_ADDITIONAL_PROPERTIES), false);
        return schema;
    }

    private ObjectNode createEnumSchema(SchemaGenerationContext context, Class<?> enumWrapperType) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));

        ObjectNode integerOption = context.getGeneratorConfig().createObjectNode();
        integerOption.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_INTEGER));
        oneOf.add(integerOption);

        ObjectNode stringOption = context.getGeneratorConfig().createObjectNode();
        stringOption.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_STRING));
        if (enumWrapperType != null) {
            ArrayNode enumValues = stringOption.putArray(getTag(context, SchemaKeyword.TAG_ENUM));
            for (String constant : getEnumNames(enumWrapperType)) {
                enumValues.add(constant);
            }
        }
        oneOf.add(stringOption);

        if (ASN1BitStringEnumBase.class.isAssignableFrom(enumWrapperType)) {
            oneOf.add(createBitStringEnumObjectSchema(context, enumWrapperType));
        } else {
            ObjectNode aliasObject = createAliasObjectSchema(context, enumWrapperType);
            if (aliasObject != null) {
                oneOf.add(aliasObject);
            }
        }

        return schema;
    }

    private ObjectNode createBitStringEnumObjectSchema(SchemaGenerationContext context, Class<?> enumWrapperType) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_OBJECT));
        ObjectNode properties = schema.putObject(getTag(context, SchemaKeyword.TAG_PROPERTIES));

        ObjectNode valuesProperty = properties.putObject("values");
        valuesProperty.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_ARRAY));
        ObjectNode itemSchema = valuesProperty.putObject(getTag(context, SchemaKeyword.TAG_ITEMS));
        ArrayNode itemAlternatives = itemSchema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        itemAlternatives.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, enumWrapperType));
        itemAlternatives.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_INTEGER, null));

        for (String alias : getJsonAliases(enumWrapperType)) {
            properties.set(alias, createAliasValueSchema(context, enumWrapperType));
        }

        return schema;
    }

    private ObjectNode createAliasObjectSchema(SchemaGenerationContext context, Class<?> enumWrapperType) {
        List<String> aliases = getJsonAliases(enumWrapperType);
        if (aliases.isEmpty()) {
            return null;
        }
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, SchemaKeyword.TAG_TYPE_OBJECT));
        ObjectNode properties = schema.putObject(getTag(context, SchemaKeyword.TAG_PROPERTIES));
        for (String alias : aliases) {
            properties.set(alias, createAliasValueSchema(context, enumWrapperType));
        }
        schema.put(getTag(context, SchemaKeyword.TAG_PROPERTIES_MIN), 1);
        return schema;
    }

    private ObjectNode createAliasValueSchema(SchemaGenerationContext context, Class<?> enumWrapperType) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        ArrayNode oneOf = schema.putArray(getTag(context, SchemaKeyword.TAG_ONEOF));
        oneOf.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_STRING, enumWrapperType));
        oneOf.add(createTypedSchema(context, SchemaKeyword.TAG_TYPE_INTEGER, null));
        return schema;
    }

    private ObjectNode createTypedSchema(SchemaGenerationContext context, SchemaKeyword typeKeyword, Class<?> enumWrapperType) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_TYPE), getTag(context, typeKeyword));
        if (typeKeyword == SchemaKeyword.TAG_TYPE_STRING && enumWrapperType != null) {
            ArrayNode enumValues = schema.putArray(getTag(context, SchemaKeyword.TAG_ENUM));
            for (String constant : getEnumNames(enumWrapperType)) {
                enumValues.add(constant);
            }
        }
        return schema;
    }

    private ObjectNode createRefWithDescription(SchemaGenerationContext context, Class<?> targetType, String description) {
        ObjectNode ref = context.createDefinitionReference(context.getTypeContext().resolve(targetType));
        ref.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION), description);
        return ref;
    }

    private ObjectNode createUnconstrainedValueSchema(SchemaGenerationContext context, String description) {
        ObjectNode schema = context.getGeneratorConfig().createObjectNode();
        schema.put(getTag(context, SchemaKeyword.TAG_DESCRIPTION), description);
        return schema;
    }

    private void rewriteDefinitionRefs(JsonNode node, boolean externaliseDefinitions) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.properties().iterator();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (getTag(SchemaKeyword.TAG_REF).equals(entry.getKey()) && entry.getValue().isString()) {
                    String ref = entry.getValue().asString();
                    if (externaliseDefinitions && ref.startsWith("#/" + getTag(SchemaKeyword.TAG_DEFINITIONS) + "/")) {
                        objectNode.put(entry.getKey(), GLOBAL_DEFINITIONS_FILE + ref);
                    }
                } else {
                    rewriteDefinitionRefs(entry.getValue(), externaliseDefinitions);
                }
            }
            return;
        }
        if (node.isArray()) {
            for (JsonNode child : node) {
                rewriteDefinitionRefs(child, externaliseDefinitions);
            }
        }
    }

    private List<String> getEnumNames(Class<?> type) {
        try {
            Field factoryField = type.getDeclaredField("FACTORY");
            factoryField.setAccessible(true);
            ASN1EnumBase.Factory<?, ?> factory = (ASN1EnumBase.Factory<?, ?>) factoryField.get(null);
            return Arrays.stream(factory.getEnumType().getEnumConstants())
                    .map(Enum::name)
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> getJsonAliases(Class<?> type) {
        try {
            Field factoryField = type.getDeclaredField("FACTORY");
            factoryField.setAccessible(true);
            ASN1EnumBase.Factory<?, ?> factory = (ASN1EnumBase.Factory<?, ?>) factoryField.get(null);
            return factory.getJsonAliases();
        } catch (Exception e) {
            return List.of();
        }
    }

    private String getEnumDescription(Class<?> type) {
        try {
            Field factoryField = type.getDeclaredField("FACTORY");
            factoryField.setAccessible(true);
            ASN1EnumBase.Factory<?, ?> factory = (ASN1EnumBase.Factory<?, ?>) factoryField.get(null);

            Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) factory.getEnumType();
            Enum<?>[] constants = enumType.getEnumConstants();

            String values = Arrays.stream(constants)
                    .map(c -> String.format("`%s` (%d)", c.name(), ((EnumWithIntegerValue) c).getValue()))
                    .collect(Collectors.joining(", "));

            StringBuilder sb = new StringBuilder("**Usage / Options:**\n");
            if (ASN1EnumeratedEnumBase.class.isAssignableFrom(type)) {
                sb.append("Accepts one of the following as a name string or integer value: ");
            } else if (ASN1BitStringEnumBase.class.isAssignableFrom(type)) {
                sb.append("Accepts an integer bitmask, or an object with a `\"values\"` array containing name strings or integers: ");
            } else {
                sb.append("Accepts either a name string or integer value: ");
            }
            sb.append(values).append(".");

            if (!factory.getJsonAliases().isEmpty()) {
                sb.append("\n\n**Supported JSON Aliases:** `").append(String.join("`, `", factory.getJsonAliases())).append("`.");
            }

            return sb.toString();
        } catch (Exception e) {
            return "Accepts name strings or integer values for this enum type.";
        }
    }

    private String getTag(SchemaGenerationContext context, SchemaKeyword keyword) {
        return context.getKeyword(keyword);
    }

    private String getTag(SchemaKeyword keyword) {
        return keyword.forVersion(SchemaVersion.DRAFT_2020_12);
    }

    private Object parseDefaultValue(Class<?> fieldType, String defaultValue) {
        if (fieldType == boolean.class || fieldType == Boolean.class || fieldType == ASN1Boolean.class) {
            return Boolean.parseBoolean(defaultValue);
        }
        if (fieldType == int.class || fieldType == Integer.class) {
            return Integer.parseInt(defaultValue);
        }
        if (fieldType == long.class || fieldType == Long.class) {
            return Long.parseLong(defaultValue);
        }
        return defaultValue;
    }
}
