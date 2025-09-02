package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import factory.ComponentIdentifierV2Factory;
import java.io.IOException;
import java.math.BigInteger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.IssuerSerial;

public class IssuerSerialDeserializer extends JsonDeserializer<IssuerSerial> {

    @Override
    public IssuerSerial deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.isObject() && node.size() == 2 && node.has(ComponentIdentifierV2Factory.Json.ISSUER.name()) && node.has(ComponentIdentifierV2Factory.Json.SERIAL.name())) {
            final JsonNode issuerNode = node.get(ComponentIdentifierV2Factory.Json.ISSUER.name());
            final JsonNode serialNode = node.get(ComponentIdentifierV2Factory.Json.SERIAL.name());
            final String issuer = issuerNode != null ? issuerNode.asText() : "";
            final String genericCertSerial = serialNode != null ? serialNode.asText() : "";
            return new IssuerSerial(new X500Name(issuer), new BigInteger(genericCertSerial));
        }
        // Handle other cases or throw an exception if the format is unexpected
        throw new IOException("Unexpected JSON format for IssuerSerial");
    }
}
