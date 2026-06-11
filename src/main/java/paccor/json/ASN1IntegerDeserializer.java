package paccor.json;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1Integer;
import paccor.normalization.HexNormalizer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for the {@code ASN1Integer} class.
 */
public class ASN1IntegerDeserializer extends ValueDeserializer<ASN1Integer> {
    @Override
    public ASN1Integer deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        if (node.isInt()) {
            return new ASN1Integer(node.asInt());
        } else if (node.isLong()) {
            return new ASN1Integer(node.asLong());
        } else if (node.isBigInteger()) {
            return new ASN1Integer(node.bigIntegerValue());
        } else if (node.isString() && HexNormalizer.isInteger(node.asString().trim())) {
            return new ASN1Integer(new BigInteger(node.asString().trim(), 10));
        }

        Optional<byte[]> binaryData = JsonUtils.handleBinaryFormat(node);
        if (binaryData.isPresent()) {
            return new ASN1Integer(new BigInteger(1, binaryData.get()));
        }

        // Handle other cases or throw an exception if the format is unexpected
        throw JacksonIOException.construct(new IOException("Unexpected JSON format for ASN1Integer"));
    }
}
