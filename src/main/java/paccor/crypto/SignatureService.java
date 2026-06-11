package paccor.crypto;

import paccor.cli.CliHelper;
import paccor.exception.InvalidKeyException;
import paccor.exception.PaccorException;
import java.io.File;
import java.io.OutputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;

/**
 * Centralized signature utilities for CLI commands.
 * Uses AlgorithmSupport for algorithm-specific checks.
 */
public final class SignatureService {
    private SignatureService() {}

    public static byte[] sign(byte[] tbs, AlgorithmIdentifier algId, File keyFile) throws PaccorException {
        // trying sample_testgen1 ways to load the Key.
        PrivateKeyInfo pki = CliHelper.loadCertSafe(keyFile.getPath(), CliHelper.x509type.PRIVATE_KEY);
        if (pki == null) {
            pki = CliHelper.loadCertSafe(keyFile.getPath(), CliHelper.x509type.RSA_PRIVATE_KEY);
        }
        if (pki == null) {
            pki = CliHelper.loadCertSafe(keyFile.getPath(), CliHelper.x509type.EC_PRIVATE_KEY);
        }
        // Could not load the key.
        if (pki == null) {
            throw new InvalidKeyException(keyFile);
        }

        return AlgorithmSupport.signWithBc(tbs, algId, pki);
    }

    public static boolean verifyWithCert(File issuerCert, AlgorithmIdentifier algId, byte[] tbs, byte[] sig) {
        try {
            X509CertificateHolder cert = CliHelper.loadCert(issuerCert.getPath(), CliHelper.x509type.CERTIFICATE);
            ContentVerifierProvider cvp = new PcBcContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder()).build(cert);
            ContentVerifier verifier = cvp.get(algId);
            try (OutputStream os = verifier.getOutputStream()) { os.write(tbs); }
            return verifier.verify(sig);
        } catch (Exception e) {
            return false;
        }
    }
}
