package exception;

import cli.ClientExitCodes;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class UnsupportedAlgorithmException extends PaccorException {
    public UnsupportedAlgorithmException(ASN1ObjectIdentifier oid) {
        super(ClientExitCodes.UNSUPPORTED_ALGORITHM, message(oid));
    }

    public UnsupportedAlgorithmException(Throwable cause) {
        this(null, cause);
    }

    public UnsupportedAlgorithmException(ASN1ObjectIdentifier oid, Throwable cause) {
        super(ClientExitCodes.UNSUPPORTED_ALGORITHM, message(oid), cause);
    }

    private static String message(ASN1ObjectIdentifier oid) {
        return "Selected algorithm (" + (oid == null ? "OID not provided" : oid.getId()) + ") is not currently supported.";
    }
}
