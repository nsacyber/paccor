package paccor.cli.pv;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import picocli.CommandLine;

/** Parses Internet timestamps with an explicit UTC designator or numeric offset. */
public final class ReferenceTimeConverter implements CommandLine.ITypeConverter<Instant> {
    private static final Pattern RFC_3339 = Pattern.compile(
            "[0-9]{4}-[0-9]{2}-[0-9]{2}[Tt][0-9]{2}:[0-9]{2}:[0-9]{2}"
                    + "(?:\\.[0-9]+)?(?:[Zz]|[+-][0-9]{2}:[0-9]{2})");

    @Override
    public Instant convert(String value) {
        try {
            if (RFC_3339.matcher(value).matches()) {
                return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant();
            }
        } catch (DateTimeParseException ignored) {
            // Report malformed dates and offsets as CLI usage errors, not execution errors.
        }
        throw new CommandLine.TypeConversionException(
                "must be an RFC 3339 timestamp with an explicit offset, such as 2024-01-01T00:00:00Z.");
    }
}
