package json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import factory.ComponentIdentifierV2Factory;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;
import tcg.credential.ComponentAddress;
import tcg.credential.ComponentAddressType;

public class ComponentAddressDeserializer extends JsonDeserializer<ComponentAddress> {
    @Override
    public ComponentAddress deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        boolean caseSens = false;

        return Optional.ofNullable(node)
                .filter(JsonNode::isObject)
                .map(n -> {
                    int size = n.size();
                    if (size == 1) {
                        return handleOneElement(n, caseSens);
                    } else if (size == 2) {
                        return handleTwoElements(n, caseSens);
                    }
                    return null;
                })
                .orElseThrow(() -> new IOException("Unexpected JSON format for ComponentAddress"));

    }

    private ComponentAddress handleTwoElements(JsonNode node, boolean caseSens) {
        ComponentAddress.ComponentAddressBuilder address = ComponentAddress.builder();
        if(JsonUtils.has(node, caseSens, "addressType", "addressValue")) {
            Optional<JsonNode> typeOpt = JsonUtils.get(node, caseSens, "addressType");
            Optional<JsonNode> valueOpt = JsonUtils.get(node, caseSens, "addressValue");

            typeOpt.ifPresent(typeNode ->
                    valueOpt.ifPresent(valueNode -> {
                        address.addressType(new ASN1ObjectIdentifier(typeNode.asText()));
                        address.addressValue(new DERUTF8String(ComponentIdentifierV2Factory.standardizeMAC(valueNode.asText())));
                    }));
        }
        return address.build();
    }

    private ComponentAddress handleOneElement(JsonNode node, boolean caseSens) {
        ComponentAddress.ComponentAddressBuilder address = ComponentAddress.builder();
        // The field name is expected to be the ComponentAddressType enum name
        final String type = node.fieldNames().next();
        final String value = node.get(type).asText();

        Arrays.stream(ComponentAddressType.class.getEnumConstants())
                .filter(constant -> constant.name().equalsIgnoreCase(type))
                .findFirst()
                .ifPresent(address::addressTypeFromEnum);

        final String filtered = ComponentIdentifierV2Factory.standardizeMAC(value);
        address.addressValue(new DERUTF8String(filtered));
        return address.build();
    }
}
