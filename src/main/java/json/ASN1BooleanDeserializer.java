package json;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Boolean;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for the {@code ASN1Boolean} class.
 */
public class ASN1BooleanDeserializer extends ValueDeserializer<ASN1Boolean> {
    @Override
    public ASN1Boolean deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        if (node.isString()) {
            String value = node.asString();
            return Boolean.parseBoolean(value) ? ASN1Boolean.TRUE : ASN1Boolean.FALSE;
        } else if (node.isBoolean()) {
            return ASN1Boolean.getInstance(node.asBoolean());
        }
        // Handle other cases or throw an exception if the format is unexpected
        throw JacksonIOException.construct(new IOException("Unexpected JSON format for ASN1Boolean"));
    }
}
