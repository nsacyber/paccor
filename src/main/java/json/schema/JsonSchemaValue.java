package json.schema;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Shared contract for documented JSON value vocabularies and field-like entries with ASN.1 mappings.
 */
public interface JsonSchemaValue {
    /**
     * Canonical JSON value used for new documentation and generated examples.
     * @return canonical JSON value
     */
    String getJsonValue();

    /**
     * Canonical JSON value used for new documentation and generated examples.
     * @return canonical JSON value
     */
    default String jsonValue() {
        return getJsonValue();
    }

    /**
     * Optional ASN.1 or implementation value associated with this JSON value.
     * @return ASN.1 value, typically an OID or tag number
     */
    default String getAsn1Value() {
        return null;
    }

    /**
     * Optional documentation string for the value.
     * @return description text
     */
    default String asn1Value() {
        return getAsn1Value();
    }

    /**
     * Optional documentation string for the value.
     * @return description text
     */
    default String getDescription() {
        return null;
    }

    /**
     * Optional documentation string for the value.
     * @return description text
     */
    default String description() {
        return getDescription();
    }

    /**
     * Optional accepted JSON aliases.
     * @return alias list
     */
    default List<String> getAliases() {
        return null;
    }

    /**
     * Optional accepted JSON aliases.
     * @return alias list
     */
    default List<String> aliases() {
        List<String> aliases = getAliases();
        return aliases == null ? List.of() : aliases;
    }

    /**
     * All accepted JSON spellings for the value.
     * @return canonical value followed by aliases
     */
    default Stream<String> jsonValues() {
        return Stream.concat(Stream.of(jsonValue()), aliases().stream())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct();
    }

    /**
     * Resolve a project-defined JSON value by canonical value, alias, enum name, or ASN.1 value.
     * @param rawValue input token
     * @param enumType enum class implementing JsonSchemaValue
     * @return matching enum constant
     * @param <T> enum type
     */
    static <T extends Enum<T> & JsonSchemaValue> T lookup(String rawValue, Class<T> enumType) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Value is required");
        }
        for (T constant : enumType.getEnumConstants()) {
            if (matches(rawValue, constant)) {
                return constant;
            }
        }
        throw new IllegalArgumentException("Unsupported value for " + enumType.getSimpleName() + ": " + rawValue);
    }

    private static boolean matches(String rawValue, JsonSchemaValue constant) {
        if (constant == null) {
            return false;
        }
        String asn1Value = constant.asn1Value();
        if (asn1Value != null && asn1Value.equalsIgnoreCase(rawValue)) {
            return true;
        }
        String normalized = normalize(rawValue);
        if (constant.jsonValues().map(JsonSchemaValue::normalize).anyMatch(normalized::equals)) {
            return true;
        }
        return constant instanceof Enum<?> enumConstant
                && normalize(enumConstant.name()).equals(normalized);
    }

    private static String normalize(String value) {
        return value == null
                ? ""
                : value.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
    }
}
