package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.bouncycastle.asn1.x509.KeyUsage;

public class KeyUsageDeserializer extends JsonDeserializer<KeyUsage> {
    private static final Map<String, Integer> NAME_TO_BIT = Map.of(
            "digitalsignature", KeyUsage.digitalSignature,
            "nonrepudiation", KeyUsage.nonRepudiation,
            "contentcommitment", KeyUsage.nonRepudiation, // alias
            "keyencipherment", KeyUsage.keyEncipherment,
            "dataencipherment", KeyUsage.dataEncipherment,
            "keyagreement", KeyUsage.keyAgreement,
            "keycertsign", KeyUsage.keyCertSign,
            "crlsign", KeyUsage.cRLSign,
            "encipheronly", KeyUsage.encipherOnly,
            "decipheronly", KeyUsage.decipherOnly
    );

    @Override
    public KeyUsage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);

        return Optional.ofNullable(node)
                // if JsonNode is an int
                .filter(JsonNode::isInt)
                .map(JsonNode::asInt)
                .map(KeyUsage::new)

                // if JsonNode is not an int
                .or(() -> parseTextual(node).map(KeyUsage::new)) // if int or hex string or option name
                .or(() -> parseArray(node)) // if options presented in array - could accept any combination of int, hex string, name

                .orElseThrow(() -> new IOException("Unexpected JSON format for KeyUsage. Supported formats: integer, hex string, option name, array of names or bits."));
    }

    private static Optional<Integer> parseTextual(JsonNode node) {
        return Optional.of(node)
                .filter(JsonNode::isTextual)
                .flatMap(n -> JsonUtils.parseHexOrInt(n.asText())
                //.map(KeyUsage::new)
                .or(() -> parseNameString(n)));
    }

    private Optional<KeyUsage> parseArray(JsonNode node) {
        return Optional.of(node)
                .filter(JsonNode::isArray)
                .map(n ->
                        JsonUtils.asStream(n.elements())
                        .map(element -> {
                            if (element.isInt()) {
                                return element.asInt();
                            } else if (element.isTextual()) {
                                return parseTextual(element).orElse(0);
                            } else {
                                throw new IllegalArgumentException("Invalid KeyUsage element type.");
                            }
                        })
                        .reduce(0, Integer::sum)
                )
                .map(KeyUsage::new);
    }

    private static Optional<Integer> parseNameString(JsonNode node) {
        return Optional.of(bitsFromName(node.asText()))
                .filter(bits -> bits != 0);
    }

    private static int bitsFromName(String name) {
        String norm = JsonUtils.removeSeparators(JsonUtils.normalize(name));
        Integer bit = NAME_TO_BIT.get(norm);
        return bit == null ? 0 : bit;
    }
}
