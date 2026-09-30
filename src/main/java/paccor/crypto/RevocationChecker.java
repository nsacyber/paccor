package paccor.crypto;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentVerifierProvider;
import paccor.cert.PlatformCertificate;

/**
 * Performs CRL checks for platform certificates.
 */
public final class RevocationChecker {
    private static final Logger LOGGER = Logger.getLogger(RevocationChecker.class.getName());

    private static final Set<ASN1ObjectIdentifier> RECOGNIZED_CRITICAL_EXTENSIONS = Set.of(
            Extension.deltaCRLIndicator,
            Extension.issuingDistributionPoint
    );

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
            if (usableCrls.stream().noneMatch(crl -> isComplete(crl, platform))) {
                LOGGER.warning("No complete CRL covering the platform certificate was provided.");
                return false;
            }
            return usableCrls.stream().noneMatch(crl -> crl.getRevokedCertificate(serial) != null);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "CRL validation could not be completed.", e);
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
        if (!RECOGNIZED_CRITICAL_EXTENSIONS.containsAll(criticalExtensions(crl))) {
            LOGGER.warning("Ignoring CRL from " + crl.getIssuer() + ": it has an unrecognized critical extension.");
            return false;
        }
        try {
            return crl.isSignatureValid(verifier);
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Ignoring this CRL because its signature could not be verified.", e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<ASN1ObjectIdentifier> criticalExtensions(X509CRLHolder crl) {
        return Optional.ofNullable(crl.getCriticalExtensionOIDs()).orElse(Set.of());
    }

    private static boolean isComplete(X509CRLHolder crl, PlatformCertificate platform) {
        if (crl.getExtension(Extension.deltaCRLIndicator) != null) {
            return false;
        }
        Extension idpExtension = crl.getExtension(Extension.issuingDistributionPoint);
        if (idpExtension == null) {
            return true;
        }
        IssuingDistributionPoint idp = IssuingDistributionPoint.getInstance(idpExtension.getParsedValue());
        if (idp.getOnlySomeReasons() != null
                || idp.isIndirectCRL()
                || idp.onlyContainsCACerts()
                || (idp.onlyContainsUserCerts() && !platform.isPublicKeyCertificate())
                || (idp.onlyContainsAttributeCerts() && !platform.isAttributeCertificate())) {
            return false;
        }
        return idp.getDistributionPoint() == null
                || certificateNamesDistributionPoint(platform, idp.getDistributionPoint());
    }

    private static boolean certificateNamesDistributionPoint(PlatformCertificate platform, DistributionPointName name) {
        return Optional.ofNullable(platform.getExtension(Extension.cRLDistributionPoints))
                .map(extension -> CRLDistPoint.getInstance(extension.getParsedValue()))
                .map(points -> Arrays.stream(points.getDistributionPoints())
                        .anyMatch(point -> name.equals(point.getDistributionPoint())))
                .orElse(false);
    }
}
