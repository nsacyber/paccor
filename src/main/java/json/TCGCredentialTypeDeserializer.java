package json;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Primitive;
import tcg.credential.TCGCredentialType;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class TCGCredentialTypeDeserializer extends ValueDeserializer<TCGCredentialType> {
    @Override
    public TCGCredentialType deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }
        byte[] data = JsonUtils.bytesFromTextualNode(node)
                .orElseThrow(() -> JacksonIOException.construct(new IOException("Unexpected JSON format for TCGCredentialType: " + node)));
        try {
            ASN1Primitive primitive = ASN1Primitive.fromByteArray(data);
            return TCGCredentialType.getInstance(primitive);
        } catch (IOException e) {
            throw JacksonIOException.construct(e);
        }
    }
}
