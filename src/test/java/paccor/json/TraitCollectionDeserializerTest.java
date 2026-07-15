package paccor.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.tcg.credential.StatusTrait;
import paccor.tcg.credential.TraitCollection;
import paccor.tcg.credential.UTF8StringTrait;
import tools.jackson.databind.ObjectMapper;

public class TraitCollectionDeserializerTest {
    @Test
    public void testDeserializeArray() throws Exception {
        String json = """
            [
                {
                    "status": "ATTRIBUTESTATUS_ADDED"
                },
                {
                    "utf8": "Test String Value"
                }
            ]
            """;
        ObjectMapper mapper = ObjectMapperFactory.create();
        TraitCollection collection = mapper.readValue(json, TraitCollection.class);

        Assertions.assertNotNull(collection);
        Assertions.assertEquals(2, collection.size());
        Assertions.assertTrue(collection.get(0) instanceof StatusTrait);
        Assertions.assertTrue(collection.get(1) instanceof UTF8StringTrait);
    }

    @Test
    public void testDeserializeObject() throws Exception {
        String json = """
            {
                "traits": [
                    {
                        "status": "ATTRIBUTESTATUS_ADDED"
                    }
                ]
            }
            """;
        ObjectMapper mapper = ObjectMapperFactory.create();
        TraitCollection collection = mapper.readValue(json, TraitCollection.class);

        Assertions.assertNotNull(collection);
        Assertions.assertEquals(1, collection.size());
        Assertions.assertTrue(collection.get(0) instanceof StatusTrait);
    }

    @Test
    public void testDeserializeSingleObjectInTraits() throws Exception {
        String json = """
            {
                "traits": {
                    "status": "ATTRIBUTESTATUS_ADDED"
                }
            }
            """;
        ObjectMapper mapper = ObjectMapperFactory.create();
        TraitCollection collection = mapper.readValue(json, TraitCollection.class);

        Assertions.assertNotNull(collection);
        Assertions.assertEquals(1, collection.size());
        Assertions.assertTrue(collection.get(0) instanceof StatusTrait);
    }

    @Test
    public void testDeserializeEmpty() throws Exception {
        String json = "[]";
        ObjectMapper mapper = ObjectMapperFactory.create();
        TraitCollection collection = mapper.readValue(json, TraitCollection.class);

        Assertions.assertNotNull(collection);
        Assertions.assertTrue(collection.isEmpty());
    }

    @Test
    public void testDeserializeNull() throws Exception {
        String json = "null";
        ObjectMapper mapper = ObjectMapperFactory.create();
        TraitCollection collection = mapper.readValue(json, TraitCollection.class);

        Assertions.assertNotNull(collection);
        Assertions.assertTrue(collection.isEmpty());
    }
}
