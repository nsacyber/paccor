package paccor.cli;

import paccor.cert.PlatformCertificate;
import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
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

@Command(name = "validate", mixinStandardHelpOptions = true, description = {
        "Validate a platform certificate.",
        "Exits 0 only when signature and component validation pass. Profile checks always run, and trust-anchor and CRL checks run when their inputs are given.",
        "Use " + CliOptionNames.SKIP_COMPONENT_VALIDATION_LONG + " to validate without a components file."})
public class ValidateCmd implements Callable<Integer>, HasCommonOptions {
    private static final Logger LOGGER = Logger.getLogger(ValidateCmd.class.getName());

    @Mixin private CommonOptions common;
    private final IssuerCertificateChecker issuerChecker = new IssuerCertificateChecker();
    private final RevocationChecker revocationChecker = new RevocationChecker();

    @Option(names = { CliOptionNames.PLATFORM_CERT_FILE_SHORT, CliOptionNames.X509V2_ATTR_CERT_LONG/*backwards compatibility*/, CliOptionNames.PKC_PLATFORM_CERT_LONG }, description = "Platform certificate file", required = true, converter = ReadableFileConverter.class)
    private File platformCertFile;
    @Option(names = { CliOptionNames.ISSUER_CERT_SHORT, CliOptionNames.ISSUER_CERT_LONG, CliOptionNames.PUBLIC_KEY_CERT_LONG/*backwards compatibility*/ }, description = "Signer certificate file", converter = ReadableFileConverter.class)
    private File signerFile;
    @Option(names = { CliOptionNames.COMPONENTS_JSON_SHORT, CliOptionNames.COMPONENTS_JSON_LONG }, description = "Components JSON to verify against the certificate components. Required unless " + CliOptionNames.SKIP_COMPONENT_VALIDATION_LONG + " is given.", converter = ReadableFileConverter.class)
    private File componentsJson;
    @Option(names = CliOptionNames.SKIP_COMPONENT_VALIDATION_LONG, description = "Do not validate components. The exit code then reflects the remaining checks only.")
    private boolean skipComponentValidation;
    @Option(names = CliOptionNames.COMPONENT_MATCHER_LONG, description = "Component matcher: NORMALIZED (default) or RAW")
    private String componentMatcherName;
    @Option(names = CliOptionNames.PREV_PCERT_LONG, description = "Previous platform certificate file(s). Repeatable. Globs allowed.")
    private List<String> previousPlatformCertsList;
    @Option(names = CliOptionNames.TRUST_ANCHOR_LONG, description = "Trust anchor(s) and intermediate CA certificates. Repeatable. Globs allowed. If provided, the issuer cert must be self-signed or chain to a self-signed trust anchor. Previous platform certificates may be signed by --issuer-cert or by any certificate given here that chains to a self-signed anchor.")
    private List<String> trustAnchorList;
    @Option(names = CliOptionNames.CRL_LONG, description = "CRL file(s) for revocation checking. Repeatable. Globs allowed.")
    private List<String> crlList;
    @Override
    public CommonOptions commonOptions() {
        return common;
    }

    @Override
    public Integer call() {
        if (skipComponentValidation && componentsJson != null) {
            common.printError(CliOptionNames.SKIP_COMPONENT_VALIDATION_LONG + " cannot be combined with " + CliOptionNames.COMPONENTS_JSON_LONG + ".");
            return ClientExitCodes.USAGE_ERROR.code();
        }
        PlatformCertificate certificate = PlatformCertificate.load(platformCertFile);
        if (certificate == null) {
            common.printError("Could not read platform certificate provided.");
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
        boolean componentsOk = validateComponents(certificate, signer);

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
        common.printInfo("Signature validation: " + (ok ? "OK" : "FAILED"));
        return ok;
    }
    private boolean reportTrust(boolean ok) {
        common.printInfo("Trust-anchor validation: " + (ok ? "OK" : "FAILED"));
        return ok;
    }
    private boolean reportCrl(boolean ok) {
        common.printInfo("CRL validation: " + (ok ? "OK" : "FAILED"));
        return ok;
    }
    private boolean validateComponents(PlatformCertificate certificate, Optional<X509CertificateHolder> signer) {
        if (skipComponentValidation) {
            common.printInfo("Component validation: SKIPPED (user requested to bypass)");
            return true;
        }
        return Optional.ofNullable(componentsJson)
                .map(json -> ComponentValidationService.builder()
                        .previousPlatformCertificates(GlobFileResolver.resolve(previousPlatformCertsList))
                        .issuerCertificate(signer.orElse(null))
                        .trustAnchors(Optional.ofNullable(trustAnchorList)
                                .map(GlobFileResolver::resolve)
                                .map(CliHelper::loadCertificates)
                                .orElse(List.of()))
                        .build()
                        .validate(certificate, json, componentMatcherName))
                .map(this::reportComponents)
                .orElseGet(() -> {
                    common.printInfo("Component validation: FAILED (" + CliOptionNames.COMPONENTS_JSON_LONG + " not provided)");
                    return false;
                });
    }
    private boolean reportComponents(boolean ok) {
        common.printInfo("Component validation: " + (ok ? "OK" : "FAILED"));
        return ok;
    }
    private boolean reportSpecification(SpecificationValidationReport report) {
        common.printInfo("Specification validation: " + (report.ok() ? "OK" : "FAILED"));
        return report.ok();
    }
    private ClientExitCodes reportOverall(boolean ok) {
        return reportOverall(ok, ok ? ClientExitCodes.SUCCESS : ClientExitCodes.VALIDATION_FAILED);
    }
    private ClientExitCodes reportOverall(boolean ok, ClientExitCodes exitCode) {
        common.printInfo("Platform Certificate validation: " + (ok ? "OK" : "FAILED"));
        return exitCode;
    }

    private boolean validateSpecification(PlatformCertificate certificate) {
        SpecificationValidationReport report = SpecificationValidator.validate(certificate);
        if (!report.ok()) {
            String detail = report.detail();
            if (!detail.isBlank()) {
                LOGGER.fine(detail);
            }
        }
        return reportSpecification(report);
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
                LOGGER.warning("Unknown --component-matcher: " + name + ", using NORMALIZED");
                return ComponentMatcher.NORMALIZED;
        }
    }
}
