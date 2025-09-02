package cli;

import cert.CertKind;
import cert.TbsEnvelope;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import crypto.SignatureService;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import tools.jackson.databind.ObjectMapper;

public class AssembleCmdTest extends TestSupport {

    private static String b64(byte[] der) {
        return Base64.getEncoder().encodeToString(der);
    }

    @Test
    public void testAssembleWithDetachedSignature_success() throws Exception {
        // Build minimal ASN.1 TBS (a simple sequence) that can be parsed
        byte[] tbs = new DERSequence(new ASN1Encodable[]{ new ASN1Integer(1) }).getEncoded("DER");
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        File key = new File("src/test/resources/TestCA.private.example.pem");
        File cert = new File("src/test/resources/TestCA.cert.example.pem");
        byte[] sig = SignatureService.sign(tbs, algId, key);

        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");

        int code = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", envJson.getAbsolutePath(),
                "--out", out.getAbsolutePath(),
                "--signature", Base64.getEncoder().encodeToString(sig),
                "--issuer-cert", cert.getAbsolutePath()
        );
        Assertions.assertEquals(0, code);
        Assertions.assertTrue(out.length() > 0);
    }

    @Test
    public void testAssembleWithDetachedSignature_failOnBadSig() throws Exception {
        byte[] tbs = new DERSequence(new ASN1Encodable[]{ new ASN1Integer(2) }).getEncoded("DER");
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        File cert = new File("src/test/resources/TestCA.cert.example.pem");

        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");

        // Corrupted signature (random bytes base64)
        String badSigB64 = Base64.getEncoder().encodeToString(new byte[]{1,2,3,4,5});
        int code = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", envJson.getAbsolutePath(),
                "--out", out.getAbsolutePath(),
                "--signature", badSigB64,
                "--issuer-cert", cert.getAbsolutePath()
        );
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), code);
    }

    @Test
    public void testAssembleLocalKey_strictVerify() throws Exception {
        byte[] tbs = new DERSequence(new ASN1Encodable[]{ new ASN1Integer(3) }).getEncoded("DER");
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption, DERNull.INSTANCE);
        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".der");

        File key = new File("src/test/resources/ca_2187.key");
        File cert = new File("src/test/resources/ca_2187.crt");

        int code = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", envJson.getAbsolutePath(),
                "--out", out.getAbsolutePath(),
                "--local-key", key.getAbsolutePath(),
                "--issuer-cert", cert.getAbsolutePath()
        );
        Assertions.assertEquals(0, code);
    }

    @Test
    public void testAssembleStubFallback_whenMissingInputs() throws Exception {
        // Envelope with only type; missing tbs/algId should trigger stub write
        TbsEnvelope env = TbsEnvelope.builder().type(CertKind.PKC).build();
        File envJson = tempFile("tbs-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-", ".txt");
        int code = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", envJson.getAbsolutePath(),
                "--out", out.getAbsolutePath()
        );
        Assertions.assertEquals(0, code);
        String content = Files.readString(out.toPath());
        Assertions.assertTrue(content.startsWith("PKC"));
    }

    @Test
    public void testAssembleLocalKeyMlDsa65() throws Exception {
        byte[] tbs = new DERSequence(new ASN1Encodable[]{ new ASN1Integer(99) }).getEncoded("DER");
        AlgorithmIdentifier algId = new AlgorithmIdentifier(NISTObjectIdentifiers.id_ml_dsa_65);
        TbsEnvelope env = TbsEnvelope.builder()
                .type(CertKind.PKC)
                .tbsDerB64(b64(tbs))
                .sigAlgDerB64(b64(algId.getEncoded()))
                .build();
        File envJson = tempFile("tbs-mldsa-", ".json");
        new ObjectMapper().writerWithDefaultPrettyPrinter().writeValue(envJson, env);
        File out = tempFile("assembled-mldsa-", ".der");

        File key = new File("src/test/resources/TestCA.mldsa65.private.example.pem");
        File cert = new File("src/test/resources/TestCA.mldsa65.cert.example.pem");

        int code = new CommandLine(new RootCmd()).execute(
                "assemble",
                "--in", envJson.getAbsolutePath(),
                "--out", out.getAbsolutePath(),
                "--local-key", key.getAbsolutePath(),
                "--issuer-cert", cert.getAbsolutePath()
        );
        Assertions.assertEquals(0, code);
        Assertions.assertTrue(out.length() > 0);
    }
}
