package paccor.exception;

/**
 * Thrown when an ASN.1 credential structure does not contain the required
 * untagged elements.
 *
 * <p>This is unchecked because ASN.1 {@code getInstance} methods are used
 * throughout the credential model and cannot practically expose a checked
 * exception without changing their public API. It remains an
 * {@link IllegalArgumentException}, so existing callers retain their current
 * error-handling behavior while callers that need to distinguish malformed
 * credentials can catch this type specifically.</p>
 */
public class MalformedCredentialException extends IllegalArgumentException {
    public MalformedCredentialException(String message) {
        super(message);
    }

    public MalformedCredentialException(String message, Throwable cause) {
        super(message, cause);
    }
}
