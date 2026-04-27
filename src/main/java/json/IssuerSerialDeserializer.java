package json;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import json.schema.ComponentSchema;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.IssuerSerial;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for the {@code IssuerSerial} class.
 */
public class IssuerSerialDeserializer extends ValueDeserializer<IssuerSerial> {
    @Override
    public IssuerSerial deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);

        Optional<JsonNode> issuerOpt = JsonUtils.get(node, false, ComponentSchema.IssuerSerialField.ISSUER_FIELD);
        Optional<JsonNode> serialOpt = JsonUtils.get(node, false, ComponentSchema.IssuerSerialField.SERIAL_FIELD);

        final String issuer = issuerOpt
                .map(JsonNode::asString)
                .map(String::trim)
                .orElse("");
        final String genericCertSerial = serialOpt
                .map(JsonNode::asString)
                .map(String::trim)
                .orElse("");

        if (!issuer.isEmpty() && !genericCertSerial.isEmpty()) {
            return new IssuerSerial(new X500Name(issuer), new BigInteger(genericCertSerial));
        }
        // Handle other cases or throw an exception if the format is unexpected
        throw JacksonIOException.construct(new IOException("Unexpected JSON format for IssuerSerial"));
    }
}
