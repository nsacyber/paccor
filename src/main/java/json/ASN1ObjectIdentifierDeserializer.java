package json;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for the {@code ASN1ObjectIdentifier} class.
 */
public class ASN1ObjectIdentifierDeserializer extends ValueDeserializer<ASN1ObjectIdentifier> {
    @Override
    public ASN1ObjectIdentifier deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        if (node == null || node.isNull()) return null;
        String oidText = null;
        if (node.isString()) {
            oidText = node.asString().trim();
        } else if (node.has("oid") && node.get("oid").isString()) {
            oidText = node.get("oid").asString().trim();
        }

        if (oidText == null || oidText.isEmpty()) {
            return null;
        }

        return new ASN1ObjectIdentifier(oidText);
    }
}
