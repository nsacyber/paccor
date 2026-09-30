package paccor.crypto;

import paccor.cert.CertSigEncoding;
import java.io.File;
import java.util.Arrays;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDefaultDigestProvider;
import org.bouncycastle.operator.bc.BcDigestProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SignatureServiceTest {

    @Test
    public void testP1363ToDerConversionForEcdsa() {
        byte[] r = new byte[32]; Arrays.fill(r, (byte)0x01);
        byte[] s = new byte[32]; Arrays.fill(s, (byte)0x02);
        byte[] p1363 = new byte[64];
        System.arraycopy(r, 0, p1363, 0, 32);
        System.arraycopy(s, 0, p1363, 32, 32);
        AlgorithmIdentifier algId = new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256);
        byte[] der = AlgorithmSupport.maybeConvertToDer(p1363, CertSigEncoding.P1363, algId);
        Assertions.assertNotNull(der);
        Assertions.assertNotEquals(64, der.length, "DER encoding should differ in length from P1363");
    }

    @Test
    public void testSignAndVerifyRoundTripRsa() throws Exception {
        AlgorithmIdentifier algId = new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption);
        byte[] tbs = "hello-tbs".getBytes();
        File key = new File("src/test/resources/TestCA.private.example.pem");
        File cert = new File("src/test/resources/TestCA.cert.example.pem");
        byte[] sig = SignatureService.sign(tbs, algId, key);
        Assertions.assertTrue(SignatureService.verifyWithCert(cert, algId, tbs, sig));
    }

    @Test
    public void testSignAndVerifyRoundTripMlDsa65() throws Exception {
        AlgorithmIdentifier algId = new AlgorithmIdentifier(NISTObjectIdentifiers.id_ml_dsa_65);
        byte[] tbs = "hello-tbs-mldsa".getBytes();
        File key = new File("src/test/resources/TestCA.mldsa65.private.example.pem");
        File cert = new File("src/test/resources/TestCA.mldsa65.cert.example.pem");
        byte[] sig = SignatureService.sign(tbs, algId, key);
        Assertions.assertTrue(SignatureService.verifyWithCert(cert, algId, tbs, sig));
    }

    @Test
    public void testSignAndVerifyRoundTripHashMlDsa65() throws Exception {
        AlgorithmIdentifier algId = new AlgorithmIdentifier(NISTObjectIdentifiers.id_hash_ml_dsa_65_with_sha512);
        AlgorithmIdentifier pure = new AlgorithmIdentifier(NISTObjectIdentifiers.id_ml_dsa_65);
        byte[] tbs = "tbs-hash-mldsa".getBytes();
        File key = new File("src/test/resources/TestCA.mldsa65.private.example.pem");
        File cert = new File("src/test/resources/TestCA.mldsa65.cert.example.pem");
        byte[] sig = SignatureService.sign(tbs, algId, key);
        Assertions.assertTrue(SignatureService.verifyWithCert(cert, algId, tbs, sig));
        // Pre-hash and pure signatures are domain separated.
        Assertions.assertFalse(SignatureService.verifyWithCert(cert, pure, tbs, sig));
    }

    @Test
    public void testSignAndVerifyRoundTripRsaPss() throws Exception {
        AlgorithmIdentifier algId = pssAlgorithm(PKCSObjectIdentifiers.id_mgf1, 1);
        byte[] tbs = "tbs-pss".getBytes();
        File key = new File("src/test/resources/TestCA.private.example.pem");
        File cert = new File("src/test/resources/TestCA.cert.example.pem");
        byte[] sig = SignatureService.sign(tbs, algId, key);
        Assertions.assertTrue(SignatureService.verifyWithCert(cert, algId, tbs, sig));
    }

    @Test
    public void testPssRejectsUnsupportedMaskGenerationOrTrailer() {
        BcDigestProvider digests = BcDefaultDigestProvider.INSTANCE;
        Assertions.assertThrows(OperatorCreationException.class, () ->
                AlgorithmSupport.buildPssSigner(pssAlgorithm(new ASN1ObjectIdentifier("1.2.3.4"), 1), digests));
        Assertions.assertThrows(OperatorCreationException.class, () ->
                AlgorithmSupport.buildPssSigner(pssAlgorithm(PKCSObjectIdentifiers.id_mgf1, 2), digests));
    }

    private static AlgorithmIdentifier pssAlgorithm(ASN1ObjectIdentifier mgf, int trailer) {
        AlgorithmIdentifier sha256 = new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256);
        return new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSASSA_PSS, new RSASSAPSSparams(
                sha256,
                new AlgorithmIdentifier(mgf, sha256),
                new ASN1Integer(32),
                new ASN1Integer(trailer)));
    }
}
