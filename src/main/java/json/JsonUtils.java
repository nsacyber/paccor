package json;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonUtils {
    private static final Pattern INT_PATTERN = Pattern.compile("^\\d+$");
    private static final Pattern HEX_PATTERN = Pattern.compile("^(?:0x|0X)?[0-9a-fA-F]+h?$");

    public static final Optional<JsonNode> get(final JsonNode node, boolean caseSensitive, final String fieldName) {
        return asStream(node.fieldNames())
                .filter(field -> (caseSensitive ? field.equals(fieldName) : field.equalsIgnoreCase(fieldName)))
                .findFirst()
                .map(node::get);
    }

    public static final boolean has(final JsonNode node, boolean caseSensitive, final String ... fieldNames) {
        return Stream.of(fieldNames)
                .allMatch(nodeField ->
                        asStream(node.fieldNames())
                                .anyMatch(providedField -> (caseSensitive ? providedField.equals(nodeField) : providedField.equalsIgnoreCase(nodeField)))
                );
    }

    public static final <T> Stream<T> asStream(final Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

    public static final <T> Stream<T> asStream(final Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }

    public static final boolean isInteger(String str) {
        return INT_PATTERN.matcher(str.trim()).matches();
    }

    public static final boolean isHex(String str) {
        return HEX_PATTERN.matcher(str.trim()).matches();
    }

    public static final Optional<JsonNode> firstNonNull(JsonNode ... nodes) {
        return Optional.ofNullable(nodes)// Treat a null array as an empty array
                .stream() .flatMap(Arrays::stream) // If "nodes" is null or empty, return Optional.empty
                .filter(n -> n != null && !n.isNull()) // Filter out null nodes
                .findFirst(); // Return the first non-null node or Optional.empty
    }

    private static String stripHexPrefix(String text) {
        String str = text == null ? "" : text;
        return str.startsWith("0x") || str.startsWith("0X") ? str.substring(2) : str;
    }

    private static String stripHexSuffix(String text) {
        String str = text == null ? "" : text;
        return str.endsWith("h") || str.endsWith("H") ? str.substring(0, str.length() - 1) : str;
    }

    public static final int parseHex(String str) {
        return Optional.ofNullable(str) // If null, return 0
                .filter(s -> !s.isBlank()) // If empty, return 0
                .map(JsonUtils::normalize)
                .map(JsonUtils::stripHexPrefix) // Strip "h" suffix if present
                .map(JsonUtils::stripHexSuffix) // Strip "0x" prefix if present
                .filter(s -> !s.isBlank()) // If empty, return 0
                .map(s -> Integer.parseUnsignedInt(s, 16)) // Does not catch NumberFormatException
                .orElse(0);
    }

    public static final Optional<Integer> parseHexOrInt(String text) {
        return Optional.ofNullable(text) // If null, return Optional.empty
                .flatMap(t ->
                            isHex(t) // Check if the text is a hex string
                            ? Optional.of(parseHex(t)) // Parse the hex string

                            : isInteger(t) // The text was not hex, check if it is an integer
                                    ? Optional.of(Integer.parseInt(t)) // Parse the integer
                                    : Optional.empty()); // Else, return Optional.empty
    }

    public static final String normalize(String text) {
        return Optional.ofNullable(text)
                .map(String::trim)
                .map(String::toLowerCase)
                .orElse("");
    }

    public static final String removeSeparators(String text) {
        return Optional.ofNullable(text)
                .map(t -> t.replace("-", ""))
                .map(t -> t.replace("_", ""))
                .map(t -> t.replace(" ", ""))
                .orElse("");
    }
}
