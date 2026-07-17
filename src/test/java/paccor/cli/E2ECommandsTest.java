package paccor.cli;

import java.util.Arrays;
import java.util.Optional;
import java.util.Vector;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.ReasonFlags;
import paccor.cert.CertSpecVersion;
import paccor.cert.PlatformCertificate;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectDirectoryAttributes;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.model.PlatformCertificateInformationModel;
import picocli.CommandLine;
import paccor.tcg.credential.PlatformConfigurationV2;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.TCGObjectIdentifier;

public class E2ECommandsTest extends TestSupport {

    // Common resources from src/test/resources (existing)
    private static final String RES_CA_CERT = "src/test/resources/TestCA.cert.example.pem";
    private static final String RES_CA_KEY = "src/test/resources/TestCA.private.example.pem";
    private static final String RES_HOLDER_CERT = "src/test/resources/ek.cer";
    private static final String RES_CA_CERT_EC = "src/test/resources/ca_2187.crt";
    private static final String RES_HOLDER_CERT_2187 = "src/test/resources/ek_cert_2187.der";
    private static final String RES_COMP_JSON_V3 = "src/test/resources/comps_medium_2187.json"; // PlatformConfigurationV3
    private static final String RES_EXT_JSON = "src/test/resources/otherExt.json";

    private static final String RES_GEN1_CA_CERT = "src/test/resources/sample_testgen1/PCTestCA.example.com.pem";
    private static final String RES_GEN1_CA_KEY = "src/test/resources/sample_testgen1/private.pem";
    private static final String RES_GEN1_HOLDER_CERT = "src/test/resources/sample_testgen1/ek.crt";
    private static final String RES_GEN1_ATTR_JSON = "src/test/resources/sample_testgen1/localhost-policyreference.json";
    private static final String RES_GEN1_ATTR_JSON_V2 = "src/test/resources/sample_testgen1/localhost-policyreference-v2.json";
    private static final String RES_GEN1_COMP_JSON_V2 = "src/test/resources/sample_testgen1/localhost-componentlist.json";
    private static final String RES_GEN1_COMP_JSON_V3 = "src/test/resources/sample_testgen1/localhost-componentlistv3adv.json"; // TODO move to larger list after some testing
    private static final String RES_GEN1_EXT_JSON = "src/test/resources/sample_testgen1/extentions.json";
    private static final String RES_GEN1_EXTPKC_JSON = "src/test/resources/sample_testgen1/extentionspkc.json";
    private static final String RES_GEN1_SIG_PROFILE = "rsa-sha256";
    private static final String RES_GEN1_BUILD_PCERT_FILE = "src/test/resources/sample_testgen1/platform_cert.20250909102720.crt";

    private static final String RES_BB_HOLDER_CERT = "src/test/resources/bare-bones-config/TCG_EK_ecc_p384_P-384_Test.pem";
    private static final String RES_BB_COMP_JSON_V2 = "src/test/resources/bare-bones-config/base-bare-bones-componentlist.json";
    private static final String RES_BB_COMP_JSON_V1 = "src/test/resources/tutorials/base-bare-bones-componentlist-v1.json";
    private static final String RES_BB_EXT_JSON = "src/test/resources/tutorials/base-bare-bones-extentions-no-ti.json";
    private static final String RES_BB_ATTR_JSON = "src/test/resources/bare-bones-config/base-bare-bones-policyreference.json";
    private static final String RES_BB_ATTR_JSON_V1 = "src/test/resources/tutorials/base-bare-bones-policyreference-v1.json";

    private static final String RES_MLDSA65_CA_CERT = "src/test/resources/TestCA.mldsa65.cert.example.pem";
    private static final String RES_MLDSA65_CA_KEY = "src/test/resources/TestCA.mldsa65.private.example.pem";
    private static final String RES_MLDSA65_SIG_PROFILE = "ml-dsa-65";

    private static final String RES_TEST4_COMP_JSON = "src/test/resources/tutorials/v2/components.json";
    private static final String RES_TEST4_COMP_WITH_TRAITS_JSON = "src/test/resources/tutorials/v2/componentswithtraits.json";
    private static final String RES_TEST4_DELTA_COMP_1_JSON = "src/test/resources/tutorials/v2/deltacomponents1.json";
    private static final String RES_TEST4_DELTA_WITH_TRAITS_JSON = "src/test/resources/tutorials/v2/deltacomponentswithtraits1.json";
    private static final String RES_TEST4_REBASE_COMP_JSON = "src/test/resources/tutorials/v2/rebasecomponents1.json";
    private static final String RES_TEST4_REBASE_WITH_TRAITS_JSON = "src/test/resources/tutorials/v2/rebasecomponentswithtraits1.json";

    @Test
    void certgen_full_options_x509_and_overwrite_flow() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-pkc.json");

        int rc = RootCmd.commandLine().execute(
            "certgen",
            "--kind", "PKC",
            "--type", "base",
            "--serial", "123456789",
            "--not-before", "20240101",
            "--not-after", "20300101",
                "--issuer-cert", RES_CA_CERT,
                "--holder-cert", RES_HOLDER_CERT,
                "--attributes-json", RES_GEN1_ATTR_JSON_V2,
                "--components-json", RES_COMP_JSON_V3,
                "--extensions-json", RES_EXT_JSON,
                "--sig-profile", "rsa-pss-sha256-32",
            "--out", env.toString()
        );
        Assertions.assertEquals(ClientExitCodes.SUCCESS.code(), rc, "certgen should succeed with full options");
        String json = Files.readString(env);
        Assertions.assertTrue(json.contains("\"type\"") && json.toLowerCase().contains("pkc"), "Envelope should record type PKC");

        // Attempt in-place overwrite without flag should fail
        int rcNoOverwrite = RootCmd.commandLine().execute(
            "certgen",
            "--in", env.toString(),
            "--out", env.toString()
        );
        Assertions.assertEquals(ClientExitCodes.USAGE_ERROR.code(), rcNoOverwrite, "Refusing to overwrite without --overwrite-in-place should exit USAGE_ERROR");

        // With overwrite flag should pass (still ok if no TBS rebuild requested)
        int rcOverwrite = RootCmd.commandLine().execute(
            "certgen",
            "--in", env.toString(),
            "--overwrite-in-place",
            "--out", env.toString()
        );
        Assertions.assertEquals(ClientExitCodes.SUCCESS.code(), rcOverwrite, "Overwrite in place should succeed");
    }

    @Test
    void certgen_and_assemble_ac_1() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-ac1.json");
        Path cer = tempDir.resolve("env-ac1.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--serial", "1",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_GEN1_CA_CERT,
                "--holder-cert", RES_GEN1_HOLDER_CERT,
                "--attributes-json", RES_GEN1_ATTR_JSON,
                "--components-json", RES_GEN1_COMP_JSON_V2,
                "--extensions-json", RES_GEN1_EXT_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc, "tbsgen should succeed with full options");
        Assertions.assertTrue(Files.exists(env));
        String json = Files.readString(env);
        Assertions.assertTrue(json.contains("\"type\"") && json.toLowerCase().contains("ac"), "Envelope should record type AC");

        int rcStub = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_GEN1_CA_KEY,
                "--issuer-cert", RES_GEN1_CA_CERT
        );
        Assertions.assertEquals(0, rcStub, "assemble stub should succeed");
        Assertions.assertTrue(Files.exists(cer));
        Assertions.assertTrue(Files.exists(new File(RES_GEN1_BUILD_PCERT_FILE).toPath()));
        X509AttributeCertificateHolder cerHolder = CliHelper.loadCert(cer.toString(), CliHelper.x509type.ATTRIBUTE_CERTIFICATE);
        Attribute[] list = cerHolder.getAttributes(TCGObjectIdentifier.tcgAtPlatformConfigurationV2);
        Assertions.assertEquals(1, list.length, "AC should have one PlatformConfigurationV2 attribute");
        ASN1Encodable value = list[0].getAttrValues().getObjectAt(0);
        Assertions.assertInstanceOf(ASN1Sequence.class, value);
        PlatformConfigurationV2 pcv2 = PlatformConfigurationV2.getInstance((ASN1Sequence) value);
        Assertions.assertNotNull(pcv2, "AC should carry PlatformConfigurationV2");
        Assertions.assertFalse(pcv2.getComponentIdentifiers().isEmpty(), "PlatformConfigurationV2 should contain components");
    }

    @Test
    void certgen_and_assemble_ac_v3() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-acv3.json");
        Path cer = tempDir.resolve("env-acv3.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--serial", "1891",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_GEN1_CA_CERT,
                "--holder-cert", RES_GEN1_HOLDER_CERT,
                "--attributes-json", RES_GEN1_ATTR_JSON,
                "--components-json", RES_GEN1_COMP_JSON_V3,
                "--extensions-json", RES_GEN1_EXT_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc, "certgen should succeed with full options");
        Assertions.assertTrue(Files.exists(env));
        String json = Files.readString(env);
        Assertions.assertTrue(json.contains("\"type\"") && json.toLowerCase().contains("ac"), "Envelope should record type AC");

        int rcStub = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_GEN1_CA_KEY,
                "--issuer-cert", RES_GEN1_CA_CERT
        );
        Assertions.assertEquals(0, rcStub, "assemble stub should succeed");
        Assertions.assertTrue(Files.exists(cer));
        Assertions.assertTrue(Files.exists(new File(RES_GEN1_BUILD_PCERT_FILE).toPath()));
        X509AttributeCertificateHolder cerHolder = CliHelper.loadCert(cer.toString(), CliHelper.x509type.ATTRIBUTE_CERTIFICATE);
        Attribute[] list = cerHolder.getAttributes(TCGObjectIdentifier.tcgAtPlatformConfigurationV2);
        Assertions.assertEquals(1, list.length, "AC should have one PlatformConfigurationV2 attribute");
        ASN1Encodable value = list[0].getAttrValues().getObjectAt(0);
        Assertions.assertInstanceOf(ASN1Sequence.class, value);
        PlatformConfigurationV2 pcv2 = PlatformConfigurationV2.getInstance((ASN1Sequence)value);
        Assertions.assertNotNull(pcv2, "AC should downcast V3 input to PlatformConfigurationV2");
        Assertions.assertFalse(pcv2.getComponentIdentifiers().isEmpty());
    }

    @Test
    void certgen_and_assemble_pkc_v3() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-pkcv3.json");
        Path cer = tempDir.resolve("env-pkcv3.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--serial", "1891",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_GEN1_CA_CERT,
                "--holder-cert", RES_GEN1_CA_CERT,
                "--attributes-json", RES_GEN1_ATTR_JSON_V2,
                "--components-json", RES_GEN1_COMP_JSON_V3,
                "--extensions-json", RES_GEN1_EXTPKC_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc, "certgen should succeed with full options");
        Assertions.assertTrue(Files.exists(env));
        String json = Files.readString(env);
        Assertions.assertTrue(json.contains("\"type\"") && json.toLowerCase().contains("pkc"), "Envelope should record type PKC");

        int rcStub = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_GEN1_CA_KEY,
                "--issuer-cert", RES_GEN1_CA_CERT
        );
        Assertions.assertEquals(0, rcStub, "assemble stub should succeed");
        Assertions.assertTrue(Files.exists(cer));
        X509CertificateHolder cerHolder = CliHelper.loadCert(cer.toString(), CliHelper.x509type.CERTIFICATE);
        List<Attribute> list = SubjectDirectoryAttributes.getInstance(cerHolder.getExtension(Extension.subjectDirectoryAttributes).getParsedValue()).getAttributes().stream().toList();
        ASN1Encodable value = list.stream().filter(obj -> obj.getAttrType().equals(TCGObjectIdentifier.tcgAtPlatformConfigurationV3)).findFirst().get().getAttrValues().getObjectAt(0);
        Assertions.assertInstanceOf(ASN1Sequence.class, value);
        PlatformConfigurationV3 pcv3 = PlatformConfigurationV3.getInstance((ASN1Sequence)value);
        Assertions.assertNotNull(pcv3, "AC should have PlatformConfigurationV3 attribute");
        Assertions.assertEquals(4, pcv3.getPlatformComponents().size());
    }

    @Test
    void certgen_and_assemble_ac_v1() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-acv1.json");
        Path cer = tempDir.resolve("env-acv1.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--kind", "AC",
                "--serial", "1",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_CA_CERT,
                "--holder-cert", RES_BB_HOLDER_CERT,
                "--attributes-json", RES_BB_ATTR_JSON_V1,
                "--components-json", RES_BB_COMP_JSON_V1,
                "--extensions-json", RES_BB_EXT_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc, "certgen should succeed with v1.0 AC tutorial inputs");
        Assertions.assertTrue(Files.exists(env));
        String json = Files.readString(env);
        Assertions.assertTrue(json.contains("\"type\"") && json.toLowerCase().contains("ac"), "Envelope should record type AC");

        int rcAssemble = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_CA_KEY,
                "--issuer-cert", RES_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble, "assemble should succeed for v1.0 AC");
        Assertions.assertTrue(Files.exists(cer));

        PlatformCertificate certificate = PlatformCertificate.load(cer.toFile());
        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.isAttributeCertificate());
        Assertions.assertEquals(CertSpecVersion.V1_0, certificate.resolvedSpecVersion());
        Assertions.assertNotNull(certificate.platformConfigurationV1(), "v1.0 AC should carry PlatformConfigurationV1");
        Assertions.assertFalse(certificate.platformConfigurationV1().getComponentIdentifiers().isEmpty(), "PlatformConfigurationV1 should contain components");
    }

    @Test
    void certgen_and_assemble_pkc_v1() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-pkcv1.json");
        Path cer = tempDir.resolve("env-pkcv1.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--kind", "PKC",
                "--serial", "1",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_CA_CERT,
                "--holder-cert", RES_CA_CERT,
                "--attributes-json", RES_BB_ATTR_JSON_V1,
                "--components-json", RES_BB_COMP_JSON_V1,
                "--extensions-json", RES_GEN1_EXTPKC_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc, "certgen should succeed with v1.0 PKC tutorial inputs");
        Assertions.assertTrue(Files.exists(env));
        String json = Files.readString(env);
        Assertions.assertTrue(json.contains("\"type\"") && json.toLowerCase().contains("pkc"), "Envelope should record type PKC");

        int rcAssemble = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_CA_KEY,
                "--issuer-cert", RES_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble, "assemble should succeed for v1.0 PKC");
        Assertions.assertTrue(Files.exists(cer));

        PlatformCertificate certificate = PlatformCertificate.load(cer.toFile());
        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.isPublicKeyCertificate());
        Assertions.assertEquals(CertSpecVersion.V1_0, certificate.resolvedSpecVersion());
        Assertions.assertNotNull(certificate.platformConfigurationV1(), "v1.0 PKC should carry PlatformConfigurationV1");
        Assertions.assertFalse(certificate.platformConfigurationV1().getComponentIdentifiers().isEmpty(), "PlatformConfigurationV1 should contain components");
    }

    @Test
    void assemble_stub_and_invalid_signature_paths() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env.json");
        Path outStub = tempDir.resolve("assembled-stub.bin");
        Path outBadSig = tempDir.resolve("assembled-badsig.bin");

        // Create envelope with algorithm and minimal data so assemble enters verification path
        int rc1 = RootCmd.commandLine().execute(
            "certgen",
            "--kind", "pkc",
            "--sig-profile", "rsa-sha256",
            "--issuer-cert", RES_CA_CERT,
            "--holder-cert", RES_HOLDER_CERT_2187,
            "--out", env.toString()
        );
        Assertions.assertEquals(ClientExitCodes.SUCCESS.code(), rc1);

        // Assemble without signature -> stub path; also pass --pem just to exercise the option parsing
        int rcStub = RootCmd.commandLine().execute(
            "assemble",
            "--in", env.toString(),
            "--out", outStub.toString(),
            "--pem"
        );
        Assertions.assertEquals(ClientExitCodes.SUCCESS.code(), rcStub, "assemble stub should succeed");
        Assertions.assertTrue(Files.exists(outStub));
        String stub = Files.readString(outStub, StandardCharsets.UTF_8).trim();
        Assertions.assertTrue(stub.equalsIgnoreCase("ac") || stub.equalsIgnoreCase("pkc"), "stub should contain type marker");

        // Now exercise signature-related options with a bogus signature and validation cert; expect validation failure
        String bogusSigB64 = Base64.getEncoder().encodeToString(new byte[64]);
        int rcBad = RootCmd.commandLine().execute(
                    "assemble",
                    "--in", env.toString(),
                    "--out", outBadSig.toString(),
                    "--signature", bogusSigB64,
                    "--sig-encoding", "p1363", // exercise conversion branch (will still fail verification)
                    "--issuer-cert", RES_CA_CERT
                );
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), rcBad, "assemble should report verification failure with invalid signature");
        Assertions.assertFalse(Files.exists(outBadSig), "no output on failed verification");
    }

    @Test
    void validate_components_ac_v2_positive_and_negative() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-ac1.json");
        Path cer = tempDir.resolve("env-ac1.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--serial", "1",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_GEN1_CA_CERT,
                "--holder-cert", RES_GEN1_HOLDER_CERT,
                "--attributes-json", RES_GEN1_ATTR_JSON,
                "--components-json", RES_GEN1_COMP_JSON_V2,
                "--extensions-json", RES_GEN1_EXT_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_GEN1_CA_KEY,
                "--issuer-cert", RES_GEN1_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", RES_GEN1_CA_CERT,
                "--components-json", RES_GEN1_COMP_JSON_V2
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check for V2");

        // Negative: tweak a copy of components JSON (change first MODEL value)
        Path badJson = tempDir.resolve("bad-v2.json");
        String orig = Files.readString(Path.of(RES_GEN1_COMP_JSON_V2), StandardCharsets.UTF_8);
        String tweaked = orig.replaceFirst("\"MODEL\"\\s*:\\s*\"", "\"MODEL\": \"INVALID-");
        Files.writeString(badJson, tweaked, StandardCharsets.UTF_8);
        int rcValidateBad = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", RES_GEN1_CA_CERT,
                "--components-json", badJson.toString()
        );
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), rcValidateBad, "validate should fail when components JSON does not match AC");
    }

    @Test
    void validate_components_ac_bb_positive() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-acbb.json");
        Path cer = tempDir.resolve("env-acbb.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--serial", "1",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_GEN1_CA_CERT,
                "--holder-cert", RES_BB_HOLDER_CERT,
                "--attributes-json", RES_BB_ATTR_JSON,
                "--components-json", RES_BB_COMP_JSON_V2,
                "--extensions-json", RES_BB_EXT_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_GEN1_CA_KEY,
                "--issuer-cert", RES_GEN1_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        PlatformCertificate certificate = PlatformCertificate.load(cer.toFile());
        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.isAttributeCertificate());
        Assertions.assertEquals(CertSpecVersion.V1_1, certificate.resolvedSpecVersion());
        Extension crlExt = certificate.getExtension(Extension.cRLDistributionPoints);
        Assertions.assertNotNull(crlExt);
        CRLDistPoint crlDistPoint = CRLDistPoint.getInstance(crlExt.getParsedValue());
        Assertions.assertNotNull(crlDistPoint);
        Assertions.assertEquals(1, crlDistPoint.getDistributionPoints().length);
        DistributionPoint dp0 = DistributionPoint.getInstance(crlDistPoint.getDistributionPoints()[0]);
        Assertions.assertNotNull(dp0);
        Assertions.assertEquals(ReasonFlags.superseded, dp0.getReasons().intValue());

        int rcValidateOk = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", RES_GEN1_CA_CERT,
                "--components-json", RES_BB_COMP_JSON_V2
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check");
    }

    @Test
    void validate_components_ac_v1_positive_and_negative() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-acv1.json");
        Path cer = tempDir.resolve("env-acv1.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--kind", "AC",
                "--serial", "1",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_CA_CERT,
                "--holder-cert", RES_BB_HOLDER_CERT,
                "--attributes-json", RES_BB_ATTR_JSON_V1,
                "--components-json", RES_BB_COMP_JSON_V1,
                "--extensions-json", RES_BB_EXT_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_CA_KEY,
                "--issuer-cert", RES_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", RES_CA_CERT,
                "--components-json", RES_BB_COMP_JSON_V1
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check for v1.0 AC");

        Path badJson = tempDir.resolve("bad-v1-ac.json");
        String orig = Files.readString(Path.of(RES_BB_COMP_JSON_V1), StandardCharsets.UTF_8);
        String tweaked = orig.replaceFirst("\"MODEL\"\\s*:\\s*\"", "\"MODEL\": \"INVALID-");
        Files.writeString(badJson, tweaked, StandardCharsets.UTF_8);
        int rcValidateBad = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", RES_CA_CERT,
                "--components-json", badJson.toString()
        );
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), rcValidateBad, "validate should fail when v1.0 AC components JSON does not match");
    }

    @Test
    void validate_components_ac_with_delta_positive() throws Exception {
        Path tempDir = tempDir();
        Path envBase = tempDir.resolve("env-actest4Base.json");
        Path cerBase = tempDir.resolve("env-actest4Base.cer");
        Path envDelta = tempDir.resolve("env-actest4Delta.json");
        Path cerDelta = tempDir.resolve("env-actest4Delta.cer");
        Path envRebase = tempDir.resolve("env-actest4Rebase.json");
        Path cerRebase = tempDir.resolve("env-actest4Rebase.cer");

        // Base
        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--serial", "1",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_MLDSA65_CA_CERT,
                "--holder-cert", RES_BB_HOLDER_CERT,
                "--attributes-json", RES_BB_ATTR_JSON,
                "--components-json", RES_TEST4_COMP_WITH_TRAITS_JSON,
                "--extensions-json", RES_BB_EXT_JSON,
                "--sig-profile", RES_MLDSA65_SIG_PROFILE,
                "--finalize",
                "--out", envBase.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = RootCmd.commandLine().execute(
                "assemble",
                "--in", envBase.toString(),
                "--out", cerBase.toString(),
                "--pem",
                "--local-key", RES_MLDSA65_CA_KEY,
                "--issuer-cert", RES_MLDSA65_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cerBase));

        int rcValidateOk = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cerBase.toString(),
                "--publicKeyCert", RES_MLDSA65_CA_CERT,
                "--components-json", RES_TEST4_COMP_JSON
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check");

        // Delta
        int rc2 = RootCmd.commandLine().execute(
                "certgen",
                "--serial", "2",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_MLDSA65_CA_CERT,
                "--holder-cert", cerBase.toString(),
                "--attributes-json", RES_BB_ATTR_JSON,
                "--components-json", RES_TEST4_DELTA_COMP_1_JSON,
                "--extensions-json", RES_BB_EXT_JSON,
                "--sig-profile", RES_MLDSA65_SIG_PROFILE,
                "--finalize",
                "--out", envDelta.toString()
        );
        Assertions.assertEquals(0, rc2);

        int rcAssemble2 = RootCmd.commandLine().execute(
                "assemble",
                "--in", envDelta.toString(),
                "--out", cerDelta.toString(),
                "--pem",
                "--local-key", RES_MLDSA65_CA_KEY,
                "--issuer-cert", RES_MLDSA65_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble2);
        Assertions.assertTrue(Files.exists(cerDelta));

        int rcValidateOk2 = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cerDelta.toString(),
                "--publicKeyCert", RES_MLDSA65_CA_CERT,
                "--components-json", RES_TEST4_DELTA_COMP_1_JSON
        );
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), rcValidateOk2,
                "delta component validation should require --prev-pcert");

        // Rebase
        int rc3 = RootCmd.commandLine().execute(
                "certgen",
                "--type", "rebase",
                "--serial", "1",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_MLDSA65_CA_CERT,
                "--holder-cert", cerBase.toString(),
                "--attributes-json", RES_BB_ATTR_JSON,
                "--components-json", RES_TEST4_REBASE_WITH_TRAITS_JSON,
                "--extensions-json", RES_BB_EXT_JSON,
                "--sig-profile", RES_MLDSA65_SIG_PROFILE,
                "--finalize",
                "--out", envRebase.toString()
        );
        Assertions.assertEquals(0, rc3);

        int rcAssemble3 = RootCmd.commandLine().execute(
                "assemble",
                "--in", envRebase.toString(),
                "--out", cerRebase.toString(),
                "--pem",
                "--local-key", RES_MLDSA65_CA_KEY,
                "--issuer-cert", RES_MLDSA65_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble3);
        Assertions.assertTrue(Files.exists(cerRebase));

        int rcValidateOk3 = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cerRebase.toString(),
                "--publicKeyCert", RES_MLDSA65_CA_CERT,
                "--components-json", RES_TEST4_REBASE_WITH_TRAITS_JSON
        );
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), rcValidateOk3,
                "rebase component validation should require --prev-pcert");

        int rcValidateOk4 = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cerDelta.toString(),
                "--publicKeyCert", RES_MLDSA65_CA_CERT,
                "--components-json", RES_TEST4_REBASE_COMP_JSON,
                "--prev-pcert", cerBase.toString()
        );
        Assertions.assertEquals(0, rcValidateOk4, "validate should pass components check");
    }

    @Test
    void validate_components_ac_v3_positive_and_negative() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-acv3.json");
        Path cer = tempDir.resolve("env-acv3.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--serial", "1891",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_GEN1_CA_CERT,
                "--holder-cert", RES_GEN1_HOLDER_CERT,
                "--attributes-json", RES_GEN1_ATTR_JSON,
                "--components-json", RES_GEN1_COMP_JSON_V3,
                "--extensions-json", RES_GEN1_EXT_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_GEN1_CA_KEY,
                "--issuer-cert", RES_GEN1_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", RES_GEN1_CA_CERT,
                "--components-json", RES_GEN1_COMP_JSON_V3
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check for V3");

        // Negative: tweak a copy of components JSON (change first VALUE string)
        Path badJson = tempDir.resolve("bad-v3.json");
        String orig = Files.readString(Path.of(RES_GEN1_COMP_JSON_V3), StandardCharsets.UTF_8);
        String tweaked = orig.replaceFirst("\\\"traitValue\\\"\\s*:\\s*\\\"", "\"traitValue\": \"INVALID-");
        Files.writeString(badJson, tweaked, StandardCharsets.UTF_8);
        int rcValidateBad = RootCmd.commandLine().execute(
                "validate",
                "--x509v2AttrCert", cer.toString(),
                "--publicKeyCert", RES_GEN1_CA_CERT,
                "--components-json", badJson.toString()
        );
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), rcValidateBad, "validate should fail when V3 components JSON does not match AC");
    }

    @Test
    void validate_components_pkc_v3_positive_and_negative() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-pkcv3.json");
        Path cer = tempDir.resolve("env-pkcv3.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--serial", "1891",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_MLDSA65_CA_CERT,
                "--holder-cert", RES_GEN1_CA_CERT,
                "--attributes-json", RES_GEN1_ATTR_JSON_V2,
                "--components-json", RES_GEN1_COMP_JSON_V3,
                "--extensions-json", RES_GEN1_EXTPKC_JSON,
                "--sig-profile", RES_MLDSA65_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_MLDSA65_CA_KEY,
                "--issuer-cert", RES_MLDSA65_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = RootCmd.commandLine().execute(
                "validate",
                "-X", cer.toString(),
                "--publicKeyCert", RES_MLDSA65_CA_CERT,
                "--components-json", RES_GEN1_COMP_JSON_V3
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check for V3");

        PlatformCertificate certificate = PlatformCertificate.load(cer.toFile());
        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.isPublicKeyCertificate());
        Extension sdaExt = certificate.getExtension(Extension.subjectDirectoryAttributes);
        Assertions.assertNotNull(sdaExt);
        SubjectDirectoryAttributes sda = SubjectDirectoryAttributes.getInstance(sdaExt.getParsedValue());
        Assertions.assertNotNull(sda);
        Object[] array = sda.getAttributes().toArray();
        long size = array.length;
        long distinct = Arrays.stream(array)
                .filter(attr -> attr instanceof Attribute)
                .map(attr -> ((Attribute) attr).getAttrType())
                .distinct()
                .count();
        Assertions.assertEquals(distinct, size, "Duplicate attributes found in subjectDirectoryAttributes");


        // Negative: tweak a copy of components JSON (change first VALUE string)
        Path badJson = tempDir.resolve("bad-v3.json");
        String orig = Files.readString(Path.of(RES_GEN1_COMP_JSON_V3), StandardCharsets.UTF_8);
        String tweaked = orig.replaceFirst("\\\"traitValue\\\"\\s*:\\s*\\\"", "\"traitValue\": \"INVALID-");
        Files.writeString(badJson, tweaked, StandardCharsets.UTF_8);
        int rcValidateBad = RootCmd.commandLine().execute(
                "validate",
                "-X", cer.toString(),
                "--publicKeyCert", RES_GEN1_CA_CERT,
                "--components-json", badJson.toString()
        );
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED, ClientExitCodes.lookupCode(rcValidateBad), "validate should fail when V3 components JSON does not match PKC");
    }

    @Test
    void validate_components_pkc_v1_positive_and_negative() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-pkcv1.json");
        Path cer = tempDir.resolve("env-pkcv1.cer");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--kind", "PKC",
                "--serial", "1",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_CA_CERT,
                "--holder-cert", RES_CA_CERT,
                "--attributes-json", RES_BB_ATTR_JSON_V1,
                "--components-json", RES_BB_COMP_JSON_V1,
                "--extensions-json", RES_GEN1_EXTPKC_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--finalize",
                "--out", env.toString()
        );
        Assertions.assertEquals(0, rc);

        int rcAssemble = RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", RES_CA_KEY,
                "--issuer-cert", RES_CA_CERT
        );
        Assertions.assertEquals(0, rcAssemble);
        Assertions.assertTrue(Files.exists(cer));

        int rcValidateOk = RootCmd.commandLine().execute(
                "validate",
                "--pkcPlatformCert", cer.toString(),
                "--publicKeyCert", RES_CA_CERT,
                "--components-json", RES_BB_COMP_JSON_V1
        );
        Assertions.assertEquals(0, rcValidateOk, "validate should pass components check for v1.0 PKC");

        Path badJson = tempDir.resolve("bad-v1-pkc.json");
        String orig = Files.readString(Path.of(RES_BB_COMP_JSON_V1), StandardCharsets.UTF_8);
        String tweaked = orig.replaceFirst("\"MODEL\"\\s*:\\s*\"", "\"MODEL\": \"INVALID-");
        Files.writeString(badJson, tweaked, StandardCharsets.UTF_8);
        int rcValidateBad = RootCmd.commandLine().execute(
                "validate",
                "--pkcPlatformCert", cer.toString(),
                "--publicKeyCert", RES_CA_CERT,
                "--components-json", badJson.toString()
        );
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), rcValidateBad, "validate should fail when v1.0 PKC components JSON does not match");
    }

    @Test
    void certgen_rejects_pkc_when_attributes_declare_v11() throws Exception {
        Path tempDir = tempDir();
        Path env = tempDir.resolve("env-specv.json");

        int rc = RootCmd.commandLine().execute(
                "certgen",
                "--kind", "PKC",
                "--serial", "1891",
                "--not-before", "20240101",
                "--not-after", "20300101",
                "--issuer-cert", RES_GEN1_CA_CERT,
                "--holder-cert", RES_GEN1_CA_CERT,
                "--attributes-json", RES_GEN1_ATTR_JSON,
                "--components-json", RES_GEN1_COMP_JSON_V3,
                "--extensions-json", RES_GEN1_EXTPKC_JSON,
                "--sig-profile", RES_GEN1_SIG_PROFILE,
                "--out", env.toString()
        );

        Assertions.assertEquals(ClientExitCodes.USAGE_ERROR.code(), rc, "certgen should reject PKC output when attributes declare the v1.1 family");
    }
}
