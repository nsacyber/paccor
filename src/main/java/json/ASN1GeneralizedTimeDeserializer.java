package json;

import cli.pv.DateConverter;
import java.io.IOException;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for the {@code ASN1GeneralizedTime} class.
 */
public class ASN1GeneralizedTimeDeserializer extends ValueDeserializer<ASN1GeneralizedTime> {
    @Override
    public ASN1GeneralizedTime deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        if (node.isString()) {
            String text = node.asString().trim();
            return new ASN1GeneralizedTime(DateConverter.dateFromJsonSafe(text));
        }

        // Handle other cases or throw an exception if the format is unexpected
        throw JacksonIOException.construct(new IOException("Unexpected JSON format for ASN1GeneralizedTime"));
    }
}
