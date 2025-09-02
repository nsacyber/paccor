package cli;

import java.util.Arrays;

/**
 * Standardized exit codes for the app.
 */
public enum ClientExitCodes {
    /**
     * Successful exit
     */
    SUCCESS(0),
    /**
     * Generic error
     */
    RUNTIME_ERROR(1),
    /**
     * Generic usage error
     */
    USAGE_ERROR(20),
    /**
     * Unsupported algorithm chosen
     */
    UNSUPPORTED_ALGORITHM(21),
    /**
     * JSON parsing/validation errors
     */
    JSON_ERROR(30),
    /**
     * JSON file-related errors
     */
    JSON_FILE_ERROR(31),
    /**
     * Validation failure
     */
    VALIDATION_FAILED(50),
    /**
     * Crypto operations
     */
    CRYPTO_ERROR(60),
    /**
     * Remote signer issues
     */
    REMOTE_ERROR(70),
    /**
     * HSM/PKCS#11 issues
     */
    PKCS11_ERROR(80),
    ;

    private final int code;

    ClientExitCodes(int code) {
        this.code = code;
    }

    /**
     * Return the exit code.
     * @return The integer exit code.
     */
    public int code() {
        return code;
    }

    /**
     * Look up the enum object by value.
     * @param code The exit code to look up.
     * @return The Enum object in T that corresponds with the value.
     */
    public static final ClientExitCodes lookupCode(int code) {
        return Arrays.stream(ClientExitCodes.class.getEnumConstants())
                .filter(constant -> constant.code() == code)
                .findFirst()
                .orElse(
                        code > 10
                        ? lookupCode(code / 10)
                        : ClientExitCodes.RUNTIME_ERROR);
    }
}
