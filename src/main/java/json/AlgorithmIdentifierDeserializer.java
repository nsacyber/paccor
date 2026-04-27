package json;

import java.io.IOException;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom {@code ValueDeserializer} for the {@code AlgorithmIdentifier} class.
 */
public class AlgorithmIdentifierDeserializer extends ValueDeserializer<AlgorithmIdentifier> {
    @Override
    public AlgorithmIdentifier deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        if (node == null || node.isNull()) {
            return null;
        }

        // Try to read value as OID
        try {
            return new AlgorithmIdentifier(context.readTreeAsValue(node, ASN1ObjectIdentifier.class));
        } catch (IllegalArgumentException ignored) {} // fall through to try another format

        // Look for keys based on RFC5280
        Optional<JsonNode> algorithmOpt = JsonUtils.get(node, false, "algorithm");
        Optional<JsonNode> paramsOpt = JsonUtils.get(node, false, "parameters");

        final ASN1ObjectIdentifier algId = algorithmOpt
                .flatMap(JsonUtils::trimmedIfText)
                .map(ASN1ObjectIdentifier::new)
                .orElse(null);

        final ASN1Sequence params = paramsOpt
                .flatMap(JsonUtils::bytesFromTextualNode)
                .map(ASN1Sequence::getInstance)
                .orElse(null);

        if (algId != null) {
            return new AlgorithmIdentifier(algId, params);
        }
        throw JacksonIOException.construct(new IOException("Unexpected JSON format for AlgorithmIdentifier: " + node));
    }
}
