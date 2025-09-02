package json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tcg.credential.AttributeStatus;
import tcg.credential.StrengthOfFunction;

public class ASN1EnumBaseDeserializerTest {
    @Test
    public void testRecoverAttributeStatus1() throws IOException {
        AttributeStatus expected = new AttributeStatus(AttributeStatus.Enumerated.added.getValue());
        String json = "{\"" + AttributeStatus.class.getSimpleName().toUpperCase() + "\": \"" + expected.getEnum().name().toLowerCase() +"\"}";
        
        JsonMapper.Builder mapperBuilder = JsonMapper.builder();
        ObjectMapper mapper = mapperBuilder.build();

        Assertions.assertEquals(expected, mapper.readValue(json, AttributeStatus.class));
    }

    @Test
    public void testRecoverAttributeStatusDifferentEnumBaseKey() throws IOException {
        AttributeStatus expected = new AttributeStatus(AttributeStatus.Enumerated.added.getValue());
        String json = "{\"" + StrengthOfFunction.class.getSimpleName().toUpperCase() + "\": \"" + expected.getEnum().name() +"\"}";

        JsonMapper.Builder mapperBuilder = JsonMapper.builder();
        ObjectMapper mapper = mapperBuilder.build();

        Assertions.assertEquals(expected, mapper.readValue(json, AttributeStatus.class));
    }

    @Test
    public void testRecoverAttributeStatusInvalidValue() throws IOException {
        String json = "{\"" + AttributeStatus.class.getSimpleName() + "\": \"-1\"}";

        JsonMapper.Builder mapperBuilder = JsonMapper.builder();
        ObjectMapper mapper = mapperBuilder.build();

        Assertions.assertThrows(Exception.class, () -> mapper.readValue(json, AttributeStatus.class));
    }

    @Test
    public void testRecoverStrengthOfFunction1() throws IOException {
        StrengthOfFunction expected = new StrengthOfFunction(StrengthOfFunction.Enumerated.high.getValue());
        String json = "{\"" + StrengthOfFunction.class.getSimpleName() + "\": \"" + expected.getEnum().getValue() +"\"}";

        JsonMapper.Builder mapperBuilder = JsonMapper.builder();
        ObjectMapper mapper = mapperBuilder.build();

        Assertions.assertEquals(expected, mapper.readValue(json, StrengthOfFunction.class));
    }
}
