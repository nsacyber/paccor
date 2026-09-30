package paccor.cert;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.cert.X509AttributeCertificateHolder;

/**
 * Holder rules that link a delta or rebase platform certificate to the certificate it builds on.
 */
public final class PlatformCertificateHolders {
    private PlatformCertificateHolders() {}

    /**
     * Check whether the delta's Holder names the target certificate itself, as V1.1 deltas do.
     * @param delta the delta platform certificate
     * @param target the certificate the delta should reference
     * @return true if the delta's Holder identifies the target
     */
    public static boolean holderMatches(PlatformCertificate delta, PlatformCertificate target) {
        if (delta == null || target == null || !delta.isAttributeCertificate()) return false;
        try {
            AttributeCertificateHolder holder = delta.getAttributeCertificate().getHolder();
            if (target.isPublicKeyCertificate()) {
                return (holder.getSerialNumber() != null || holder.getObjectDigest() != null)
                        && holder.match(target.getPublicKeyCertificate());
            }
            return target.isAttributeCertificate()
                    && holder.getSerialNumber() != null
                    && holder.getIssuer() != null
                    && holder.getSerialNumber().equals(target.serialNumber())
                    && Arrays.stream(holder.getIssuer()).anyMatch(issuer -> targetIssuer(target, issuer));
        } catch (Exception ignored) {
            return false;
        }
    }

    /**
     * Check whether a delta or rebase is consistent with the certificate it builds on.
     * V1.1 deltas name the certificate they update as their holder. V2.0 deltas and rebases share
     * that certificate's holder.
     * @param delta the delta or rebase platform certificate
     * @param anchor the base or rebase certificate it builds on
     * @return true if the holders are consistent
     */
    public static boolean holderConsistent(PlatformCertificate delta, PlatformCertificate anchor) {
        if (delta != null && delta.resolvedSpecVersion() == CertSpecVersion.V1_1) {
            return holderMatches(delta, anchor);
        }
        return holderConsistentV2(delta, anchor);
    }

    private static boolean holderConsistentV2(PlatformCertificate delta, PlatformCertificate anchor) {
        if (delta == null || anchor == null) {
            return false;
        }
        // PKC has no Holder
        if (!delta.isAttributeCertificate() && !anchor.isAttributeCertificate()) {
            return true;
        }
        if (delta.isAttributeCertificate() != anchor.isAttributeCertificate()) {
            return false;
        }
        return sameRoTHolder(delta, anchor);
    }

    private static Optional<AttributeCertificateHolder> roTHolder(PlatformCertificate certificate) {
        return Optional.ofNullable(certificate)
                .filter(PlatformCertificate::isAttributeCertificate)
                .map(PlatformCertificate::getAttributeCertificate)
                .map(X509AttributeCertificateHolder::getHolder)
                .filter(holder -> holder.getSerialNumber() != null
                        && holder.getIssuer() != null);
    }

    private static boolean sameRoTHolder(PlatformCertificate a, PlatformCertificate b) {
        return roTHolder(a)
                .flatMap(left -> roTHolder(b)
                        .map(right -> Objects.equals(left.getSerialNumber(), right.getSerialNumber())
                                && Arrays.equals(left.getIssuer(), right.getIssuer())))
                .orElse(false);
    }

    private static boolean targetIssuer(PlatformCertificate target, X500Name issuer) {
        return Arrays.asList(target.getAttributeCertificate().getIssuer().getNames()).contains(issuer);
    }
}
