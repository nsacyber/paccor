package json;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tcg.credential.PlatformConfigurationV3;

public class PlatformConfigurationV3DeserializationTest {
    @Test
    public void testDeserializationFromAdvJson() throws Exception {
        File jsonFile = new File("src/test/resources/sample_testgen1/localhost-componentlistv3adv.json");
        Assertions.assertTrue(jsonFile.exists(), "Test file should exist");
        
        PlatformConfigurationV3 pcv3 = ObjectMapperFactory.fromJson(jsonFile, PlatformConfigurationV3.class);
        
        Assertions.assertNotNull(pcv3);
        Assertions.assertNotNull(pcv3.getPlatformComponents());
        // Based on localhost-componentlistv3adv.json, there are 4 outer arrays in "COMPONENTS"
        Assertions.assertEquals(4, pcv3.getPlatformComponents().size(), "Should have 4 components");
        
        // Let's check the 4th component, which should have 5 traits
        Assertions.assertEquals(5, pcv3.getPlatformComponents().get(3).size(), "4th component should have 5 traits");
    }
}
