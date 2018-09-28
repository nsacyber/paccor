package cli.pv;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.io.File;

public class FileExistsParameterValidator implements IParameterValidator {
    @Override
    public void validate(String name, String value) throws ParameterException {
        if (value == null || value.isEmpty()) {
            throw new ParameterException(name + " was empty.");
        }
        File file = new File(value);
        if (!file.exists()) {
            throw new ParameterException("File \"" + value + "\" does not exist.");
        }
    }
}
