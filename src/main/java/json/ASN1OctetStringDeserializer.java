package json;

import java.io.IOException;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.DEROctetString;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for the {@code ASN1OctetString} class.
 */
public class ASN1OctetStringDeserializer extends ValueDeserializer<ASN1OctetString> {
    @Override
    public ASN1OctetString deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        if (node == null || node.isNull()) return null;

        Optional<byte[]> data = JsonUtils.handleBinaryFormat(node);
        if (data.isPresent()) {
            return new DEROctetString(data.get());
        }

        throw JacksonIOException.construct(new IOException("Unexpected JSON for ASN1OctetString: " + node));
    }
}
