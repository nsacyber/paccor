package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import factory.CRLDistPointFactory;
import java.io.IOException;
import org.bouncycastle.asn1.x509.CRLDistPoint;

public class CrlDeserializer extends JsonDeserializer<CRLDistPoint> {
    @Override
    public CRLDistPoint deserialize(JsonParser p, DeserializationContext c) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        return CRLDistPointFactory.fromJsonNode(node).build();
    }
}