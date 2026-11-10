package paccor.validator;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import paccor.cert.PlatformCertificate;
import paccor.cli.CliHelper;
import paccor.cli.ClientExitCodes;
import paccor.cli.RootCmd;
import paccor.cli.TestSupport;
import paccor.crypto.CertificateValidity;

class PlatformCertificateValidationServiceTest extends TestSupport {
    private static final String CA_CERT = "src/test/resources/sample_testgen1/PCTestCA.example.com.pem";
    private static final String CA_KEY = "src/test/resources/sample_testgen1/private.pem";
    private static final String EK_CERT = "src/test/resources/sample_testgen1/ek.crt";
    private static final String ATTRIBUTES = "src/test/resources/sample_testgen1/localhost-policyreference-v2.json";
    private static final String EXTENSIONS = "src/test/resources/tutorials/base-bare-bones-extentions-no-ti.json";
    private static final File COMPONENTS = new File("src/test/resources/tutorials/v2/components.json");
    private static final File DELTA_COMPONENTS = new File("src/test/resources/tutorials/v2/deltacomponents1.json");
    private static final File AFTER_DELTA = new File("src/test/resources/tutorials/v2/rebasecomponents1.json");
    private static final Instant NOW = Instant.parse("2027-01-15T00:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static X509CertificateHolder issuer;
    private static PrivateKeyInfo issuerKey;

    @BeforeAll
    static void loadIssuer() throws Exception {
        issuer = CliHelper.loadCert(CA_CERT, CliHelper.x509type.CERTIFICATE);
        issuerKey = CliHelper.loadCert(CA_KEY, CliHelper.x509type.PRIVATE_KEY);
    }

    @Test
    void singleSettingEnforcesCurrentAcAndPkcWithoutTrustOrCrlInputs() throws Exception {
        for (String kind : List.of("ac", "pkc")) {
            for (String[] window : List.of(
                    new String[] {"20240101", "20260101", "EXPIRED"},
                    new String[] {"20280101", "20300101", "NOT_YET_VALID"},
                    new String[] {"20240101", "20300101", "OK"})) {
                PlatformCertificate certificate = PlatformCertificate.load(issue(kind + window[2], kind,
                        window[0], window[1], COMPONENTS, null).toFile());
                Assertions.assertNotNull(certificate);
                PlatformCertificateValidationService.Result informational = service(false).validateDetailed(certificate);
                PlatformCertificateValidationService.Result enforced = service(true).validateDetailed(certificate);
                Assertions.assertEquals(CertificateValidity.valueOf(window[2]), informational.platformValidity());
                Assertions.assertEquals(informational.platformValidity(), enforced.platformValidity());
                Assertions.assertEquals(NOW, enforced.evaluationTime());
                Assertions.assertEquals(CertificateValidity.OK, enforced.signerValidity().orElseThrow());
                Assertions.assertTrue(informational.report().ok());
                Assertions.assertTrue(enforced.report().signatureOk());
                Assertions.assertTrue(enforced.report().specificationOk());
                Assertions.assertTrue(enforced.report().componentsOk());
                Assertions.assertEquals(window[2].equals("OK"), enforced.report().validityOk());
                Assertions.assertEquals(window[2].equals("OK"), enforced.report().ok());
                Assertions.assertFalse(enforced.trustChecked());
                Assertions.assertFalse(enforced.crlChecked());
                Assertions.assertEquals(enforced.report(), service(true).validate(certificate));
                Assertions.assertEquals(enforced.report().ok() ? 0 : ClientExitCodes.VALIDATION_FAILED.code(),
                        cliValidate(certificate.getFile(), true));
                Assertions.assertEquals(0, cliValidate(certificate.getFile(), false));
            }
        }
    }

    @Test
    void signerWindowIsEnforcedWithoutTrustPath() throws Exception {
        PlatformCertificate certificate = PlatformCertificate.load(issue("base", "ac", "20240101", "20300101",
                COMPONENTS, null).toFile());
        for (Instant end : List.of(NOW.minusSeconds(60), NOW.plusSeconds(120))) {
            Instant start = end.minusSeconds(60);
            X509CertificateHolder signer = issuerWithWindow(start, end);
            PlatformCertificateValidationService.Result result = PlatformCertificateValidationService.builder()
                    .issuerCertificate(signer).componentsJson(COMPONENTS).enforceValidity(true).clock(CLOCK)
                    .build().validateDetailed(certificate);
            Assertions.assertTrue(result.report().signatureOk());
            Assertions.assertFalse(result.report().validityOk());
            Assertions.assertFalse(result.report().ok());
            Assertions.assertEquals(end.isBefore(NOW) ? CertificateValidity.EXPIRED : CertificateValidity.NOT_YET_VALID,
                    result.signerValidity().orElseThrow());
            Assertions.assertTrue(PlatformCertificateValidationService.builder().issuerCertificate(signer)
                    .componentsJson(COMPONENTS).clock(CLOCK).build().validate(certificate).ok());
        }
    }

    @Test
    void previousCertificatesAreEnforcedOnlyWhenMaterialized() throws Exception {
        Path expiredBase = issue("expired-base", "ac", "20240101", "20260101", COMPONENTS, null);
        Path validBase = issue("valid-base", "ac", "20240101", "20300101", COMPONENTS, null);
        PlatformCertificate delta = PlatformCertificate.load(issue("delta", "ac", "20240101", "20300101",
                DELTA_COMPONENTS, expiredBase).toFile());
        PlatformCertificate validDelta = PlatformCertificate.load(issue("valid-delta", "ac", "20240101", "20300101",
                DELTA_COMPONENTS, validBase).toFile());
        List<File> previous = List.of(expiredBase.toFile(), validBase.toFile());
        Assertions.assertTrue(PlatformCertificateValidationService.builder().issuerCertificate(issuer)
                .previousPlatformCertificates(previous).componentsJson(AFTER_DELTA).clock(CLOCK)
                .build().validate(delta).ok());
        ValidationReport enforced = PlatformCertificateValidationService.builder().issuerCertificate(issuer)
                .previousPlatformCertificates(previous).componentsJson(AFTER_DELTA).clock(CLOCK).enforceValidity(true)
                .build().validate(delta);
        Assertions.assertFalse(enforced.componentsOk());
        Assertions.assertFalse(enforced.ok());
        Assertions.assertTrue(PlatformCertificateValidationService.builder().issuerCertificate(issuer)
                .previousPlatformCertificates(previous).componentsJson(AFTER_DELTA).clock(CLOCK).enforceValidity(true)
                .build().validate(validDelta).ok());
        Assertions.assertTrue(PlatformCertificateValidationService.builder().issuerCertificate(issuer)
                .previousPlatformCertificates(previous).skipComponentValidation(true).clock(CLOCK).enforceValidity(true)
                .build().validate(delta).ok());
        // A same-key signer with an expired window still verifies cryptographically, but
        // must be rejected for previous certificates too, not just the current certificate.
        X509CertificateHolder expiredSigner = issuerWithWindow(NOW.minusSeconds(120), NOW.minusSeconds(60));
        ValidationReport expiredIssuerReport = PlatformCertificateValidationService.builder()
                .issuerCertificate(expiredSigner).previousPlatformCertificates(previous).componentsJson(AFTER_DELTA)
                .clock(CLOCK).enforceValidity(true).build().validate(validDelta);
        Assertions.assertTrue(expiredIssuerReport.signatureOk());
        Assertions.assertFalse(expiredIssuerReport.validityOk());
        Assertions.assertFalse(expiredIssuerReport.componentsOk());
    }

    @Test
    void trustAndCrlTimeChecksRemainIndependentOfValidityEnforcement() throws Exception {
        PlatformCertificate certificate = PlatformCertificate.load(issue("base", "ac", "20240101", "20300101",
                COMPONENTS, null).toFile());
        X509CRLHolder usable = crl(NOW.minusSeconds(60), NOW.plusSeconds(60));
        X509CRLHolder expired = crl(NOW.minusSeconds(240), NOW.minusSeconds(180));
        for (Instant at : List.of(NOW, NOW.plusSeconds(61), NOW.minusSeconds(61))) {
            PlatformCertificateValidationService.Result result = PlatformCertificateValidationService.builder()
                    .issuerCertificate(issuer).trustAnchors(List.of(issuer)).crls(List.of(expired, usable))
                    .componentsJson(COMPONENTS).clock(Clock.fixed(at, ZoneOffset.UTC))
                    .build().validateDetailed(certificate);
            Assertions.assertTrue(result.trustChecked());
            Assertions.assertTrue(result.crlChecked());
            Assertions.assertTrue(result.report().trustOk());
            Assertions.assertEquals(at.equals(NOW), result.report().crlOk());
            Assertions.assertEquals(at.equals(NOW), result.report().ok());
        }
        X509CertificateHolder expiredAnchor = issuerWithWindow(NOW.minusSeconds(120), NOW.minusSeconds(60));
        ValidationReport report = PlatformCertificateValidationService.builder().issuerCertificate(expiredAnchor)
                .trustAnchors(List.of(expiredAnchor)).componentsJson(COMPONENTS).clock(CLOCK)
                .build().validate(certificate);
        Assertions.assertTrue(report.signatureOk());
        Assertions.assertTrue(report.validityOk());
        Assertions.assertFalse(report.trustOk());
        Assertions.assertFalse(report.ok());
    }

    @Test
    void capturesOneInstantPerCallEvenWithAnAdvancingClock() throws Exception {
        Path base = issue("base", "ac", "20240101", "20300101", COMPONENTS, null);
        PlatformCertificate delta = PlatformCertificate.load(issue("delta", "ac", "20240101", "20300101",
                DELTA_COMPONENTS, base).toFile());
        Clock clock = Mockito.mock(Clock.class);
        Mockito.when(clock.instant()).thenReturn(NOW, Instant.parse("2040-01-01T00:00:00Z"));
        PlatformCertificateValidationService service = PlatformCertificateValidationService.builder()
                .issuerCertificate(issuer).trustAnchors(List.of(issuer)).crls(List.of(crl(NOW, NOW.plusSeconds(60))))
                .previousPlatformCertificates(List.of(base.toFile())).componentsJson(AFTER_DELTA)
                .enforceValidity(true).clock(clock).build();
        Assertions.assertTrue(service.validate(delta).ok());
        Mockito.verify(clock, Mockito.times(1)).instant();
        PlatformCertificateValidationService.Result second = service.validateDetailed(delta);
        Assertions.assertFalse(second.report().ok());
        Assertions.assertEquals(Instant.parse("2040-01-01T00:00:00Z"), second.evaluationTime());
        Mockito.verify(clock, Mockito.times(2)).instant();
    }

    @Test
    void missingInputsAreFailuresUnlessChecksAreOptionalOrExplicitlySkipped() throws Exception {
        PlatformCertificate certificate = PlatformCertificate.load(issue("base", "ac", "20240101", "20300101",
                COMPONENTS, null).toFile());
        PlatformCertificateValidationService.Result missing = PlatformCertificateValidationService.builder()
                .clock(CLOCK).build().validateDetailed(certificate);
        Assertions.assertFalse(missing.report().signatureOk());
        Assertions.assertFalse(missing.report().componentsOk());
        Assertions.assertTrue(missing.signerValidity().isEmpty());
        Assertions.assertFalse(missing.report().ok());
        Assertions.assertTrue(service(true).validate(certificate).ok());
        ValidationReport wrongSigner = PlatformCertificateValidationService.builder()
                .issuerCertificate(CliHelper.loadPKCSafe("src/test/resources/TestCA.cert.example.pem"))
                .componentsJson(COMPONENTS).clock(CLOCK).build().validate(certificate);
        Assertions.assertFalse(wrongSigner.signatureOk());
        Assertions.assertTrue(wrongSigner.componentsOk());
        Assertions.assertFalse(wrongSigner.ok());
        ValidationReport mismatch = PlatformCertificateValidationService.builder().issuerCertificate(issuer)
                .componentsJson(AFTER_DELTA).clock(CLOCK).build().validate(certificate);
        Assertions.assertTrue(mismatch.signatureOk());
        Assertions.assertFalse(mismatch.componentsOk());
        Assertions.assertFalse(mismatch.ok());
        ValidationReport skipped = PlatformCertificateValidationService.builder().issuerCertificate(issuer)
                .skipComponentValidation(true).clock(CLOCK).build().validate(certificate);
        Assertions.assertTrue(skipped.ok());
        PlatformCertificateValidationService.Result requested = PlatformCertificateValidationService.builder()
                .issuerCertificate(issuer).skipComponentValidation(true).checkTrust(true).checkRevocation(true)
                .clock(CLOCK).build().validateDetailed(certificate);
        Assertions.assertTrue(requested.trustChecked());
        Assertions.assertTrue(requested.crlChecked());
        Assertions.assertFalse(requested.report().trustOk());
        Assertions.assertFalse(requested.report().crlOk());
        Assertions.assertFalse(requested.report().ok());
        Assertions.assertThrows(IllegalArgumentException.class, () -> PlatformCertificateValidationService.builder()
                .componentsJson(COMPONENTS).skipComponentValidation(true).build());
        Assertions.assertThrows(NullPointerException.class, () -> service(true).validate(null));
    }

    @Test
    void suppliedListsAreSnapshottedAndSpecificationFailuresAreRetained() throws Exception {
        PlatformCertificate certificate = PlatformCertificate.load(issue("base", "ac", "20240101", "20300101",
                COMPONENTS, null).toFile());
        List<X509CertificateHolder> anchors = new ArrayList<>(List.of(issuer));
        List<X509CRLHolder> crls = new ArrayList<>(List.of(crl(NOW, NOW.plusSeconds(60))));
        PlatformCertificateValidationService service = PlatformCertificateValidationService.builder()
                .issuerCertificate(issuer).componentsJson(COMPONENTS).trustAnchors(anchors).crls(crls).clock(CLOCK).build();
        anchors.clear();
        crls.clear();
        Assertions.assertTrue(service.validate(certificate).ok());
        PlatformCertificateValidationService.Result invalidProfile = PlatformCertificateValidationService.builder()
                .issuerCertificate(issuer).skipComponentValidation(true).clock(CLOCK).build()
                .validateDetailed(PlatformCertificate.fromPublicKeyCertificate(issuer.getEncoded()));
        Assertions.assertFalse(invalidProfile.specification().ok());
        Assertions.assertFalse(invalidProfile.specification().issues().isEmpty());
        Assertions.assertFalse(invalidProfile.report().specificationOk());
        Assertions.assertFalse(invalidProfile.report().ok());
    }

    @Test
    void omittedClockUsesCurrentSystemTime() throws Exception {
        PlatformCertificate certificate = PlatformCertificate.load(issue("base", "ac", "20240101", "20300101",
                COMPONENTS, null).toFile());
        Instant before = Clock.systemUTC().instant();
        PlatformCertificateValidationService.Result result = PlatformCertificateValidationService.builder()
                .issuerCertificate(issuer).componentsJson(COMPONENTS).build().validateDetailed(certificate);
        Instant after = Clock.systemUTC().instant();
        Assertions.assertFalse(result.evaluationTime().isBefore(before));
        Assertions.assertFalse(result.evaluationTime().isAfter(after));
        Assertions.assertTrue(result.report().validityOk());
    }

    private PlatformCertificateValidationService service(boolean enforceValidity) {
        return PlatformCertificateValidationService.builder().issuerCertificate(issuer).componentsJson(COMPONENTS)
                .enforceValidity(enforceValidity).clock(CLOCK).build();
    }

    private Path issue(String name, String kind, String notBefore, String notAfter, File components,
                       Path previous) throws Exception {
        Path envelope = tempPath(name + ".json");
        Path certificate = tempPath(name + ".pem");
        List<String> args = new ArrayList<>(List.of("certgen", "--serial", previous == null ? "1" : "2",
                "--kind", kind, "--not-before", notBefore, "--not-after", notAfter,
                "--issuer-cert", CA_CERT, "--holder-cert", previous == null ? EK_CERT : previous.toString(),
                "--attributes-json", ATTRIBUTES, "--components-json", components.toString(),
                "--extensions-json", EXTENSIONS, "--sig-profile", "rsa-sha256", "--finalize",
                "--out", envelope.toString()));
        if (previous != null) args.addAll(List.of("--type", "delta", "--prev-pcert", previous.toString()));
        Assertions.assertEquals(0, RootCmd.commandLine().execute(args.toArray(String[]::new)));
        Assertions.assertEquals(0, RootCmd.commandLine().execute("assemble", "--in", envelope.toString(),
                "--out", certificate.toString(), "--pem", "--local-key", CA_KEY, "--issuer-cert", CA_CERT));
        Assertions.assertTrue(Files.exists(certificate));
        return certificate;
    }

    private int cliValidate(File certificate, boolean enforce) {
        List<String> args = new ArrayList<>(List.of("validate", "-X", certificate.toString(), "--issuer-cert", CA_CERT,
                "--components-json", COMPONENTS.toString(), "--reference-time", NOW.toString()));
        if (enforce) args.add("--enforce-validity");
        return RootCmd.commandLine().execute(args.toArray(String[]::new));
    }

    private X509CertificateHolder issuerWithWindow(Instant start, Instant end) throws Exception {
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(issuer.getIssuer(), BigInteger.TEN,
                Date.from(start), Date.from(end), issuer.getSubject(), issuer.getSubjectPublicKeyInfo());
        for (ASN1ObjectIdentifier oid : issuer.getExtensions().getExtensionOIDs()) {
            builder.addExtension(issuer.getExtension(oid));
        }
        return builder.build(new JcaContentSignerBuilder("SHA256withRSA")
                .build(new JcaPEMKeyConverter().getPrivateKey(issuerKey)));
    }

    private X509CRLHolder crl(Instant start, Instant end) throws Exception {
        X509v2CRLBuilder builder = new X509v2CRLBuilder(issuer.getSubject(), Date.from(start));
        builder.setNextUpdate(Date.from(end));
        return builder.build(new JcaContentSignerBuilder("SHA256withRSA")
                .build(new JcaPEMKeyConverter().getPrivateKey(issuerKey)));
    }
}
