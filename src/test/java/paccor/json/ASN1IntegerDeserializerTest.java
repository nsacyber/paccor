package paccor.json;

import java.io.IOException;
import org.bouncycastle.asn1.ASN1Integer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.json.ObjectMapperFactory;
import tools.jackson.databind.ObjectMapper;

public class ASN1IntegerDeserializerTest {

    public static final String intJson_1 = """
                0
    """;
    public static final String intJson_2 = """
               "0x123456"
    """;
    public static final String intJson_3 = """
                "123456"
    """;



    @Test
    public void testDeserializeInt1() throws IOException {
        ASN1Integer expected = new ASN1Integer(0);

        ObjectMapper mapper = ObjectMapperFactory.create();

        Assertions.assertEquals(expected, mapper.readValue(intJson_1, ASN1Integer.class));
    }

    @Test
    public void testDeserializeInt2() throws IOException {
        ASN1Integer expected = new ASN1Integer(Integer.valueOf("123456", 16));

        ObjectMapper mapper = ObjectMapperFactory.create();

        Assertions.assertEquals(expected, mapper.readValue(intJson_2, ASN1Integer.class));
    }

    @Test
    public void testDeserializeInt3() throws IOException {
        ASN1Integer expected = new ASN1Integer(123456);

        ObjectMapper mapper = ObjectMapperFactory.create();

        Assertions.assertEquals(expected, mapper.readValue(intJson_3, ASN1Integer.class));
    }
}
