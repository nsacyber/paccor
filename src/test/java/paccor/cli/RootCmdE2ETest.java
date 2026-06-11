package paccor.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class RootCmdE2ETest extends TestSupport {

    @Test
    void testTbsGenAssembleValidateFlow() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env.json");
        Path out = tempDir.resolve("assembled.bin");

        // tbsgen: minimal stub creates envelope
        int rc1 = new CommandLine(new RootCmd()).execute(
                "certgen",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc1, "certgen should exit 0");
        Assertions.assertTrue(Files.exists(env), "Envelope should be created");
        String json = Files.readString(env);
        Assertions.assertTrue(json.contains("tbsDerB64") || json.contains("type"), "Envelope JSON should be present");

        // assemble: stub writes a file
        int rc2 = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", env.toString(),
                "--out", out.toString()
        );
        Assertions.assertEquals(0, rc2, "assemble should exit 0");
        Assertions.assertTrue(Files.exists(out), "Assembled output should be created");

        // validate: show usage (no signer present in this smoke test)
        int rc3 = new CommandLine(new RootCmd()).execute(
                "validate",
                "--help"
        );
        Assertions.assertEquals(0, rc3, "validate --help should exit 0");
    }

    @Test
    void testViewCommandShowsCertificateSummary() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            int rc = new CommandLine(new RootCmd()).execute(
                    "view",
                    "--certificate", "src/test/resources/TestCA.cert.example.pem"
            );
            Assertions.assertEquals(0, rc);
        } finally {
            System.setOut(original);
        }

        String text = out.toString(StandardCharsets.UTF_8);
        Assertions.assertTrue(text.contains("Certificate Kind: PKC"));
        Assertions.assertTrue(text.contains("Issuer:"));
        Assertions.assertTrue(text.contains("Cryptographic Anchors:"));
    }
}
