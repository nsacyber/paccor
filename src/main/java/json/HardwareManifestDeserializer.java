package json;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class HardwareManifestDeserializer extends ValueDeserializer<HardwareManifestJsonHelper> {
    @Override
    public HardwareManifestJsonHelper deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        return HardwareManifestJsonHelper.fromJsonNode(node);
    }

}
