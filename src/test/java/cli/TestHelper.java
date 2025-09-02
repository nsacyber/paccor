package cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public final class TestHelper {
    private static final String ROOT_PREFIX = "paccor-";

    private TestHelper() {
    }

    public static Path createTempDir(Class<?> testClass) throws IOException {
        return Files.createTempDirectory(systemTempDir(), ROOT_PREFIX + shortNameFor(testClass) + "-");
    }

    public static Path classTempPath(Class<?> testClass, String fileName) throws IOException {
        Path classDir = classTempRoot(testClass);
        return classDir.resolve(fileName);
    }

    public static Path classTempRoot(Class<?> testClass) throws IOException {
        Path classDir = classTempRootPath(testClass);
        Files.createDirectories(classDir);
        return classDir;
    }

    public static void deleteClassTempRoot(Class<?> testClass) throws IOException {
        deleteRecursively(classTempRootPath(testClass));
    }

    public static void deleteRecursively(Path path) throws IOException {
        if (path == null || Files.notExists(path)) {
            return;
        }

        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException ignored) {
                    }
                });
        }
    }

    static String shortNameFor(Class<?> testClass) {
        String simpleName = testClass.getSimpleName().replaceFirst("Tests?$", "");
        StringBuilder shortName = new StringBuilder();

        for (int i = 0; i < simpleName.length(); i++) {
            char ch = simpleName.charAt(i);
            if (Character.isUpperCase(ch) || Character.isDigit(ch)) {
                shortName.append(Character.toLowerCase(ch));
            }
        }

        if (shortName.length() == 0) {
            String fallback = simpleName.toLowerCase().replaceAll("[^a-z0-9]+", "");
            return fallback.isEmpty() ? "test" : fallback.substring(0, Math.min(fallback.length(), 8));
        }

        return shortName.toString();
    }

    private static Path classTempRootPath(Class<?> testClass) {
        return systemTempDir().resolve(ROOT_PREFIX + shortNameFor(testClass));
    }

    private static Path systemTempDir() {
        return Paths.get(System.getProperty("java.io.tmpdir"));
    }
}
