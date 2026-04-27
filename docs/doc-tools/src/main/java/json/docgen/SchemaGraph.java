package json.docgen;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Minimal field/value graph model for Mermaid diagrams.
 * @param title graph title
 * @param nodes nodes
 * @param edges directed edges
 */
public record SchemaGraph(String title, List<Node> nodes, List<Edge> edges) {
    public String toMermaid() {
        StringBuilder builder = new StringBuilder("graph TD\n");
        Map<String, Node> uniqueNodes = new LinkedHashMap<>();
        for (Node node : nodes) {
            uniqueNodes.putIfAbsent(node.id(), node);
        }
        uniqueNodes.values().forEach(node ->
                builder.append("  ")
                        .append(node.id())
                        .append("[\"")
                        .append(escape(node.label()))
                        .append("\"]\n"));
        edges.stream()
                .filter(Objects::nonNull)
                .forEach(edge -> {
                    builder.append("  ")
                            .append(edge.from())
                            .append(" -->");
                    if (edge.label() != null && !edge.label().isBlank()) {
                        builder.append("|\"")
                                .append(escape(edge.label()))
                                .append("\"|");
                    }
                    builder.append(" ")
                            .append(edge.to())
                            .append("\n");
                });
        builder.append("  classDef path fill:#1f6feb,stroke:#0b3d8c,color:#fff\n")
                .append("  classDef field fill:#0e7c66,stroke:#06463a,color:#fff\n")
                .append("  classDef alias fill:#6e7681,stroke:#3b414a,color:#fff\n")
                .append("  classDef value fill:#bf8700,stroke:#6e4d00,color:#fff\n")
                .append("  classDef asn1 fill:#8957e5,stroke:#4f31a3,color:#fff\n");
        uniqueNodes.values().stream()
                .filter(node -> node.cssClass() != null && !node.cssClass().isBlank())
                .forEach(node -> builder.append("  class ")
                        .append(node.id())
                        .append(" ")
                        .append(node.cssClass())
                        .append("\n"));
        return builder.toString();
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\"", "\\\"");
    }

    public record Node(String id, String label, String cssClass) {
        public Node(String id, String label) {
            this(id, label, null);
        }
    }

    public record Edge(String from, String to, String label) {
    }
}
