package paccor.json;

import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for the {@code AuthorityInformationAccess} class.
 */
public class AuthorityInformationAccessDeserializer extends ValueDeserializer<AuthorityInformationAccess> {
    @Override
    public AuthorityInformationAccess deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        return AuthorityInformationAccessJson.read(node);
    }
}
