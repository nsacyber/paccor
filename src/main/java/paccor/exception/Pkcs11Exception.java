package paccor.exception;

import paccor.cli.ClientExitCodes;

/**
 * Exception thrown when a PKCS#11 operation fails.
 */
public class Pkcs11Exception extends PaccorException {
    public Pkcs11Exception(String reason) {
        super(ClientExitCodes.PKCS11_ERROR, "PKCS#11 operation failed: " + reason);
    }

    public Pkcs11Exception(Throwable cause) {
        this(cause.getMessage(), cause);
    }

    /**
     * Constructs a new Pkcs11Exception with the specified reason for the failure
     * and the underlying cause.
     *
     * @param reason the reason for the PKCS#11 issue
     * @param cause the underlying cause of the failure
     */

    public Pkcs11Exception(String reason, Throwable cause) {
        super(ClientExitCodes.PKCS11_ERROR, "PKCS#11 operation failed: " + reason, cause);
    }
}
