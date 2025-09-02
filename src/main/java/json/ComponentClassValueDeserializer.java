package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HexFormat;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;

public class ComponentClassValueDeserializer extends JsonDeserializer<ASN1OctetString> {

    @Override
    public ASN1OctetString deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isTextual()) {
            String value = node.asText();
            return new DEROctetString(HexFormat.of().parseHex(value));
        }
        // Handle other cases or throw an exception if the format is unexpected
        throw new IOException("Unexpected JSON format for ComponentClassValue");
    }
}
