package paccor.cli;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;

public abstract class TestSupport {
    private Path tempDir;

    protected Path tempDir() throws IOException {
        if (tempDir == null) {
            tempDir = TestHelper.createTempDir(getClass());
        }
        return tempDir;
    }

    protected Path tempPath(String fileName) throws IOException {
        return tempDir().resolve(fileName);
    }

    protected File tempFile(String prefix, String suffix) throws IOException {
        return Files.createTempFile(tempDir(), normalizePrefix(prefix), suffix).toFile();
    }

    @AfterEach
    protected void cleanupTempDir() throws IOException {
        TestHelper.deleteRecursively(tempDir);
        tempDir = null;
    }

    private static String normalizePrefix(String prefix) {
        String normalized = prefix.replaceAll("[^a-zA-Z0-9-]", "");
        if (normalized.length() >= 3) {
            return normalized;
        }
        return (normalized + "tmp").substring(0, 3);
    }
}
