package paccor.normalization;

import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Utility for normalizing hexadecimal strings for case-insensitive comparison.
 * Used by PCI ID translators and MAC address handling.
 */
public final class HexNormalizer {
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");

    private HexNormalizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Normalize a hexadecimal string to lowercase, zero-padded format.
     * Strips common prefixes (0x, 0X), suffixes (h), whitespace, colons, and hyphens.
     *
     * @param hex The hex string to normalize (e.g., "0x8AB", "8AB")
     * @return Normalized hex string
     */
    public static final String normalize(String hex) {
        return Optional.ofNullable(hex)
                .map(HexNormalizer::stripFixes)
                .map(HexNormalizer::removeSeparators)
                .orElse("");
    }

    /**
     * Normalize a hexadecimal string to lowercase, zero-padded format.
     * Strips common prefixes (0x, 0X), suffixes (h), whitespace, colons, and hyphens.
     *
     * @param hex The hex string to normalize (e.g., "0x8AB", "8AB")
     * @param expectedBytes Number of bytes the hex value should represent (determines padding)
     * @return Normalized hex string (e.g., "08ab" for expectedBytes=2)
     */
    public static String normalize(String hex, int expectedBytes) {
        if (hex == null || hex.isEmpty()) {
            return "0".repeat(expectedBytes * 2);
        }

        // normalize
        String cleaned = normalize(hex);

        // Validate hex characters
        if (!cleaned.matches("[0-9a-f]*")) {
            // Invalid hex, return zero-padded empty
            return "0".repeat(expectedBytes * 2);
        }

        // Zero-pad to expected length
        int expectedLength = expectedBytes * 2;
        if (cleaned.length() < expectedLength) {
            cleaned = "0".repeat(expectedLength - cleaned.length()) + cleaned;
        } else if (cleaned.length() > expectedLength) {
            // Truncate from left (keep rightmost digits)
            cleaned = cleaned.substring(cleaned.length() - expectedLength);
        }

        return cleaned;
    }

    private static String stripFixes(String hex) {
        String cleaned = hex == null ? "" : hex.trim().toLowerCase(Locale.ROOT);
        if (cleaned.startsWith("0x")) {
            cleaned = cleaned.substring(2);
        }
        if (cleaned.startsWith("#")) {
            cleaned = cleaned.substring(1);
        }
        if (cleaned.endsWith("h")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }
        return cleaned;
    }

    public static final String removeSeparators(String text) {
        return Optional.ofNullable(text)
                .map(str -> str.replaceAll("[:_.\\-\\s]", ""))
                .orElse("");
    }

    /**
     * Normalize a MAC address to lowercase hexadecimal without delimiters.
     * Handles various MAC address formats:
     * - AA:BB:CC:DD:EE:FF
     * - AA-BB-CC-DD-EE-FF
     * - AABBCCDDEEFF
     * - aa:bb:cc:dd:ee:ff
     *
     * @param mac MAC address string
     * @return Normalized MAC address (12 hex characters, lowercase, no delimiters)
     */
    public static String normalizeMac(String mac) {
        if (mac == null || mac.isEmpty()) {
            return "000000000000";
        }

        // Strip whitespace and convert case
        String cleaned = mac.trim().toUpperCase(Locale.ROOT);

        // Remove common delimiters (colons, hyphens, periods, spaces)
        cleaned = cleaned.replaceAll("[:\\s.-]", "");

        // Validate hex characters
        if (!cleaned.matches("[0-9A-F]*")) {
            return "000000000000";
        }

        // MAC addresses should be 12 hex characters (6 bytes)
        if (cleaned.length() < 12) {
            cleaned = "0".repeat(12 - cleaned.length()) + cleaned;
        } else if (cleaned.length() > 12) {
            // Truncate to 12 characters
            cleaned = cleaned.substring(0, 12);
        }

        return cleaned;
    }

    /**
     * Parse a hexadecimal string to an integer value.
     * Returns -1 if the string is not a valid hex number.
     *
     * @param hex Hexadecimal string (with or without a prefix or suffix)
     * @return Integer value, or -1 on error
     */
    public static Optional<Integer> parseHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return Optional.empty();
        }

        String cleaned = normalize(hex);

        try {
            return Optional.of(Integer.parseUnsignedInt(cleaned, 16));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Convert a hexadecimal string to a byte array.
     * @param hex Hexadecimal string
     * @return Optional containing byte array or empty if input is invalid
     */
    public static Optional<byte[]> hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return Optional.empty();
        }

        String cleaned = normalize(hex);

        int len = cleaned.length();
        byte[] out = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) Integer.parseInt(cleaned.substring(i, i + 2), 16);
        }
        return Optional.of(out);
    }

    /**
     * Check if a string appears to be a hexadecimal value.
     *
     * @param str String to check
     * @return true if the string looks like hex (optionally with 0x prefix or h suffix)
     */
    public static boolean isHexString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        String cleaned = stripFixes(str);

        return cleaned.matches("[0-9a-f]+");
    }
    
    /**
     * Generate a list of variant forms of the given hex string.
     * Variants include transformations to upper, lowercase, common prefixes, suffixes,
     * as well as removing whitespace among other common formatting differences.
     *
     * @param hex hex string
     * @return A list of potential variant forms of the provided hex string.
     */
    public static List<String> generateHexVariants(String hex) {
        return List.of(
                hex,                         // Try as provided
                hex.toLowerCase(),           // Try lowercase
                hex.toUpperCase(),           // Try uppercase
                "0x" + hex,
                "0x" + hex.toLowerCase(),
                "0x" + hex.toUpperCase(),
                hex + "h",
                hex.toLowerCase() + "h",
                hex.toUpperCase() + "h",
                hex + "H",
                hex.toLowerCase() + "H",
                hex.toUpperCase() + "H",
                "0h" + hex,
                "0h" + hex.toLowerCase(),
                "0h" + hex.toUpperCase(),
                "x'" + hex + "'",
                "x'" + hex.toLowerCase() + "'",
                "x'" + hex.toUpperCase() + "'",
                "X'" + hex + "'",
                "X'" + hex.toLowerCase() + "'",
                "X'" + hex.toUpperCase() + "'",
                "%" + hex,
                "%" + hex.toLowerCase(),
                "%" + hex.toUpperCase(),
                "#" + hex,
                "#" + hex.toLowerCase(),
                "#" + hex.toUpperCase(),
                "#x" + hex,
                "#x" + hex.toLowerCase(),
                "#x" + hex.toUpperCase(),
                hex.replaceAll("\\s", ""),
                hex.replaceAll("\\s", "").toLowerCase(),
                hex.replaceAll("\\s", "").toUpperCase()
        );
    }

    /**
     * Check if a string is a valid integer.
     * @param str String to check
     * @return true if the string is a valid integer. Otherwise, false.
     */
    public static final boolean isInteger(String str) {
        return INT_PATTERN.matcher(str.trim()).matches();
    }

    /**
     * Parses and normalizes the string to an integer value.
     *
     * @param text The input string to parse. This can be a hexadecimal or decimal integer.
     * @return Optional containing an integer or empty.
     */
    public static final Optional<Integer> parseHexOrInt(String text) {
        return Optional.ofNullable(text) // If null, return Optional.empty
                .flatMap(t ->
                        isHexString(t) // Check if the text is a hex string
                        ? parseHex(t) // Parse the hex string
                        : isInteger(t) // The text was not hex, check if it is an integer
                            ? Optional.of(Integer.parseInt(t)) // Parse the integer
                            : Optional.empty()); // Else, return Optional.empty
    }

    /**
     * Convert a byte array to a hex string.
     * @param in The byte array to convert.
     * @return The hex string representation of the byte array.
     */
    public static String toHexString(byte[] in) {
        return HexFormat.of().formatHex(in);
    }
}
