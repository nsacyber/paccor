package exception;

import cli.ClientExitCodes;

/**
 * Exception thrown when a signature operation fails.
 */
public class SignatureFailedException extends PaccorException {
    public SignatureFailedException(Throwable cause) {
        super(ClientExitCodes.CRYPTO_ERROR, cause.getMessage(), cause);
    }

    /**
     * Constructs a new SignatureFailedException with the specified reason for the failure
     * and the underlying cause.
     *
     * @param reason the reason for the signature issue
     * @param cause the underlying cause of the failure
     */
    public SignatureFailedException(String reason, Throwable cause) {
        super(ClientExitCodes.CRYPTO_ERROR, "Signature operation failed: " + reason, cause);
    }
}
