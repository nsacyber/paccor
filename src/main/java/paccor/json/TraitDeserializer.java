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
        return extractOid(context, traitNode, "traitId")
                .map(TraitId::getTraitClassForId)
                .filter(clazz -> clazz != ASN1ObjectTrait.class)
                .or(() -> Optional.of(resolveByAlias(traitNode))
                        .filter(clazz -> clazz != ASN1ObjectTrait.class))
                .or(() -> extractOid(context, traitNode, "traitCategory")
                        .map(TraitId::getTraitClassForCategory))
                .orElse(ASN1ObjectTrait.class);
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
        return TraitId.getRegisteredAliases().stream()
                .filter(alias -> JsonUtils.has(traitNode, false, alias))
                .map(TraitId::getTraitClassByAlias)
                .filter(clazz -> clazz != ASN1ObjectTrait.class)
                .findFirst()
                .orElse(ASN1ObjectTrait.class);
    }

    public static final JsonNode checkTraitValueAliases(JsonNode node, String defaultValueKey) {
        return Optional.ofNullable(node)
                .filter(n -> !n.isNull())
                .flatMap(n -> TraitId.getRegisteredAliases().stream()
                        .map(alias -> JsonUtils.get(n, false, alias))
                        .flatMap(Optional::stream)
                        .filter(hit -> !hit.isNull())
                        .findFirst()
                        .or(() -> Optional.ofNullable(n.get(defaultValueKey))))
                .orElse(null);
    }

    public static final JsonNode checkTraitValueAliases(JsonNode node) {
        return checkTraitValueAliases(node, "traitValue");
    }
}
