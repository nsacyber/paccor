package paccor.cli.pv;

import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class ReferenceTimeConverterTest {
    private final ReferenceTimeConverter converter = new ReferenceTimeConverter();

    @Test
    void acceptsUtcOffsetsAndFractionalSeconds() {
        Instant expected = Instant.parse("2027-01-15T00:00:00Z");
        Assertions.assertEquals(expected, converter.convert("2027-01-15T00:00:00Z"));
        Assertions.assertEquals(expected, converter.convert("2027-01-15T02:30:00+02:30"));
        Assertions.assertEquals(expected, converter.convert("2027-01-14T19:00:00-05:00"));
        Assertions.assertEquals(expected, converter.convert("2027-01-15t00:00:00z"));
        Assertions.assertEquals(expected.plusNanos(123456789), converter.convert("2027-01-15T00:00:00.123456789Z"));
    }

    @Test
    void rejectsInvalidDatesMissingOffsetsAndNonInternetFormats() {
        for (String value : new String[] {"2027-01-15", "2027-01-15T00:00:00", "2027-02-29T00:00:00Z",
                "2027-01-15 00:00:00Z", "2027-01-15T24:00:00Z", "2027-01-15T00:00Z",
                "2027-01-15T00:00:00+0200", "2027-01-15T00:00:00+02:30:45", "garbage"}) {
            Assertions.assertThrows(CommandLine.TypeConversionException.class, () -> converter.convert(value), value);
        }
    }
}
