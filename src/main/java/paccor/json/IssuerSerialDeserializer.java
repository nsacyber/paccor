package paccor.json;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import paccor.json.schema.ComponentSchema;
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
        return Optional.ofNullable(context.readTree(p))
                .filter(node -> !node.isNull())
                .flatMap(node -> JsonUtils.get(node, false, ComponentSchema.IssuerSerialField.ISSUER_FIELD)
                        .flatMap(JsonUtils::trimmedValueAsText)
                        .flatMap(issuer -> JsonUtils.get(node, false, ComponentSchema.IssuerSerialField.SERIAL_FIELD)
                                .flatMap(JsonUtils::trimmedValueAsText)
                                .map(serial -> new IssuerSerial(new X500Name(issuer), new BigInteger(serial)))))
                .orElseThrow(() -> JacksonIOException.construct(new IOException("Unexpected JSON format for IssuerSerial")));
    }
}
