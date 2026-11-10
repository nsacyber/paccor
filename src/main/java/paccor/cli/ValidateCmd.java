package paccor.cli;

import paccor.cert.PlatformCertificate;
import java.io.File;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.Callable;
import lombok.extern.java.Log;
import paccor.cli.pv.ReadableFileConverter;
import paccor.cli.pv.ReferenceTimeConverter;
import paccor.crypto.CertificateValidity;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import paccor.validator.ComponentMatcher;
import paccor.validator.PlatformCertificateValidationService;
import paccor.validator.SpecificationValidationReport;
import paccor.validator.ValidationReport;

@Log
@Command(name = "validate", mixinStandardHelpOptions = true, description = {
        "Validate a platform certificate.",
        "Exits 0 only when signature and component validation pass. Profile checks always run, and trust-anchor and CRL checks run when their inputs are given.",
        "Validity windows are informational unless --enforce-validity is given.",
        "Use " + CliOptionNames.SKIP_COMPONENT_VALIDATION_LONG + " to validate without a components file."})
public class ValidateCmd implements Callable<Integer>, HasCommonOptions {
    @Mixin private CommonOptions common;

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
    @Option(names = CliOptionNames.ENFORCE_VALIDITY_LONG, description = "Enforce validity window checking for the platform certificate, previous certificates used in materialization, and their signers.")
    private boolean enforceValidity;
    @Option(names = CliOptionNames.REFERENCE_TIME_LONG, paramLabel = "TIMESTAMP", converter = ReferenceTimeConverter.class, description = "Evaluation time for validity, issuer paths, and CRL freshness in RFC 3339 format (e.g. 2024-01-01T00:00:00Z). Default: current systemUTC time. Does not enable --enforce-validity.")
    private Instant referenceTime;
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

        boolean checkTrust = trustAnchorList != null && !trustAnchorList.isEmpty();
        boolean checkCrl = crlList != null && !crlList.isEmpty();
        PlatformCertificateValidationService.Result result = PlatformCertificateValidationService.builder()
                .issuerCertificate(signerFile == null ? null : CliHelper.loadPKCSafe(signerFile.getPath()))
                .trustAnchors(CliHelper.loadCertificates(GlobFileResolver.resolve(trustAnchorList)))
                .crls(CliHelper.loadCrls(GlobFileResolver.resolve(crlList)))
                .previousPlatformCertificates(skipComponentValidation ? List.of()
                        : GlobFileResolver.resolve(previousPlatformCertsList))
                .componentsJson(componentsJson)
                .componentMatcherName(componentMatcherName)
                .skipComponentValidation(skipComponentValidation)
                .enforceValidity(enforceValidity)
                .checkTrust(checkTrust)
                .checkRevocation(checkCrl)
                .clock(referenceTime == null ? Clock.systemUTC() : Clock.fixed(referenceTime, ZoneOffset.UTC))
                .build()
                .validateDetailed(certificate);
        ValidationReport report = result.report();
        reportValidity("Platform certificate", result.platformValidity(), result.evaluationTime(), enforceValidity);
        result.signerValidity().ifPresent(validity -> reportValidity("Signer certificate", validity,
                result.evaluationTime(), enforceValidity || result.trustChecked()));
        reportSignature(report.signatureOk());
        if (result.trustChecked()) reportTrust(report.trustOk());
        if (result.crlChecked()) reportCrl(report.crlOk());
        reportSpecification(result.specification());
        if (skipComponentValidation) {
            common.printInfo("Component validation: SKIPPED (user requested to bypass)");
        } else if (componentsJson == null) {
            common.printInfo("Component validation: FAILED (" + CliOptionNames.COMPONENTS_JSON_LONG + " not provided)");
        } else {
            reportComponents(report.componentsOk());
        }
        return reportOverall(report.ok()).code();
    }

    private void reportValidity(String scope, CertificateValidity validity, Instant evaluationTime, boolean enforced) {
        common.printInfo(scope + " validity: " + validity + " as of " + evaluationTime
                + (enforced ? " (enforced)" : " (not enforced)"));
    }

    private void reportSignature(boolean ok) {
        common.printInfo("Signature validation: " + (ok ? "OK" : "FAILED"));
    }
    private void reportTrust(boolean ok) {
        common.printInfo("Trust-anchor validation: " + (ok ? "OK" : "FAILED"));
    }
    private void reportCrl(boolean ok) {
        common.printInfo("CRL validation: " + (ok ? "OK" : "FAILED"));
    }
    private void reportComponents(boolean ok) {
        common.printInfo("Component validation: " + (ok ? "OK" : "FAILED"));
    }
    private void reportSpecification(SpecificationValidationReport report) {
        if (!report.ok() && !report.detail().isBlank()) {
            log.fine(report.detail());
        }
        common.printInfo("Specification validation: " + (report.ok() ? "OK" : "FAILED"));
    }
    private ClientExitCodes reportOverall(boolean ok) {
        return reportOverall(ok, ok ? ClientExitCodes.SUCCESS : ClientExitCodes.VALIDATION_FAILED);
    }
    private ClientExitCodes reportOverall(boolean ok, ClientExitCodes exitCode) {
        common.printInfo("Platform Certificate validation: " + (ok ? "OK" : "FAILED"));
        return exitCode;
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
                log.warning("Unknown --component-matcher: " + name + ", using NORMALIZED");
                return ComponentMatcher.NORMALIZED;
        }
    }
}
