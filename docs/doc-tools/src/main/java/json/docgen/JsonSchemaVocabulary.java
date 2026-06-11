package json.docgen;

import java.util.List;
import paccor.json.schema.JsonSchemaField;
import paccor.json.schema.JsonSchemaValue;

/**
 * Documented set of allowed JSON values for a specific field.
 * @param id stable artifact id
 * @param title human-readable title
 * @param jsonPath JSON path that owns this vocabulary
 * @param description summary for docs
 * @param field owning field
 * @param values documented values
 */
public record JsonSchemaVocabulary(
        String id,
        String title,
        String jsonPath,
        String description,
        JsonSchemaField field,
        List<? extends JsonSchemaValue> values) {
}
