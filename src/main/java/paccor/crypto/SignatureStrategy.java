package paccor.crypto;

import paccor.exception.PaccorException;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public interface SignatureStrategy {
    byte[] sign(byte[] content, AlgorithmIdentifier algId) throws PaccorException;
    boolean isLocal();
}
