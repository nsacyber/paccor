package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.bouncycastle.asn1.ASN1Boolean;

public class ASN1BooleanDeserializer extends JsonDeserializer<ASN1Boolean> {

    @Override
    public ASN1Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isTextual()) {
            String value = node.asText();
            return Boolean.parseBoolean(value) ? ASN1Boolean.TRUE : ASN1Boolean.FALSE;
        } else if (node.isBoolean()) {
            return ASN1Boolean.getInstance(node.asBoolean());
        }
        // Handle other cases or throw an exception if the format is unexpected
        throw new IOException("Unexpected JSON format for ASN1Boolean");
    }
}
