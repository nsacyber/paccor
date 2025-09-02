package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import factory.CertificatePoliciesFactory;
import java.io.IOException;
import org.bouncycastle.asn1.x509.CertificatePolicies;

public class CertificatePoliciesDeserializer extends JsonDeserializer<CertificatePolicies> {
    @Override
    public CertificatePolicies deserialize(JsonParser p, DeserializationContext c) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        return CertificatePoliciesFactory.fromJsonNode(node).build();
    }
}
