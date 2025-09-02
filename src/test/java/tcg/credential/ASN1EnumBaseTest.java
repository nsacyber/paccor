package tcg.credential;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ASN1EnumBaseTest {
    @Test
    void testGetInstanceWithASN1EnumBase1() {
        SecurityLevel level = new SecurityLevel(SecurityLevel.Enumerated.level1.getValue());

        SecurityLevel result = ASN1EnumBase.getInstance(level, SecurityLevel.FACTORY);

        Assertions.assertEquals(level, result);
    }

    @Test
    void testGetInstanceWithInteger1() {
        SecurityLevel result = ASN1EnumBase.getInstance(2, SecurityLevel.FACTORY);

        Assertions.assertEquals(2, result.getValue());
    }

    @Test
    void testGetInstanceWithEnumName1() {
        SecurityLevel result = ASN1EnumBase.getInstance("LevEL1", SecurityLevel.FACTORY);

        Assertions.assertEquals(SecurityLevel.Enumerated.level1, result.getEnum());
    }

    @Test
    void testGetInstanceWithUnsupportedType() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> ASN1EnumBase.getInstance(3.14, SecurityLevel.FACTORY));
    }

}