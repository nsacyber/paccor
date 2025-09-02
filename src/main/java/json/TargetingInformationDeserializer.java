package json;

import org.bouncycastle.asn1.x509.TargetInformation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class TargetingInformationDeserializer extends ValueDeserializer<TargetInformation> {
    @Override
    public TargetInformation deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        return TargetingInformationJson.read(node);
    }
}
