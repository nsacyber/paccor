package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public class AlgorithmIdentifierDeserializer extends JsonDeserializer<AlgorithmIdentifier> {
    @Override
    public AlgorithmIdentifier deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isTextual()) {
            String value = node.asText();
            return new AlgorithmIdentifier(new ASN1ObjectIdentifier(value));
        }
        // Handle other cases or throw an exception if the format is unexpected
        throw new IOException("Unexpected JSON format for ASN1Boolean");
    }
}
