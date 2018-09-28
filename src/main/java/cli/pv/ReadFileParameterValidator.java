package cli.pv;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.io.File;

public class ReadFileParameterValidator implements IParameterValidator {
    @Override
    public void validate(String name, String value) throws ParameterException {
        File file = new File(value);
        if (!file.canRead()) {
            throw new ParameterException("File \"" + value + "\" cannot be read.");
        }
    }
}
