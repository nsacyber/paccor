package paccor.validator;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import lombok.Builder;
import org.bouncycastle.cert.X509CertificateHolder;
import paccor.cert.PlatformCertificate;
import paccor.crypto.IssuerCertificateChecker;
import paccor.tcg.credential.PlatformConfigurationV3;

/**
 * Loads previous platform certificates and keeps only those that parse, are signed by a provided
 * issuer or trust anchor, and carry a platform configuration.
 */
@Builder
final class PreviousCertificateLoader {
    private static final Logger LOGGER = Logger.getLogger(PreviousCertificateLoader.class.getName());

    private final X509CertificateHolder issuerCertificate;
    private final List<X509CertificateHolder> trustAnchors;

    /**
     * A verified previous platform certificate and its canonical platform configuration.
     * @param certificate the previous platform certificate
     * @param configuration its platform configuration
     */
    record ResolvedPrevious(PlatformCertificate certificate, PlatformConfigurationV3 configuration) {}

    private record LoadedPrevious(File file, PlatformCertificate certificate) {}

    List<ResolvedPrevious> load(List<File> files) {
        return files.stream()
                .filter(this::isReadable)
                .map(this::parse)
                .filter(Objects::nonNull)
                .filter(previous -> isSignatureValid(previous.file(), previous.certificate()))
                .map(this::resolve)
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean isReadable(File file) {
        if (file == null || !file.exists()) {
            LOGGER.warning("Rejected previous platform certificate file " + file + ": file does not exist.");
            return false;
        }
        return true;
    }

    private LoadedPrevious parse(File file) {
        PlatformCertificate certificate = PlatformCertificate.loadSafe(file);
        if (certificate == null) {
            LOGGER.warning("Rejected previous platform certificate file " + file + ": could not be parsed.");
            return null;
        }
        return new LoadedPrevious(file, certificate);
    }

    private ResolvedPrevious resolve(LoadedPrevious previous) {
        // A delta without a platform configuration attribute records no component changes.
        PlatformConfigurationV3 configuration = Optional.ofNullable(previous.certificate().canonicalizedPlatformConfigurationV3())
                .orElseGet(() -> previous.certificate().isDelta() ? PlatformConfigurationV3.builder().build() : null);
        if (configuration == null) {
            LOGGER.warning("Rejected previous platform certificate file " + previous.file()
                    + ": no supported platform configuration was found.");
            return null;
        }
        return new ResolvedPrevious(previous.certificate(), configuration);
    }

    private boolean isSignatureValid(File file, PlatformCertificate certificate) {
        IssuerCertificateChecker checker = new IssuerCertificateChecker();
        boolean signed = candidateIssuers().stream()
                .anyMatch(issuer -> acceptsFromIssuer(certificate, issuer, checker));
        if (!signed) {
            LOGGER.warning("Rejected previous platform certificate file " + file + ": " + rejectionReason() + ".");
        }
        return signed;
    }

    private List<X509CertificateHolder> candidateIssuers() {
        return Stream.concat(
                        Optional.ofNullable(issuerCertificate).stream(),
                        Optional.ofNullable(trustAnchors).orElse(List.of()).stream())
                .toList();
    }

    private String rejectionReason() {
        return candidateIssuers().isEmpty()
                ? "no issuer certificate or trust anchors were configured"
                : "the previous certificate signature did not verify with a configured issuer, "
                        + "or that issuer did not have a valid path to the configured trust anchors";
    }

    private boolean acceptsFromIssuer(PlatformCertificate certificate, X509CertificateHolder issuer, IssuerCertificateChecker checker) {
        return checker.validateSignature(certificate, issuer)
                && (trustAnchors == null || trustAnchors.isEmpty() || checker.validateTrustPath(issuer, trustAnchors));
    }
}
