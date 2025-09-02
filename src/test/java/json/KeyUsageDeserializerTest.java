package json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;

public class KeyUsageDeserializerTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(KeyUsage.class, new KeyUsageDeserializer());
        mapper.registerModule(module);
    }

    private KeyUsage read(String json) throws IOException {
        return mapper.readValue(json, KeyUsage.class);
    }

    @Test
    void testDeserializeFromInteger() throws IOException {
        KeyUsage ku = read("5");
        Assertions.assertNotNull(ku);
        Assertions.assertEquals(new KeyUsage(5), ku);
        Assertions.assertEquals(new KeyUsage(KeyUsage.encipherOnly | KeyUsage.keyCertSign), ku);
        Assertions.assertNotEquals(new KeyUsage(KeyUsage.keyCertSign), ku);
    }

    @Test
    void testDeserializeFromHexString() throws IOException {
        KeyUsage ku = read("\"0x03\"");
        Assertions.assertNotNull(ku);
    }

    @Test
    void testDeserializeFromName() throws IOException {
        KeyUsage ku = read("\"digitalSignature\"");
        Assertions.assertNotNull(ku);
        Assertions.assertEquals(new KeyUsage(KeyUsage.digitalSignature), ku);
        Assertions.assertNotEquals(new KeyUsage(KeyUsage.keyCertSign), ku);
    }

    @Test
    void testDeserializeFromArrayMixed() throws IOException {
        KeyUsage ku = read("[\"digitalSignature\", \"0x02\", 4]");
        Assertions.assertNotNull(ku);
        Assertions.assertEquals(new KeyUsage(4 | 0x02 | KeyUsage.digitalSignature), ku);
        Assertions.assertNotEquals(new KeyUsage(KeyUsage.digitalSignature), ku);
    }

    @Test
    void testUnexpectedFormatThrows() {
        Assertions.assertThrows(IOException.class, () -> read("{}"));
    }
}
