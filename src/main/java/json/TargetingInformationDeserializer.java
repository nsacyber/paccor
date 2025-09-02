package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import factory.TargetingInformationFactory;
import java.io.IOException;
import org.bouncycastle.asn1.x509.TargetInformation;

public class TargetingInformationDeserializer extends JsonDeserializer<TargetInformation> {
    @Override
    public TargetInformation deserialize(JsonParser p, DeserializationContext c) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        return TargetingInformationFactory.fromJsonNode(node).build();
    }
}
