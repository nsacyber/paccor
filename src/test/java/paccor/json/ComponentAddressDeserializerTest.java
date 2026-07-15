package paccor.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.tcg.credential.ComponentAddress;
import tools.jackson.databind.ObjectMapper;

public class ComponentAddressDeserializerTest {
    @Test
    public void testDeserializeOneElement() throws Exception {
        String json = "{\"ethernetMac\": \"00:11:22:33:44:55\"}";
        ObjectMapper mapper = ObjectMapperFactory.create();
        ComponentAddress address = mapper.readValue(json, ComponentAddress.class);

        Assertions.assertNotNull(address);
        Assertions.assertNotNull(address.getAddressType());
        Assertions.assertNotNull(address.getAddressValue());
        Assertions.assertEquals("001122334455", address.getAddressValue().getString());
    }

    @Test
    public void testDeserializeTwoElements() throws Exception {
        String json = "{\"addressType\": \"ethernetMac\", \"addressValue\": \"00:11:22:33:44:55\"}";
        ObjectMapper mapper = ObjectMapperFactory.create();
        ComponentAddress address = mapper.readValue(json, ComponentAddress.class);

        Assertions.assertNotNull(address);
        Assertions.assertNotNull(address.getAddressType());
        Assertions.assertNotNull(address.getAddressValue());
        Assertions.assertEquals("001122334455", address.getAddressValue().getString());
    }

    @Test
    public void testDeserializeEmpty() throws Exception {
        String json = "{}";
        ObjectMapper mapper = ObjectMapperFactory.create();
        // The current implementation of handleOneElement might fail if there are no elements
        // because it calls iterator().next().
        // But the main deserialize method filters for size == 1 or 2.
        // Size 0 will return null (which might throw JacksonIOException due to orElseThrow).
        
        Assertions.assertThrows(Exception.class, () -> mapper.readValue(json, ComponentAddress.class));
    }
}
