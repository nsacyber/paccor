package paccor.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import paccor.json.schema.HardwareManifestSchema;
import paccor.normalization.ComponentIdentifierV2Converter;
import paccor.normalization.PlatformConfigurationNormalizer;
import paccor.tcg.credential.PlatformConfiguration;
import paccor.tcg.credential.ComponentIdentifierV2;
import paccor.tcg.credential.PlatformConfigurationV2;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.PlatformPropertiesV2;
import paccor.tcg.credential.TraitMap;
import tools.jackson.databind.JsonNode;

@Builder(toBuilder = true)
@Jacksonized
@JsonClassDescription("Top-level JSON input for hardware manifest content, including v2 and v3 platform configuration forms.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
public record HardwareManifestJsonHelper(
    @JsonPropertyDescription("Platform Configuration Version 1 details.")
    PlatformConfiguration pcV1,
    @JsonPropertyDescription("Platform Configuration Version 2 details.")
    PlatformConfigurationV2 pcV2,
    @JsonPropertyDescription("Platform Configuration Version 3 details.")
    PlatformConfigurationV3 pcV3,
    @JsonPropertyDescription("Trait-based platform identifier data derived from the hardware manifest.")
    TraitMap platformTraits) {

    public static final HardwareManifestJsonHelper fromJsonNode(JsonNode node) {
        if (node == null || node.isNull()) return null;
        String jsonString = node.toString();

        HardwareManifestJsonHelper.HardwareManifestJsonHelperBuilder hwManifest = HardwareManifestJsonHelper.builder();
        PlatformConfiguration pcV1 = ObjectMapperFactory.fromJsonSafe(jsonString, PlatformConfiguration.class);
        PlatformConfigurationV2 pcV2 = ObjectMapperFactory.fromJsonSafe(jsonString, PlatformConfigurationV2.class);
        PlatformConfigurationV3 pcV3 = resolvePcv3(jsonString, node, pcV1, pcV2);
        hwManifest.pcV1(pcV1);
        hwManifest.pcV2(pcV2);
        hwManifest.pcV3(pcV3);
        TraitMap traits = SubjectAlternativeNameJson.readPlatformTraits(node);
        if (traits != null && !traits.isEmpty()) {
            hwManifest.platformTraits(traits);
        }
        return hwManifest.build();
    }

    /**
     * Reads components from JSON file.
     * @param componentsJson file to read
     * @return HardwareManifestJsonHelper with populated data
     */
    public static final HardwareManifestJsonHelper readComponents(File componentsJson) {
        if (componentsJson == null) return null;

        PlatformConfiguration pcV1 = ObjectMapperFactory.fromJsonSafe(componentsJson, PlatformConfiguration.class);
        PlatformConfigurationV2 pcV2 = ObjectMapperFactory.fromJsonSafe(componentsJson, PlatformConfigurationV2.class);
        PlatformConfigurationV3 pcV3 = resolvePcv3(componentsJson, pcV1, pcV2);
        TraitMap traits = SubjectAlternativeNameJson.readPlatformTraits(componentsJson);

        return HardwareManifestJsonHelper.builder()
                .pcV1(pcV1)
                .pcV2(pcV2)
                .pcV3(pcV3)
                .platformTraits(traits == null || traits.isEmpty() ? null : traits)
                .build();
    }

    private static PlatformConfigurationV3 buildPcv3FromNode(JsonNode root) {
        if (root == null || root.isNull()) return null;
        JsonNode comps = JsonUtils.get(root, false, HardwareManifestSchema.Field.COMPONENTS_FIELD).orElse(null);
        JsonNode props = JsonUtils.get(root, false, HardwareManifestSchema.Field.PROPERTIES_FIELD).orElse(null);

        PlatformConfigurationV3.PlatformConfigurationV3Builder b = PlatformConfigurationV3.builder();
        readPlatformComponents(comps).forEach(b::platformComponent);
        List<PlatformPropertiesV2> properties = readPlatformProperties(props);
        if (!properties.isEmpty()) {
            b.platformProperties(properties);
        }
        PlatformConfigurationV3 pcv3 = b.build();
        return PlatformConfigurationNormalizer.hasContent(pcv3) ? pcv3 : null;
    }

    /**
     * Given multiple options, choose V3 over V2 over V1.
     * @param jsonString JSON string
     * @param node JSON node
     * @param pcV1 platform configuration v1
     * @param pcV2 platform configuration v2
     * @return platform configuration v3
     */
    private static PlatformConfigurationV3 resolvePcv3(String jsonString, JsonNode node, PlatformConfiguration pcV1, PlatformConfigurationV2 pcV2) {
        PlatformConfigurationV3 pcV3 = ObjectMapperFactory.fromJsonSafe(jsonString, PlatformConfigurationV3.class);
        if (pcV3 == null) {
            pcV3 = buildPcv3FromNode(node);
        }
        return pcV3 != null ? pcV3 : canonicalize(pcV1, pcV2);
    }

    private static PlatformConfigurationV3 resolvePcv3(File componentsJson, PlatformConfiguration pcV1, PlatformConfigurationV2 pcV2) {
        PlatformConfigurationV3 pcV3 = ObjectMapperFactory.fromJsonSafe(componentsJson, PlatformConfigurationV3.class);
        if (pcV3 == null) {
            pcV3 = readJsonNode(componentsJson)
                    .map(HardwareManifestJsonHelper::buildPcv3FromNode)
                    .orElse(null);
        }
        return pcV3 != null ? pcV3 : canonicalize(pcV1, pcV2);
    }

    private static PlatformConfigurationV3 canonicalize(PlatformConfiguration pcV1, PlatformConfigurationV2 pcV2) {
        if (PlatformConfigurationNormalizer.hasContent(pcV2)) {
            return PlatformConfigurationNormalizer.canonicalize(pcV2);
        }
        if (PlatformConfigurationNormalizer.hasContent(pcV1)) {
            return PlatformConfigurationNormalizer.canonicalize(pcV1);
        }
        return null;
    }

    private static Optional<JsonNode> readJsonNode(File componentsJson) {
        try {
            return Optional.ofNullable(ObjectMapperFactory.get().readTree(componentsJson));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private static List<TraitMap> readPlatformComponents(JsonNode comps) {
        List<TraitMap> components = new ArrayList<>();
        if (comps == null || !comps.isArray()) {
            return components;
        }
        for (JsonNode node : comps) {
            TraitMap tm = readTraitMap(node);
            if (tm != null && !tm.isEmpty()) {
                components.add(tm);
            }
        }
        return components;
    }

    private static TraitMap readTraitMap(JsonNode node) {
        try {
            TraitMap tm = ObjectMapperFactory.fromJsonNode(node, TraitMap.class);
            if (tm != null && !tm.isEmpty()) {
                return tm;
            }
        } catch (Exception ignored) { }

        try {
            ComponentIdentifierV2 v2 = ObjectMapperFactory.fromJsonNode(node, ComponentIdentifierV2.class);
            return ComponentIdentifierV2Converter.toTraitMap(v2);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<PlatformPropertiesV2> readPlatformProperties(JsonNode props) {
        List<PlatformPropertiesV2> out = new ArrayList<>();
        if (props == null || !props.isArray()) {
            return out;
        }
        for (JsonNode p : props) {
            try {
                PlatformPropertiesV2 prop = ObjectMapperFactory.fromJsonNode(p, PlatformPropertiesV2.class);
                if (prop != null) {
                    out.add(prop);
                }
            } catch (Exception ignored) { }
        }
        return out;
    }
}
