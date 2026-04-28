package json.schema;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Shared contract for project-owned JSON field definitions.
 */
public interface JsonSchemaField {
    /**
     * Canonical JSON name used for new schema documentation and output.
     * @return canonical JSON property name
     */
    String getJsonName();

    /**
     * Canonical JSON name used for new schema documentation and output.
     * @return canonical JSON property name
     */
    default String jsonName() {
        return getJsonName();
    }

    /**
     * Optional non-case aliases accepted on input.
     * @return supported alias list
     */
    default List<String> getAliases() {
        return List.of();
    }

    /**
     * Optional non-case aliases accepted on input.
     * @return supported alias list
     */
    default List<String> aliases() {
        return getAliases();
    }

    /**
     * Optional documentation string for this field.
     * @return field description, or {@code null} if not provided
     */
    default String description() {
        return null;
    }

    /**
     * All acceptable property names for the field.
     * @return canonical name followed by aliases
     */
    default Stream<String> jsonNames() {
        return Stream.concat(Stream.of(jsonName()), aliases().stream())
                .filter(name -> name != null && !name.isBlank())
                .distinct();
    }

    /**
     * Convenience helper for enum constructors.
     * @param aliases optional aliases
     * @return immutable alias list
     */
    static List<String> aliasList(String... aliases) {
        return aliases == null ? List.of() : List.copyOf(Arrays.asList(aliases));
    }
}
