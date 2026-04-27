package json;

import java.io.IOException;
import java.util.Optional;
import json.schema.ComponentSchema;
import json.schema.JsonSchemaValue;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;
import normalization.HexNormalizer;
import tcg.credential.ComponentAddress;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom deserializer for the {@code ComponentAddress} class.
 */
public class ComponentAddressDeserializer extends ValueDeserializer<ComponentAddress> {
    @Override
    public ComponentAddress deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
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
                .orElseThrow(() -> JacksonIOException.construct(new IOException("Unexpected JSON format for ComponentAddress")));

    }

    private ComponentAddress handleTwoElements(JsonNode node, boolean caseSens) {
        ComponentAddress.ComponentAddressBuilder address = ComponentAddress.builder();
        if (JsonUtils.has(node, caseSens,
                ComponentSchema.AddressField.ADDRESS_TYPE_FIELD,
                ComponentSchema.AddressField.ADDRESS_VALUE_FIELD)) {
            Optional<JsonNode> typeOpt = JsonUtils.get(node, caseSens, ComponentSchema.AddressField.ADDRESS_TYPE_FIELD);
            Optional<JsonNode> valueOpt = JsonUtils.get(node, caseSens, ComponentSchema.AddressField.ADDRESS_VALUE_FIELD);

            typeOpt.ifPresent(typeNode ->
                    valueOpt.ifPresent(valueNode -> {
                        ComponentSchema.AddressTypeValue typeValue =
                                JsonSchemaValue.lookup(typeNode.asString(), ComponentSchema.AddressTypeValue.class);
                        address.addressType(typeValue.oid());
                        address.addressValue(new DERUTF8String(HexNormalizer.normalizeMac(valueNode.asString())));
                    }));
        }
        return address.build();
    }

    private ComponentAddress handleOneElement(JsonNode node, boolean caseSens) {
        ComponentAddress.ComponentAddressBuilder address = ComponentAddress.builder();
        final String type = node.propertyNames().iterator().next();
        final String value = node.get(type).asString();

        ComponentSchema.AddressTypeValue typeValue = JsonSchemaValue.lookup(type, ComponentSchema.AddressTypeValue.class);
        address.addressType(new ASN1ObjectIdentifier(typeValue.getValue()));

        final String filtered = HexNormalizer.normalizeMac(value);
        address.addressValue(new DERUTF8String(filtered));
        return address.build();
    }
}
