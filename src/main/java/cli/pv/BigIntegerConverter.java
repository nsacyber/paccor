package cli.pv;

import java.math.BigInteger;
import java.text.ParseException;
import lombok.NonNull;
import picocli.CommandLine;

/**
 * Picocli converter for BigInteger.
 */
public class BigIntegerConverter implements CommandLine.ITypeConverter<BigInteger> {
    @Override
    public BigInteger convert(String value) throws Exception {
        try {
            return decode(value.trim().toLowerCase());
        } catch (ParseException e) {
            throw new CommandLine.TypeConversionException("must be a decimal integer or hexadecimal string");
        }
    }

    /**
     * Decode a string into a BigInteger.
     * @param s String to decode
     * @return BigInteger
     * @throws Exception if the string cannot be decoded
     */
    public static BigInteger decode(@NonNull String s) throws Exception {
        int radix = 10;
        int startIndex = 0;
        int endIndex = s.length();

        if (s.startsWith("0x") || s.startsWith("#")) {
            radix = 16;
            startIndex = 2;
        }

        if (s.endsWith("h")) {
            radix = 16;
            endIndex--;
        }

        if (s.matches("^[0-9a-f]+$")) {
            radix = 16;
        }

        String numberString = s.substring(startIndex, endIndex);
        return new BigInteger(numberString, radix);
    }
}
