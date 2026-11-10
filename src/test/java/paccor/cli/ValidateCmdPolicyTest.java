package paccor.cli;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import paccor.cert.CertSpecVersion;
import paccor.cert.CertType;
import paccor.cert.PlatformCertificate;
import paccor.crypto.CertificateValidity;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.validator.ComponentValidationService;
import paccor.validator.PreviousPlatformCertificateValidator;

/**
 * End-to-end policy tests for component validation across base, delta, and rebase chains.
 * Certificates are generated and signed with the CLI so every check runs against real encodings.
 */
public class ValidateCmdPolicyTest extends TestSupport {
    private static final String CA_CERT = "src/test/resources/sample_testgen1/PCTestCA.example.com.pem";
    private static final String CA_KEY = "src/test/resources/sample_testgen1/private.pem";
    private static final String OTHER_CA_CERT = "src/test/resources/TestCA.cert.example.pem";
    private static final String OTHER_CA_KEY = "src/test/resources/TestCA.private.example.pem";
    private static final String EK_CERT = "src/test/resources/sample_testgen1/ek.crt";
    private static final String ATTR_V11 = "src/test/resources/bare-bones-config/base-bare-bones-policyreference.json";
    private static final String ATTR_V20 = "src/test/resources/sample_testgen1/localhost-policyreference-v2.json";
    private static final String EXT = "src/test/resources/tutorials/base-bare-bones-extentions-no-ti.json";
    private static final String BASE_COMPONENTS = "src/test/resources/tutorials/v2/components.json";
    private static final String DELTA_COMPONENTS = "src/test/resources/tutorials/v2/deltacomponents1.json";
    private static final String AFTER_DELTA_COMPONENTS = "src/test/resources/tutorials/v2/rebasecomponents1.json";
    private static final String REFERENCE_TIME = "2027-01-15T00:00:00Z";

    @Test
    void platformValidityIsInformationalUnlessEnforcedForAcAndPkc() throws Exception {
        for (String kind : List.of("ac", "pkc")) {
            Path expired = issueWithValidity("expired-" + kind, ATTR_V20, BASE_COMPONENTS, null,
                    EK_CERT, null, CA_CERT, CA_KEY, "10", "20240101", "20260101", kind);
            Path future = issueWithValidity("future-" + kind, ATTR_V20, BASE_COMPONENTS, null,
                    EK_CERT, null, CA_CERT, CA_KEY, "11", "20280101", "20300101", kind);
            List<String> output = captureOutput(() -> Assertions.assertEquals(0,
                    validateWith(expired, "--skip-component-validation", "--reference-time", REFERENCE_TIME)));
            Assertions.assertTrue(output.contains("Platform certificate validity: EXPIRED as of "
                    + REFERENCE_TIME + " (not enforced)"), output.toString());
            output = captureOutput(() -> Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(),
                    validateWith(expired, "--skip-component-validation", "--reference-time", REFERENCE_TIME,
                            "--enforce-validity")));
            Assertions.assertTrue(output.contains("Platform certificate validity: EXPIRED as of "
                    + REFERENCE_TIME + " (enforced)"), output.toString());
            output = captureOutput(() -> Assertions.assertEquals(0,
                    validateWith(future, "--skip-component-validation", "--reference-time", REFERENCE_TIME)));
            Assertions.assertTrue(output.contains("Platform certificate validity: NOT_YET_VALID as of "
                    + REFERENCE_TIME + " (not enforced)"), output.toString());
            Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), validateWith(future,
                    "--skip-component-validation", "--reference-time", REFERENCE_TIME, "--enforce-validity"));
        }
    }

    @Test
    void validityEnforcementDoesNotRequireTrustAnchorsOrCrls() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        List<String> output = captureOutput(() -> Assertions.assertEquals(0, validateWith(base,
                "--skip-component-validation", "--reference-time", "2027-01-15T02:00:00+02:00",
                "--enforce-validity")));
        Assertions.assertTrue(output.contains("Platform certificate validity: OK as of "
                + REFERENCE_TIME + " (enforced)"), output.toString());
        Assertions.assertTrue(output.stream().noneMatch(line -> line.startsWith("Trust-anchor validation:")
                || line.startsWith("CRL validation:")), output.toString());
    }

    @Test
    void signerValidityIsEnforcedEvenWithoutTrustAnchors() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        for (String[] window : List.of(new String[] {"2024-01-01T00:00:00Z", "2026-01-01T00:00:00Z", "EXPIRED"},
                new String[] {"2028-01-01T00:00:00Z", "2030-01-01T00:00:00Z", "NOT_YET_VALID"})) {
            Path signer = issuerWithValidity("signer-" + window[2], CA_CERT, CA_KEY, window[0], window[1]);
            Assertions.assertEquals(0, validateSignedBy(base, signer.toString(), "--skip-component-validation",
                    "--reference-time", REFERENCE_TIME));
            List<String> output = captureOutput(() -> Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(),
                    validateSignedBy(base, signer.toString(), "--skip-component-validation",
                            "--reference-time", REFERENCE_TIME, "--enforce-validity")));
            Assertions.assertTrue(output.contains("Signer certificate validity: " + window[2] + " as of "
                    + REFERENCE_TIME + " (enforced)"), output.toString());
            Assertions.assertTrue(output.contains("Signature validation: OK"), output.toString());
        }
    }

    @Test
    void onlyPreviousCertificatesUsedInMaterializationMustBeValid() throws Exception {
        Path base = issueWithValidity("expired-base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null,
                CA_CERT, CA_KEY, "1", "20240101", "20260101", "ac");
        Path delta = issue("delta", ATTR_V20, DELTA_COMPONENTS, "delta", base.toString(), base, CA_CERT, CA_KEY, "2");
        Assertions.assertEquals(0, validateWith(delta, "--components-json", AFTER_DELTA_COMPONENTS,
                "--prev-pcert", base.toString(), "--reference-time", REFERENCE_TIME));
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), validateWith(delta,
                "--components-json", AFTER_DELTA_COMPONENTS, "--prev-pcert", base.toString(),
                "--reference-time", REFERENCE_TIME, "--enforce-validity"));

        Path validBase = issue("valid-base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "3");
        Path validDelta = issue("valid-delta", ATTR_V20, DELTA_COMPONENTS, "delta", validBase.toString(),
                validBase, CA_CERT, CA_KEY, "4");
        Assertions.assertEquals(0, validateWith(validDelta, "--components-json", AFTER_DELTA_COMPONENTS,
                "--prev-pcert", tempDir().resolve("*base.pem").toString(), "--reference-time", REFERENCE_TIME,
                "--enforce-validity"));
        // If materialization is skipped, even the referenced expired base is not used.
        Assertions.assertEquals(0, validateWith(delta, "--skip-component-validation", "--prev-pcert", base.toString(),
                "--reference-time", REFERENCE_TIME, "--enforce-validity"));
    }

    @Test
    void previousDeltaValidityAndHistoricalIssuerPathsUseReferenceTime() throws Exception {
        Path historicalIssuer = issuerWithValidity("historical-issuer", OTHER_CA_CERT, OTHER_CA_KEY,
                "2027-01-01T00:00:00Z", "2028-01-01T00:00:00Z");
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null,
                historicalIssuer.toString(), OTHER_CA_KEY, "1");
        Path delta = issue("delta", ATTR_V20, DELTA_COMPONENTS, "delta", base.toString(), base, CA_CERT, CA_KEY, "2");
        Assertions.assertEquals(0, validateWith(delta, "--components-json", AFTER_DELTA_COMPONENTS,
                "--prev-pcert", base.toString(), "--trust-anchor", CA_CERT,
                "--trust-anchor", historicalIssuer.toString(), "--reference-time", REFERENCE_TIME, "--enforce-validity"));
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), validateWith(delta,
                "--components-json", AFTER_DELTA_COMPONENTS, "--prev-pcert", base.toString(),
                "--trust-anchor", CA_CERT, "--trust-anchor", historicalIssuer.toString(),
                "--reference-time", "2026-01-15T00:00:00Z"));

        Path expiredDelta = issueWithValidity("expired-delta", ATTR_V20, DELTA_COMPONENTS, "delta",
                base.toString(), base, CA_CERT, CA_KEY, "3", "20240101", "20260101", "ac");
        Path removeCpu = writeJson("remove-cpu.json", manifest(
                component(CLASS_CPU, "Intel(R) Corporation", "C6", " ", "removed")));
        Path afterSecondDelta = writeJson("after-second-delta.json", manifest(
                component(CLASS_BIOS, "American Megatrends International, LLC.", "UP6502ZA.305", null, null, "05NO"),
                component(CLASS_BASEBOARD, "ASUSTeK COMPUTER INC.", "Zenbook UP6502ZA_Q529ZA", "A3A2PI88M1789543", null, "1.0       "),
                withWlanMac(component(CLASS_NIC, "8086:8086:", "51F0:0094:", null, null, "01"), "AAB1238907EE")));
        Path currentDelta = issue("current-delta", ATTR_V20, removeCpu.toString(), "delta",
                base.toString(), expiredDelta, CA_CERT, CA_KEY, "4");
        Assertions.assertEquals(0, validateWith(currentDelta, "--components-json", afterSecondDelta.toString(),
                "--prev-pcert", base.toString(), "--prev-pcert", expiredDelta.toString(),
                "--trust-anchor", CA_CERT, "--trust-anchor", historicalIssuer.toString(), "--reference-time", REFERENCE_TIME));
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), validateWith(currentDelta,
                "--components-json", afterSecondDelta.toString(), "--prev-pcert", base.toString(),
                "--prev-pcert", expiredDelta.toString(), "--trust-anchor", CA_CERT,
                "--trust-anchor", historicalIssuer.toString(), "--reference-time", REFERENCE_TIME, "--enforce-validity"));
    }

    @Test
    void previousSignerValidityWithoutTrustPathAndLegacyHolderMatchingAreEnforced() throws Exception {
        Path expiredSigner = issuerWithValidity("expired-signer", CA_CERT, CA_KEY,
                "2024-01-01T00:00:00Z", "2026-01-01T00:00:00Z");
        Path base = issue("base", ATTR_V11, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        Path delta = issue("delta", ATTR_V11, DELTA_COMPONENTS, "delta", base.toString(), null, CA_CERT, CA_KEY, "2");
        Assertions.assertEquals(0, validateSignedBy(delta, expiredSigner.toString(),
                "--components-json", AFTER_DELTA_COMPONENTS, "--prev-pcert", base.toString(),
                "--reference-time", REFERENCE_TIME));
        // Exercise previous-certificate signer policy directly, without a current-signer failure masking it.
        ComponentValidationService service = ComponentValidationService.builder()
                .previousPlatformCertificates(List.of(base.toFile()))
                .issuerCertificate(CliHelper.loadPKCSafe(expiredSigner.toString()))
                .clock(Clock.fixed(Instant.parse(REFERENCE_TIME), ZoneOffset.UTC))
                .enforceValidity(true).build();
        Assertions.assertFalse(service.validate(PlatformCertificate.load(delta.toFile()),
                new File(AFTER_DELTA_COMPONENTS), "NORMALIZED"));

        Path expiredBase = issueWithValidity("expired-base", ATTR_V11, BASE_COMPONENTS, null,
                EK_CERT, null, CA_CERT, CA_KEY, "3", "20240101", "20260101", "ac");
        Path legacyDelta = issue("legacy-delta", ATTR_V11, DELTA_COMPONENTS, "delta", expiredBase.toString(),
                null, CA_CERT, CA_KEY, "4");
        Assertions.assertEquals(0, validateWith(legacyDelta, "--components-json", AFTER_DELTA_COMPONENTS,
                "--prev-pcert", expiredBase.toString(), "--reference-time", REFERENCE_TIME));
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), validateWith(legacyDelta,
                "--components-json", AFTER_DELTA_COMPONENTS, "--prev-pcert", expiredBase.toString(),
                "--reference-time", REFERENCE_TIME, "--enforce-validity"));
    }

    @Test
    void expiredEntriesBeforeLastRebaseDoNotAffectValidityEnforcement() throws Exception {
        Path base = issueWithValidity("expired-base", ATTR_V20, BASE_COMPONENTS, null,
                EK_CERT, null, CA_CERT, CA_KEY, "1", "20240101", "20260101", "ac");
        Path delta = issueWithValidity("expired-delta", ATTR_V20, DELTA_COMPONENTS, "delta",
                base.toString(), base, CA_CERT, CA_KEY, "2", "20240101", "20260101", "ac");
        Path rebase = issue("rebase", ATTR_V20, AFTER_DELTA_COMPONENTS, "rebase",
                base.toString(), delta, CA_CERT, CA_KEY, "3");
        Path addDimm = writeJson("add-dimm.json", manifest(component(CLASS_RAM, "Acme", "DIMM-16G", "SN-A", "added")));
        Path current = issue("current", ATTR_V20, addDimm.toString(), "delta",
                rebase.toString(), rebase, CA_CERT, CA_KEY, "4");
        PlatformCertificate certificate = PlatformCertificate.load(current.toFile());
        PlatformConfigurationV3 expected = PreviousPlatformCertificateValidator.builder()
                .previousPlatformCertificates(List.of(base.toFile(), delta.toFile(), rebase.toFile()))
                .issuerCertificate(CliHelper.loadPKCSafe(CA_CERT))
                .clock(Clock.fixed(Instant.parse(REFERENCE_TIME), ZoneOffset.UTC))
                .enforceValidity(true).build()
                .materialize(certificate, certificate.canonicalizedPlatformConfigurationV3());
        Assertions.assertNotNull(expected);
        Assertions.assertEquals(0, validateWith(current, "--skip-component-validation",
                "--prev-pcert", tempDir().resolve("*.pem").toString(), "--reference-time", REFERENCE_TIME,
                "--enforce-validity"));
    }

    @Test
    void crlFreshnessUsesReferenceTimeIndependentlyOfEnforcement() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        Path usable = crlWithWindow("usable", "2027-01-01T00:00:00Z", "2027-02-01T00:00:00Z");
        Path expired = crlWithWindow("expired", "2026-01-01T00:00:00Z", "2026-02-01T00:00:00Z");
        Assertions.assertEquals(0, validateWith(base, "--skip-component-validation", "--crl", usable.toString(),
                "--reference-time", REFERENCE_TIME));
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), validateWith(base,
                "--skip-component-validation", "--crl", usable.toString(), "--reference-time", "2027-02-02T00:00:00Z"));
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), validateWith(base,
                "--skip-component-validation", "--crl", usable.toString(), "--reference-time", "2026-12-31T00:00:00Z"));
        Assertions.assertEquals(0, validateWith(base, "--skip-component-validation", "--crl", usable.toString(),
                "--crl", expired.toString(), "--reference-time", REFERENCE_TIME, "--enforce-validity"));
    }

    @Test
    void platformValidityBoundariesAreInclusive() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        PlatformCertificate certificate = PlatformCertificate.load(base.toFile());
        Instant start = certificate.extractNotBefore().toInstant();
        Instant end = certificate.extractNotAfter().toInstant();
        Assertions.assertEquals(CertificateValidity.OK, CertificateValidity.at(certificate, start));
        Assertions.assertEquals(CertificateValidity.OK, CertificateValidity.at(certificate, end));
        Assertions.assertEquals(CertificateValidity.NOT_YET_VALID, CertificateValidity.at(certificate, start.minusNanos(1)));
        Assertions.assertEquals(CertificateValidity.EXPIRED, CertificateValidity.at(certificate, end.plusNanos(1)));
        Assertions.assertEquals(0, validateWith(base, "--skip-component-validation", "--reference-time", end.toString(),
                "--enforce-validity"));
    }

    @Test
    void invalidReferenceTimeIsUsageErrorAndHelpDocumentsSystemUtcDefault() {
        CommandLine command = RootCmd.commandLine();
        StringWriter errors = new StringWriter();
        command.setErr(new PrintWriter(errors));
        Assertions.assertEquals(ClientExitCodes.USAGE_ERROR.code(), command.execute("validate", "-X", CA_CERT,
                "--reference-time", "2027-01-15T00:00:00"));
        Assertions.assertTrue(errors.toString().contains("RFC 3339"), errors.toString());
        String help = command.getSubcommands().get("validate").getUsageMessage();
        Assertions.assertTrue(help.contains("systemUTC"), help);
        Assertions.assertFalse(help.contains("--at"), help);
    }

    @Test
    void missing_components_json_fails_with_reason() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");

        List<String> output = captureOutput(() -> Assertions.assertEquals(
                ClientExitCodes.VALIDATION_FAILED.code(), validateWith(base)));

        Assertions.assertTrue(output.contains("Component validation: FAILED (--components-json not provided)"), output.toString());
    }

    @Test
    void component_mismatch_reports_plain_failure() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");

        List<String> output = captureOutput(() -> Assertions.assertEquals(
                ClientExitCodes.VALIDATION_FAILED.code(), validateWith(base, "--components-json", AFTER_DELTA_COMPONENTS)));

        Assertions.assertTrue(output.contains("Component validation: FAILED"), output.toString());
        Assertions.assertTrue(output.stream().noneMatch(line -> line.contains("not provided")), output.toString());
    }

    @Test
    void skip_component_validation_passes_on_remaining_checks() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");

        List<String> output = captureOutput(() -> Assertions.assertEquals(
                0, validateWith(base, "--skip-component-validation")));

        Assertions.assertTrue(output.contains("Component validation: SKIPPED (user requested to bypass)"), output.toString());
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(),
                validateSignedBy(base, OTHER_CA_CERT, "--skip-component-validation"),
                "skipping components must not hide a signature failure");
    }

    @Test
    void skip_component_validation_with_components_json_is_a_usage_error() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");

        Assertions.assertEquals(ClientExitCodes.USAGE_ERROR.code(),
                validateWith(base, "--skip-component-validation", "--components-json", BASE_COMPONENTS));
    }

    @Test
    void v20_base_then_delta_materializes_through_chain() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        Path delta = issue("delta", ATTR_V20, DELTA_COMPONENTS, "delta", base.toString(), base, CA_CERT, CA_KEY, "2");

        PlatformCertificate deltaCert = PlatformCertificate.load(delta.toFile());
        Assertions.assertEquals(CertSpecVersion.V2_0, deltaCert.resolvedSpecVersion());
        Assertions.assertEquals(CertType.DELTA, deltaCert.getCertType());
        Assertions.assertFalse(deltaCert.previousPlatformCertificateTraits().isEmpty(),
                "a V2.0 delta should carry a PreviousPlatformCertificates chain");

        Assertions.assertEquals(0, validate(delta, AFTER_DELTA_COMPONENTS, base.toString()));
        // The base's own component list no longer matches once the delta is applied.
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), validate(delta, BASE_COMPONENTS, base.toString()));
    }

    @Test
    void v20_second_delta_applies_on_top_of_first() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        Path delta1 = issue("delta1", ATTR_V20, DELTA_COMPONENTS, "delta", base.toString(), base, CA_CERT, CA_KEY, "2");
        // Remove the CPU that delta1 added; the entry repeats the identity delta1 recorded, including its blank serial.
        Path delta2Json = writeJson("delta2.json", manifest(
                component(CLASS_CPU, "Intel(R) Corporation", "C6", " ", "removed")));
        Path delta2 = issue("delta2", ATTR_V20, delta2Json.toString(), "delta", base.toString(), delta1, CA_CERT, CA_KEY, "3");

        Path afterDelta2 = writeJson("after-delta2.json", manifest(
                component(CLASS_BIOS, "American Megatrends International, LLC.", "UP6502ZA.305", null, null, "05NO"),
                component(CLASS_BASEBOARD, "ASUSTeK COMPUTER INC.", "Zenbook UP6502ZA_Q529ZA", "A3A2PI88M1789543", null, "1.0       "),
                withWlanMac(component(CLASS_NIC, "8086:8086:", "51F0:0094:", null, null, "01"), "AAB1238907EE")));
        Assertions.assertEquals(0, validate(delta2, afterDelta2.toString(), base.toString(), delta1.toString()));
        // Leaving out delta1 breaks the chain.
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(),
                validate(delta2, afterDelta2.toString(), base.toString()));
    }

    @Test
    void delta_removing_unknown_component_fails() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        Path deltaJson = writeJson("delta-bad-remove.json", manifest(
                component(CLASS_CPU, "Intel(R) Corporation", "Not In Base", null, "removed")));
        Path delta = issue("delta", ATTR_V20, deltaJson.toString(), "delta", base.toString(), base, CA_CERT, CA_KEY, "2");

        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), validate(delta, BASE_COMPONENTS, base.toString()));
    }

    @Test
    void delta_without_component_changes_keeps_base_components() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        Path deltaJson = writeJson("delta-properties.json", """
                {%s, "COMPONENTS": [], "PROPERTIES": [{"propertyName": "firmware", "propertyValue": "2.0", "propertyStatus": "added"}]}
                """.formatted(PLATFORM));
        Path delta = issue("delta", ATTR_V20, deltaJson.toString(), "delta", base.toString(), base, CA_CERT, CA_KEY, "2");

        Assertions.assertEquals(0, validate(delta, BASE_COMPONENTS, base.toString()));
    }

    @Test
    void delta_adding_identical_serialless_component_counts_it() throws Exception {
        String dimm = component(CLASS_RAM, "Acme", "DIMM-16G", null, null);
        Path baseJson = writeJson("base-dimms.json", manifest(dimm, dimm));
        Path base = issue("base", ATTR_V20, baseJson.toString(), null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        Path deltaJson = writeJson("delta-add-dimm.json", manifest(component(CLASS_RAM, "Acme", "DIMM-16G", null, "added")));
        Path delta = issue("delta", ATTR_V20, deltaJson.toString(), "delta", base.toString(), base, CA_CERT, CA_KEY, "2");

        Assertions.assertEquals(0, validate(delta, writeJson("three.json", manifest(dimm, dimm, dimm)).toString(), base.toString()));
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(),
                validate(delta, writeJson("two.json", manifest(dimm, dimm)).toString(), base.toString()));
    }

    @Test
    void base_components_must_match_one_to_one() throws Exception {
        String dimmA = component(CLASS_RAM, "Acme", "DIMM-16G", "SN-A", null);
        String dimmB = component(CLASS_RAM, "Acme", "DIMM-16G", "SN-B", null);
        Path base = issue("base", ATTR_V20, writeJson("base.json", manifest(dimmA, dimmB)).toString(),
                null, EK_CERT, null, CA_CERT, CA_KEY, "1");

        Assertions.assertEquals(0, validate(base, writeJson("same.json", manifest(dimmB, dimmA)).toString()));
        // A part reporting a sibling's identity cannot satisfy both certified entries.
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(),
                validate(base, writeJson("clone.json", manifest(dimmA, dimmA)).toString()));
        // A certified component that is no longer present fails.
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(),
                validate(base, writeJson("missing.json", manifest(dimmA)).toString()));
    }

    @Test
    void previous_certificate_signed_by_unconfigured_ca_is_rejected() throws Exception {
        Path base = issue("base", ATTR_V20, BASE_COMPONENTS, null, EK_CERT, null, OTHER_CA_CERT, OTHER_CA_KEY, "1");
        Path delta = issue("delta", ATTR_V20, DELTA_COMPONENTS, "delta", base.toString(), base, CA_CERT, CA_KEY, "2");

        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), validate(delta, AFTER_DELTA_COMPONENTS, base.toString()));
    }

    @Test
    void v11_delta_finds_its_base_among_unrelated_previous_files() throws Exception {
        Path unrelated = issue("a-unrelated", ATTR_V11, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "7");
        Path base = issue("b-base", ATTR_V11, BASE_COMPONENTS, null, EK_CERT, null, CA_CERT, CA_KEY, "1");
        Path delta = issue("delta", ATTR_V11, DELTA_COMPONENTS, "delta", base.toString(), null, CA_CERT, CA_KEY, "2");
        Assertions.assertEquals(CertSpecVersion.V1_1, PlatformCertificate.load(delta.toFile()).resolvedSpecVersion());
        Assertions.assertTrue(unrelated.getFileName().toString().compareTo(base.getFileName().toString()) < 0);

        Assertions.assertEquals(0, validate(delta, AFTER_DELTA_COMPONENTS, unrelated.toString(), base.toString()));
    }

    private static final String PLATFORM = """
            "PLATFORM": {"PLATFORMMANUFACTURERSTR": "ASUSTeK COMPUTER INC.", "PLATFORMMODEL": "Zenbook UP6502ZA_Q529ZA",
                         "PLATFORMVERSION": "1.0", "PLATFORMSERIAL": "A3A2PI88M1789543"}""";

    private static final String CLASS_BIOS = "0000810D";
    private static final String CLASS_BASEBOARD = "00010000";
    private static final String CLASS_CPU = "00040003";
    private static final String CLASS_RAM = "00060001";
    private static final String CLASS_NIC = "00028000";

    private static String component(String classValue, String manufacturer, String model, String serial, String status) {
        return component(classValue, manufacturer, model, serial, status, null);
    }

    private static String component(String classValue, String manufacturer, String model, String serial,
                                    String status, String revision) {
        String registry = CLASS_NIC.equals(classValue) ? "2.23.133.18.3.4" : "2.23.133.18.3.3";
        StringBuilder json = new StringBuilder()
                .append("{\"COMPONENTCLASS\": {\"COMPONENTCLASSREGISTRY\": \"").append(registry)
                .append("\", \"COMPONENTCLASSVALUE\": \"").append(classValue).append("\"}")
                .append(", \"MANUFACTURER\": \"").append(manufacturer).append('"')
                .append(", \"MODEL\": \"").append(model).append('"');
        if (serial != null) {
            json.append(", \"SERIAL\": \"").append(serial).append('"');
        }
        if (revision != null) {
            json.append(", \"REVISION\": \"").append(revision).append('"');
        }
        if (status != null) {
            json.append(", \"STATUS\": \"").append(status).append('"');
        }
        return json.append('}').toString();
    }

    private static String withWlanMac(String componentJson, String mac) {
        return componentJson.substring(0, componentJson.length() - 1) + ", \"ADDRESSES\": [{\"WLANMAC\": \"" + mac + "\"}]}";
    }

    private static String manifest(String... components) {
        return "{" + PLATFORM + ", \"COMPONENTS\": [" + String.join(", ", components) + "], \"PROPERTIES\": []}";
    }

    private Path issue(String name, String attributes, String components, String type, String holder,
                       Path previous, String caCert, String caKey, String serial) throws Exception {
        return issueWithValidity(name, attributes, components, type, holder, previous, caCert, caKey, serial,
                "20240101", "20300101", "ac");
    }

    private Path issueWithValidity(String name, String attributes, String components, String type, String holder,
                                   Path previous, String caCert, String caKey, String serial,
                                   String notBefore, String notAfter, String kind) throws Exception {
        Path env = tempPath(name + ".json");
        Path cer = tempPath(name + ".pem");
        List<String> args = new ArrayList<>(List.of(
                "certgen",
                "--serial", serial,
                "--not-before", notBefore,
                "--not-after", notAfter,
                "--kind", kind,
                "--issuer-cert", caCert,
                "--holder-cert", holder,
                "--attributes-json", attributes,
                "--components-json", components,
                "--extensions-json", EXT,
                "--sig-profile", "rsa-sha256",
                "--finalize",
                "--out", env.toString()));
        if (type != null) {
            args.addAll(List.of("--type", type));
        }
        if (previous != null) {
            args.addAll(List.of("--prev-pcert", previous.toString()));
        }
        Assertions.assertEquals(0, RootCmd.commandLine().execute(args.toArray(String[]::new)), "certgen " + name);
        Assertions.assertEquals(0, RootCmd.commandLine().execute(
                "assemble",
                "--in", env.toString(),
                "--out", cer.toString(),
                "--pem",
                "--local-key", caKey,
                "--issuer-cert", caCert), "assemble " + name);
        return cer;
    }

    private Path issuerWithValidity(String name, String caCert, String caKey, String notBefore, String notAfter) throws Exception {
        X509CertificateHolder original = CliHelper.loadCert(caCert, CliHelper.x509type.CERTIFICATE);
        PrivateKeyInfo keyInfo = CliHelper.loadCert(caKey, CliHelper.x509type.PRIVATE_KEY);
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(original.getIssuer(), BigInteger.TEN,
                Date.from(Instant.parse(notBefore)), Date.from(Instant.parse(notAfter)),
                original.getSubject(), original.getSubjectPublicKeyInfo());
        for (ASN1ObjectIdentifier oid : original.getExtensions().getExtensionOIDs()) {
            builder.addExtension(original.getExtension(oid));
        }
        X509CertificateHolder certificate = builder.build(new JcaContentSignerBuilder("SHA256withRSA")
                .build(new JcaPEMKeyConverter().getPrivateKey(keyInfo)));
        Path file = tempPath(name + ".pem");
        try (JcaPEMWriter writer = new JcaPEMWriter(Files.newBufferedWriter(file))) {
            writer.writeObject(certificate);
        }
        return file;
    }

    private Path crlWithWindow(String name, String thisUpdate, String nextUpdate) throws Exception {
        X509CertificateHolder issuer = CliHelper.loadCert(CA_CERT, CliHelper.x509type.CERTIFICATE);
        PrivateKeyInfo keyInfo = CliHelper.loadCert(CA_KEY, CliHelper.x509type.PRIVATE_KEY);
        X509v2CRLBuilder builder = new X509v2CRLBuilder(issuer.getSubject(), Date.from(Instant.parse(thisUpdate)));
        builder.setNextUpdate(Date.from(Instant.parse(nextUpdate)));
        Path file = tempPath(name + ".crl.pem");
        try (JcaPEMWriter writer = new JcaPEMWriter(Files.newBufferedWriter(file))) {
            writer.writeObject(builder.build(new JcaContentSignerBuilder("SHA256withRSA")
                    .build(new JcaPEMKeyConverter().getPrivateKey(keyInfo))));
        }
        return file;
    }

    private int validate(Path certificate, String components, String... previous) {
        List<String> args = new ArrayList<>(List.of(
                "validate",
                "--x509v2AttrCert", certificate.toString(),
                "--issuer-cert", CA_CERT,
                "--reference-time", REFERENCE_TIME,
                "--components-json", components));
        for (String prev : previous) {
            args.addAll(List.of("--prev-pcert", prev));
        }
        return RootCmd.commandLine().execute(args.toArray(String[]::new));
    }

    private int validateWith(Path certificate, String... extraArgs) {
        return validateSignedBy(certificate, CA_CERT, extraArgs);
    }

    private int validateSignedBy(Path certificate, String issuer, String... extraArgs) {
        List<String> args = new ArrayList<>(List.of(
                "validate",
                "--x509v2AttrCert", certificate.toString(),
                "--issuer-cert", issuer));
        args.addAll(List.of(extraArgs));
        return RootCmd.commandLine().execute(args.toArray(String[]::new));
    }

    private static List<String> captureOutput(Runnable action) {
        List<String> messages = new CopyOnWriteArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                messages.add(logRecord.getMessage());
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        Logger logger = Logger.getLogger(CommonOptions.class.getName());
        logger.addHandler(handler);
        try {
            action.run();
        } finally {
            logger.removeHandler(handler);
        }
        return messages;
    }

    private Path writeJson(String name, String json) throws Exception {
        Path path = tempPath(name);
        Files.writeString(path, json, StandardCharsets.UTF_8);
        return path;
    }
}
