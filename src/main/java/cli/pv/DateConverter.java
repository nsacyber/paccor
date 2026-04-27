package cli.pv;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import picocli.CommandLine;

public class DateConverter implements CommandLine.ITypeConverter<Date> {
    public static final String DATE_FORMAT = "yyyyMMdd";

    @Override
    public Date convert(String value) throws Exception {
        try {
            return dateFromJson(value);
        } catch (ParseException e) {
            throw new CommandLine.TypeConversionException("must be a valid date in the format " + DATE_FORMAT + ".");
        }
    }

    public static final Date dateFromJson(String value) throws ParseException {
        return new SimpleDateFormat(DATE_FORMAT).parse(value);
    }

    public static final Date dateFromJsonSafe(String value) {
        Date result = null;
        try {
            result = dateFromJson(value);
        } catch (Exception ignored) {}
        return result;
    }
}
