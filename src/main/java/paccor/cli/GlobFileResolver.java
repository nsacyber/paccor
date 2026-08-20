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
        Path full = Paths.get(pattern);
        Path base = findGlobRoot(full);
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern.replace("\\", "/"));
        List<File> matches = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(base)) {
            boolean absolute = full.isAbsolute();
            paths.filter(path -> matcher.matches(absolute ? path : base.relativize(path)))
                    .map(Path::toFile)
                    .forEach(matches::add);
        } catch (Exception ignored) {
            // An unmatched or unreadable glob resolves to no files.
        }
        return matches;
    }

    private static Path findGlobRoot(Path path) {
        Path root = path.getRoot();
        Path accumulator = root;
        for (Path part : path) {
            if (hasGlob(part.toString())) break;
            accumulator = accumulator == null ? part : accumulator.resolve(part);
        }
        return accumulator == null ? Paths.get(".") : accumulator;
    }
}
