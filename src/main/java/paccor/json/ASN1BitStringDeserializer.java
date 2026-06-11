package paccor.json;

import java.io.IOException;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.DERBitString;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom {@code ValueDeserializer} for the {@code ASN1BitString} class.
 */
public class ASN1BitStringDeserializer extends ValueDeserializer<ASN1BitString> {
    @Override
    public ASN1BitString deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        if (node == null || node.isNull()) return null;

        Optional<byte[]> data = JsonUtils.handleBinaryFormat(node);
        if (data.isPresent()) {
            return new DERBitString(data.get());
        }

        throw JacksonIOException.construct(new IOException("Unexpected JSON for ASN1BitString: " + node));
    }
}
