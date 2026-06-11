package paccor.json;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import paccor.json.schema.JsonSchemaField;
import paccor.normalization.HexNormalizer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.exc.JsonNodeException;

/**
 * Utility for working with JSON.
 */
public class JsonUtils {
    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Retrieves the JsonNode by key from the given JsonNode.
     *
     * @param node          the JsonNode to search for the key
     * @param caseSensitive {@code true/false} to control whether the search should/should not be case-sensitive
     * @param key           the name of the property to retrieve
     * @return If found, an {@code Optional<JsonNode>}; Otherwise, an empty {@code Optional}
     */
    public static final Optional<JsonNode> get(final JsonNode node, boolean caseSensitive, final String key) {
        return asStream(node.propertyNames())
                .filter(property -> (caseSensitive ? property.equals(key) : property.equalsIgnoreCase(key)))
                .findFirst()
                .map(node::get);
    }

    /**
     * Retrieves a JsonNode by schema field name or alias.
     * @param node the JsonNode to search
     * @param caseSensitive whether lookup should be case-sensitive
     * @param field project-owned schema field definition
     * @return optional node value
     */
    public static Optional<JsonNode> get(final JsonNode node, boolean caseSensitive, final JsonSchemaField field) {
        if (node == null || node.isNull() || field == null) {
            return Optional.empty();
        }
        return field.jsonNames()
                .map(name -> get(node, caseSensitive, name))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    /**
     * Checks if the given JSON node contains all the specified keys.
     *
     * @param node          the JsonNode to search for the keys
     * @param caseSensitive {@code true/false} to control whether the search should/should not be case-sensitive
     * @param keys          the keys to search for
     * @return True if all the specified keys are present in the JsonNode; Otherwise false.
     */
    public static final boolean has(final JsonNode node, boolean caseSensitive, final String ... keys) {
        return Stream.of(keys)
                .allMatch(nodeProperty ->
                        asStream(node.propertyNames())
                                .anyMatch(providedProperty -> (caseSensitive ? providedProperty.equals(nodeProperty) : providedProperty.equalsIgnoreCase(nodeProperty)))
                );
    }

    /**
     * Checks whether all schema-defined fields exist on the node.
     * @param node the JsonNode to search
     * @param caseSensitive whether lookup should be case-sensitive
     * @param fields schema field definitions
     * @return true when every field is present by canonical name or alias
     */
    public static boolean has(final JsonNode node, boolean caseSensitive, final JsonSchemaField... fields) {
        return Stream.of(fields).allMatch(field -> get(node, caseSensitive, field).isPresent());
    }

    /**
     * Processes a given {@code JsonNode} to return an {@code Optional<String>} that contains
     * the trimmed text value if the node is textual and non-blank.
     *
     * @param node the {@code JsonNode} to process
     * @return If the node is textual and non-blank, an {@code Optional<String>} containing the trimmed text value;
     *         Otherwise, an empty {@code Optional}
     */
    public static final Optional<String> trimmedIfText(JsonNode node) {
        return Optional.of(node)
                .filter(JsonNode::isString)
                .map(JsonNode::asString)
                .filter(s -> !s.isBlank())
                .map(String::trim);
    }

    /**
     * Returns an {@code Optional<JsonNode>} if the node is a string and is not blank.
     * @param node the node to check
     * @return an {@code Optional<JsonNode>} if the node is a string and is not blank.
     */
    public static final Optional<JsonNode> onlyNotBlankString(JsonNode node) {
        return Optional.of(node)
                .filter(n -> n.isString() && !n.asString().isBlank());
    }

    /**
     * Read the JSON into a byte array. Supports several fallbacks and property names:
     * 1. Textual node: hex or base64
     * 2. Array node: hex or base64
     * 3. Binary node: base64
     * 4. Property hex: hex
     * 5. Property base64: base64
     * @param node the node to check
     * @return an {@code Optional<byte[]>}
     */
    public static final Optional<byte[]> handleBinaryFormat(JsonNode node) {
        if (node == null || node.isNull()) {
            return Optional.empty();
        }
        if (node.isString()) {
            return bytesFromTextualNode(node);
        }
        if (node.isArray()) {
            return Optional.of(ObjectMapperFactory.fromJsonNode(node, byte[].class));
        }
        if (node.isBinary()) {
            return fromBinaryNode(node);
        }
        Optional<byte[]> base64Result = bytesFromBase64Property(node);
        if (base64Result.isPresent()) {
            return base64Result;
        }
        return bytesFromHexProperty(node);
    }

    /**
     * Read the JSON into a string. Supports several fallbacks and property names:
     * 1. Textual node
     * 2. Property string
     * @param node the node to check
     * @return an {@code Optional<String>}
     */
    public static Optional<String> handleStringFormat(JsonNode node) {
        if (node == null || node.isNull()) {
            return Optional.empty();
        }
        if (node.isString()) {
            return Optional.of(node.asString());

        }
        JsonNode str = node.get("string");
        if (str != null && str.isString()) {
            return Optional.of(str.asString());
        }
        return Optional.empty();
    }

    /**
     * Read the JSON into a byte array from a textual node. Supports hex and base64 formats.
     * @param node the node to check
     * @return an {@code Optional<byte[]>}
     */
    public static Optional<byte[]> bytesFromTextualNode(JsonNode node) {
        String text = trimmedIfText(node).orElse("");

        return HexNormalizer.isHexString(text)
                ? HexNormalizer.hexToBytes(text)
                : base64ToBytes(text);
    }

    /**
     * Read the JSON into a byte array from a binary node.
     * @param node the node to check
     * @return an {@code Optional<byte[]>}
     */
    private static Optional<byte[]> fromBinaryNode(JsonNode node) {
        try {
            return Optional.of(node.binaryValue());
        } catch (JsonNodeException e) {
            return Optional.empty();
        }
    }

    /**
     * Read the JSON into a byte array from base64 if given a property.
     * @param node the node to check
     * @return an {@code Optional<byte[]>}
     */
    public static Optional<byte[]> bytesFromBase64Property(JsonNode node) {
        return JsonUtils.get(node, false, "base64")
                .flatMap(JsonUtils::trimmedIfText)
                .flatMap(JsonUtils::base64ToBytes);
    }

    /**
     * Read the JSON into a byte array from hex if given a property.
     * @param node the node to check
     * @return an {@code Optional<byte[]>}
     */
    public static Optional<byte[]> bytesFromHexProperty(JsonNode node) {
        return JsonUtils.get(node, false, "hex")
                .flatMap(JsonUtils::trimmedIfText)
                .flatMap(HexNormalizer::hexToBytes);
    }

    /**
     * Convert a base64 string to a byte array.
     * @param base64String the base64 string
     * @return an {@code Optional<byte[]>}
     */
    private static Optional<byte[]> base64ToBytes(String base64String) {
        try {
            return Optional.of(Base64.getDecoder().decode(base64String));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    /**
     * Simple method that converts an Iterator to Stream. Some Jackson methods use Iterator.
     * This should probably be a standard library function.
     *
     * @param <T>      the type of elements
     * @param collection a Collection
     * @return a Stream with the same elements
     */
    public static final <T> Stream<T> asStream(final Collection<T> collection) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(collection.iterator(), Spliterator.ORDERED), false);
    }

    /**
     * Simple method that converts an Iterator to Stream. Some Jackson methods use Iterator.
     * This should probably be a standard library function.
     *
     * @param <T>      the type of elements
     * @param iterator an Iterator
     * @return a Stream with the same elements
     */
    public static final <T> Stream<T> asStream(final Iterator<T> iterator) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false);
    }

    /**
     * Converts the given Spliterator to Stream. Some Jackson methods use Spliterator.
     *
     * @param <T>         the type of elements
     * @param spliterator a Spliterator
     * @return a Stream with the same elements
     */
    public static final <T> Stream<T> asStream(final Spliterator<T> spliterator) {
        return StreamSupport.stream(spliterator, false);
    }

    /**
     * Returns the first non-null and non-empty JsonNode.
     *
     * @param nodes one or more JsonNodes
     * @return If found, an {@code Optional<JsonNode>}; Otherwise, an empty {@code Optional}
     */
    public static final Optional<JsonNode> firstNonNull(JsonNode ... nodes) {
        return Arrays.stream(nodes != null ? nodes : new JsonNode[0])
                .filter(n -> n != null && !n.isNull())
                .findFirst();
    }
}
