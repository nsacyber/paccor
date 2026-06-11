package paccor.tcg.credential;

import java.util.Arrays;
import lombok.NonNull;

/**
 * Interface for enums that map string values - separate from names - to enums and handle conversions.
 */
public interface EnumWithStringValue {
    /**
     * Gets the value associated with the enum constant. Not the name or ordinal.
     * @return String value
     */
    String getValue();

    /**
     * Generic static method to return an enum object given its name and the enum class it belongs to.
     * @param name Name of an enum that could be a member of the enum class.
     * @param enumType Enum class to search through. Cannot be null.
     * @return The first enum within enum class that matches the given name. Null if no match is found.
     * @param <T> Any enum class.
     */
    static <T extends Enum<T> & EnumWithStringValue> T lookupName(@NonNull String name, @NonNull Class<T> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .filter(constant -> constant.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enum " + enumType.getName() + " does not have a member with this name: " + name));
    }

    /**
     * Look up the enum object by value.
     * @param value The value to look up.
     * @param enumType The enum class to search through. Cannot be null.
     * @return The Enum object in T that corresponds with the value.
     * @param <T> Any enum class that implements EnumWithStringValue.
     */
    static <T extends Enum<T> & EnumWithStringValue> T lookupValue(@NonNull String value, @NonNull Class<T> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .filter(constant -> constant.getValue().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enum " + enumType.getName() + " does not have a member with this value: " + value));
    }
}
