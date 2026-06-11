package paccor.docgen;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.util.DocTrees;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

/**
 * Custom Javadoc doclet that extracts ASN.1 notation blocks and member-level
 * Javadoc from concrete classes (default scope: tcg.credential.*) and emits
 * a JSON sidecar consumed by the project doc renderer.
 *
 * <p>Output layout (single JSON object keyed by simple class name):</p>
 * <pre>{@code
 * {
 *   "ComponentIdentifierV2": {
 *     "fqcn": "paccor.tcg.credential.ComponentIdentifierV2",
 *     "asn1": "ComponentIdentifier ::= SEQUENCE { ... }",
 *     "classDoc": "Trimmed plain-text class doc.",
 *     "members": {
 *       "componentManufacturer": "Field-level Javadoc text."
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p>Options:</p>
 * <ul>
 *   <li>{@code --asn1-output <path>} — required; output JSON file path.</li>
 *   <li>{@code --package-filter <prefix>} — optional; package prefix filter (default {@code tcg.credential}).</li>
 * </ul>
 */
public class AsnDoclet implements Doclet {

    private static final Pattern PRE_CODE_BLOCK = Pattern.compile(
            "<pre>\\s*\\{@code\\s+(.+?)\\}\\s*</pre>",
            Pattern.DOTALL);

    private String outputPath;
    private String packageFilter = "paccor.tcg.credential";
    private Reporter reporter;

    public static void main(String[] args) throws Exception {
        AsnDoclet extractor = new AsnDoclet();
        Path sourceRoot = Paths.get("src/main/java");

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--asn1-output" -> extractor.outputPath = args[++i];
                case "--package-filter" -> extractor.packageFilter = args[++i];
                case "--source-root" -> sourceRoot = Paths.get(args[++i]);
                default -> throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (extractor.outputPath == null || extractor.outputPath.isBlank()) {
            throw new IllegalArgumentException("Missing required --asn1-output <path> argument.");
        }

        extractor.extractFromSources(sourceRoot);
    }

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.reporter = reporter;
    }

    @Override
    public String getName() {
        return "AsnDoclet";
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        // Custom doclet must accept the standard flags Gradle's Javadoc task injects
        // (-d, -windowtitle, -doctitle, -notimestamp, etc.) as no-ops or the tool
        // rejects them as invalid.
        Consumer<String> ignore = v -> {};
        return Set.of(
                new SimpleOption("--asn1-output", 1,
                        "Required output JSON path", v -> outputPath = v),
                new SimpleOption("--package-filter", 1,
                        "Package prefix filter (default tcg.credential)", v -> packageFilter = v),
                new SimpleOption("-d", 1, "Destination dir (ignored)", ignore),
                new SimpleOption("-windowtitle", 1, "Window title (ignored)", ignore),
                new SimpleOption("-doctitle", 1, "Doc title (ignored)", ignore),
                new SimpleOption("-header", 1, "Header (ignored)", ignore),
                new SimpleOption("-footer", 1, "Footer (ignored)", ignore),
                new SimpleOption("-bottom", 1, "Bottom (ignored)", ignore),
                new SimpleOption("-notimestamp", 0, "No timestamp (ignored)", ignore),
                new SimpleOption("-quiet", 0, "Quiet (ignored)", ignore),
                new SimpleOption("-author", 0, "Author (ignored)", ignore),
                new SimpleOption("-version", 0, "Version (ignored)", ignore),
                new SimpleOption("-use", 0, "Use (ignored)", ignore),
                new SimpleOption("-Xlint:-options", 0, "Xlint options (ignored)", ignore),
                new SimpleOption("-encoding", 1, "Encoding (ignored)", ignore),
                new SimpleOption("-charset", 1, "Charset (ignored)", ignore),
                new SimpleOption("-docencoding", 1, "Doc encoding (ignored)", ignore),
                new SimpleOption("-source", 1, "Source version (ignored)", ignore),
                new SimpleOption("-locale", 1, "Locale (ignored)", ignore)
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean run(DocletEnvironment env) {
        if (outputPath == null || outputPath.isBlank()) {
            reporter.print(Diagnostic.Kind.ERROR,
                    "AsnDoclet requires --asn1-output <path>");
            return false;
        }

        DocTrees trees = env.getDocTrees();
        Map<String, Map<String, Object>> entries = new TreeMap<>();
        for (Element el : env.getIncludedElements()) {
            if (el.getKind() != ElementKind.CLASS) {
                continue;
            }
            TypeElement type = (TypeElement) el;
            String fqcn = type.getQualifiedName().toString();
            if (!fqcn.startsWith(packageFilter)) {
                continue;
            }
            Map<String, Object> entry = buildEntry(type, trees);
            if (entry != null) {
                entries.put(type.getSimpleName().toString(), entry);
            }
        }

        try {
            Path out = Paths.get(outputPath);
            if (out.getParent() != null) {
                Files.createDirectories(out.getParent());
            }
            Files.writeString(out, toJson(entries));
            reporter.print(Diagnostic.Kind.NOTE,
                    "AsnDoclet wrote " + entries.size() + " entries to " + out);
            return true;
        } catch (IOException e) {
            reporter.print(Diagnostic.Kind.ERROR,
                    "AsnDoclet failed to write " + outputPath + ": " + e.getMessage());
            return false;
        }
    }

    private void extractFromSources(Path sourceRoot) throws IOException {
        Map<String, Map<String, Object>> entries = new TreeMap<>();
        try (var paths = Files.walk(sourceRoot)) {
            paths.filter(path -> path.getFileName().toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            Map.Entry<String, Map<String, Object>> entry = buildEntryFromSource(path);
                            if (entry != null) {
                                entries.put(entry.getKey(), entry.getValue());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to parse " + path + ": " + e.getMessage(), e);
                        }
                    });
        }

        Path out = Paths.get(outputPath);
        if (out.getParent() != null) {
            Files.createDirectories(out.getParent());
        }
        Files.writeString(out, toJson(entries));
    }

    private Map<String, Object> buildEntry(TypeElement type, DocTrees trees) {
        DocCommentTree classDoc = trees.getDocCommentTree(type);
        String classDocText = classDoc == null ? null : docText(classDoc);
        String asn1 = extractAsn1Block(classDocText);
        Map<String, String> members = new LinkedHashMap<>();
        for (Element enclosed : type.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.FIELD) {
                continue;
            }
            DocCommentTree fieldDoc = trees.getDocCommentTree(enclosed);
            if (fieldDoc == null) {
                continue;
            }
            String text = docText(fieldDoc);
            if (text != null && !text.isBlank()) {
                members.put(enclosed.getSimpleName().toString(), text);
            }
        }
        if (asn1 == null && (classDocText == null || classDocText.isBlank()) && members.isEmpty()) {
            return null;
        }
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("fqcn", type.getQualifiedName().toString());
        if (asn1 != null) {
            entry.put("asn1", asn1);
        }
        if (classDocText != null && !classDocText.isBlank()) {
            entry.put("classDoc", classDocText.trim());
        }
        if (!members.isEmpty()) {
            entry.put("members", members);
        }
        return entry;
    }

    private Map.Entry<String, Map<String, Object>> buildEntryFromSource(Path path) throws IOException {
        String source = Files.readString(path);
        Matcher packageMatcher = Pattern.compile("(?m)^package\\s+([\\w.]+);").matcher(source);
        if (!packageMatcher.find()) {
            return null;
        }

        String pkg = packageMatcher.group(1);
        if (!pkg.startsWith(packageFilter)) {
            return null;
        }

        Matcher classMatcher = Pattern.compile(
                "(?s)(/\\*\\*.*?\\*/)?\\s*(?:public\\s+)?(?:final\\s+|abstract\\s+)?class\\s+(\\w+)")
                .matcher(source);
        if (!classMatcher.find()) {
            return null;
        }

        String simpleName = classMatcher.group(2);
        String classDocText = cleanJavadoc(classMatcher.group(1));
        String asn1 = extractAsn1Block(classDocText);
        Map<String, String> members = new LinkedHashMap<>();

        Matcher fieldMatcher = Pattern.compile(
                "(?s)/\\*\\*(.*?)\\*/\\s*(?:@[\\w.]+(?:\\([^)]*\\))?\\s*)*"
                        + "(?:private|protected|public)?\\s*"
                        + "(?:static\\s+)?(?:final\\s+)?(?:transient\\s+)?(?:volatile\\s+)?"
                        + "[\\w<>\\[\\].?, ]+\\s+(\\w+)\\s*(?:=|;)")
                .matcher(source);
        while (fieldMatcher.find()) {
            String memberDoc = cleanJavadoc("/**" + fieldMatcher.group(1) + "*/");
            if (memberDoc != null && !memberDoc.isBlank()) {
                members.put(fieldMatcher.group(2), memberDoc);
            }
        }

        if (asn1 == null && (classDocText == null || classDocText.isBlank()) && members.isEmpty()) {
            return null;
        }

        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("fqcn", pkg + "." + simpleName);
        if (asn1 != null) {
            entry.put("asn1", asn1);
        }
        if (classDocText != null && !classDocText.isBlank()) {
            entry.put("classDoc", classDocText.trim());
        }
        if (!members.isEmpty()) {
            entry.put("members", members);
        }
        return Map.entry(simpleName, entry);
    }

    private static String docText(DocCommentTree tree) {
        StringBuilder builder = new StringBuilder();
        tree.getFullBody().forEach(t -> builder.append(t.toString()));
        return builder.toString();
    }

    private static String cleanJavadoc(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String body = raw
                .replaceFirst("^/\\*\\*", "")
                .replaceFirst("\\*/$", "");
        StringBuilder cleaned = new StringBuilder();
        for (String line : body.split("\\R", -1)) {
            if (cleaned.length() > 0) {
                cleaned.append('\n');
            }
            cleaned.append(line.replaceFirst("^\\s*\\* ?", ""));
        }
        return cleaned.toString().trim();
    }

    private static String extractAsn1Block(String classDocText) {
        if (classDocText == null) {
            return null;
        }
        Matcher matcher = PRE_CODE_BLOCK.matcher(classDocText);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1).strip();
    }

    private static String toJson(Map<String, Map<String, Object>> entries) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        boolean firstClass = true;
        for (Map.Entry<String, Map<String, Object>> classEntry : entries.entrySet()) {
            if (!firstClass) {
                sb.append(",\n");
            }
            firstClass = false;
            sb.append("  ").append(quote(classEntry.getKey())).append(": {\n");
            boolean firstField = true;
            for (Map.Entry<String, Object> field : classEntry.getValue().entrySet()) {
                if (!firstField) {
                    sb.append(",\n");
                }
                firstField = false;
                sb.append("    ").append(quote(field.getKey())).append(": ");
                appendValue(sb, field.getValue(), "    ");
            }
            sb.append("\n  }");
        }
        sb.append("\n}\n");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendValue(StringBuilder sb, Object value, String indent) {
        if (value == null) {
            sb.append("null");
            return;
        }
        if (value instanceof String s) {
            sb.append(quote(s));
            return;
        }
        if (value instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) value;
            if (map.isEmpty()) {
                sb.append("{}");
                return;
            }
            sb.append("{\n");
            boolean first = true;
            String childIndent = indent + "  ";
            for (Map.Entry<String, Object> e : map.entrySet()) {
                if (!first) {
                    sb.append(",\n");
                }
                first = false;
                sb.append(childIndent).append(quote(e.getKey())).append(": ");
                appendValue(sb, e.getValue(), childIndent);
            }
            sb.append("\n").append(indent).append("}");
            return;
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            sb.append("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    sb.append(", ");
                }
                first = false;
                appendValue(sb, item, indent);
            }
            sb.append("]");
            return;
        }
        sb.append(quote(value.toString()));
    }

    private static String quote(String s) {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.append("\"").toString();
    }

    private static final class SimpleOption implements Option {
        private final String name;
        private final int argCount;
        private final String description;
        private final Consumer<String> handler;

        SimpleOption(String name, int argCount, String description, Consumer<String> handler) {
            this.name = name;
            this.argCount = argCount;
            this.description = description;
            this.handler = handler;
        }

        @Override
        public int getArgumentCount() { return argCount; }

        @Override
        public String getDescription() { return description; }

        @Override
        public Kind getKind() { return Kind.STANDARD; }

        @Override
        public List<String> getNames() { return List.of(name); }

        @Override
        public String getParameters() { return "<value>"; }

        @Override
        public boolean process(String option, List<String> arguments) {
            if (argCount > 0 && !arguments.isEmpty()) {
                handler.accept(arguments.get(0));
            } else {
                handler.accept(null);
            }
            return true;
        }
    }
}
