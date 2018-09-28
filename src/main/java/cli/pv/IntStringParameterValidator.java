package cli.pv;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class IntStringParameterValidator implements IParameterValidator {
    @Override
    public void validate(String name, String value) throws ParameterException {
        if (value == null || value.isEmpty()) {
            throw new ParameterException(name + " was empty.");
        }
        if (!value.matches("^[0-9]+$")) {
            throw new ParameterException(name + " must be a decimal integer.");
        }
    }
}
