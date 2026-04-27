package validator;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import normalization.pci.PciIdsRegistry;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tcg.credential.ComponentIdentifierV2;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.TraitMap;
import tcg.credential.UTF8StringTrait;

/**
 * Tests for the normalized component matcher with string synonyms and PCIe field translation.
 */
public class NormalizedComponentMatcherTest {
    private final ComponentMatcher matcher = ComponentMatcher.NORMALIZED;

    @BeforeEach
    public void setupPciRegistry() {
        // Install a minimal test pci.ids database
        String pciIds = """
            8086  Intel Corporation
            \t1234  Test Device
            \t\t8086 5678  Test Subsystem
            10de  NVIDIA Corporation
            \t0001  GeForce Card
            """;
        try {
            PciIdsRegistry testRegistry = PciIdsRegistry.parse(
                new ByteArrayInputStream(pciIds.getBytes(StandardCharsets.UTF_8))
            );
            PciIdsRegistry.installForTests(testRegistry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test PCI registry", e);
        }
    }

    @SuppressWarnings("all") // Intentional same object check
    @Test
    void usesInstanceSingleton() {
        Assertions.assertNotNull(ComponentMatcher.NORMALIZED);
        Assertions.assertSame(ComponentMatcher.NORMALIZED, ComponentMatcher.NORMALIZED);
    }

    @Test
    void matchesComponentsWithStringSynonyms() {
        // "unknown" and "n/a" should match after synonym normalization
        ComponentIdentifierV2 comp1 = RawComponentMatcherTest.createTestComponentV2("unknown", "model1");
        ComponentIdentifierV2 comp2 = RawComponentMatcherTest.createTestComponentV2("n/a", "model1");

        boolean result = matcher.matchV2(List.of(comp1), List.of(comp2));
        Assertions.assertTrue(result, "String synonyms should normalize to match");
    }

    @Test
    void matchesEmptyWithUnknown() {
        ComponentIdentifierV2 comp1 = RawComponentMatcherTest.createTestComponentV2("", "model1");
        ComponentIdentifierV2 comp2 = RawComponentMatcherTest.createTestComponentV2("unknown", "model1");

        boolean result = matcher.matchV2(List.of(comp1), List.of(comp2));
        Assertions.assertTrue(result, "Empty string and 'unknown' should match");
    }

    @Test
    void preservesCaseForNonSynonyms() {
        // Non-synonym values should preserve case and NOT match if case differs
        ComponentIdentifierV2 comp1 = RawComponentMatcherTest.createTestComponentV2("Intel", "model1");
        ComponentIdentifierV2 comp2 = RawComponentMatcherTest.createTestComponentV2("intel", "model1");

        boolean result = matcher.matchV2(List.of(comp1), List.of(comp2));
        Assertions.assertFalse(result, "Non-synonyms should preserve case sensitivity");
    }

    @Test
    void supportsSubsetMatching() {
        // Actual can have MORE components than expected (Option B)
        ComponentIdentifierV2 comp1 = RawComponentMatcherTest.createTestComponentV2("vendor1", "model1");
        ComponentIdentifierV2 comp2 = RawComponentMatcherTest.createTestComponentV2("vendor2", "model2");
        ComponentIdentifierV2 comp3 = RawComponentMatcherTest.createTestComponentV2("vendor3", "model3");

        boolean result = matcher.matchV2(
            List.of(comp1, comp2),  // Expected: 2 components
            List.of(comp1, comp2, comp3)  // Actual: 3 components
        );
        Assertions.assertTrue(result, "Actual can have more components than expected");
    }

    @Test
    void rejectsWhenExpectedNotInActual() {
        ComponentIdentifierV2 comp1 = RawComponentMatcherTest.createTestComponentV2("vendor1", "model1");
        ComponentIdentifierV2 comp2 = RawComponentMatcherTest.createTestComponentV2("vendor2", "model2");
        ComponentIdentifierV2 comp3 = RawComponentMatcherTest.createTestComponentV2("vendor3", "model3");

        boolean result = matcher.matchV2(
            List.of(comp1, comp2),  // Expected
            List.of(comp1, comp3)   // Actual missing comp2
        );
        Assertions.assertFalse(result, "Should reject when expected component not in actual");
    }

    @Test
    void v3TraitMapMatching() {
        // Test V3 matching with trait sequences
        TraitMap expected = TraitMap.builder()
            .trait(UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                .traitValue(new DERUTF8String("unknown"))
                .build())
            .build();

        TraitMap actual = TraitMap.builder()
            .trait(UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                .traitValue(new DERUTF8String("n/a"))
                .build())
            .build();

        boolean result = matcher.matchV3(List.of(expected), List.of(actual));
        Assertions.assertTrue(result, "V3 matching should apply string synonym normalization");
    }

    @Test
    void pciFieldsAreNormalized() {
        // PCIe fields with hex IDs should be case-insensitive.
        // Create traits with PCIe registry to trigger PciFieldTranslator
        TraitMap expected = TraitMap.builder()
            .trait(UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                .traitRegistry(TCGObjectIdentifier.tcgRegistryComponentClassPcie)
                .traitValue(new DERUTF8String("8086"))  // lowercase
                .build())
            .build();

        TraitMap actual = TraitMap.builder()
            .trait(UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                .traitRegistry(TCGObjectIdentifier.tcgRegistryComponentClassPcie)
                .traitValue(new DERUTF8String("Intel Corporation"))
                .build())
            .build();

        boolean result = matcher.matchV3(List.of(expected), List.of(actual));
        Assertions.assertTrue(result, "PCIe hex fields should normalize case");
    }

    @Test
    void pciFieldsWithTypo() {
        // PCIe fields with hex IDs should be case-insensitive.
        // Create traits with PCIe registry to trigger PciFieldTranslator
        TraitMap expected = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistryComponentClassPcie)
                        .traitValue(new DERUTF8String("8086"))  // lowercase
                        .build())
                .build();

        TraitMap actual = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistryComponentClassPcie)
                        .traitValue(new DERUTF8String("Intel Corporations"))
                        .build())
                .build();

        boolean result = matcher.matchV3(List.of(expected), List.of(actual));
        Assertions.assertFalse(result, "PCIe hex fields should normalize case");
    }

    @Test
    void emptyComponentLists() {
        boolean result = matcher.matchV2(List.of(), List.of());
        Assertions.assertTrue(result, "Empty lists should match");
    }

    @Test
    void expectedEmptyActualNonEmpty() {
        ComponentIdentifierV2 comp = RawComponentMatcherTest.createTestComponentV2("vendor", "model");
        boolean result = matcher.matchV2(List.of(), List.of(comp));
        Assertions.assertTrue(result, "Empty expected should match any actual");
    }
}
