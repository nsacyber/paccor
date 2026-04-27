package json.docgen;

import java.util.List;
import json.schema.JsonSchemaField;

/**
 * Group of related JSON fields documented together.
 * @param id stable artifact id
 * @param title human-readable title
 * @param jsonPath JSON path for the containing object
 * @param description summary for docs
 * @param fields field definitions
 */
public record JsonSchemaFieldSet(
        String id,
        String title,
        String jsonPath,
        String description,
        List<? extends JsonSchemaField> fields) {
}
