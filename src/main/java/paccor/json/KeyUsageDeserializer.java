package paccor.json;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import org.bouncycastle.asn1.x509.KeyUsage;
import paccor.normalization.HexNormalizer;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class KeyUsageDeserializer extends ValueDeserializer<KeyUsage> {
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
    public KeyUsage deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);

        return Optional.ofNullable(node)
                // if JsonNode is an int
                .filter(JsonNode::isInt)
                .map(JsonNode::asInt)
                .map(KeyUsage::new)

                // if JsonNode is not an int
                .or(() -> parseTextual(node).map(KeyUsage::new)) // if int or hex string or option name
                .or(() -> parseArray(node)) // if options presented in array - could accept any combination of int, hex string, name

                .orElseThrow(() -> JacksonIOException.construct(new IOException("Unexpected JSON format for KeyUsage. Supported formats: integer, hex string, option name, array of names or bits.")));
    }

    private static Optional<Integer> parseTextual(JsonNode node) {
        return Optional.of(node)
                .filter(JsonNode::isString)
                .flatMap(n -> HexNormalizer.parseHexOrInt(n.asString())
                //.map(KeyUsage::new)
                .or(() -> parseNameString(n)));
    }

    private Optional<KeyUsage> parseArray(JsonNode node) {
        return Optional.of(node)
                .filter(JsonNode::isArray)
                .map(n ->
                        JsonUtils.asStream(n.iterator())
                        .map(element -> {
                            if (element.isInt()) {
                                return element.asInt();
                            } else if (element.isString()) {
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
        return Optional.of(bitsFromName(node.asString()))
                .filter(bits -> bits != 0);
    }

    private static int bitsFromName(String name) {
        String norm = HexNormalizer.normalize(name);
        Integer bit = NAME_TO_BIT.get(norm);
        return bit == null ? 0 : bit;
    }
}
