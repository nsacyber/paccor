package paccor.normalization;

import java.util.Set;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paccor.normalization.StringSynonymTranslator;
import paccor.tcg.credential.TCGObjectIdentifier;

/**
 * Tests for StringSynonymTranslator.
 */
class StringSynonymTranslatorTest {

    private StringSynonymTranslator translator;
    private static final ASN1ObjectIdentifier CATEGORY_MANUFACTURER =
            TCGObjectIdentifier.tcgTrCatComponentManufacturer;
    private static final ASN1ObjectIdentifier CATEGORY_MODEL =
            TCGObjectIdentifier.tcgTrCatComponentModel;
    private static final ASN1ObjectIdentifier CATEGORY_UNSUPPORTED =
            new ASN1ObjectIdentifier("1.2.3.4.5");

    @BeforeEach
    void setUp() {
        translator = new StringSynonymTranslator(Set.of(CATEGORY_MANUFACTURER, CATEGORY_MODEL));
    }

    @Test
    void supports_supportedCategory_returnsTrue() {
        Assertions.assertTrue(translator.supports(null, CATEGORY_MANUFACTURER, null));
        Assertions.assertTrue(translator.supports(null, CATEGORY_MODEL, null));
    }

    @Test
    void supports_unsupportedCategory_returnsFalse() {
        Assertions.assertFalse(translator.supports(null, CATEGORY_UNSUPPORTED, null));
    }

    @Test
    void translate_unknownSynonym_normalizesToEmpty() {
        DERUTF8String input = new DERUTF8String("unknown");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("", result.getString());
    }

    @Test
    void translate_unknownMixedCase_normalizesToEmpty() {
        DERUTF8String input = new DERUTF8String("Unknown");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("", result.getString());

        input = new DERUTF8String("UNKNOWN");
        result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("", result.getString());
    }

    @Test
    void translate_naSynonym_normalizesToEmpty() {
        DERUTF8String input = new DERUTF8String("n/a");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("", result.getString());
    }

    @Test
    void translate_naMixedCase_normalizesToEmpty() {
        DERUTF8String input = new DERUTF8String("N/A");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("", result.getString());
    }

    @Test
    void translate_emptyString_normalizesToEmpty() {
        DERUTF8String input = new DERUTF8String("");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("", result.getString());
    }

    @Test
    void translate_whitespaceOnly_normalizesToEmpty() {
        DERUTF8String input = new DERUTF8String("   ");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("", result.getString());

        input = new DERUTF8String("\t\n");
        result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("", result.getString());
    }

    @Test
    void translate_unknownWithWhitespace_normalizesToEmpty() {
        DERUTF8String input = new DERUTF8String("  unknown  ");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("", result.getString());
    }

    @Test
    void translate_nonSynonymValue_preservesOriginal() {
        DERUTF8String input = new DERUTF8String("Intel Corporation");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("Intel Corporation", result.getString());
        Assertions.assertSame(input, result); // Should return same object
    }

    @Test
    void translate_nonSynonymPreservesCase() {
        DERUTF8String input = new DERUTF8String("MixedCaseValue");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("MixedCaseValue", result.getString());
    }

    @Test
    void translate_partialSynonymMatch_preservesOriginal() {
        // "unknowns" is not a synonym (only exact "unknown" is)
        DERUTF8String input = new DERUTF8String("unknowns");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("unknowns", result.getString());

        // "not available" is not a synonym (only "n/a" is)
        input = new DERUTF8String("not available");
        result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("not available", result.getString());
    }

    @Test
    void translate_nullValue_returnsEmpty() {
        // DERUTF8String with null string results in empty string
        DERUTF8String input = new DERUTF8String("");
        DERUTF8String result = (DERUTF8String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertEquals("", result.getString());
    }

    @Test
    void translate_nonUTF8String_returnsOriginal() {
        DERIA5String input = new DERIA5String("test");
        DERIA5String result = (DERIA5String) translator.translate(null, CATEGORY_MANUFACTURER, null, input);
        Assertions.assertSame(input, result);
    }

    @Test
    void constructor_copiesSet() {
        Set<ASN1ObjectIdentifier> original = Set.of(CATEGORY_MANUFACTURER);
        StringSynonymTranslator t = new StringSynonymTranslator(original);

        // Verify translator works
        Assertions.assertTrue(t.supports(null, CATEGORY_MANUFACTURER, null));
    }
}
