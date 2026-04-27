package json;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import json.schema.ComponentSchema;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import json.schema.HardwareManifestSchema;
import normalization.ComponentIdentifierV2Converter;
import tcg.credential.ComponentIdentifierV11Trait;
import tcg.credential.ComponentIdentifierV2;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.Trait;
import tcg.credential.TraitCollection;
import tcg.credential.TraitMap;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.node.ObjectNode;

public class TraitMapDeserializer extends ValueDeserializer<TraitMap> {
    @Override
    public TraitMap deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode root = context.readTree(p);
        TraitMap.TraitMapBuilder builder = TraitMap.builder();
        if (root == null || root.isNull()) {
            return builder.build();
        }

        JsonNode traitsNode = root.isObject() ? findTraitsNode(root) : root;
        JsonNode legacyNode = root.isObject() ? root : null;
        List<Trait<?, ?>> explicitTraits = collectExplicitTraits(builder, context, traitsNode);
        if (isLegacyComponentIdentifier(legacyNode)) {
            mergeLegacyComponentTraits(builder, legacyNode, explicitTraits, context);
        }

        return builder.build();
    }

    private static JsonNode findTraitsNode(JsonNode root) {
        return JsonUtils.get(root, false, "traits")
                .filter(JsonNode::isArray)
                .orElse(null);
    }

    private static List<Trait<?, ?>> collectExplicitTraits(
            TraitMap.TraitMapBuilder builder,
            DeserializationContext context,
            JsonNode traitsNode) {
        List<Trait<?, ?>> explicitTraits = new ArrayList<>();
        if (traitsNode == null || !traitsNode.isArray() || traitsNode.isEmpty()) {
            return explicitTraits;
        }
        for (JsonNode traitNode : traitsNode) {
            Trait<?, ?> trait = readTrait(context, traitNode);
            if (trait != null) {
                explicitTraits.add(trait);
                builder.trait(trait);
            }
        }
        return explicitTraits;
    }

    private static Trait<?, ?> readTrait(DeserializationContext context, JsonNode traitNode) {
        Class<? extends Trait<?, ?>> traitClass = TraitDeserializer.resolveTraitClass(context, traitNode);
        return Trait.getInstance(traitNode, traitClass);
    }

    private static boolean isLegacyComponentIdentifier(JsonNode legacyNode) {
        return legacyNode != null
                && legacyNode.isObject()
                && (JsonUtils.has(legacyNode, false,
                    ComponentSchema.Field.COMPONENT_CLASS_FIELD,
                    ComponentSchema.Field.MANUFACTURER_FIELD,
                    ComponentSchema.Field.MODEL_FIELD)
                || JsonUtils.has(legacyNode, false,
                    ComponentSchema.Field.COMPONENT_CLASS_FIELD,
                    ComponentSchema.Field.MODEL_FIELD)
                || JsonUtils.has(legacyNode, false,
                    ComponentSchema.Field.COMPONENT_CLASS_FIELD,
                    ComponentSchema.Field.MANUFACTURER_FIELD));
    }

    private static void mergeLegacyComponentTraits(
            TraitMap.TraitMapBuilder builder,
            JsonNode legacyNode,
            List<Trait<?, ?>> explicitTraits,
            DeserializationContext context) {
        ComponentIdentifierV2 v2 = tryParseLegacyV2(legacyNode, explicitTraits, context);
        if (v2 == null) {
            return;
        }
        if (explicitTraits.isEmpty()) {
            builder.trait(ComponentIdentifierV11Trait.fromComponentIdentifierV2(v2));
            return;
        }

        Set<String> explicitKeys = explicitTraits.stream()
                .map(TraitMapDeserializer::keyFor)
                .collect(Collectors.toCollection(HashSet::new));
        TraitMap legacyTraits = ComponentIdentifierV2Converter.toTraitMap(v2);
        legacyTraits.flattenTraits().stream()
                .filter(t -> t != null && !explicitKeys.contains(keyFor(t)))
                .forEach(builder::trait);
    }

    private static String keyFor(Trait<?, ?> trait) {
        if (trait == null) return "";
        return trait.getTraitId()
                + "|" + trait.getTraitCategory()
                + "|" + trait.getTraitRegistry();
    }

    private static ComponentIdentifierV2 tryParseLegacyV2(
            JsonNode legacyNode,
            List<Trait<?, ?>> explicitTraits,
            DeserializationContext context) {
        try {
            return ObjectMapperFactory.fromJsonNode(legacyNode, ComponentIdentifierV2.class);
        } catch (Exception ignored) {
            // Try patched conversion below.
        }

        ObjectNode patched = patchLegacyComponentNode(legacyNode, explicitTraits, context);
        if (patched == null) {
            return null;
        }
        try {
            return ObjectMapperFactory.fromJsonNode(patched, ComponentIdentifierV2.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static ObjectNode patchLegacyComponentNode(
            JsonNode legacyNode,
            List<Trait<?, ?>> explicitTraits,
            DeserializationContext context) {
        ObjectNode patched = context.getNodeFactory().objectNode();
        Optional<JsonNode> componentClassNode = JsonUtils.get(legacyNode, false, ComponentSchema.Field.COMPONENT_CLASS_FIELD);
        if (componentClassNode.isEmpty()) {
            return null;
        }
        patched.set(ComponentSchema.COMPONENT_CLASS, componentClassNode.get());
        putManufacturerOrModel(patched, legacyNode, explicitTraits, ComponentSchema.MANUFACTURER, "componentManufacturer",
                TCGObjectIdentifier.tcgTrCatComponentManufacturer);
        putManufacturerOrModel(patched, legacyNode, explicitTraits, ComponentSchema.MODEL, "componentModel",
                TCGObjectIdentifier.tcgTrCatComponentModel);
        if (patched.get("componentManufacturer") == null || patched.get("componentModel") == null) {
            return null;
        }

        copyOptionalField(legacyNode, patched, ComponentSchema.SERIAL, "componentSerial");
        copyOptionalField(legacyNode, patched, ComponentSchema.REVISION, "componentRevision");
        copyOptionalField(legacyNode, patched, ComponentSchema.MANUFACTURER_ID, "componentManufacturerId");
        copyOptionalField(legacyNode, patched, ComponentSchema.FIELD_REPLACEABLE, "fieldReplaceable");
        copyOptionalField(legacyNode, patched, ComponentSchema.ADDRESSES, "componentAddresses");
        copyOptionalField(legacyNode, patched, ComponentSchema.PLATFORM_CERT, "componentPlatformCert");
        copyOptionalField(legacyNode, patched, ComponentSchema.PLATFORM_CERT_URI, "componentPlatformCertUri");
        copyOptionalField(legacyNode, patched, ComponentSchema.STATUS, "status");
        return patched;
    }

    private static void putManufacturerOrModel(
            ObjectNode patched,
            JsonNode legacyNode,
            List<Trait<?, ?>> explicitTraits,
            String legacyField,
            String targetField,
            ASN1ObjectIdentifier category) {
        JsonUtils.get(legacyNode, false, legacyField)
                .ifPresentOrElse(
                        node -> patched.set(targetField, node),
                        () -> (new TraitCollection(explicitTraits)).firstStringWithCategory(category)
                                .ifPresent(value -> patched.put(targetField, value)));
    }

    private static void copyOptionalField(JsonNode source, ObjectNode target, String sourceField, String targetField) {
        JsonUtils.get(source, false, sourceField).ifPresent(node -> target.set(targetField, node));
    }
}
