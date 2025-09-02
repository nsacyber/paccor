package json;

import org.bouncycastle.asn1.x509.CRLDistPoint;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class CrlDeserializer extends ValueDeserializer<CRLDistPoint> {
    @Override
    public CRLDistPoint deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        return CrlDistributionPointsJson.read(node);
    }
}
