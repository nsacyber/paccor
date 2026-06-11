package paccor.crypto;

import paccor.cert.CertSigEncoding;
import paccor.exception.PaccorException;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.encoders.Base64;

/**
 * Enables usage of signatures produced outside paccor.
 * @param signatureB64 Base64-encoded signature
 * @param sigEncoding Signature encoding format
 */
public record ProvidedSignatureStrategy(String signatureB64, CertSigEncoding sigEncoding) implements SignatureStrategy {
    @Override
    public byte[] sign(byte[] tbs, AlgorithmIdentifier algId) throws PaccorException {
        return AlgorithmSupport.maybeConvertToDer(Base64.decode(signatureB64), sigEncoding, algId);
    }

    @Override public boolean isLocal() {
        return false;
    }
}
