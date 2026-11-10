package paccor.crypto;

import java.time.Instant;
import java.util.Date;
import org.bouncycastle.cert.X509CertificateHolder;
import paccor.cert.PlatformCertificate;

/** The validity-window status of a certificate at an explicit evaluation instant. */
public enum CertificateValidity {
    OK,
    EXPIRED,
    NOT_YET_VALID,
    INVALID;

    public static CertificateValidity at(PlatformCertificate certificate, Instant instant) {
        return certificate == null ? INVALID
                : at(certificate.extractNotBefore(), certificate.extractNotAfter(), instant);
    }

    public static CertificateValidity at(X509CertificateHolder certificate, Instant instant) {
        return certificate == null ? INVALID
                : at(certificate.getNotBefore(), certificate.getNotAfter(), instant);
    }

    private static CertificateValidity at(Date notBefore, Date notAfter, Instant instant) {
        if (notBefore == null || notAfter == null || notAfter.before(notBefore)) {
            return INVALID;
        }
        if (instant.isBefore(notBefore.toInstant())) return NOT_YET_VALID;
        if (instant.isAfter(notAfter.toInstant())) return EXPIRED;
        return OK;
    }
}
