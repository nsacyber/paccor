package tcg.credential;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import lombok.NonNull;

/**
 * Interface for enums that map integer values to enums and handle conversions.
 */
public interface EnumWithIntegerValue {
    /**
     * Gets the int value associated with the enum constant. Not the ordinal.
     * @return int value
     */
    int getValue();

    /**
     * Generic static method to return an enum object given its name and the enum class it belongs to.
     * @param name Name of an enum that could be a member of the enum class.
     * @param enumType Enum class to search through. Cannot be null.
     * @return The first enum within enum class that matches the given name. Null if no match is found.
     * @param <T> Any enum class.
     */
    static <T extends Enum<T> & EnumWithIntegerValue> T lookupName(@NonNull String name, @NonNull Class<T> enumType) {
        Optional<T> result = Arrays.stream(enumType.getEnumConstants())
                .filter(constant -> constant.name().equalsIgnoreCase(name))
                .findFirst();

        return result
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enum " + enumType.getName() + " does not have a member with this name: " + name));
    }

    /**
     * Look up the enum object by value.
     * @return The Enum object in T that corresponds with the value.
     */
    static <T extends Enum<T> & EnumWithIntegerValue> T lookupValue(int value, @NonNull Class<T> enumType) {
        Optional<T> result = Arrays.stream(Objects.requireNonNull(enumType).getEnumConstants())
                .filter(constant -> constant.getValue() == value)
                .findFirst();

        return result
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enum " + enumType.getName() + "does not have a member with this value: " + value));
    }
}
