package paccor.crypto;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
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
            X509Certificate target = toJcaCertificate(issuer);
            List<X509Certificate> certificates = toJcaCertificates(trustStoreCertificates);
            var anchors = certificates.stream()
                    .filter(IssuerCertificateChecker::isSelfSigned)
                    .map(certificate -> new TrustAnchor(certificate, null))
                    .collect(java.util.stream.Collectors.toCollection(HashSet::new));
            List<X509Certificate> intermediates = certificates.stream()
                    .filter(certificate -> !isSelfSigned(certificate))
                    .toList();
            if (anchors.isEmpty()) return false;
            if (anchors.stream().map(TrustAnchor::getTrustedCert).anyMatch(target::equals)) return true;

            X509CertSelector selector = new X509CertSelector();
            selector.setCertificate(target);
            PKIXBuilderParameters parameters = new PKIXBuilderParameters(anchors, selector);
            parameters.setRevocationEnabled(false);
            parameters.addCertStore(CertStore.getInstance("Collection",
                    new CollectionCertStoreParameters(intermediates)));
            CertPathBuilder.getInstance("PKIX").build(parameters);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static X509Certificate toJcaCertificate(X509CertificateHolder holder)
            throws GeneralSecurityException, IOException {
        return (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(holder.getEncoded()));
    }

    private static List<X509Certificate> toJcaCertificates(List<X509CertificateHolder> holders) throws Exception {
        List<X509Certificate> certificates = new ArrayList<>();
        for (X509CertificateHolder holder : holders) {
            certificates.add(toJcaCertificate(holder));
        }
        return certificates;
    }

    private static boolean isSelfSigned(X509Certificate certificate) {
        try {
            certificate.verify(certificate.getPublicKey());
            return certificate.getSubjectX500Principal().equals(certificate.getIssuerX500Principal());
        } catch (GeneralSecurityException ignored) {
            return false;
        }
    }
}
