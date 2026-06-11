package paccor.tcg.credential;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
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
        if (isInteger(name)) {
            return lookupValue(Integer.parseInt(name), enumType);
        }

        String stripClassName = name.replaceAll("(?i)" + enumType.getEnclosingClass().getSimpleName() + "_", "");

        return Arrays.stream(enumType.getEnumConstants())
                .filter(constant -> constant.name().equalsIgnoreCase(stripClassName))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enum " + enumType.getName() + " does not have a member with this name: " + stripClassName));
    }

    /**
     * Look up the enum object by value.
     * @param value The value to look up.
     * @param enumType The enum class to search through. Cannot be null.
     * @return The Enum object in T that corresponds with the value.
     * @param <T> Any enum class that implements EnumWithIntegerValue.
     */
    static <T extends Enum<T> & EnumWithIntegerValue> T lookupValue(int value, @NonNull Class<T> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .filter(constant -> constant.getValue() == value)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Enum " + enumType.getName() + " does not have a member with this value: " + value));
    }

    /**
     * Decode a mask into a list of enum objects.
     * @param mask The integer mask to decode.
     * @param enumType The enum class to decode the mask for.
     * @return A list of enum objects that correspond to the set bits in the mask.
     * @param <T> Any enum class that implements EnumWithIntegerValue.
     */
    static <T extends Enum<T> & EnumWithIntegerValue> List<T> decodeMask(int mask, Class<T> enumType) {
        return Arrays.stream(enumType.getEnumConstants())
                .filter(constant -> (mask & (1 << constant.getValue())) != 0)
                .collect(Collectors.toList());
    }

    private static boolean isInteger(String str) {
        return Pattern.compile("^\\d+$").matcher(str).matches();
    }
}
