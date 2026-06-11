package paccor.json;

import java.io.File;
import java.util.Optional;
import paccor.json.schema.HardwareManifestSchema;
import paccor.json.schema.SubjectAlternativeNameSchema;
import paccor.tcg.credential.ASN1Utils;
import paccor.tcg.credential.PENTrait;
import paccor.tcg.credential.Trait;
import paccor.tcg.credential.TraitCollection;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.UTF8StringTrait;
import tools.jackson.databind.JsonNode;

public final class SubjectAlternativeNameJson {
    private SubjectAlternativeNameJson() {}

    public static TraitMap readPlatformTraits(JsonNode root) {
        if (root == null || root.isNull()) {
            return TraitMap.builder().build();
        }
        JsonNode platformNode = JsonUtils.get(root, false, HardwareManifestSchema.Field.PLATFORM_FIELD)
                .orElse(root);
        return parsePlatformTraits(platformNode);
    }

    public static TraitMap readPlatformTraits(File file) {
        if (file == null) {
            return TraitMap.builder().build();
        }
        try {
            JsonNode root = ObjectMapperFactory.get().readTree(file);
            return readPlatformTraits(root);
        } catch (Exception ignored) {
            return TraitMap.builder().build();
        }
    }

    private static TraitMap parsePlatformTraits(JsonNode node) {
        TraitMap.TraitMapBuilder builder = TraitMap.builder();
        if (node == null || node.isNull()) {
            return builder.build();
        }
        builder.traitsFromJson(node);
        TraitCollection snapshot = TraitCollection.from(builder.build());
        for (SubjectAlternativeNameSchema.PlatformField field : SubjectAlternativeNameSchema.PlatformField.values()) {
            addMissingTrait(snapshot, builder, node, field);
        }
        return builder.build();
    }

    private static void addMissingTrait(
            TraitCollection snapshot,
            TraitMap.TraitMapBuilder builder,
            JsonNode node,
            SubjectAlternativeNameSchema.PlatformField field) {
        if (snapshot.containsCategory(field.getTraitCategoryOid())) {
            return;
        }
        JsonUtils.get(node, false, field)
                .flatMap(JsonUtils::onlyNotBlankString)
                .map(JsonNode::asString)
                .flatMap(value -> traitFromJsonValue(value, field))
                .ifPresent(builder::trait);
    }

    private static Optional<Trait<?, ?>> traitFromJsonValue(String value, SubjectAlternativeNameSchema.PlatformField field) {
        if (value == null || field == null) {
            return Optional.empty();
        }
        return switch (field) {
            case PLATFORM_MANUFACTURER_ID_FIELD -> Optional.of(PENTrait.builder()
                    .traitCategory(field.getTraitCategoryOid())
                    .traitValue(ASN1Utils.getOID(value))
                    .build());
            case PLATFORM_MANUFACTURER_FIELD, PLATFORM_MODEL_FIELD, PLATFORM_SERIAL_FIELD, PLATFORM_VERSION_FIELD ->
                    Optional.of(UTF8StringTrait.builder()
                            .traitCategory(field.getTraitCategoryOid())
                            .traitValue(ASN1Utils.getUTF8String(value))
                            .build());
        };
    }
}
