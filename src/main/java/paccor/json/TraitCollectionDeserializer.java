package paccor.json;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import paccor.tcg.credential.Trait;
import paccor.tcg.credential.TraitCollection;
import paccor.tcg.credential.TraitMap;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class TraitCollectionDeserializer extends ValueDeserializer<TraitCollection> {
    @Override
    public TraitCollection deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        return Optional.ofNullable(context.readTree(p))
                .filter(node -> !node.isNull())
                .map(node -> node.isArray() ? deserializeArray(node, context) : deserializeObject(node))
                .orElseGet(TraitCollection::empty);
    }

    @Override
    public TraitCollection getNullValue(DeserializationContext ctxt) {
        return TraitCollection.empty();
    }

    private static TraitCollection deserializeArray(JsonNode node, DeserializationContext context) {
        return StreamSupport.stream(node.spliterator(), false)
                .map(traitNode -> readTrait(context, traitNode))
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), TraitCollection::new));
    }

    private static TraitCollection deserializeObject(JsonNode node) {
        // Fallback to TraitMapDeserializer if it's an object (might have "traits" field or be legacy)
        return TraitCollection.from(ObjectMapperFactory.fromJsonNodeSafe(node, TraitMap.class));
    }

    private static Trait<?, ?> readTrait(DeserializationContext context, JsonNode traitNode) {
        return Trait.getInstance(traitNode, TraitDeserializer.resolveTraitClass(context, traitNode));
    }
}
