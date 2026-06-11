package paccor.exception;

import paccor.cli.ClientExitCodes;

public class JsonException extends PaccorException {
    public JsonException(Throwable cause) {
        super(ClientExitCodes.JSON_ERROR, "Could not parse JSON.", cause);
    }

    public JsonException(String path, Throwable cause) {
        super(ClientExitCodes.JSON_FILE_ERROR, "Could not parse JSON file: " + path, cause);
    }
}
