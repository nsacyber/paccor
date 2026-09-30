package paccor.cli;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Resolves repeatable file and glob command-line arguments. */
public final class GlobFileResolver {
    private GlobFileResolver() {
    }

    public static List<File> resolve(List<String> specifications) {
        if (specifications == null) return List.of();
        return specifications.stream()
                .filter(specification -> specification != null && !specification.isBlank())
                .flatMap(specification -> hasGlob(specification)
                        ? expandGlob(specification).stream()
                        : Stream.of(new File(specification)))
                .distinct()
                .toList();
    }

    private static boolean hasGlob(String specification) {
        return specification.contains("*") || specification.contains("?") || specification.contains("[");
    }

    private static List<File> expandGlob(String pattern) {
        String normalized = pattern.replace("\\", "/"); // Support Windows
        Path base = globRoot(normalized);
        boolean relativeToWorkingDirectory = base.equals(Paths.get("."));
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + normalized);
        List<File> matches = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(base)) {
            paths.filter(path -> matcher.matches(relativeToWorkingDirectory ? base.relativize(path) : path))
                    .map(Path::toFile)
                    .forEach(matches::add);
        } catch (Exception ignored) {
            // An unmatched or unreadable glob resolves to no files.
        }
        return matches;
    }

    private static Path globRoot(String normalizedPattern) {
        int firstGlob = firstGlobIndex(normalizedPattern);
        int separator = normalizedPattern.lastIndexOf('/', firstGlob);
        if (separator < 0) {
            return Paths.get(".");
        }
        String root = separator == 0 ? "/" : normalizedPattern.substring(0, separator);
        // A bare drive such as "C:" means the current directory on that drive
        // the pattern means its root.
        return Paths.get(root.endsWith(":") ? root + "/" : root);
    }

    private static int firstGlobIndex(String specification) {
        int index = specification.length();
        for (char glob : new char[] {'*', '?', '['}) {
            int found = specification.indexOf(glob);
            if (found >= 0) {
                index = Math.min(index, found);
            }
        }
        return index;
    }
}
