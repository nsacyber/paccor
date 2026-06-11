package paccor.cli;

import paccor.crypto.LocalSignatureStrategy;
import paccor.crypto.SignatureService;
import paccor.crypto.SignatureStrategy;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

/**
 * Contains compatibility tests for the signing CLI
 */
public class CompatCliTest extends TestSupport {
    private static final String IN_EK = "src/test/resources/ek.cer";
    private static final String IN_DEV_JSON = "src/test/resources/deviceInfo.json";
    private static final String IN_POL_JSON = "src/test/resources/policyRef.json";
    private static final String IN_OXT_JSON = "src/test/resources/otherExt.json";
    public static final String IN_PUB_CERT = "src/test/resources/TestCA.cert.example.pem";
    private static final String IN_PRIV_KEY = "src/test/resources/TestCA.private.example.pem";
    
    public static final String IN_PKCS1_PUB = "src/test/resources/ca.pkcs1.pub.pem";
    private static final String IN_PKCS1_KEY = "src/test/resources/ca.pkcs1.pem";
    
    private static final String SERIAL_NUMBER = "85748596854741335865214";
    private static final String NOT_BEFORE = "20180628";
    private static final String NOT_AFTER = "20280630";
    
    private static final String IN_EK_2187 = "src/test/resources/ek_cert_2187.der";
    private static final String IN_DEV_JSON_LARGE_2187 = "src/test/resources/comps_large_2187.json";
    private static final String IN_POL_JSON_2187 = "src/test/resources/refopts_2187.json";
    private static final String IN_OXT_JSON_2187 = "src/test/resources/otherext_2187.json";
    public static final String PUB_CERT_2187 = "src/test/resources/ca_2187.crt";
    private static final String IN_PRIV_KEY_2187 = "src/test/resources/ca_2187.key";
    private static final String SERIAL_NUMBER_LARGE_2187 = "34146254462154519453612545265143";
    
    private static final String IN_DEV_JSON_MEDIUM_2187 = "src/test/resources/comps_medium_2187.json";
    private static final String SERIAL_NUMBER_MEDIUM_2187 = "97643418218536546461465465475484";
    
    private static final String IN_DEV_JSON_FLAWED_2187 = "src/test/resources/comps_flawed_2187.json";
    private static final String SERIAL_NUMBER_FLAWED_2187 = "1264412569842165127559455612352835923762345";
    
    public static final String IN_PUB_CERT_PKCS12 = "src/test/resources/TestCA2.cert.example.der";
    private static final String IN_PRIV_PKCS12 = "src/test/resources/TestCA2.cert.example.pkcs12";
    
    @Test
    public void test1NoExceptions() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-compat1.json");
        Path cer = tempDir.resolve("env-compat1.cer");

        int rc = new CommandLine(new RootCmd()).execute(
                "certgen",
                "--serial", SERIAL_NUMBER,
                "--not-before", NOT_BEFORE,
                "--not-after", NOT_AFTER,
                "--issuer-cert", IN_PUB_CERT,
                "--holder-cert", IN_EK,
                "--attributes-json", IN_POL_JSON,
                "--components-json", IN_DEV_JSON,
                "--extensions-json", IN_OXT_JSON,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", IN_PRIV_KEY,
                "--issuer-cert", IN_PUB_CERT
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = new CommandLine(new RootCmd()).execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", IN_PUB_CERT,
                "--components-json", IN_DEV_JSON
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check");
    }
    
    @Test
    public void test1NoExceptionsPKCS1RSA() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-compatPKCS1RSA.json");
        Path cer = tempDir.resolve("env-compatPKCS1RSA.cer");

        int rc = new CommandLine(new RootCmd()).execute(
                "certgen",
                "--serial", SERIAL_NUMBER,
                "--not-before", NOT_BEFORE,
                "--not-after", NOT_AFTER,
                "--issuer-cert", IN_PKCS1_PUB,
                "--holder-cert", IN_EK,
                "--attributes-json", IN_POL_JSON,
                "--components-json", IN_DEV_JSON,
                "--extensions-json", IN_OXT_JSON,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", IN_PKCS1_KEY,
                "--issuer-cert", IN_PKCS1_PUB
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = new CommandLine(new RootCmd()).execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", IN_PKCS1_PUB,
                "--components-json", IN_DEV_JSON
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check");
    }
    
    @Test
    public void testLarge2187NoExceptions() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-compatLarge2187.json");
        Path cer = tempDir.resolve("env-compatLarge2187.cer");

        int rc = new CommandLine(new RootCmd()).execute(
                "certgen",
                "--serial", SERIAL_NUMBER_LARGE_2187,
                "--not-before", NOT_BEFORE,
                "--not-after", NOT_AFTER,
                "--issuer-cert", PUB_CERT_2187,
                "--holder-cert", IN_EK_2187,
                "--kind", "AC", // THIS EK DOES NOT SET Extended Key Usage
                "--attributes-json", IN_POL_JSON_2187,
                "--components-json", IN_DEV_JSON_LARGE_2187,
                "--extensions-json", IN_OXT_JSON_2187,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", IN_PRIV_KEY_2187,
                "--issuer-cert", PUB_CERT_2187
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = new CommandLine(new RootCmd()).execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", PUB_CERT_2187,
                "--components-json", IN_DEV_JSON_LARGE_2187
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check");
    }
    
    @Test
    public void testMedium2187NoExceptions() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-compatMedium2187.json");
        Path cer = tempDir.resolve("env-compatMedium2187.cer");

        int rc = new CommandLine(new RootCmd()).execute(
                "certgen",
                "--serial", SERIAL_NUMBER_MEDIUM_2187,
                "--not-before", NOT_BEFORE,
                "--not-after", NOT_AFTER,
                "--issuer-cert", PUB_CERT_2187,
                "--holder-cert", IN_EK_2187,
                "--kind", "AC", // THIS EK DOES NOT SET Extended Key Usage
                "--attributes-json", IN_POL_JSON_2187,
                "--components-json", IN_DEV_JSON_MEDIUM_2187,
                "--extensions-json", IN_OXT_JSON_2187,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", IN_PRIV_KEY_2187,
                "--issuer-cert", PUB_CERT_2187
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = new CommandLine(new RootCmd()).execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", PUB_CERT_2187,
                "--components-json", IN_DEV_JSON_MEDIUM_2187
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check");
    }
    
    @Test
    public void testFlawed2187NoExceptions() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-compatFlawed2187.json");
        Path cer = tempDir.resolve("env-compatFlawed2187.cer");

        int rc = new CommandLine(new RootCmd()).execute(
                "certgen",
                "--serial", SERIAL_NUMBER_FLAWED_2187,
                "--not-before", NOT_BEFORE,
                "--not-after", NOT_AFTER,
                "--issuer-cert", PUB_CERT_2187,
                "--holder-cert", IN_EK_2187,
                "--kind", "AC", // THIS EK DOES NOT SET Extended Key Usage
                "--attributes-json", IN_POL_JSON_2187,
                "--components-json", IN_DEV_JSON_FLAWED_2187,
                "--extensions-json", IN_OXT_JSON_2187,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", IN_PRIV_KEY_2187,
                "--issuer-cert", PUB_CERT_2187
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = new CommandLine(new RootCmd()).execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", PUB_CERT_2187,
                "--components-json", IN_DEV_JSON_FLAWED_2187
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check");
    }
    
    @Test
    public void testPKCS12() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-compatPKCS12.json");
        Path cer = tempDir.resolve("env-compatPKCS12.cer");

        int rc = new CommandLine(new RootCmd()).execute(
                "certgen",
                "--serial", SERIAL_NUMBER,
                "--not-before", NOT_BEFORE,
                "--not-after", NOT_AFTER,
                "--issuer-cert", IN_PUB_CERT_PKCS12,
                "--holder-cert", IN_EK,
                "--attributes-json", IN_POL_JSON,
                "--components-json", IN_DEV_JSON,
                "--extensions-json", IN_OXT_JSON,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", IN_PRIV_PKCS12,
                "--local-key-password", "password",
                "--issuer-cert", IN_PUB_CERT_PKCS12
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = new CommandLine(new RootCmd()).execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", IN_PUB_CERT_PKCS12,
                "--components-json", IN_DEV_JSON
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check");
    }

    @Test
    public void testSignAndVerifyRoundTripPkcs12Rsa() throws Exception {
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption);
        byte[] tbs = "hello-tbs-pkcs12".getBytes();
        File key = new File("src/test/resources/TestCA2.cert.example.pkcs12");
        File cert = new File("src/test/resources/TestCA2.cert.example.der");

        SignatureStrategy strategy = new LocalSignatureStrategy(key, "password", null);
        byte[] sig = strategy.sign(tbs, algId);
        Assertions.assertTrue(SignatureService.verifyWithCert(cert, algId, tbs, sig));
    }
}
