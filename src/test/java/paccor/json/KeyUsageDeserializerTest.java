package paccor.json;

import paccor.exception.JsonException;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paccor.json.ObjectMapperFactory;

public class KeyUsageDeserializerTest {

    @Test
    void testDeserializeFromInteger() throws JsonException {
        KeyUsage ku = ObjectMapperFactory.fromJson("5", KeyUsage.class);
        Assertions.assertNotNull(ku);
        Assertions.assertEquals(new KeyUsage(5), ku);
        Assertions.assertEquals(new KeyUsage(KeyUsage.encipherOnly | KeyUsage.keyCertSign), ku);
        Assertions.assertNotEquals(new KeyUsage(KeyUsage.keyCertSign), ku);
    }

    @Test
    void testDeserializeFromHexString() throws JsonException {
        KeyUsage ku = ObjectMapperFactory.fromJson("\"0x03\"", KeyUsage.class);
        Assertions.assertNotNull(ku);
    }

    @Test
    void testDeserializeFromName() throws JsonException {
        KeyUsage ku = ObjectMapperFactory.fromJson("\"digitalSignature\"", KeyUsage.class);
        Assertions.assertNotNull(ku);
        Assertions.assertEquals(new KeyUsage(KeyUsage.digitalSignature), ku);
        Assertions.assertNotEquals(new KeyUsage(KeyUsage.keyCertSign), ku);
    }

    @Test
    void testDeserializeFromArrayMixed() throws JsonException {
        KeyUsage ku = ObjectMapperFactory.fromJson("[\"digitalSignature\", \"0x02\", 4]", KeyUsage.class);
        Assertions.assertNotNull(ku);
        Assertions.assertEquals(new KeyUsage(4 | 0x02 | KeyUsage.digitalSignature), ku);
        Assertions.assertNotEquals(new KeyUsage(KeyUsage.digitalSignature), ku);
    }

    @Test
    void testUnexpectedFormatThrows() {
        Assertions.assertThrows(JsonException.class, () -> ObjectMapperFactory.fromJson("{}", KeyUsage.class));
    }
}
