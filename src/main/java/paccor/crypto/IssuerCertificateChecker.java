package paccor.crypto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.path.CertPath;
import org.bouncycastle.cert.path.CertPathValidation;
import org.bouncycastle.cert.path.CertPathValidationResult;
import org.bouncycastle.cert.path.validations.BasicConstraintsValidation;
import org.bouncycastle.cert.path.validations.KeyUsageValidation;
import org.bouncycastle.cert.path.validations.ParentCertIssuedValidation;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import paccor.cert.PlatformCertificate;

/** Validates platform-certificate signatures and issuer certificate paths. */
public final class IssuerCertificateChecker {
    public boolean validateSignature(PlatformCertificate platform, X509CertificateHolder issuer) {
        try {
            ContentVerifierProvider verifier = SignatureService.buildWithDefault(issuer);
            return platform != null && platform.isSignatureValid(verifier);
        } catch (OperatorCreationException | CertException ignored) {
            return false;
        }
    }

    public boolean validateTrustPath(X509CertificateHolder issuer, List<X509CertificateHolder> trustStoreCertificates) {
        try {
            List<X509CertificateHolder> anchors = trustStoreCertificates.stream()
                    .filter(IssuerCertificateChecker::isSelfSigned)
                    .toList();
            if (anchors.isEmpty()) return false;

            List<X509CertificateHolder> path = findPath(
                    issuer, trustStoreCertificates, anchors, new ArrayList<>(), new HashSet<>());
            if (path == null) return false;

            CertPathValidation[] validations = {
                    new ParentCertIssuedValidation(PQC_VERIFIER_BUILDER),
                    new BasicConstraintsValidation(),
                    new KeyUsageValidation(false)
            };
            CertPathValidationResult result = new CertPath(
                    path.toArray(X509CertificateHolder[]::new)).validate(validations);
            return result.isValid()
                    && result.getUnhandledCriticalExtensionOIDs().isEmpty();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static final PcBcContentVerifierProviderBuilder PQC_VERIFIER_BUILDER =
            new PcBcContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder());

    private static List<X509CertificateHolder> findPath(
            X509CertificateHolder current,
            List<X509CertificateHolder> certificates,
            List<X509CertificateHolder> anchors,
            List<X509CertificateHolder> path,
            Set<X509CertificateHolder> seen) throws Exception {
        if (!isCurrentlyValid(current) || !seen.add(current)) return null;

        path.add(current);
        if (anchors.stream().anyMatch(anchor -> sameCertificate(anchor, current))) {
            return new ArrayList<>(path);
        }

        for (X509CertificateHolder parent : certificates) {
            if (!parent.getSubject().equals(current.getIssuer())
                    || !isCurrentlyValid(parent)
                    || seen.contains(parent)
                    || !current.isSignatureValid(PQC_VERIFIER_BUILDER.build(parent))) {
                continue;
            }
            List<X509CertificateHolder> result = findPath(parent, certificates, anchors, path, seen);
            if (result != null) return result;
        }
        path.remove(path.size() - 1);
        seen.remove(current);
        return null;
    }

    private static boolean sameCertificate(X509CertificateHolder first, X509CertificateHolder second) {
        return first.equals(second);
    }

    private static boolean isSelfSigned(X509CertificateHolder certificate) {
        try {
            return certificate.getSubject().equals(certificate.getIssuer())
                    && certificate.isSignatureValid(PQC_VERIFIER_BUILDER.build(certificate));
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isCurrentlyValid(X509CertificateHolder certificate) {
        Date now = new Date();
        return !now.before(certificate.getNotBefore()) && !now.after(certificate.getNotAfter());
    }
}
