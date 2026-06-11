package paccor.cli;

import paccor.cert.CertKind;
import paccor.cert.TbsEnvelope;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import paccor.crypto.SignatureService;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import picocli.CommandLine;
import tools.jackson.databind.ObjectMapper;

public class AssembleCmdSigningPathsTest extends TestSupport {

    private static String b64(byte[] der) {
        return Base64.getEncoder().encodeToString(der);
    }

    @Test
    public void testMutuallyExclusiveOptions_error() throws Exception {
        // Minimal TBS/algId
        byte[] tbs = new byte[]{0x30, 0x00}; // empty sequence DER
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");
        File key = new File("src/test/resources/TestCA.private.example.pem");
        File cert = new File("src/test/resources/TestCA.cert.example.pem");

        int code = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", envJson.getAbsolutePath(),
                "--out", out.getAbsolutePath(),
                "--signature", Base64.getEncoder().encodeToString(new byte[]{1,2,3}),
                "--local-key", key.getAbsolutePath(),
                "--issuer-cert", cert.getAbsolutePath()
        );
        Assertions.assertEquals(ClientExitCodes.USAGE_ERROR.code(), code);
    }

    @Test
    public void testLocalMissingIssuer_requiresIssuerCert() throws Exception {
        byte[] tbs = new byte[]{0x30, 0x00};
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");
        File key = new File("src/test/resources/TestCA.private.example.pem");

        int code = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", envJson.getAbsolutePath(),
                "--out", out.getAbsolutePath(),
                "--local-key", key.getAbsolutePath()
        );
        Assertions.assertEquals(ClientExitCodes.USAGE_ERROR.code(), code);
    }

    @Test
    public void testLocalPkcs12PasswordOption_skipsPrompt() throws Exception {
        byte[] tbs = new byte[]{0x30, 0x00};
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");
        File key = new File("src/test/resources/TestCA2.cert.example.pkcs12");
        File cert = new File("src/test/resources/TestCA2.cert.example.der");

        try (MockedStatic<CliHelper> mockedStatic = Mockito.mockStatic(CliHelper.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> CliHelper.getPassword(ArgumentMatchers.anyString()))
                    .thenThrow(new AssertionError("interactive prompt should not be used"));
            int code = new CommandLine(new RootCmd()).execute(
                    "assemble",
                    "--in", envJson.getAbsolutePath(),
                    "--out", out.getAbsolutePath(),
                    "--issuer-cert", cert.getAbsolutePath(),
                    "--local-key", key.getAbsolutePath(),
                    "--local-key-password", "password"
            );
            Assertions.assertEquals(ClientExitCodes.SUCCESS.code(), code);
            Assertions.assertTrue(out.length() > 0);
        }
    }

    @Test
    public void testLocalPkcs12PasswordFile_skipsPrompt() throws Exception {
        byte[] tbs = new byte[]{0x30, 0x00};
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");
        File key = new File("src/test/resources/TestCA2.cert.example.pkcs12");
        File cert = new File("src/test/resources/TestCA2.cert.example.der");
        File pwFile = tempFile("pkcs12-password-", ".txt");
        Files.writeString(pwFile.toPath(), "password\n", StandardCharsets.UTF_8);

        try (MockedStatic<CliHelper> mockedStatic = Mockito.mockStatic(CliHelper.class, Mockito.CALLS_REAL_METHODS)) {
            mockedStatic.when(() -> CliHelper.getPassword(ArgumentMatchers.anyString()))
                    .thenThrow(new AssertionError("interactive prompt should not be used"));
            int code = new CommandLine(new RootCmd()).execute(
                    "assemble",
                    "--in", envJson.getAbsolutePath(),
                    "--out", out.getAbsolutePath(),
                    "--issuer-cert", cert.getAbsolutePath(),
                    "--local-key", key.getAbsolutePath(),
                    "--local-key-password-file", pwFile.getAbsolutePath()
            );
            Assertions.assertEquals(ClientExitCodes.SUCCESS.code(), code);
            Assertions.assertTrue(out.length() > 0);
        }
    }

    @Test
    public void testLocalPkcs12PasswordOptionsMutuallyExclusive() throws Exception {
        byte[] tbs = new byte[]{0x30, 0x00};
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");
        File key = new File("src/test/resources/TestCA2.cert.example.pkcs12");
        File cert = new File("src/test/resources/TestCA2.cert.example.der");
        File pwFile = tempFile("pkcs12-password-", ".txt");
        Files.writeString(pwFile.toPath(), "password", StandardCharsets.UTF_8);

        int code = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", envJson.getAbsolutePath(),
                "--out", out.getAbsolutePath(),
                "--issuer-cert", cert.getAbsolutePath(),
                "--local-key", key.getAbsolutePath(),
                "--local-key-password", "password",
                "--local-key-password-file", pwFile.getAbsolutePath()
        );
        Assertions.assertEquals(ClientExitCodes.USAGE_ERROR.code(), code);
    }

    @Test
    public void testPkcs11Validation_missingPin() throws Exception {
        // Validation should fail before attempting to load the module when PIN is missing
        byte[] tbs = new byte[]{0x30, 0x00};
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");
        File cert = new File("src/test/resources/TestCA.cert.example.pem");

        // Try to run with a clearly non-existent module.
        // If it returns USAGE_ERROR, it means validation caught the missing PIN (or missing key selector).
        // If it returns RUNTIME_ERROR, it might have bypassed validation due to env vars and failed loading the module.
        int code = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", envJson.getAbsolutePath(),
                "--out", out.getAbsolutePath(),
                "--issuer-cert", cert.getAbsolutePath(),
                "--pkcs11-module", "/no/such/libpkcs11.so",
                "--pkcs11-key-alias", "mykey"
        );

        if (System.getenv("PKCS11_PIN") != null) {
            // If PKCS11_PIN is set in the environment, validation might pass
            // but then it will fail to load the non-existent module.
            Assertions.assertTrue(code == ClientExitCodes.PKCS11_ERROR.code() || code == ClientExitCodes.RUNTIME_ERROR.code(),
                    "Expected PKCS11_ERROR or RUNTIME_ERROR when module is missing and PIN might be in env. Got: " + code);
        } else {
            Assertions.assertEquals(ClientExitCodes.USAGE_ERROR.code(), code, "Should fail with USAGE_ERROR when PIN is missing");
        }
    }

    @Test
    public void testPkcs11Validation_missingKeySelector() throws Exception {
        byte[] tbs = new byte[]{0x30, 0x00};
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");
        File cert = new File("src/test/resources/TestCA.cert.example.pem");

        int code = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", envJson.getAbsolutePath(),
                "--out", out.getAbsolutePath(),
                "--issuer-cert", cert.getAbsolutePath(),
                "--pkcs11-module", "/no/such/libpkcs11.so",
                "--pkcs11-pin", "1234"
        );
        Assertions.assertTrue(code == ClientExitCodes.USAGE_ERROR.code() || code == ClientExitCodes.RUNTIME_ERROR.code(),
                "Expected USAGE_ERROR or RUNTIME_ERROR when key selector is missing. Got: " + code);
    }

    @Test
    public void testRemoteSigner_success() throws Exception {
        // Prepare TBS and algorithm
        byte[] tbs = new byte[]{0x30, 0x00};
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");
        File key = new File("src/test/resources/TestCA.private.example.pem");
        File cert = new File("src/test/resources/TestCA.cert.example.pem");

        // Start a minimal HTTP server that signs using our local key
        InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 0);
        HttpServer server = HttpServer.create(addr, 0);
        server.createContext("/sign", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    byte[] body = exchange.getRequestBody().readAllBytes();
                    ObjectMapper om = new ObjectMapper();
                    Map<?,?> m = om.readValue(body, Map.class);
                    String payloadB64 = (String) m.get("payloadB64");
                    byte[] toSign = Base64.getDecoder().decode(payloadB64);
                    // Use same algorithm as provided by envelope (sha256WithRSA)
                    AlgorithmIdentifier a = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
                    byte[] sig = SignatureService.sign(toSign, a, key);
                    Map<String,Object> resp = new HashMap<>();
                    resp.put("signatureB64", Base64.getEncoder().encodeToString(sig));
                    resp.put("encoding", "der");
                    byte[] outJson = om.writeValueAsBytes(resp);
                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, outJson.length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(outJson); }
                } catch (Exception e) {
                    byte[] err = ("{"+"\"error\":\""+e.getMessage()+"\"}").getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(500, err.length);
                    try (OutputStream os = exchange.getResponseBody()) { os.write(err); }
                }
            }
        });
        server.start();
        int port = server.getAddress().getPort();
        String url = "http://127.0.0.1:" + port + "/sign";

        int code;
        try {
            code = new CommandLine(new RootCmd()).execute(
                    "assemble",
                    "--in", envJson.getAbsolutePath(),
                    "--out", out.getAbsolutePath(),
                    "--issuer-cert", cert.getAbsolutePath(),
                    "--remote-url", url
            );
        } finally {
            server.stop(0);
        }
        Assertions.assertEquals(0, code);
        Assertions.assertTrue(out.length() > 0);
    }
}
