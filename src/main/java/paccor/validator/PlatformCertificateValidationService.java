package paccor.validator;

import java.io.File;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Builder;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import paccor.cert.PlatformCertificate;
import paccor.crypto.CertificateValidity;
import paccor.crypto.IssuerCertificateChecker;
import paccor.crypto.RevocationChecker;

/**
 * Validation steps for platform certificates.
 */
public final class PlatformCertificateValidationService {
    private final X509CertificateHolder issuerCertificate;
    private final List<X509CertificateHolder> trustAnchors;
    private final List<X509CRLHolder> crls;
    private final List<File> previousPlatformCertificates;
    private final File componentsJson;
    private final String componentMatcherName;
    private final boolean skipComponentValidation;
    private final boolean enforceValidity;
    private final boolean checkTrust;
    private final boolean checkRevocation;
    private final Clock clock;

    /**
     * Configure using {@link #builder()}. Omitted lists are empty, the clock defaults to
     * {@link Clock#systemUTC()}, and validity enforcement is disabled by default.
     *
     * @param issuerCertificate signer of the current certificate; absent signers fail signature validation
     * @param trustAnchors supplied anchors and intermediate CA certificates
     * @param crls supplied CRLs for the current certificate
     * @param previousPlatformCertificates previous certificate files available for materialization
     * @param componentsJson expected component manifest; required unless component validation is skipped
     * @param componentMatcherName matcher name, defaulting to NORMALIZED
     * @param skipComponentValidation bypass component checks and previous platform certificate materialization
     * @param enforceValidity require validity windows for all participating certificates
     * @param checkTrust request a trust check even if the supplied trust list is empty
     * @param checkRevocation request a CRL check even if the supplied CRL list is empty
     * @param clock source of the evaluation instant
     */
    @Builder
    private PlatformCertificateValidationService(
            X509CertificateHolder issuerCertificate, List<X509CertificateHolder> trustAnchors,
            List<X509CRLHolder> crls, List<File> previousPlatformCertificates,
            File componentsJson, String componentMatcherName, boolean skipComponentValidation,
            boolean enforceValidity, boolean checkTrust, boolean checkRevocation, Clock clock) {
        if (skipComponentValidation && componentsJson != null) {
            throw new IllegalArgumentException("skipComponentValidation cannot be combined with componentsJson.");
        }
        this.issuerCertificate = issuerCertificate;
        this.trustAnchors = trustAnchors == null ? List.of() : List.copyOf(trustAnchors);
        this.crls = crls == null ? List.of() : List.copyOf(crls);
        this.previousPlatformCertificates = previousPlatformCertificates == null
                ? List.of() : List.copyOf(previousPlatformCertificates);
        this.componentsJson = componentsJson;
        this.componentMatcherName = componentMatcherName;
        this.skipComponentValidation = skipComponentValidation;
        this.enforceValidity = enforceValidity;
        this.checkTrust = checkTrust || !this.trustAnchors.isEmpty();
        this.checkRevocation = checkRevocation || !this.crls.isEmpty();
        this.clock = clock == null ? Clock.systemUTC() : clock;
    }

    /**
     * Validate with the configured policy, without CLI output or exit codes.
     * @param certificate parsed platform certificate
     * @return independent acceptance results and their overall conjunction
     */
    public ValidationReport validate(PlatformCertificate certificate) {
        return validateDetailed(certificate).report();
    }

    /**
     * Validate and retain the observed validity status and specification details.
     * This performs the same checks as {@link #validate(PlatformCertificate)} exactly once.
     * @param certificate parsed platform certificate
     * @return report with details for diagnostics and presentation
     */
    public Result validateDetailed(PlatformCertificate certificate) {
        Objects.requireNonNull(certificate, "certificate");
        Instant evaluationTime = clock.instant();
        Clock evaluationClock = Clock.fixed(evaluationTime, ZoneOffset.UTC);
        IssuerCertificateChecker issuerChecker = new IssuerCertificateChecker(evaluationClock);
        RevocationChecker revocationChecker = new RevocationChecker(evaluationClock);
        CertificateValidity platformValidity = CertificateValidity.at(certificate, evaluationTime);
        Optional<CertificateValidity> signerValidity = Optional.ofNullable(issuerCertificate)
                .map(issuer -> CertificateValidity.at(issuer, evaluationTime));
        boolean signatureOk = issuerCertificate != null
                && issuerChecker.validateSignature(certificate, issuerCertificate);
        boolean trustOk = !checkTrust || (issuerCertificate != null
                && issuerChecker.validateTrustPath(issuerCertificate, trustAnchors));
        boolean crlOk = !checkRevocation || (issuerCertificate != null
                && revocationChecker.validate(certificate, issuerCertificate, crls));
        SpecificationValidationReport specification = SpecificationValidator.validate(certificate);
        boolean componentsOk = skipComponentValidation || (componentsJson != null
                && ComponentValidationService.builder()
                        .previousPlatformCertificates(previousPlatformCertificates)
                        .issuerCertificate(issuerCertificate)
                        .trustAnchors(trustAnchors)
                        .clock(evaluationClock)
                        .enforceValidity(enforceValidity)
                        .build()
                        .validate(certificate, componentsJson, componentMatcherName));
        boolean validityOk = !enforceValidity || (platformValidity == CertificateValidity.OK
                && signerValidity.filter(validity -> validity == CertificateValidity.OK).isPresent());
        ValidationReport report = ValidationReport.builder()
                .signatureOk(signatureOk)
                .trustOk(trustOk)
                .crlOk(crlOk)
                .specificationOk(specification.ok())
                .componentsOk(componentsOk)
                .validityOk(validityOk)
                .build();
        return new Result(report, evaluationTime, platformValidity, signerValidity,
                specification, checkTrust, checkRevocation);
    }

    /**
     * Details observed during a single validation. Validity statuses are observations, not
     * acceptance decisions: consult {@link Result#report()} for the configured policy's result.
     * Empty signer validity means that no signer was supplied. Unrequested trust/CRL checks
     * have true acceptance terms in the report; the checked flags distinguish them from passes.
     *
     * @param report acceptance results for the configured policy
     * @param evaluationTime single instant shared by all time-dependent checks
     * @param platformValidity observed validity window of the current platform certificate
     * @param signerValidity observed signer validity, empty when no signer was supplied
     * @param specification detailed specification check result
     * @param trustChecked whether issuer trust-path validation was requested
     * @param crlChecked whether CRL validation was requested
     */
    public record Result(
            ValidationReport report,
            Instant evaluationTime,
            CertificateValidity platformValidity,
            Optional<CertificateValidity> signerValidity,
            SpecificationValidationReport specification,
            boolean trustChecked,
            boolean crlChecked) {}
}
