package paccor.cli;

import paccor.cert.PlatformCertificate;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import paccor.cli.pv.ReadableFileConverter;
import paccor.crypto.IssuerCertificateChecker;
import paccor.crypto.RevocationChecker;
import org.bouncycastle.cert.X509CertificateHolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import paccor.validator.ComponentMatcher;
import paccor.validator.ComponentValidationService;
import paccor.validator.SpecificationValidationReport;
import paccor.validator.SpecificationValidator;
import paccor.validator.ValidationReport;

@Command(name = "validate", mixinStandardHelpOptions = true, description = "Validate signature, and optionally components and certificate profile")
public class ValidateCmd implements Callable<Integer>, HasCommonOptions {
    @Mixin private CommonOptions common;
    private final IssuerCertificateChecker issuerChecker = new IssuerCertificateChecker();
    private final RevocationChecker revocationChecker = new RevocationChecker();

    @Option(names = { CliOptionNames.PLATFORM_CERT_FILE_SHORT, CliOptionNames.X509V2_ATTR_CERT_LONG/*backwards compatibility*/, CliOptionNames.PKC_PLATFORM_CERT_LONG }, description = "Platform certificate file", required = true, converter = ReadableFileConverter.class)
    private File platformCertFile;
    @Option(names = { CliOptionNames.ISSUER_CERT_SHORT, CliOptionNames.ISSUER_CERT_LONG, CliOptionNames.PUBLIC_KEY_CERT_LONG/*backwards compatibility*/ }, description = "Signer certificate file", converter = ReadableFileConverter.class)
    private File signerFile;
    @Option(names = { CliOptionNames.COMPONENTS_JSON_SHORT, CliOptionNames.COMPONENTS_JSON_LONG }, description = "Components JSON to verify against AC components", converter = ReadableFileConverter.class)
    private File componentsJson;
    @Option(names = CliOptionNames.COMPONENT_MATCHER_LONG, description = "Component matcher: NORMALIZED (default) or RAW")
    private String componentMatcherName;
    @Option(names = CliOptionNames.PREV_PCERT_LONG, description = "Previous platform certificate file(s). Repeatable. Globs allowed.")
    private List<String> previousPlatformCertsList;
    @Option(names = CliOptionNames.TRUST_ANCHOR_LONG, description = "Trust anchor(s). Repeatable. Globs allowed. If provided, the issuer cert must be self-signed or chain to a self-signed trust anchor.")
    private List<String> trustAnchorList;
    @Option(names = CliOptionNames.CRL_LONG, description = "CRL file(s) for revocation checking. Repeatable. Globs allowed.")
    private List<String> crlList;
    @Override
    public CommonOptions commonOptions() {
        return common;
    }

    @Override
    public Integer call() {
        PlatformCertificate certificate = PlatformCertificate.load(platformCertFile);
        if (certificate == null) {
            if (!common.quiet) {
                System.out.println("Could not read platform certificate provided.");
            }
            return reportOverall(false, ClientExitCodes.USAGE_ERROR).code();
        }

        Optional<X509CertificateHolder> signer = Optional.ofNullable(
                signerFile == null ? null : CliHelper.loadPKCSafe(signerFile.getPath()));
        boolean signatureOk = signer.map(value -> issuerChecker.validateSignature(certificate, value))
                .map(this::reportSignature)
                .orElseGet(() -> reportSignature(false));
        boolean trustOk = trustAnchorList == null || trustAnchorList.isEmpty()
                || signer.map(value -> issuerChecker.validateTrustPath(value,
                        CliHelper.loadCertificates(GlobFileResolver.resolve(trustAnchorList))))
                        .map(this::reportTrust)
                        .orElseGet(() -> reportTrust(false));
        boolean crlOk = crlList == null || crlList.isEmpty()
                || signer.map(value -> revocationChecker.validate(certificate, value,
                        CliHelper.loadCrls(GlobFileResolver.resolve(crlList))))
                        .map(this::reportCrl)
                        .orElseGet(() -> reportCrl(false));
        boolean specificationOk = validateSpecification(certificate);
        boolean componentsOk = ComponentValidationService.builder()
                .quiet(common.quiet)
                .logLevel(common.logLevel)
                .previousPlatformCertificates(previousPlatformCertsList)
                .build()
                .validate(certificate, componentsJson, componentMatcherName);

        ValidationReport report = ValidationReport.builder()
                .signatureOk(signatureOk)
                .trustOk(trustOk)
                .crlOk(crlOk)
                .specificationOk(specificationOk)
                .componentsOk(componentsOk)
                .build();
        return reportOverall(report.ok()).code();
    }

    private boolean reportSignature(boolean ok) {
        report("Signature validation: " + (ok ? "OK" : "FAILED"));
        return ok;
    }
    private boolean reportTrust(boolean ok) {
        report("Trust-anchor validation: " + (ok ? "OK" : "FAILED"));
        return ok;
    }
    private boolean reportCrl(boolean ok) {
        report("CRL validation: " + (ok ? "OK" : "FAILED"));
        return ok;
    }
    private boolean reportSpecification(SpecificationValidationReport report) {
        report("Specification validation: " + (report.ok() ? "OK" : "FAILED"));
        return report.ok();
    }
    private ClientExitCodes reportOverall(boolean ok) {
        return reportOverall(ok, ok ? ClientExitCodes.SUCCESS : ClientExitCodes.VALIDATION_FAILED);
    }
    private ClientExitCodes reportOverall(boolean ok, ClientExitCodes exitCode) {
        report("Platform Certificate validation: " + (ok ? "OK" : "FAILED"));
        return exitCode;
    }

    private boolean validateSpecification(PlatformCertificate certificate) {
        SpecificationValidationReport report = SpecificationValidator.validate(certificate);
        if (!report.ok() && shouldPrintDetails()) {
            String detail = report.detail();
            if (!detail.isBlank()) {
                System.out.println(detail);
            }
        }
        return reportSpecification(report);
    }

    private boolean shouldPrintDetails() {
        if (common == null || common.logLevel == null) {
            return false;
        }
        String logLevel = common.logLevel.trim().toUpperCase(Locale.ROOT);
        return "DEBUG".equals(logLevel) || "TRACE".equals(logLevel);
    }

    public static ComponentMatcher resolveMatcher() {
        return resolveMatcher("NORMALIZED");
    }

    public static ComponentMatcher resolveMatcher(String componentMatcherName) {
        String name = Optional.ofNullable(componentMatcherName).orElse("NORMALIZED");
        switch (name.toUpperCase(Locale.ROOT)) {
            case "NORMALIZED":
            case "DEFAULT":
            case "PCI_AWARE":
                return ComponentMatcher.NORMALIZED;
            case "RAW":
            case "STRICT":
                return ComponentMatcher.RAW;
            default:
                System.err.println("Unknown --component-matcher: " + name + ", using NORMALIZED");
                return ComponentMatcher.NORMALIZED;
        }
    }

    private void report(String message) {
        if (!common.quiet) System.out.println(message);
    }
}
