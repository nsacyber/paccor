package paccor.exception;

import paccor.cli.ClientExitCodes;

public class ValidationException extends PaccorException {
    public ValidationException(String message) {
        super(ClientExitCodes.VALIDATION_FAILED, message);
    }
}
