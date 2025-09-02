package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.util.encoders.Base64;

public class ASN1BitStringFromBase64Deserializer extends JsonDeserializer<ASN1BitString> {

    @Override
    public ASN1BitString deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isTextual()) {
            String value = node.asText();
            return new DERBitString(Base64.decode(value));
        }
        // Handle other cases or throw an exception if the format is unexpected
        throw new IOException("Unexpected JSON format for ASN1BitStringFromBase64");
    }
}
