package paccor.crypto;

import java.math.BigInteger;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.operator.ContentSigner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.cli.CliHelper;

class IssuerCertificateCheckerTest {
    private static final Path RESOURCES = Path.of("src/test/resources");
    private static final AlgorithmIdentifier ML_DSA_65 =
            new AlgorithmIdentifier(NISTObjectIdentifiers.id_ml_dsa_65);

    private final IssuerCertificateChecker checker = new IssuerCertificateChecker();

    @Test
    void acceptsMlDsa65TrustAnchor() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa65.cert.example.pem");

        Assertions.assertTrue(checker.validateTrustPath(anchor, List.of(anchor)));
    }

    @Test
    void acceptsMlDsa87TrustAnchor() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa87.cert.example.pem");

        Assertions.assertTrue(checker.validateTrustPath(anchor, List.of(anchor)));
    }

    @Test
    void acceptsPqcCertificateChainedToPqcAnchor() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder leaf = signedCertificate(anchor, "CN=ML-DSA leaf",
                new Date(System.currentTimeMillis() - 60_000),
                new Date(System.currentTimeMillis() + 60_000), false);

        Assertions.assertTrue(leaf.isSignatureValid(SignatureService.buildWithDefault(anchor)));
        Assertions.assertTrue(checker.validateTrustPath(leaf, List.of(anchor)));
    }

    @Test
    void rejectsCertificateWithWrongTrustAnchor() throws Exception {
        X509CertificateHolder anchor65 = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder anchor87 = loadCertificate("TestCA.mldsa87.cert.example.pem");
        X509CertificateHolder leaf = signedCertificate(anchor65, "CN=ML-DSA leaf",
                new Date(System.currentTimeMillis() - 60_000),
                new Date(System.currentTimeMillis() + 60_000), false);

        Assertions.assertFalse(checker.validateTrustPath(leaf, List.of(anchor87)));
    }

    @Test
    void rejectsCertificateWhenIntermediateIsMissing() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder intermediate = signedCertificate(anchor, "CN=ML-DSA intermediate",
                new Date(System.currentTimeMillis() - 60_000),
                new Date(System.currentTimeMillis() + 60_000), true);
        X509CertificateHolder leaf = signedCertificate(intermediate, "CN=ML-DSA leaf",
                new Date(System.currentTimeMillis() - 60_000),
                new Date(System.currentTimeMillis() + 60_000), false);

        Assertions.assertFalse(checker.validateTrustPath(leaf, List.of(anchor)));
    }

    @Test
    void rejectsExpiredCertificate() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder expired = signedCertificate(anchor, "CN=expired ML-DSA leaf",
                new Date(System.currentTimeMillis() - 120_000),
                new Date(System.currentTimeMillis() - 60_000), false);

        Assertions.assertFalse(checker.validateTrustPath(expired, List.of(anchor)));
    }

    @Test
    void rejectsCyclicChainWithoutInfiniteLoop() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder first = signedCertificate(new X500Name("CN=cycle-two"),
                anchor, "CN=cycle-one");
        X509CertificateHolder second = signedCertificate(new X500Name("CN=cycle-one"),
                anchor, "CN=cycle-two");

        Assertions.assertFalse(checker.validateTrustPath(first, List.of(anchor, second)));
    }

    private static X509CertificateHolder loadCertificate(String filename) throws Exception {
        return CliHelper.loadCert(RESOURCES.resolve(filename).toString(), CliHelper.x509type.CERTIFICATE);
    }

    private static X509CertificateHolder signedCertificate(
            X509CertificateHolder issuer,
            String subject,
            Date notBefore,
            Date notAfter,
            boolean ca) throws Exception {
        return signedCertificate(issuer.getSubject(), issuer, subject, notBefore, notAfter, ca);
    }

    private static X509CertificateHolder signedCertificate(
            X500Name issuerName,
            X509CertificateHolder signingCertificate,
            String subject) throws Exception {
        return signedCertificate(issuerName, signingCertificate, subject,
                new Date(System.currentTimeMillis() - 60_000),
                new Date(System.currentTimeMillis() + 60_000), false);
    }

    private static X509CertificateHolder signedCertificate(
            X500Name issuerName,
            X509CertificateHolder signingCertificate,
            String subject,
            Date notBefore,
            Date notAfter,
            boolean ca) throws Exception {
        PrivateKeyInfo privateKey = CliHelper.loadCert(
                RESOURCES.resolve("TestCA.mldsa65.private.example.pem").toString(),
                CliHelper.x509type.PRIVATE_KEY);
        AsymmetricKeyParameter key = PqcHelper.createKeyFromInfo(privateKey);
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                issuerName,
                BigInteger.valueOf(System.nanoTime()),
                notBefore,
                notAfter,
                new X500Name(subject),
                signingCertificate.getSubjectPublicKeyInfo());
        builder.addExtension(Extension.basicConstraints, true,
                new BasicConstraints(ca));
        ContentSigner signer = new PcBcContentSignerBuilder(
                ML_DSA_65, new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha512)).build(key);
        return builder.build(signer);
    }
}
