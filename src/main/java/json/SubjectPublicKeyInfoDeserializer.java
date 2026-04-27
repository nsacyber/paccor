package json;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class SubjectPublicKeyInfoDeserializer extends ValueDeserializer<SubjectPublicKeyInfo> {
    @Override
    public SubjectPublicKeyInfo deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);

        if (node.isObject() && node.size() == 2) {
            AlgorithmIdentifier algId = context.readTreeAsValue(node.get(0), AlgorithmIdentifier.class);
            ASN1BitString keyData = context.readTreeAsValue(node.get(1), ASN1BitString.class);
            return new SubjectPublicKeyInfo(algId, keyData);
        }
        // Handle other cases or throw an exception if the format is unexpected
        throw JacksonIOException.construct(new IOException("Unexpected JSON format for SubjectPublicKeyInfo"));
    }
}
