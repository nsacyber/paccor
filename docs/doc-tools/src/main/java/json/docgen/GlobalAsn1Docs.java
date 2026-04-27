package json.docgen;

import java.util.List;

/**
 * Documentation entry for an ASN.1-backed Java type used in JSON deserialization.
 * Drives both the victools description resolver and the rendered global-types page.
 *
 * @param title human-readable title (e.g., "ASN1Integer")
 * @param description prose description, possibly multi-line Markdown
 * @param acceptedFormats list of accepted JSON shapes (e.g., "JSON number", "decimal string", "hex/base64 string")
 * @param examples example JSON snippets (Markdown-fenced or inline)
 */
public record GlobalAsn1Docs(String title,
                             String description,
                             List<String> acceptedFormats,
                             List<String> examples) {

    public GlobalAsn1Docs(String title, String description) {
        this(title, description, List.of(), List.of());
    }
}
