package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import factory.AuthorityInfoAccessFactory;
import java.io.IOException;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;

public class AuthorityInformationAccessDeserializer extends JsonDeserializer<AuthorityInformationAccess> {
    @Override
    public AuthorityInformationAccess deserialize(JsonParser p, DeserializationContext c) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        return AuthorityInfoAccessFactory.fromJsonNode(node).build();
    }
}
