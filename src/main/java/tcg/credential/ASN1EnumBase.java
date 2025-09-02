package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import json.ASN1EnumBaseDeserializer;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;

/**
 * Common functionality for usage of Java Enums with BC ASN1Objects that can be identified by an integer value
 * @param <T> An enum that implements EnumWithValue
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
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
        } else if (obj instanceof ASN1Primitive asn1Primitive) { // ASN1 object to type
            return factory.create(factory.fromASN1(asn1Primitive));
        } else if (obj instanceof JsonNode jsonNode) { // JSON data to type
            return fromJsonNode(jsonNode, factory.getEnumType(), factory);
        } else if (obj instanceof String enumName) { // name value to type
            T constant = findEnumByName(enumName, factory.getEnumType());
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

        if (jsonNode.isInt() || (jsonNode.isTextual() && isInteger(jsonNode.asText()))) { // int value to type
            return factory.create(jsonNode.asInt());
        } else if (jsonNode.isTextual()) { // name value to type
            T constant = findEnumByName(jsonNode.asText(), enumType);
            return factory.create(constant.getValue());
        } else if (jsonNode.isObject() && jsonNode.fields().hasNext()) { // parse object value
            return fromJsonNode(jsonNode.fields().next().getValue(), enumType, factory);
        }

        throw new IllegalArgumentException("Unsupported json data: " + jsonNode);
    }

    private static boolean isInteger(String str) {
        return Pattern.compile("^\\d+$").matcher(str).matches();
    }

    /**
     * Generic static method to return an enum object given its name and the enum class it belongs to.
     * @param name Name of an enum that could be a member of the enum class.
     * @param enumType Enum class to search through. Cannot be null.
     * @return The first enum within enum class that matches the given name. Null if no match is found.
     * @param <T> Any enum class.
     */
    private static <T extends Enum<T> & EnumWithIntegerValue> T findEnumByName(@NonNull String name, @NonNull Class<T> enumType) {
        Optional<T> result = Arrays.stream(enumType.getEnumConstants())
                .filter(constant -> constant.name().equalsIgnoreCase(name))
                .findFirst();

        return result
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enum " + enumType.getName() + " does not have a member with this name: " + name));
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
    }
}
