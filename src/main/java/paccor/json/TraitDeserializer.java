package paccor.json;

import java.util.Optional;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.tcg.credential.ASN1ObjectTrait;
import paccor.tcg.credential.Trait;
import paccor.tcg.credential.TraitId;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

/**
 * Custom {@code ValueDeserializer} for the {@code Trait<?, ?>} class.
 */
public class TraitDeserializer extends ValueDeserializer<Trait<?, ?>>  {
    @Override
    public Trait<?, ?> deserialize(JsonParser p, DeserializationContext context) throws JacksonException {
        JsonNode node = context.readTree(p);
        Class<? extends Trait<?, ?>> traitClass = resolveTraitClass(context, node);
        return Trait.getInstance(node, traitClass);
    }

    /**
     * Resolves concrete Trait class based on the JSON context.
     * This method identifies the Trait class using a priority order:
     * First, using the traitId OID.
     * Next, using known aliases of traitValue.
     * Next, using the traitCategory OID.
     * Finally, if no match is found, returns the default {@code ASN1ObjectTrait}.
     *
     * @param context {@link DeserializationContext}
     * @param traitNode The {@link JsonNode} containing the trait information.
     * @return If resolution is successful, the concrete {@link Class} of type {@code Trait<?, ?>}.
     *         Otherwise, the default {@code ASN1ObjectTrait.class}.
     */
    public static final Class<? extends Trait<?, ?>> resolveTraitClass(DeserializationContext context, JsonNode traitNode) {
        Optional<String> traitIdOid = extractOid(context, traitNode, "traitId");
        Optional<String> traitCategoryOid = extractOid(context, traitNode, "traitCategory");

        // 1. Try to resolve by traitId first
        Class<? extends Trait<?, ?>> resultById = ASN1ObjectTrait.class;
        if (traitIdOid.isPresent()) {
            resultById = TraitId.getTraitClassForId(traitIdOid.get());
        }
        if (resultById != ASN1ObjectTrait.class) {
            return resultById;
        }

        // 2. Resolve by Alias (e.g., "status", "booleanValue", "utf8")
        Class<? extends Trait<?, ?>> resultByAlias = resolveByAlias(traitNode);
        if (resultByAlias != ASN1ObjectTrait.class) {
            return resultByAlias;
        }

        // 3. Try to resolve by traitCategory
        Class<? extends Trait<?, ?>> resultByCat = ASN1ObjectTrait.class;
        if (traitCategoryOid.isPresent()) {
            resultByCat = TraitId.getTraitClassForCategory(traitCategoryOid.get());
        }

        return resultByCat;
    }

    /**
     * Extracts the OID (Object Identifier) from the given JSON node based on the provided property name.
     * The method checks for the presence of the key in the JSON node, ensures that the value is textual,
     * non-blank, and then converts the value into an OID string.
     *
     * @param context A {@link DeserializationContext} instance used to convert the JSON value to an {@code ASN1ObjectIdentifier}.
     * @param node   The {@link JsonNode} to search for the key.
     * @param name   The name of the property to retrieve the OID from.
     * @return An {@code Optional<String>} containing the extracted OID if it is successfully found and converted;
     *         otherwise, an empty {@code Optional}.
     */
    public static Optional<String> extractOid(DeserializationContext context, JsonNode node, String name) {
        if (node == null || node.isNull()) return Optional.empty();
        return JsonUtils.get(node, false, name)
                .flatMap(JsonUtils::onlyNotBlankString)
                .map(oidNode -> context.readTreeAsValue(oidNode, ASN1ObjectIdentifier.class))
                .map(ASN1ObjectIdentifier::getId);
    }

    private static Class<? extends Trait<?, ?>> resolveByAlias(JsonNode traitNode) {
        Class<? extends Trait<?, ?>> resultByAlias = ASN1ObjectTrait.class;
        for (String alias : TraitId.getRegisteredAliases()) {
            if (JsonUtils.has(traitNode, false, alias)) {
                resultByAlias = TraitId.getTraitClassByAlias(alias);
                if (resultByAlias != ASN1ObjectTrait.class) {
                    break;
                }
            }
        }
        return resultByAlias;
    }

    public static final JsonNode checkTraitValueAliases(JsonNode node, String defaultValueKey) {
        if (node == null || node.isNull()) {
            return null;
        }

        for (String alias : TraitId.getRegisteredAliases()) {
            Optional<JsonNode> hit = JsonUtils.get(node, false, alias);
            if (hit.isPresent() && !hit.get().isNull()) {
                return hit.get();
            }
        }

        return node.get(defaultValueKey);
    }

    public static final JsonNode checkTraitValueAliases(JsonNode node) {
        return checkTraitValueAliases(node, "traitValue");
    }
}
