package paccor.json;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.json.JsonUtils;
import paccor.normalization.HexNormalizer;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

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

        List<String> fields = JsonUtils.asStream(node.propertyNames()).toList();
        Assertions.assertTrue(fields.contains("a") && fields.contains("b") && fields.contains("c"));

        List<JsonNode> values = JsonUtils.asStream(node.spliterator()).toList();
        Assertions.assertEquals(3, values.size());
    }

    @Test
    void testIsIntegerAndIsHex() {
        Assertions.assertTrue(HexNormalizer.isInteger("123"));
        Assertions.assertFalse(HexNormalizer.isInteger("12a"));

        Assertions.assertTrue(HexNormalizer.isHexString("0x1f"));
        Assertions.assertTrue(HexNormalizer.isHexString("1Fh"));
        Assertions.assertTrue(HexNormalizer.isHexString("ABCDEF"));
        Assertions.assertFalse(HexNormalizer.isHexString("0x"));
        Assertions.assertFalse(HexNormalizer.isHexString("g1"));
    }

    @Test
    void testFirstNonNull() {
        StringNode a = null;
        StringNode b = StringNode.valueOf("b");
        StringNode c = StringNode.valueOf("c");
        Optional<JsonNode> res = JsonUtils.firstNonNull(a, null, b, c);
        Assertions.assertTrue(res.isPresent());
        Assertions.assertEquals("b", res.get().asString());

        Optional<JsonNode> empty = JsonUtils.firstNonNull();
        Assertions.assertTrue(empty.isEmpty());
    }

    @Test
    void testParseHex() {
        Assertions.assertEquals(Optional.of(0x1f), HexNormalizer.parseHex("0x1f"));
        Assertions.assertEquals(Optional.of(0x1f), HexNormalizer.parseHex("1fh"));
        Assertions.assertEquals(Optional.of(0xABC), HexNormalizer.parseHex("ABC"));
        Assertions.assertTrue(HexNormalizer.parseHex(null).isEmpty());
        Assertions.assertTrue(HexNormalizer.parseHex("").isEmpty());
    }

    @Test
    void testParseHexOrInt() {
        Assertions.assertEquals(Optional.of(31), HexNormalizer.parseHexOrInt("0x1f"));
        Assertions.assertEquals(Optional.of(31), HexNormalizer.parseHexOrInt("1fh"));
        Assertions.assertEquals(Optional.of(0x123), HexNormalizer.parseHexOrInt("123")); // interpreted as hex per HEX_PATTERN
        Assertions.assertEquals(Optional.of(0x123), HexNormalizer.parseHexOrInt("123h")); // hex due to 'h' suffix
        Assertions.assertTrue(HexNormalizer.parseHexOrInt("zzz").isEmpty());
        Assertions.assertTrue(HexNormalizer.parseHexOrInt(null).isEmpty());
    }

    @Test
    void testNormalizeAndRemoveSeparators() {
        Assertions.assertEquals("hello", HexNormalizer.normalize("  HeLLo  "));
        Assertions.assertEquals("abcd", HexNormalizer.removeSeparators("a_b-c d"));
        Assertions.assertEquals("abc", HexNormalizer.removeSeparators("a-b c"));
    }
}
