package paccor.normalization.pci;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paccor.normalization.pci.PciFieldTranslator;
import paccor.normalization.pci.PciIdsRegistry;
import paccor.tcg.credential.TCGObjectIdentifier;

/**
 * Tests for PciFieldTranslator.
 */
class PciFieldTranslatorTest {

    private PciFieldTranslator translator;
    private static final ASN1ObjectIdentifier PCIE_REGISTRY =
            TCGObjectIdentifier.tcgRegistryComponentClassPcie;
    private static final ASN1ObjectIdentifier NON_PCIE_REGISTRY =
            TCGObjectIdentifier.tcgRegistry;

    @BeforeEach
    void setUp() {
        // Set up the test PCI registry with known data
        String pciIds = """
            8086  Intel Corporation
            \t1234  Test Device One
            \t\t8086 5678  Test Subsystem One
            10de  NVIDIA Corporation
            \t0001  GeForce Card
            \t\t10de 1111  GeForce Subsystem
            1234  Test Vendor
            """;
        try {
            PciIdsRegistry testRegistry = PciIdsRegistry.parse(
                new ByteArrayInputStream(pciIds.getBytes(StandardCharsets.UTF_8))
            );
            translator = new PciFieldTranslator(testRegistry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test PCI registry", e);
        }
    }

    @Test
    void supports_pcieRegistry_returnsTrue() {
        Assertions.assertTrue(translator.supports(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY
        ));
    }

    @Test
    void supports_nonPcieRegistry_returnsFalse() {
        Assertions.assertFalse(translator.supports(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                NON_PCIE_REGISTRY
        ));
    }

    // ===== Manufacturer Field Tests =====

    @Test
    void manufacturer_simpleVendorId_normalizes() {
        DERUTF8String input = new DERUTF8String("8086");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("8086::", result.getString());
    }

    @Test
    void manufacturer_vendorIdMixedCase_normalizes() {
        DERUTF8String input = new DERUTF8String("8086");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("8086::", result.getString());
    }

    @Test
    void manufacturer_vendorName_translates() {
        DERUTF8String input = new DERUTF8String("Intel Corporation");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("8086::", result.getString());
    }

    @Test
    void manufacturer_compoundField_normalizes() {
        DERUTF8String input = new DERUTF8String("8086:10DE:VPD_NAME");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("8086:10de:VPD_NAME", result.getString());
    }

    @Test
    void manufacturer_compoundWithVendorName_translates() {
        DERUTF8String input = new DERUTF8String("Intel Corporation:10DE:VPD_NAME");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("8086:10de:VPD_NAME", result.getString());
    }

    @Test
    void manufacturer_vpdPreservedAsIs() {
        DERUTF8String input = new DERUTF8String("8086:10DE:MixedCaseVPD");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("8086:10de:MixedCaseVPD", result.getString());
    }

    @Test
    void manufacturer_onlyTwoParts_normalizesWithEmptyVpd() {
        DERUTF8String input = new DERUTF8String("8086:10DE");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("8086:10de:", result.getString());
    }

    @Test
    void manufacturer_emptyString_normalizesToEmptyParts() {
        DERUTF8String input = new DERUTF8String("");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertSame(input, result); // Empty returns original
    }

    @Test
    void manufacturer_unknownVendorName_preservedAsIs() {
        DERUTF8String input = new DERUTF8String("Unknown Vendor Corp");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("Unknown Vendor Corp::", result.getString());
    }

    // ===== Model Field Tests =====

    @Test
    void model_simpleDeviceId_normalizes() {
        DERUTF8String input = new DERUTF8String("1234");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentModel,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("1234::", result.getString());
    }

    @Test
    void model_compoundField_normalizes() {
        DERUTF8String input = new DERUTF8String("1234:5678:VPD_PN");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentModel,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("1234:5678:VPD_PN", result.getString());
    }

    @Test
    void model_vpdPreservedAsIs() {
        DERUTF8String input = new DERUTF8String("1234:5678:MixedCasePartNumber");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentModel,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("1234:5678:MixedCasePartNumber", result.getString());
    }

    @Test
    void model_hexCaseNormalized() {
        DERUTF8String input = new DERUTF8String("ABCD:EFGH:VPD");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentModel,
                PCIE_REGISTRY,
                input
        );
        // Non-hex value becomes 0000
        Assertions.assertEquals("abcd:0000:VPD", result.getString());
    }

    // ===== Serial Field Tests =====

    @Test
    void serial_simpleSerialNumber_normalizes() {
        DERUTF8String input = new DERUTF8String("1234567890ABCDEF");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentSerial,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("1234567890abcdef:", result.getString());
    }

    @Test
    void serial_shortSerial_zeroPadded() {
        DERUTF8String input = new DERUTF8String("ABCD");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentSerial,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("000000000000abcd:", result.getString());
    }

    @Test
    void serial_compoundField_normalizes() {
        DERUTF8String input = new DERUTF8String("1234567890ABCDEF:VPD_SN_12345");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentSerial,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("1234567890abcdef:VPD_SN_12345", result.getString());
    }

    @Test
    void serial_vpdPreservedAsIs() {
        DERUTF8String input = new DERUTF8String("1234567890ABCDEF:MixedCaseSN");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentSerial,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("1234567890abcdef:MixedCaseSN", result.getString());
    }

    // ===== Revision Field Tests =====

    @Test
    void revision_simpleHex_normalizes() {
        DERUTF8String input = new DERUTF8String("AB");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentRevision,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("00ab", result.getString());
    }

    @Test
    void revision_mixedCase_normalizes() {
        DERUTF8String input = new DERUTF8String("AbCd");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentRevision,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("abcd", result.getString());
    }

    @Test
    void revision_shortHex_zeroPadded() {
        DERUTF8String input = new DERUTF8String("1");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentRevision,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertEquals("0001", result.getString());
    }

    // ===== Edge Cases =====

    @Test
    void translate_nonUTF8String_returnsOriginal() {
        DERIA5String input = new DERIA5String("test");
        DERIA5String result = (DERIA5String) translator.translate(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertSame(input, result);
    }

    @Test
    void translate_unsupportedCategory_returnsOriginal() {
        DERUTF8String input = new DERUTF8String("test");
        ASN1ObjectIdentifier unsupportedCategory = new ASN1ObjectIdentifier("1.2.3.4.5");
        DERUTF8String result = (DERUTF8String) translator.translate(
                null,
                unsupportedCategory,
                PCIE_REGISTRY,
                input
        );
        Assertions.assertSame(input, result);
    }

    @Test
    void constructor_defaultRegistry() {
        PciFieldTranslator defaultTranslator = new PciFieldTranslator();
        Assertions.assertNotNull(defaultTranslator);
        Assertions.assertTrue(defaultTranslator.supports(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY
        ));
    }

    @Test
    void constructor_nullRegistry_usesDefault() {
        PciFieldTranslator nullRegistryTranslator = new PciFieldTranslator(null);
        Assertions.assertNotNull(nullRegistryTranslator);
        Assertions.assertTrue(nullRegistryTranslator.supports(
                null,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                PCIE_REGISTRY
        ));
    }
}
