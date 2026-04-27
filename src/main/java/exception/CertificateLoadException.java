package exception;

import cli.ClientExitCodes;

/**
 * Exception thrown when a certificate cannot be loaded from the specified path.
 */
public class CertificateLoadException extends PaccorException {
    /**
     * Constructs a new CertificateLoadException with a specified path to the certificate
     * and the underlying cause of the exception.
     *
     * @param path the file path of the certificate that failed to load
     * @param cause the cause of the failure
     */
    public CertificateLoadException(String path, Throwable cause) {
        super(ClientExitCodes.USAGE_ERROR, "Failed to load certificate from path: " + path, cause);
    }
}
