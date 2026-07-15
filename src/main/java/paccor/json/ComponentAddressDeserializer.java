package paccor.json;

import java.io.IOException;
import java.util.Optional;
import paccor.json.schema.ComponentSchema;
import paccor.json.schema.JsonSchemaValue;
import paccor.normalization.HexNormalizer;
import paccor.tcg.credential.ASN1Utils;
import paccor.tcg.credential.ComponentAddress;
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
        return JsonUtils.get(node, caseSens, ComponentSchema.AddressField.ADDRESS_TYPE_FIELD)
                .flatMap(typeNode -> JsonUtils.get(node, caseSens, ComponentSchema.AddressField.ADDRESS_VALUE_FIELD)
                        .map(valueNode -> ComponentAddress.builder()
                                .addressType(JsonSchemaValue.lookup(typeNode.asString(), ComponentSchema.AddressTypeValue.class).oid())
                                .addressValue(ASN1Utils.getUTF8String(HexNormalizer.normalizeMac(valueNode.asString())))
                                .build()))
                .orElseGet(() -> ComponentAddress.builder().build());
    }

    private ComponentAddress handleOneElement(JsonNode node, boolean caseSens) {
        String type = node.propertyNames().iterator().next();
        String value = node.get(type).asString();

        ComponentSchema.AddressTypeValue typeValue = JsonSchemaValue.lookup(type, ComponentSchema.AddressTypeValue.class);
        return ComponentAddress.builder()
                .addressType(ASN1Utils.getOID(typeValue.getValue()))
                .addressValue(ASN1Utils.getUTF8String(HexNormalizer.normalizeMac(value)))
                .build();
    }
}
