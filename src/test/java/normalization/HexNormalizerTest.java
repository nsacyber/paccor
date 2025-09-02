package normalization;

import java.lang.reflect.Constructor;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for HexNormalizer utility class.
 */
class HexNormalizerTest {

    @Test
    void normalize_validHex_normalizes() {
        Assertions.assertEquals("08ab", HexNormalizer.normalize("8AB", 2));
        Assertions.assertEquals("08ab", HexNormalizer.normalize("8ab", 2));
        Assertions.assertEquals("08ab", HexNormalizer.normalize("0x8AB", 2));
        Assertions.assertEquals("08ab", HexNormalizer.normalize("0x8ab", 2));
    }

    @Test
    void normalize_withDelimiters_removesDelimiters() {
        Assertions.assertEquals("08ab", HexNormalizer.normalize("8:AB", 2));
        Assertions.assertEquals("08ab", HexNormalizer.normalize("8-AB", 2));
        Assertions.assertEquals("08ab", HexNormalizer.normalize("8 AB", 2));
    }

    @Test
    void normalize_withWhitespace_trimsAndNormalizes() {
        Assertions.assertEquals("08ab", HexNormalizer.normalize("  8AB  ", 2));
        Assertions.assertEquals("08ab", HexNormalizer.normalize("\t8AB\n", 2));
    }

    @Test
    void normalize_shortHex_zeroPads() {
        Assertions.assertEquals("0001", HexNormalizer.normalize("1", 2));
        Assertions.assertEquals("000a", HexNormalizer.normalize("a", 2));
        Assertions.assertEquals("00ff", HexNormalizer.normalize("ff", 2));
    }

    @Test
    void normalize_longHex_truncates() {
        Assertions.assertEquals("abcd", HexNormalizer.normalize("1234abcd", 2));
        Assertions.assertEquals("cd", HexNormalizer.normalize("abcd", 1));
    }

    @Test
    void normalize_nullOrEmpty_returnsZeroPadded() {
        Assertions.assertEquals("0000", HexNormalizer.normalize(null, 2));
        Assertions.assertEquals("0000", HexNormalizer.normalize("", 2));
        Assertions.assertEquals("000000000000", HexNormalizer.normalize("", 6));
    }

    @Test
    void normalize_invalidHex_returnsZeroPadded() {
        Assertions.assertEquals("0000", HexNormalizer.normalize("invalid", 2));
        Assertions.assertEquals("0000", HexNormalizer.normalize("xyz", 2));
        Assertions.assertEquals("0000", HexNormalizer.normalize("12g3", 2));
    }

    @Test
    void normalize_differentByteSizes() {
        Assertions.assertEquals("01", HexNormalizer.normalize("1", 1));
        Assertions.assertEquals("0001", HexNormalizer.normalize("1", 2));
        Assertions.assertEquals("00000001", HexNormalizer.normalize("1", 4));
        Assertions.assertEquals("0000000000000001", HexNormalizer.normalize("1", 8));
    }

    @Test
    void normalizeMac_validMac_normalizes() {
        Assertions.assertEquals("AABBCCDDEEFF", HexNormalizer.normalizeMac("AA:BB:CC:DD:EE:FF"));
        Assertions.assertEquals("AABBCCDDEEFF", HexNormalizer.normalizeMac("aa:bb:cc:dd:ee:ff"));
        Assertions.assertEquals("AABBCCDDEEFF", HexNormalizer.normalizeMac("AA-BB-CC-DD-EE-FF"));
        Assertions.assertEquals("AABBCCDDEEFF", HexNormalizer.normalizeMac("AABBCCDDEEFF"));
    }

    @Test
    void normalizeMac_withPeriods_normalizes() {
        Assertions.assertEquals("AABBCCDDEEFF", HexNormalizer.normalizeMac("AA.BB.CC.DD.EE.FF"));
        Assertions.assertEquals("AABBCCDDEEFF", HexNormalizer.normalizeMac("aabb.ccdd.eeff"));
    }

    @Test
    void normalizeMac_withWhitespace_trimsAndNormalizes() {
        Assertions.assertEquals("AABBCCDDEEFF", HexNormalizer.normalizeMac("  AA:BB:CC:DD:EE:FF  "));
    }

    @Test
    void normalizeMac_shortMac_zeroPads() {
        Assertions.assertEquals("0000000000AB", HexNormalizer.normalizeMac("ab"));
        Assertions.assertEquals("000000ABCDEF", HexNormalizer.normalizeMac("abcdef"));
    }

    @Test
    void normalizeMac_longMac_truncates() {
        Assertions.assertEquals("AABBCCDDEE11", HexNormalizer.normalizeMac("aabbccddee1122"));
    }

    @Test
    void normalizeMac_nullOrEmpty_returnsZeroPadded() {
        Assertions.assertEquals("000000000000", HexNormalizer.normalizeMac(null));
        Assertions.assertEquals("000000000000", HexNormalizer.normalizeMac(""));
    }

    @Test
    void normalizeMac_invalidMac_returnsZeroPadded() {
        Assertions.assertEquals("000000000000", HexNormalizer.normalizeMac("invalid"));
        Assertions.assertEquals("000000000000", HexNormalizer.normalizeMac("GG:HH:II:JJ:KK:LL"));
    }

    @Test
    void parseHex_validHex_parsesToInt() {
        Assertions.assertEquals(Optional.of(0x8086), HexNormalizer.parseHex("8086"));
        Assertions.assertEquals(Optional.of(0x8086), HexNormalizer.parseHex("0x8086"));
        Assertions.assertEquals(Optional.of(255), HexNormalizer.parseHex("ff"));
        Assertions.assertEquals(Optional.of(255), HexNormalizer.parseHex("FF"));
        Assertions.assertEquals(Optional.of(16), HexNormalizer.parseHex("10"));
    }

    @Test
    void parseHex_withWhitespace_trimsAndParses() {
        Assertions.assertEquals(Optional.of(0x8086), HexNormalizer.parseHex("  8086  "));
    }

    @Test
    void parseHex_nullOrEmpty_returnsNegativeOne() {
        Assertions.assertTrue(HexNormalizer.parseHex(null).isEmpty());
        Assertions.assertTrue(HexNormalizer.parseHex("").isEmpty());
    }

    @Test
    void parseHex_invalidHex_returnsNegativeOne() {
        Assertions.assertTrue(HexNormalizer.parseHex("invalid").isEmpty());
        Assertions.assertTrue(HexNormalizer.parseHex("xyz").isEmpty());
        Assertions.assertTrue(HexNormalizer.parseHex("12g3").isEmpty());
    }

    @Test
    void isHexString_validHex_returnsTrue() {
        Assertions.assertTrue(HexNormalizer.isHexString("8086"));
        Assertions.assertTrue(HexNormalizer.isHexString("0x8086"));
        Assertions.assertTrue(HexNormalizer.isHexString("ff"));
        Assertions.assertTrue(HexNormalizer.isHexString("FF"));
        Assertions.assertTrue(HexNormalizer.isHexString("0xABCDEF"));
        Assertions.assertTrue(HexNormalizer.isHexString("123456789abcdef"));
    }

    @Test
    void isHexString_withWhitespace_trimsAndChecks() {
        Assertions.assertTrue(HexNormalizer.isHexString("  8086  "));
    }

    @Test
    void isHexString_nullOrEmpty_returnsFalse() {
        Assertions.assertFalse(HexNormalizer.isHexString(null));
        Assertions.assertFalse(HexNormalizer.isHexString(""));
        Assertions.assertFalse(HexNormalizer.isHexString("   "));
    }

    @Test
    void isHexString_invalidHex_returnsFalse() {
        Assertions.assertFalse(HexNormalizer.isHexString("invalid"));
        Assertions.assertFalse(HexNormalizer.isHexString("xyz"));
        Assertions.assertFalse(HexNormalizer.isHexString("12g3"));
        Assertions.assertFalse(HexNormalizer.isHexString("Intel Corporation"));
    }

    @Test
    void constructor_throwsUnsupportedOperation() throws Exception {
        Constructor<HexNormalizer> constructor = HexNormalizer.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Exception exception = Assertions.assertThrows(Exception.class, constructor::newInstance);
        // InvocationTargetException wraps the actual UnsupportedOperationException
        Assertions.assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }
}
