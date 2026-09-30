package paccor.cli;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link GlobFileResolver}.
 */
class GlobFileResolverTest extends TestSupport {

    private Path fixture() throws Exception {
        Path dir = tempDir();
        Files.createDirectories(dir.resolve("certs"));
        Files.writeString(dir.resolve("certs/a.cer"), "a");
        Files.writeString(dir.resolve("certs/b.cer"), "b");
        Files.writeString(dir.resolve("certs/c.pem"), "c");
        return dir;
    }

    @Test
    void absoluteGlob_matchesFilesInDirectory() throws Exception {
        Path dir = fixture();

        List<File> files = GlobFileResolver.resolve(List.of(dir + File.separator + "certs" + File.separator + "*.cer"));

        Assertions.assertEquals(2, files.size(), files.toString());
    }

    @Test
    void absoluteGlob_withForwardSlashes_matchesFiles() throws Exception {
        Path dir = fixture();

        List<File> files = GlobFileResolver.resolve(List.of(dir.toString().replace('\\', '/') + "/certs/*.cer"));

        Assertions.assertEquals(2, files.size(), files.toString());
    }

    @Test
    void unmatchedGlob_resolvesToNothing() throws Exception {
        Path dir = fixture();

        Assertions.assertTrue(GlobFileResolver.resolve(List.of(dir + File.separator + "certs" + File.separator + "*.der")).isEmpty());
        Assertions.assertTrue(GlobFileResolver.resolve(List.of(dir + File.separator + "missing" + File.separator + "*.cer")).isEmpty());
    }

    @Test
    void relativeGlob_withDirectory_matchesFiles() {
        List<File> files = GlobFileResolver.resolve(List.of("src/test/resources/sample_testgen1/*.pem"));

        Assertions.assertFalse(files.isEmpty());
        Assertions.assertTrue(files.stream().allMatch(file -> file.getName().endsWith(".pem")), files.toString());
    }

    @Test
    void plainPath_isReturnedAsIs() {
        List<File> files = GlobFileResolver.resolve(List.of("does-not-exist.cer"));

        Assertions.assertEquals(List.of(new File("does-not-exist.cer")), files);
    }
}
