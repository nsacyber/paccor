package json;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

public class JsonUtilsTest {

    @Test
    void testGetCaseInsensitiveAndHas() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("KeyOne", 1);
        node.put("keyTwo", 2);

        Optional<JsonNode> found1 = JsonUtils.get(node, false, "keyone");
        Optional<JsonNode> found2 = JsonUtils.get(node, false, "KEYTWO");
        Optional<JsonNode> notFound = JsonUtils.get(node, true, "keyone");

        Assertions.assertTrue(found1.isPresent());
        Assertions.assertEquals(1, found1.get().asInt());
        Assertions.assertTrue(found2.isPresent());
        Assertions.assertEquals(2, found2.get().asInt());
        Assertions.assertTrue(notFound.isEmpty());

        Assertions.assertTrue(JsonUtils.has(node, false, "KEYONE"));
        Assertions.assertTrue(JsonUtils.has(node, true, "KeyOne"));
        Assertions.assertFalse(JsonUtils.has(node, true, "KEYONE"));
        Assertions.assertFalse(JsonUtils.has(node, false, "missing", "keytwo"));
    }

    @Test
    void testAsStreamIteratorAndSpliterator() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("a", 1);
        node.put("b", 2);
        node.put("c", 3);

        List<String> fields = JsonUtils.asStream(node.fieldNames()).toList();
        Assertions.assertTrue(fields.contains("a") && fields.contains("b") && fields.contains("c"));

        List<JsonNode> values = JsonUtils.asStream(node.spliterator()).toList();
        Assertions.assertEquals(3, values.size());
    }

    @Test
    void testIsIntegerAndIsHex() {
        Assertions.assertTrue(JsonUtils.isInteger("123"));
        Assertions.assertFalse(JsonUtils.isInteger("12a"));

        Assertions.assertTrue(JsonUtils.isHex("0x1f"));
        Assertions.assertTrue(JsonUtils.isHex("1Fh"));
        Assertions.assertTrue(JsonUtils.isHex("ABCDEF"));
        Assertions.assertFalse(JsonUtils.isHex("0x"));
        Assertions.assertFalse(JsonUtils.isHex("g1"));
    }

    @Test
    void testFirstNonNull() {
        TextNode a = null;
        TextNode b = TextNode.valueOf("b");
        TextNode c = TextNode.valueOf("c");
        Optional<JsonNode> res = JsonUtils.firstNonNull(a, null, b, c);
        Assertions.assertTrue(res.isPresent());
        Assertions.assertEquals("b", res.get().asText());

        Optional<JsonNode> empty = JsonUtils.firstNonNull();
        Assertions.assertTrue(empty.isEmpty());
    }

    @Test
    void testParseHex() {
        Assertions.assertEquals(0x1f, JsonUtils.parseHex("0x1f"));
        Assertions.assertEquals(0x1f, JsonUtils.parseHex("1fh"));
        Assertions.assertEquals(0xABC, JsonUtils.parseHex("ABC"));
        Assertions.assertEquals(0, JsonUtils.parseHex(null));
        Assertions.assertEquals(0, JsonUtils.parseHex(""));
    }

    @Test
    void testParseHexOrInt() {
        Assertions.assertEquals(Optional.of(31), JsonUtils.parseHexOrInt("0x1f"));
        Assertions.assertEquals(Optional.of(31), JsonUtils.parseHexOrInt("1fh"));
        Assertions.assertEquals(Optional.of(0x123), JsonUtils.parseHexOrInt("123")); // interpreted as hex per HEX_PATTERN
        Assertions.assertEquals(Optional.of(0x123), JsonUtils.parseHexOrInt("123h")); // hex due to 'h' suffix
        Assertions.assertTrue(JsonUtils.parseHexOrInt("zzz").isEmpty());
        Assertions.assertTrue(JsonUtils.parseHexOrInt(null).isEmpty());
    }

    @Test
    void testNormalizeAndRemoveSeparators() {
        Assertions.assertEquals("hello", JsonUtils.normalize("  HeLLo  "));
        Assertions.assertEquals("abcd", JsonUtils.removeSeparators("a_b-c d"));
        Assertions.assertEquals("abc", JsonUtils.removeSeparators("a-b c"));
    }
}
