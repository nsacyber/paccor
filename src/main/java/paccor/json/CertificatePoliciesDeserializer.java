package paccor.json;

import org.bouncycastle.asn1.x509.CertificatePolicies;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for the {@code CertificatePolicies} class.
 */
public class CertificatePoliciesDeserializer extends ValueDeserializer<CertificatePolicies> {
    @Override
    public CertificatePolicies deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        return CertificatePoliciesJson.read(node);
    }
}
