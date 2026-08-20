package paccor.crypto;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentVerifierProvider;
import paccor.cert.PlatformCertificate;

/**
 * Performs CRL checks for platform certificates.
*/
public final class RevocationChecker {
    /**
     * CRL validation for PKC and attribute certificates.
     * @param platform the platform certificate
     * @param issuer the issuer certificate
     * @param crls the CRL file(s)
     * @return true if the certificate is valid, false otherwise
     */
    public boolean validate(PlatformCertificate platform, X509CertificateHolder issuer, List<X509CRLHolder> crls) {
        try {
            BigInteger serial = platform.serialNumber();
            X500Name issuerName = issuer.getSubject();
            Date now = new Date();
            ContentVerifierProvider verifier = SignatureService.buildWithDefault(issuer);
            List<X509CRLHolder> usableCrls = crls.stream()
                    .filter(crl -> isUsable(crl, issuerName, now, verifier))
                    .toList();
            return !usableCrls.isEmpty()
                    && usableCrls.stream().noneMatch(crl -> crl.getRevokedCertificate(serial) != null);
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean isUsable(
            X509CRLHolder crl,
            X500Name issuerName,
            Date now,
            ContentVerifierProvider verifier) {
        if (!crl.getIssuer().equals(issuerName)
                || crl.getThisUpdate() == null
                || crl.getThisUpdate().after(now)
                || Optional.ofNullable(crl.getNextUpdate()).filter(date -> date.before(now)).isPresent()) {
            return false;
        }
        try {
            return crl.isSignatureValid(verifier);
        } catch (Exception ignored) {
            return false;
        }
    }
}
