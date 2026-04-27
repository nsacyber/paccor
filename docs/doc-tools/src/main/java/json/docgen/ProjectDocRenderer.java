package json.docgen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import json.schema.JsonSchemaField;
import json.schema.JsonSchemaValue;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Renders project-owned JSON schema documentation: per-fieldset and per-vocabulary
 * Markdown pages, JSON fragments, and Mermaid graph files. Operates purely on the
 * {@link ProjectJsonSchema} registry and the {@link GlobalAsn1DocsRegistry}.
 *
 * <p>This was previously embedded in {@code SchemaUtils} test code; extraction
 * keeps doc generation independent of the victools-based JSON Schema generation.</p>
 */
public final class ProjectDocRenderer {

    private static final String ASN1_BLOCKS_FILE = "asn1-blocks.json";

    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode asn1Blocks;

    /**
     * Write all project-doc artifacts under the supplied output root.
     *
     * <p>Layout:</p>
     * <pre>
     * &lt;outputRoot&gt;/fragments/fields/&lt;fieldSetId&gt;.json
     * &lt;outputRoot&gt;/fragments/values/&lt;vocabularyId&gt;.json
     * &lt;outputRoot&gt;/docs/fields/&lt;fieldSetId&gt;.md
     * &lt;outputRoot&gt;/docs/values/&lt;vocabularyId&gt;.md
     * &lt;outputRoot&gt;/docs/field-sets.md
     * &lt;outputRoot&gt;/docs/value-vocabularies.md
     * &lt;outputRoot&gt;/docs/global-asn1-types.md
     * &lt;outputRoot&gt;/graphs/&lt;id&gt;.mmd
     * </pre>
     */
    public void writeArtifacts(Path outputRoot) throws IOException {
        Path fieldFragmentDir = outputRoot.resolve("fragments/fields");
        Path vocabularyFragmentDir = outputRoot.resolve("fragments/values");
        Path docsFieldDir = outputRoot.resolve("docs/fields");
        Path docsVocabularyDir = outputRoot.resolve("docs/values");
        Path graphDir = outputRoot.resolve("graphs");

        Files.createDirectories(fieldFragmentDir);
        Files.createDirectories(vocabularyFragmentDir);
        Files.createDirectories(docsFieldDir);
        Files.createDirectories(docsVocabularyDir);
        Files.createDirectories(graphDir);

        loadAsn1Blocks(outputRoot.resolve(ASN1_BLOCKS_FILE));

        for (JsonSchemaFieldSet fieldSet : ProjectJsonSchema.FIELD_SETS) {
            Files.writeString(fieldFragmentDir.resolve(fieldSet.id() + ".json"),
                    createFieldFragment(fieldSet).toPrettyString());
            Files.writeString(docsFieldDir.resolve(fieldSet.id() + ".md"), renderFieldSetMarkdown(fieldSet));
            Files.writeString(graphDir.resolve(fieldSet.id() + ".mmd"), buildFieldSetGraph(fieldSet).toMermaid());
        }

        for (JsonSchemaVocabulary vocabulary : ProjectJsonSchema.VOCABULARIES) {
            Files.writeString(vocabularyFragmentDir.resolve(vocabulary.id() + ".json"),
                    createVocabularyFragment(vocabulary).toPrettyString());
            Files.writeString(docsVocabularyDir.resolve(vocabulary.id() + ".md"), renderVocabularyMarkdown(vocabulary));
            Files.writeString(graphDir.resolve(vocabulary.id() + ".mmd"), buildVocabularyGraph(vocabulary).toMermaid());
        }

        Files.writeString(outputRoot.resolve("docs/field-sets.md"), renderFieldSetIndex());
        Files.writeString(outputRoot.resolve("docs/value-vocabularies.md"), renderVocabularyIndex());
        Files.writeString(outputRoot.resolve("docs/global-asn1-types.md"), renderGlobalAsn1TypesPage());
    }

    private ObjectNode createFieldFragment(JsonSchemaFieldSet fieldSet) {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("$id", "fragments/fields/" + fieldSet.id() + ".json");
        schema.put("title", fieldSet.title());
        schema.put("description", fieldSet.description());
        schema.put("type", "object");
        schema.put("x-jsonPath", fieldSet.jsonPath());
        ObjectNode properties = schema.putObject("properties");
        for (JsonSchemaField field : fieldSet.fields()) {
            ObjectNode property = properties.putObject(field.jsonName());
            if (!field.aliases().isEmpty()) {
                ArrayNode aliases = property.putArray("x-aliases");
                field.aliases().forEach(aliases::add);
            }
            String description = resolveDescription(field);
            if (description != null) {
                property.put("description", description);
            }
            if (field instanceof JsonSchemaValue value && value.asn1Value() != null) {
                property.put("x-asn1Value", value.asn1Value());
            }
        }
        schema.put("additionalProperties", true);
        return schema;
    }

    private ObjectNode createVocabularyFragment(JsonSchemaVocabulary vocabulary) {
        ObjectNode schema = mapper.createObjectNode();
        schema.put("$id", "fragments/values/" + vocabulary.id() + ".json");
        schema.put("title", vocabulary.title());
        schema.put("description", vocabulary.description());
        schema.put("type", "string");
        schema.put("x-jsonPath", vocabulary.jsonPath());
        schema.put("x-field", vocabulary.field().jsonName());
        ArrayNode enumValues = schema.putArray("enum");
        ArrayNode oneOf = schema.putArray("oneOf");
        for (JsonSchemaValue value : vocabulary.values()) {
            enumValues.add(value.jsonValue());
            ObjectNode option = oneOf.addObject();
            option.put("const", value.jsonValue());
            if (!value.aliases().isEmpty()) {
                ArrayNode aliases = option.putArray("x-aliases");
                value.aliases().forEach(aliases::add);
            }
            if (value.asn1Value() != null) {
                option.put("x-asn1Value", value.asn1Value());
            }
            if (value.description() != null) {
                option.put("description", value.description());
            }
        }
        return schema;
    }

    private String renderFieldSetIndex() {
        StringBuilder builder = new StringBuilder();
        builder.append("# Field Sets\n\n")
                .append("| Id | Path | Title |\n")
                .append("| --- | --- | --- |\n");
        for (JsonSchemaFieldSet fieldSet : ProjectJsonSchema.FIELD_SETS) {
            builder.append("| `")
                    .append(fieldSet.id())
                    .append("` | `")
                    .append(fieldSet.jsonPath())
                    .append("` | ")
                    .append(fieldSet.title())
                    .append(" |\n");
        }
        return builder.toString();
    }

    private String renderVocabularyIndex() {
        StringBuilder builder = new StringBuilder();
        builder.append("# Value Vocabularies\n\n")
                .append("| Id | Path | Field |\n")
                .append("| --- | --- | --- |\n");
        for (JsonSchemaVocabulary vocabulary : ProjectJsonSchema.VOCABULARIES) {
            builder.append("| `")
                    .append(vocabulary.id())
                    .append("` | `")
                    .append(vocabulary.jsonPath())
                    .append("` | `")
                    .append(vocabulary.field().jsonName())
                    .append("` |\n");
        }
        return builder.toString();
    }

    private String renderFieldSetMarkdown(JsonSchemaFieldSet fieldSet) {
        StringBuilder builder = new StringBuilder();
        builder.append("# ")
                .append(fieldSet.title())
                .append("\n\n")
                .append("Path: `")
                .append(fieldSet.jsonPath())
                .append("`\n\n")
                .append(fieldSet.description())
                .append("\n\n");

        JsonNode backing = backingClassNode(fieldSet.id());
        if (backing != null) {
            String fqcn = textOrNull(backing.get("fqcn"));
            String asn1 = textOrNull(backing.get("asn1"));
            if (fqcn != null) {
                builder.append("**Backing class:** `").append(fqcn).append("`\n\n");
            }
            if (asn1 != null) {
                builder.append("```asn1\n").append(asn1).append("\n```\n\n");
            }
        }

        builder.append("| Field | Aliases | ASN.1 | Description |\n")
                .append("| --- | --- | --- | --- |\n");
        for (JsonSchemaField field : fieldSet.fields()) {
            String asn1Value = field instanceof JsonSchemaValue value && value.asn1Value() != null ? value.asn1Value() : "";
            String description = resolveDescription(field);
            if ((description == null || description.isBlank()) && backing != null) {
                description = memberDocFor(backing, field.jsonName());
                if (description == null) {
                    for (String alias : field.aliases()) {
                        description = memberDocFor(backing, alias);
                        if (description != null) {
                            break;
                        }
                    }
                }
            }
            builder.append("| `")
                    .append(field.jsonName())
                    .append("` | ")
                    .append(renderList(field.aliases()))
                    .append(" | ")
                    .append(renderValue(asn1Value))
                    .append(" | ")
                    .append(renderText(description))
                    .append(" |\n");
        }
        builder.append("\n## Mermaid\n\n```mermaid\n")
                .append(buildFieldSetGraph(fieldSet).toMermaid())
                .append("```\n");
        return builder.toString();
    }

    private void loadAsn1Blocks(Path path) {
        if (!Files.exists(path)) {
            asn1Blocks = null;
            return;
        }
        try {
            asn1Blocks = mapper.readTree(Files.readString(path));
        } catch (IOException e) {
            asn1Blocks = null;
        }
    }

    private JsonNode backingClassNode(String fieldSetId) {
        if (asn1Blocks == null) {
            return null;
        }
        String simpleName = BackingClassRegistry.simpleNameFor(fieldSetId);
        if (simpleName == null) {
            return null;
        }
        JsonNode entry = asn1Blocks.get(simpleName);
        return entry == null || entry.isNull() ? null : entry;
    }

    private String memberDocFor(JsonNode backing, String memberName) {
        JsonNode members = backing.get("members");
        if (members == null) {
            return null;
        }
        JsonNode doc = members.get(memberName);
        return doc == null ? null : textOrNull(doc);
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String text = node.asString();
        return text.isBlank() ? null : text;
    }

    private String renderVocabularyMarkdown(JsonSchemaVocabulary vocabulary) {
        StringBuilder builder = new StringBuilder();
        builder.append("# ")
                .append(vocabulary.title())
                .append("\n\n")
                .append("Path: `")
                .append(vocabulary.jsonPath())
                .append("`\n\n")
                .append(vocabulary.description())
                .append("\n\n")
                .append("| Value | Aliases | ASN.1 | Description |\n")
                .append("| --- | --- | --- | --- |\n");
        for (JsonSchemaValue value : vocabulary.values()) {
            builder.append("| `")
                    .append(value.jsonValue())
                    .append("` | ")
                    .append(renderList(value.aliases()))
                    .append(" | ")
                    .append(renderValue(value.asn1Value()))
                    .append(" | ")
                    .append(renderText(value.description()))
                    .append(" |\n");
        }
        builder.append("\n## Mermaid\n\n```mermaid\n")
                .append(buildVocabularyGraph(vocabulary).toMermaid())
                .append("```\n");
        return builder.toString();
    }

    private String renderGlobalAsn1TypesPage() {
        StringBuilder builder = new StringBuilder();
        builder.append("# Global ASN.1 Types\n\n")
                .append("Documented JSON-side acceptance rules for ASN.1-backed Java types.\n\n");
        for (Map.Entry<Class<?>, GlobalAsn1Docs> entry : GlobalAsn1DocsRegistry.ENTRIES.entrySet()) {
            GlobalAsn1Docs docs = entry.getValue();
            builder.append("## ")
                    .append(docs.title())
                    .append("\n\n")
                    .append("`")
                    .append(entry.getKey().getName())
                    .append("`\n\n")
                    .append(docs.description())
                    .append("\n\n");
            if (!docs.acceptedFormats().isEmpty()) {
                builder.append("**Accepted formats:**\n\n");
                for (String format : docs.acceptedFormats()) {
                    builder.append("- ").append(format).append("\n");
                }
                builder.append("\n");
            }
            if (!docs.examples().isEmpty()) {
                builder.append("**Examples:**\n\n");
                for (String example : docs.examples()) {
                    builder.append(example).append("\n\n");
                }
            }
        }
        return builder.toString();
    }

    private SchemaGraph buildFieldSetGraph(JsonSchemaFieldSet fieldSet) {
        List<SchemaGraph.Node> nodes = new ArrayList<>();
        List<SchemaGraph.Edge> edges = new ArrayList<>();
        String rootId = graphId(fieldSet.id());
        nodes.add(new SchemaGraph.Node(rootId, fieldSet.jsonPath(), "path"));
        for (JsonSchemaField field : fieldSet.fields()) {
            String fieldId = graphId(fieldSet.id() + "-" + field.jsonName());
            nodes.add(new SchemaGraph.Node(fieldId, field.jsonName(), "field"));
            edges.add(new SchemaGraph.Edge(rootId, fieldId, "field"));
            for (String alias : field.aliases()) {
                String aliasId = graphId(fieldSet.id() + "-" + field.jsonName() + "-alias-" + alias);
                nodes.add(new SchemaGraph.Node(aliasId, alias, "alias"));
                edges.add(new SchemaGraph.Edge(fieldId, aliasId, "alias"));
            }
            if (field instanceof JsonSchemaValue value && value.asn1Value() != null) {
                String asn1Id = graphId(fieldSet.id() + "-" + field.jsonName() + "-asn1");
                nodes.add(new SchemaGraph.Node(asn1Id, value.asn1Value(), "asn1"));
                edges.add(new SchemaGraph.Edge(fieldId, asn1Id, "asn1"));
            }
        }
        return new SchemaGraph(fieldSet.title(), nodes, edges);
    }

    private SchemaGraph buildVocabularyGraph(JsonSchemaVocabulary vocabulary) {
        List<SchemaGraph.Node> nodes = new ArrayList<>();
        List<SchemaGraph.Edge> edges = new ArrayList<>();
        String rootId = graphId(vocabulary.id());
        nodes.add(new SchemaGraph.Node(rootId, vocabulary.field().jsonName(), "path"));
        for (JsonSchemaValue value : vocabulary.values()) {
            String valueId = graphId(vocabulary.id() + "-" + value.jsonValue());
            nodes.add(new SchemaGraph.Node(valueId, value.jsonValue(), "value"));
            edges.add(new SchemaGraph.Edge(rootId, valueId, "value"));
            for (String alias : value.aliases()) {
                String aliasId = graphId(vocabulary.id() + "-" + value.jsonValue() + "-alias-" + alias);
                nodes.add(new SchemaGraph.Node(aliasId, alias, "alias"));
                edges.add(new SchemaGraph.Edge(valueId, aliasId, "alias"));
            }
            if (value.asn1Value() != null) {
                String asn1Id = graphId(vocabulary.id() + "-" + value.jsonValue() + "-asn1");
                nodes.add(new SchemaGraph.Node(asn1Id, value.asn1Value(), "asn1"));
                edges.add(new SchemaGraph.Edge(valueId, asn1Id, "asn1"));
            }
        }
        return new SchemaGraph(vocabulary.title(), nodes, edges);
    }

    private String resolveDescription(JsonSchemaField field) {
        String description = field.description();
        if (description != null) {
            return description;
        }
        if (field instanceof JsonSchemaValue value && value.description() != null) {
            return value.description();
        }
        return null;
    }

    private String graphId(String rawValue) {
        return rawValue.replaceAll("[^A-Za-z0-9]", "_");
    }

    private String renderList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .map(value -> "`" + value + "`")
                .collect(Collectors.joining(", "));
    }

    private String renderValue(String value) {
        return value == null || value.isBlank() ? "" : "`" + value + "`";
    }

    private String renderText(String value) {
        return value == null ? "" : value.replace("\n", "<br/>");
    }
}
