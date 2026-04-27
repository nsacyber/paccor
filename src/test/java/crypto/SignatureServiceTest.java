package crypto;

import cert.CertSigEncoding;
import java.io.File;
import java.util.Arrays;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
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
}
