package paccor.exception;

import paccor.cli.ClientExitCodes;
import lombok.Getter;

/**
 * A custom exception class that represents errors encountered when running paccor commands.
 * Encapsulates an application-specific exit code and an error message.
 */
@Getter
public class PaccorException extends Exception {
    private final ClientExitCodes exitCode;
    private final String userMessage;

    /**
     * Constructs a new {@code PaccorException} with the specified exit code and user message.
     *
     * @param exitCode the application-specific exit code that represents the error
     * @param userMessage the error message intended for display to the user
     */
    public PaccorException(ClientExitCodes exitCode, String userMessage) {
        super(userMessage);
        this.exitCode = exitCode;
        this.userMessage = userMessage;
    }

    /**
     * Constructs a new {@code PaccorException} with the specified exit code and user message.
     *
     * @param exitCode the application-specific exit code that represents the error
     * @param cause the underlying cause of the exception
     */
    public PaccorException(ClientExitCodes exitCode, Throwable cause) {
        super(cause);
        this.exitCode = exitCode;
        this.userMessage = cause.getMessage();
    }

    /**
     * Constructs a new {@code PaccorException} with the specified exit code, user message,
     * and underlying cause of the exception.
     *
     * @param exitCode the application-specific exit code that represents the error
     * @param userMessage the error message intended for display to the user
     * @param cause the underlying cause of the exception
     */
    public PaccorException(ClientExitCodes exitCode, String userMessage, Throwable cause) {
        super(userMessage, cause);
        this.exitCode = exitCode;
        this.userMessage = userMessage;
    }
}
