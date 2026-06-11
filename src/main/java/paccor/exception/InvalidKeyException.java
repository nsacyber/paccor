package paccor.exception;

import paccor.cli.ClientExitCodes;
import java.io.File;
import lombok.NonNull;

/**
 * Exception thrown when a private key cannot be loaded.
 */
public class InvalidKeyException extends PaccorException {
    /**
     * Constructs a new InvalidKeyException with a specified path to the private key.
     *
     * @param keyPath the file path of the private key that could not be loaded
     */
    public InvalidKeyException(String keyPath) {
        super(ClientExitCodes.CRYPTO_ERROR, "Could not load private key from " + keyPath + ". Supported: PKCS#8, PKCS#1, EC, PKCS#12");
    }

    public InvalidKeyException(File keyFile) {
        this(keyFile == null ? "" : keyFile.getAbsolutePath());
    }
}
