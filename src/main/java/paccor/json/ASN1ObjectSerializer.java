package paccor.json;

import java.io.IOException;
import java.util.Base64;
import org.bouncycastle.asn1.ASN1Object;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Custom serializer for the {@code ASN1Object} class.
 */
public class ASN1ObjectSerializer extends ValueSerializer<ASN1Object> {
    @Override
    public void serialize(ASN1Object value, JsonGenerator gen, SerializationContext context) throws JacksonException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        // Bouncy Castle keys off DER to produce DEROutputStream
        try {
            gen.writeString(Base64.getEncoder().encodeToString(value.getEncoded("DER")));
        } catch (IOException e) {
            throw JacksonIOException.construct(e);
        }
    }
}
