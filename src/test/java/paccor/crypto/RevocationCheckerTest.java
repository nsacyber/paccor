package paccor.crypto;

import java.io.File;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.CRLNumber;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.IssuingDistributionPoint;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v2CRLBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import paccor.cert.PlatformCertificate;
import paccor.cli.CliHelper;

/**
 * Unit tests for CRL selection and revocation checks.
 */
public class RevocationCheckerTest {
    private static final File PLATFORM_CERT = new File("src/test/resources/sample_testgen1/platform_cert.20250909102720.crt");
    private static final String CA_CERT = "src/test/resources/sample_testgen1/PCTestCA.example.com.pem";
    private static final String CA_KEY = "src/test/resources/sample_testgen1/private.pem";
    private static final long DAY = 24L * 60 * 60 * 1000;

    private static PlatformCertificate platform;
    private static X509CertificateHolder issuer;
    private static PrivateKey issuerKey;
    private final RevocationChecker checker = new RevocationChecker();

    @BeforeAll
    static void load() throws Exception {
        platform = PlatformCertificate.load(PLATFORM_CERT);
        issuer = CliHelper.loadCert(CA_CERT, CliHelper.x509type.CERTIFICATE);
        PrivateKeyInfo keyInfo = CliHelper.loadCertSafe(CA_KEY, CliHelper.x509type.PRIVATE_KEY);
        issuerKey = new JcaPEMKeyConverter().getPrivateKey(keyInfo);
        Assertions.assertTrue(new IssuerCertificateChecker().validateSignature(platform, issuer),
                "fixture platform certificate must be signed by the fixture CA");
    }

    @Test
    void completeCrlWithoutSerial_passes() throws Exception {
        Assertions.assertTrue(checker.validate(platform, issuer, List.of(crl(builder -> {}))));
    }

    @Test
    void completeCrlListingSerial_fails() throws Exception {
        Assertions.assertFalse(checker.validate(platform, issuer, List.of(crl(this::revokePlatform))));
    }

    @Test
    void deltaCrlAlone_isNotComplete() throws Exception {
        X509CRLHolder delta = crl(builder -> addExtension(builder, Extension.deltaCRLIndicator, true, new CRLNumber(BigInteger.ONE)));

        Assertions.assertFalse(checker.validate(platform, issuer, List.of(delta)));
    }

    @Test
    void deltaCrlListingSerial_revokesEvenWithCleanBase() throws Exception {
        X509CRLHolder base = crl(builder -> {});
        X509CRLHolder delta = crl(builder -> {
            addExtension(builder, Extension.deltaCRLIndicator, true, new CRLNumber(BigInteger.ONE));
            revokePlatform(builder);
        });

        Assertions.assertFalse(checker.validate(platform, issuer, List.of(base, delta)));
    }

    @Test
    void crlWithUnrecognizedCriticalExtension_isIgnored() throws Exception {
        X509CRLHolder unknown = crl(builder -> addExtension(builder, new ASN1ObjectIdentifier("1.2.3.4.5"), true, new DERUTF8String("x")));

        Assertions.assertFalse(checker.validate(platform, issuer, List.of(unknown)));
    }

    @Test
    void scopedCrlExcludingAttributeCertificates_isNotComplete() throws Exception {
        Assertions.assertTrue(platform.isAttributeCertificate());
        X509CRLHolder userOnly = crl(builder -> addExtension(builder, Extension.issuingDistributionPoint, true,
                new IssuingDistributionPoint(null, true, false, null, false, false)));

        Assertions.assertFalse(checker.validate(platform, issuer, List.of(userOnly)));
    }

    @Test
    void scopedCrlForAttributeCertificates_isComplete() throws Exception {
        X509CRLHolder attributeOnly = crl(builder -> addExtension(builder, Extension.issuingDistributionPoint, true,
                new IssuingDistributionPoint(null, false, false, null, false, true)));

        Assertions.assertTrue(checker.validate(platform, issuer, List.of(attributeOnly)));
    }

    @Test
    void expiredCrl_isIgnored() throws Exception {
        Date now = new Date();
        X509v2CRLBuilder builder = new X509v2CRLBuilder(issuer.getSubject(), new Date(now.getTime() - 3 * DAY));
        builder.setNextUpdate(new Date(now.getTime() - DAY));

        Assertions.assertFalse(checker.validate(platform, issuer, List.of(sign(builder))));
    }

    @Test
    void noCrls_fails() {
        Assertions.assertFalse(checker.validate(platform, issuer, List.of()));
    }

    private void revokePlatform(X509v2CRLBuilder builder) {
        builder.addCRLEntry(platform.serialNumber(), new Date(System.currentTimeMillis() - DAY), CRLReason.keyCompromise);
    }

    private static void addExtension(X509v2CRLBuilder builder, ASN1ObjectIdentifier oid, boolean critical,
                                     ASN1Encodable value) {
        try {
            builder.addExtension(oid, critical, value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static X509CRLHolder crl(Consumer<X509v2CRLBuilder> customize) throws Exception {
        Date now = new Date();
        X509v2CRLBuilder builder = new X509v2CRLBuilder(issuer.getSubject(), new Date(now.getTime() - DAY));
        builder.setNextUpdate(new Date(now.getTime() + DAY));
        customize.accept(builder);
        return sign(builder);
    }

    private static X509CRLHolder sign(X509v2CRLBuilder builder) throws Exception {
        return builder.build(new JcaContentSignerBuilder("SHA256withRSA").build(issuerKey));
    }
}
