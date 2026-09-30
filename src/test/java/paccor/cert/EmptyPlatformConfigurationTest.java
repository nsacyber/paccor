package paccor.cert;

import java.math.BigInteger;
import java.util.Date;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.cert.AttributeCertificateIssuer;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v2AttributeCertificateBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.cli.CliHelper;
import paccor.tcg.credential.TCGObjectIdentifier;

/**
 * A platform configuration attribute that is present but has no components or properties is an
 * empty configuration, not a missing one.
 */
class EmptyPlatformConfigurationTest {
    private static final String CA_CERT = "src/test/resources/sample_testgen1/PCTestCA.example.com.pem";
    private static final String CA_KEY = "src/test/resources/sample_testgen1/private.pem";
    private static final long DAY = 24L * 60 * 60 * 1000;

    private static byte[] attributeCertificate(ASN1Encodable platformConfiguration) throws Exception {
        X509CertificateHolder ca = CliHelper.loadCert(CA_CERT, CliHelper.x509type.CERTIFICATE);
        PrivateKeyInfo key = CliHelper.loadCertSafe(CA_KEY, CliHelper.x509type.PRIVATE_KEY);
        Date now = new Date();
        X509v2AttributeCertificateBuilder builder = new X509v2AttributeCertificateBuilder(
                new AttributeCertificateHolder(ca),
                new AttributeCertificateIssuer(ca.getSubject()),
                BigInteger.TEN,
                new Date(now.getTime() - DAY),
                new Date(now.getTime() + DAY));
        if (platformConfiguration != null) {
            builder.addAttribute(TCGObjectIdentifier.tcgAtPlatformConfigurationV3, platformConfiguration);
        }
        return builder.build(new JcaContentSignerBuilder("SHA256withRSA")
                .build(new JcaPEMKeyConverter().getPrivateKey(key))).getEncoded();
    }

    @Test
    void presentButEmptyConfiguration_isEmptyNotMissing() throws Exception {
        PlatformCertificate certificate = PlatformCertificate.load(attributeCertificate(new DERSequence()));

        Assertions.assertNotNull(certificate);
        Assertions.assertTrue(certificate.hasPcv3());
        Assertions.assertNotNull(certificate.canonicalizedPlatformConfigurationV3());
        Assertions.assertTrue(certificate.canonicalizedPlatformConfigurationV3().getPlatformComponents().isEmpty());
    }

    @Test
    void absentConfiguration_isMissing() throws Exception {
        PlatformCertificate certificate = PlatformCertificate.load(attributeCertificate(null));

        Assertions.assertNotNull(certificate);
        Assertions.assertNull(certificate.canonicalizedPlatformConfigurationV3());
    }
}
