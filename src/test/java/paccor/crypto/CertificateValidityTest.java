package paccor.crypto;

import java.time.Instant;
import java.util.Date;
import org.bouncycastle.cert.X509CertificateHolder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import paccor.cert.PlatformCertificate;

class CertificateValidityTest {
    private static final Instant NOW = Instant.parse("2027-01-15T00:00:00Z");

    @Test
    void missingAndReversedWindowsAreInvalid() {
        Assertions.assertEquals(CertificateValidity.INVALID, CertificateValidity.at((PlatformCertificate) null, NOW));
        Assertions.assertEquals(CertificateValidity.INVALID, CertificateValidity.at((X509CertificateHolder) null, NOW));
        X509CertificateHolder certificate = Mockito.mock(X509CertificateHolder.class);
        Assertions.assertEquals(CertificateValidity.INVALID, CertificateValidity.at(certificate, NOW));
        Mockito.when(certificate.getNotBefore()).thenReturn(Date.from(NOW.plusSeconds(60)));
        Mockito.when(certificate.getNotAfter()).thenReturn(Date.from(NOW));
        Assertions.assertEquals(CertificateValidity.INVALID, CertificateValidity.at(certificate, NOW));
    }
}
