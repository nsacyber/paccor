package cli.pv;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import cli.SigningCli;

public class DateParameterValidator implements IParameterValidator {
    @Override
    public void validate(String name, String value) throws ParameterException {
        if (value == null || value.isEmpty()) {
            throw new ParameterException(name + " was empty.");
        }
        SimpleDateFormat sdf = new SimpleDateFormat(SigningCli.DATE_FORMAT);
        try {
            sdf.parse(value);
        } catch (ParseException e) {
            System.out.println(name + " must be a valid date in the format " + SigningCli.DATE_FORMAT + ".");
        }
    }
}
