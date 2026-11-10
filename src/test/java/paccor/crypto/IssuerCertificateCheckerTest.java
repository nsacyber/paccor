package paccor.crypto;

import java.math.BigInteger;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
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

    private static final Instant NOW = Instant.parse("2027-01-15T00:00:00Z");
    private final IssuerCertificateChecker checker = new IssuerCertificateChecker(Clock.fixed(NOW, ZoneOffset.UTC));

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
                Date.from(NOW.minusSeconds(60)),
                Date.from(NOW.plusSeconds(60)), false);

        Assertions.assertTrue(leaf.isSignatureValid(SignatureService.buildWithDefault(anchor)));
        Assertions.assertTrue(checker.validateTrustPath(leaf, List.of(anchor)));
    }

    @Test
    void rejectsCertificateWithWrongTrustAnchor() throws Exception {
        X509CertificateHolder anchor65 = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder anchor87 = loadCertificate("TestCA.mldsa87.cert.example.pem");
        X509CertificateHolder leaf = signedCertificate(anchor65, "CN=ML-DSA leaf",
                Date.from(NOW.minusSeconds(60)),
                Date.from(NOW.plusSeconds(60)), false);

        Assertions.assertFalse(checker.validateTrustPath(leaf, List.of(anchor87)));
    }

    @Test
    void rejectsCertificateWhenIntermediateIsMissing() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder intermediate = signedCertificate(anchor, "CN=ML-DSA intermediate",
                Date.from(NOW.minusSeconds(60)), Date.from(NOW.plusSeconds(60)), true);
        X509CertificateHolder leaf = signedCertificate(intermediate, "CN=ML-DSA leaf",
                Date.from(NOW.minusSeconds(60)), Date.from(NOW.plusSeconds(60)), false);

        Assertions.assertFalse(checker.validateTrustPath(leaf, List.of(anchor)));
    }

    @Test
    void rejectsExpiredCertificate() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder expired = signedCertificate(anchor, "CN=expired ML-DSA leaf",
                Date.from(NOW.minusSeconds(120)), Date.from(NOW.minusSeconds(60)), false);

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

    @Test
    void rejectsNotYetValidCertificate() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder future = signedCertificate(anchor, "CN=future leaf",
                Date.from(NOW.plusSeconds(60)), Date.from(NOW.plusSeconds(120)), false);
        Assertions.assertFalse(checker.validateTrustPath(future, List.of(anchor)));
    }

    @Test
    void checksIntermediateAndAnchorAtReferenceTime() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder intermediate = signedCertificate(anchor, "CN=expired intermediate",
                Date.from(NOW.minusSeconds(120)), Date.from(NOW.minusSeconds(60)), true);
        X509CertificateHolder leaf = signedCertificate(intermediate, "CN=leaf",
                Date.from(NOW.minusSeconds(60)), Date.from(NOW.plusSeconds(60)), false);
        Assertions.assertFalse(checker.validateTrustPath(leaf, List.of(intermediate, anchor)));
        Assertions.assertFalse(new IssuerCertificateChecker(
                Clock.fixed(anchor.getNotBefore().toInstant().minusSeconds(1), ZoneOffset.UTC))
                .validateTrustPath(anchor, List.of(anchor)));
        Assertions.assertFalse(new IssuerCertificateChecker(
                Clock.fixed(anchor.getNotAfter().toInstant().plusSeconds(1), ZoneOffset.UTC))
                .validateTrustPath(anchor, List.of(anchor)));
    }

    @Test
    void validityBoundariesAreInclusiveAndUnusedExpiredCertificatesAreIgnored() throws Exception {
        X509CertificateHolder anchor = loadCertificate("TestCA.mldsa65.cert.example.pem");
        X509CertificateHolder leaf = signedCertificate(anchor, "CN=boundary leaf",
                Date.from(NOW), Date.from(NOW.plusSeconds(60)), false);
        X509CertificateHolder unrelated = signedCertificate(anchor, "CN=unused expired CA",
                Date.from(NOW.minusSeconds(120)), Date.from(NOW.minusSeconds(60)), true);
        Assertions.assertTrue(checker.validateTrustPath(leaf, List.of(unrelated, anchor)));
        Assertions.assertTrue(new IssuerCertificateChecker(Clock.fixed(NOW.plusSeconds(60), ZoneOffset.UTC))
                .validateTrustPath(leaf, List.of(unrelated, anchor)));
        Assertions.assertFalse(new IssuerCertificateChecker(Clock.fixed(NOW.plusSeconds(61), ZoneOffset.UTC))
                .validateTrustPath(leaf, List.of(unrelated, anchor)));
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
                Date.from(NOW.minusSeconds(60)),
                Date.from(NOW.plusSeconds(60)), false);
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
