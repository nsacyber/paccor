package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import json.ASN1EnumBaseDeserializer;
import json.JsonUtils;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Common functionality for usage of Java Enums with BC ASN1Objects that can be identified by an integer value
 * @param <T> An enum that implements EnumWithValue
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)  // generate standard equals and hashCode
@Getter
@JsonDeserialize(using = ASN1EnumBaseDeserializer.class)
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public abstract class ASN1EnumBase<T extends Enum<T> & EnumWithIntegerValue> extends ASN1Object {
    private final int value;
    private final Class<T> enumType;

    /**
     * Get Instance.
     * @param obj Input object
     * @param factory Utility to convert values.
     * @return U
     * @param <T> Java enum type that implements EnumWithValue.
     * @param <U> Type relevant to &lt;T&gt; that extends AbstractEnumWithASN1Value.
     */
    public static <T extends Enum<T> & EnumWithIntegerValue, U extends ASN1EnumBase<T>> U getInstance(
            Object obj, @NonNull Factory<T, U> factory) {
        if (obj instanceof ASN1EnumBase<?>) { // type to type
            return factory.create(((ASN1EnumBase<?>)obj).getValue());
        } else if (obj instanceof Integer intValue) { // value to type
            return factory.create(intValue);
        } else if (obj instanceof ASN1Primitive prim) { // ASN1 object to type
            return factory.create(factory.fromASN1(prim));
        } else if (obj instanceof JsonNode jsonNode) { // JSON data to type
            return fromJsonNode(jsonNode, factory.getEnumType(), factory);
        } else if (obj instanceof String enumName) { // name value to type
            T constant = EnumWithIntegerValue.lookupName(enumName, factory.getEnumType());
            return factory.create(constant.getValue());
        }

        throw new IllegalArgumentException("Unsupported object type: " + obj.getClass());
    }

    /**
     * Get Instance from JsonNode
     * @param jsonNode json object
     * @param enumType Target enum class.
     * @param factory Utility to convert values.
     * @return U
     * @param <T> Java enum type that implements EnumWithValue.
     * @param <U> Type relevant to &lt;T&gt; that extends AbstractEnumWithASN1Value.
     */
    public static <T extends Enum<T> & EnumWithIntegerValue, U extends ASN1EnumBase<T>> U fromJsonNode(
            @NonNull JsonNode jsonNode, @NonNull Class<T> enumType, @NonNull Factory<T, U> factory) {
        if (isNumericNode(jsonNode)) {
            return factory.create(jsonNode.asInt());
        }
        if (jsonNode.isString()) {
            T constant = EnumWithIntegerValue.lookupName(jsonNode.asString(), enumType);
            return factory.create(constant.getValue());
        }
        if (jsonNode.isObject()) {
            return fromJsonObject(jsonNode, enumType, factory);
        }

        throw new IllegalArgumentException("Unsupported json data: " + jsonNode);
    }

    private static boolean isNumericNode(JsonNode jsonNode) {
        return jsonNode.isInt() || (jsonNode.isString() && isInteger(jsonNode.asString()));
    }

    private static <T extends Enum<T> & EnumWithIntegerValue, U extends ASN1EnumBase<T>> U fromJsonObject(
            JsonNode jsonNode,
            Class<T> enumType,
            Factory<T, U> factory) {
        JsonNode valuesNode = JsonUtils.get(jsonNode, false, "values").orElse(null);
        if (valuesNode != null && valuesNode.isArray()) {
            return factory.create(maskFromValues(valuesNode, enumType));
        }

        JsonNode aliasNode = findAliasedNode(jsonNode, factory);
        if (aliasNode != null) {
            return fromJsonNode(aliasNode, enumType, factory);
        }

        return firstFieldValue(jsonNode)
                .map(node -> fromJsonNode(node, enumType, factory))
                .orElseThrow(() -> new IllegalArgumentException("Unsupported json data: " + jsonNode));
    }

    private static <T extends Enum<T> & EnumWithIntegerValue> int maskFromValues(JsonNode valuesNode, Class<T> enumType) {
        int mask = 0;
        for (JsonNode valueNode : valuesNode) {
            mask |= bitFor(valueNode, enumType);
        }
        return mask;
    }

    private static <T extends Enum<T> & EnumWithIntegerValue> int bitFor(JsonNode valueNode, Class<T> enumType) {
        if (valueNode.isString()) {
            return 1 << EnumWithIntegerValue.lookupName(valueNode.asString(), enumType).getValue();
        }
        if (valueNode.isInt()) {
            return 1 << EnumWithIntegerValue.lookupValue(valueNode.asInt(), enumType).getValue();
        }
        return 0;
    }

    private static <T extends Enum<T> & EnumWithIntegerValue, U extends ASN1EnumBase<T>> JsonNode findAliasedNode(
            JsonNode jsonNode,
            Factory<T, U> factory) {
        for (String alias : factory.getJsonAliases()) {
            if (jsonNode.has(alias)) {
                return jsonNode.get(alias);
            }
        }
        return null;
    }

    private static Optional<JsonNode> firstFieldValue(JsonNode jsonNode) {
        return jsonNode.properties().stream()
                .findFirst()
                .map(Map.Entry::getValue);
    }

    private static boolean isInteger(String str) {
        return Pattern.compile("^\\d+$").matcher(str).matches();
    }

    /**
     * Converts the value to its ASN.1 representation.
     * @return value as ASN1Primitive
     */
    public abstract ASN1Primitive toASN1Primitive();

    /**
     * Look up the enum object by value.
     * @return The Enum object in T that corresponds with the value.
     */
    public T getEnum() {
        Optional<T> result = Arrays.stream(Objects.requireNonNull(enumType).getEnumConstants())
                .filter(constant -> constant.getValue() == value)
                .findFirst();

        return result
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enum " + enumType.getName() + "does not have a member with this value: " + value));
    }

    /**
     * Create instances and manage ASN.1 conversion.
     *
     * @param <T> Enum type.
     * @param <U> Subclass of AbstractEnumWithASN1Value.
     */
    public interface Factory<T extends Enum<T> & EnumWithIntegerValue, U extends ASN1EnumBase<T>> {
        /**
         * Creates a new instance of the subclass using an integer value.
         * @param value int
         * @return U
         */
        U create(int value);

        /**
         * Creates a new instance of the subclass using an enum.
         * @param value T
         * @return U
         */
        U create(T value);

        /**
         * Get the enum type.
         * @return Enum type class object.
         */
        Class<T> getEnumType();

        /**
         * Convert an ASN1 object to its int sub value.
         * @param asn1Primitive ASN1 object.
         * @return int sub value.
         */
        int fromASN1(ASN1Primitive asn1Primitive);

        /**
         * Get JSON aliases.
         * @return List of alias strings.
         */
        default List<String> getJsonAliases() {
            return List.of();
        }
    }
}
