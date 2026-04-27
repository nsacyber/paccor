package normalization.pci;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for PciIdsRegistry.
 */
class PciIdsRegistryTest {

    private PciIdsRegistry registry;

    @BeforeEach
    void setUp() throws Exception {
        String pciIds = """
            # Comment line
            8086  Intel Corporation
            \t1234  Intel Device One
            \t\t8086 5678  Intel Subsystem One
            \t5678  Intel Device Two
            10de  NVIDIA Corporation
            \t0001  GeForce GTX 1080
            \t\t10de 1111  NVIDIA Subsystem One
            \t0002  GeForce GTX 1070
            1234  Test Vendor Inc
            \t9999  Test Device

            # PCI Device Classes
            C 01  Mass storage controller
            """;
        registry = PciIdsRegistry.parse(
                new ByteArrayInputStream(pciIds.getBytes(StandardCharsets.UTF_8))
        );
    }

    @Test
    void vendorIdFromName_knownVendor_returnsId() {
        Assertions.assertEquals(Optional.of("8086"), registry.vendorIdFromName("Intel Corporation"));
        Assertions.assertEquals(Optional.of("10de"), registry.vendorIdFromName("NVIDIA Corporation"));
        Assertions.assertEquals(Optional.of("1234"), registry.vendorIdFromName("Test Vendor Inc"));
    }

    @Test
    void vendorIdFromName_caseInsensitive_returnsId() {
        Assertions.assertEquals(Optional.of("8086"), registry.vendorIdFromName("INTEL CORPORATION"));
        Assertions.assertEquals(Optional.of("8086"), registry.vendorIdFromName("intel corporation"));
        Assertions.assertEquals(Optional.of("10de"), registry.vendorIdFromName("nvidia corporation"));
    }

    @Test
    void vendorIdFromName_withExtraWhitespace_returnsId() {
        Assertions.assertEquals(Optional.of("8086"), registry.vendorIdFromName("  Intel Corporation  "));
        Assertions.assertEquals(Optional.of("8086"), registry.vendorIdFromName("Intel  Corporation"));
    }

    @Test
    void vendorIdFromName_withNonAlphanumeric_normalizes() {
        // normalizeNameKey removes non-alphanumeric characters
        Assertions.assertEquals(Optional.of("1234"), registry.vendorIdFromName("Test-Vendor-Inc"));
        Assertions.assertEquals(Optional.of("1234"), registry.vendorIdFromName("Test Vendor Inc."));
    }

    @Test
    void vendorIdFromName_unknownVendor_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.vendorIdFromName("Unknown Vendor"));
        Assertions.assertEquals(Optional.empty(), registry.vendorIdFromName("XYZ Corp"));
    }

    @Test
    void vendorIdFromName_null_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.vendorIdFromName(null));
    }

    @Test
    void vendorIdFromName_empty_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.vendorIdFromName(""));
    }

    @Test
    void canonicalVendorId_acceptsHexOrLookupName() {
        Assertions.assertEquals(Optional.of("8086"), registry.canonicalVendorId("8086"));
        Assertions.assertEquals(Optional.of("8086"), registry.canonicalVendorId("Intel Corporation"));
        Assertions.assertEquals(Optional.of("8086"), registry.canonicalVendorId("intel corporation"));
    }

    @Test
    void vendorName_knownVendor_returnsName() {
        Assertions.assertEquals(Optional.of("Intel Corporation"), registry.vendorName("8086"));
        Assertions.assertEquals(Optional.of("NVIDIA Corporation"), registry.vendorName("10de"));
        Assertions.assertEquals(Optional.of("Test Vendor Inc"), registry.vendorName("1234"));
    }

    @Test
    void vendorName_caseInsensitive_returnsName() {
        Assertions.assertEquals(Optional.of("Intel Corporation"), registry.vendorName("8086"));
        Assertions.assertEquals(Optional.of("Intel Corporation"), registry.vendorName("8086"));
    }

    @Test
    void vendorName_unknownVendor_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.vendorName("9999"));
        Assertions.assertEquals(Optional.empty(), registry.vendorName("FFFF"));
    }

    @Test
    void vendorName_null_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.vendorName(null));
    }

    @Test
    void deviceIdFromVendorAndName_knownDevice_returnsId() {
        Assertions.assertEquals(Optional.of("1234"), registry.deviceIdFromVendorAndName("8086", "Intel Device One"));
        Assertions.assertEquals(Optional.of("5678"), registry.deviceIdFromVendorAndName("8086", "Intel Device Two"));
        Assertions.assertEquals(Optional.of("0001"), registry.deviceIdFromVendorAndName("10de", "GeForce GTX 1080"));
    }

    @Test
    void deviceIdFromVendorAndName_caseInsensitive_returnsId() {
        Assertions.assertEquals(Optional.of("1234"), registry.deviceIdFromVendorAndName("8086", "INTEL DEVICE ONE"));
        Assertions.assertEquals(Optional.of("0001"), registry.deviceIdFromVendorAndName("10de", "geforce gtx 1080"));
    }

    @Test
    void deviceIdFromVendorAndName_unknownDevice_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.deviceIdFromVendorAndName("8086", "Unknown Device"));
    }

    @Test
    void deviceIdFromVendorAndName_wrongVendor_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.deviceIdFromVendorAndName("10de", "Intel Device One"));
    }

    @Test
    void deviceIdFromVendorAndName_nullVendor_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.deviceIdFromVendorAndName(null, "Intel Device One"));
    }

    @Test
    void deviceName_knownDevice_returnsName() {
        Assertions.assertEquals(Optional.of("Intel Device One"), registry.deviceName("8086", "1234"));
        Assertions.assertEquals(Optional.of("Intel Device Two"), registry.deviceName("8086", "5678"));
        Assertions.assertEquals(Optional.of("GeForce GTX 1080"), registry.deviceName("10de", "0001"));
    }

    @Test
    void deviceName_caseInsensitiveIds_returnsName() {
        Assertions.assertEquals(Optional.of("Intel Device One"), registry.deviceName("8086", "1234"));
        Assertions.assertEquals(Optional.of("GeForce GTX 1080"), registry.deviceName("10DE", "0001"));
    }

    @Test
    void deviceName_unknownDevice_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.deviceName("8086", "9999"));
    }

    @Test
    void deviceName_unknownVendor_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.deviceName("FFFF", "1234"));
    }

    @Test
    void deviceName_nullIds_returnsEmpty() {
        Assertions.assertEquals(Optional.empty(), registry.deviceName(null, "1234"));
        Assertions.assertEquals(Optional.empty(), registry.deviceName("8086", null));
    }

    @Test
    void subsystemName_knownSubsystem_returnsName() {
        Assertions.assertEquals(
                Optional.of("Intel Subsystem One"),
                registry.subsystemName("8086", "1234", "8086", "5678")
        );
        Assertions.assertEquals(
                Optional.of("NVIDIA Subsystem One"),
                registry.subsystemName("10de", "0001", "10de", "1111")
        );
    }

    @Test
    void subsystemName_caseInsensitive_returnsName() {
        Assertions.assertEquals(
                Optional.of("Intel Subsystem One"),
                registry.subsystemName("8086", "1234", "8086", "5678")
        );
    }

    @Test
    void subsystemName_unknownSubsystem_returnsEmpty() {
        Assertions.assertEquals(
                Optional.empty(),
                registry.subsystemName("8086", "1234", "8086", "9999")
        );
    }

    @Test
    void subsystemName_nullIds_returnsEmpty() {
        Assertions.assertEquals(
                Optional.empty(),
                registry.subsystemName(null, "1234", "8086", "5678")
        );
        Assertions.assertEquals(
                Optional.empty(),
                registry.subsystemName("8086", "1234", "8086", null)
        );
    }

    @Test
    void normalizeNameKey_removesNonAlphanumeric() {
        Assertions.assertEquals("intelcorporation", PciIdsRegistry.normalizeNameKey("Intel Corporation"));
        Assertions.assertEquals("intelcorporation", PciIdsRegistry.normalizeNameKey("Intel-Corporation"));
        Assertions.assertEquals("intelcorporation", PciIdsRegistry.normalizeNameKey("Intel_Corporation"));
        Assertions.assertEquals("test123", PciIdsRegistry.normalizeNameKey("Test 123"));
    }

    @Test
    void normalizeNameKey_lowercase() {
        Assertions.assertEquals("intelcorporation", PciIdsRegistry.normalizeNameKey("INTEL CORPORATION"));
        Assertions.assertEquals("intelcorporation", PciIdsRegistry.normalizeNameKey("Intel Corporation"));
    }

    @Test
    void normalizeNameKey_trimsWhitespace() {
        Assertions.assertEquals("intelcorporation", PciIdsRegistry.normalizeNameKey("  Intel Corporation  "));
    }

    @Test
    void normalizeNameKey_null_returnsEmpty() {
        Assertions.assertEquals("", PciIdsRegistry.normalizeNameKey(null));
    }

    @Test
    void normalizeNameKey_empty_returnsEmpty() {
        Assertions.assertEquals("", PciIdsRegistry.normalizeNameKey(""));
    }

    @Test
    void parse_emptyInput_createsEmptyRegistry() throws Exception {
        String empty = "";
        PciIdsRegistry emptyRegistry = PciIdsRegistry.parse(
                new ByteArrayInputStream(empty.getBytes(StandardCharsets.UTF_8))
        );
        Assertions.assertNotNull(emptyRegistry);
        Assertions.assertEquals(Optional.empty(), emptyRegistry.vendorName("8086"));
    }

    @Test
    void parse_commentsOnly_createsEmptyRegistry() throws Exception {
        String comments = """
            # Comment 1
            # Comment 2
            """;
        PciIdsRegistry commentRegistry = PciIdsRegistry.parse(
                new ByteArrayInputStream(comments.getBytes(StandardCharsets.UTF_8))
        );
        Assertions.assertNotNull(commentRegistry);
        Assertions.assertEquals(Optional.empty(), commentRegistry.vendorName("8086"));
    }

    @Test
    void parse_classSection_skipsClasses() throws Exception {
        String withClasses = """
            8086  Intel Corporation
            10de  NVIDIA Corporation
            C 01  Mass storage controller
            C 02  Network controller
            """;
        PciIdsRegistry classRegistry = PciIdsRegistry.parse(
                new ByteArrayInputStream(withClasses.getBytes(StandardCharsets.UTF_8))
        );

        // Should have vendors before the class section, but everything after "C" is skipped
        Assertions.assertEquals(Optional.of("Intel Corporation"), classRegistry.vendorName("8086"));
        Assertions.assertEquals(Optional.of("NVIDIA Corporation"), classRegistry.vendorName("10de"));
    }

    @Test
    void parse_malformedVendorId_skipsLine() throws Exception {
        String malformed = """
            8086  Intel Corporation
            XXXX  Invalid Vendor
            10de  NVIDIA Corporation
            """;
        PciIdsRegistry malformedRegistry = PciIdsRegistry.parse(
                new ByteArrayInputStream(malformed.getBytes(StandardCharsets.UTF_8))
        );

        Assertions.assertEquals(Optional.of("Intel Corporation"), malformedRegistry.vendorName("8086"));
        Assertions.assertEquals(Optional.of("NVIDIA Corporation"), malformedRegistry.vendorName("10de"));
        Assertions.assertEquals(Optional.empty(), malformedRegistry.vendorName("XXXX"));
    }

    @Test
    void get_defaultRegistry_isNotNull() {
        PciIdsRegistry defaultRegistry = PciIdsRegistry.get();
        Assertions.assertNotNull(defaultRegistry);
    }

    @Test
    void installForTests_customRegistry_usedByGet() throws Exception {
        String custom = """
            AAAA  Custom Vendor
            """;
        PciIdsRegistry customRegistry = PciIdsRegistry.parse(
                new ByteArrayInputStream(custom.getBytes(StandardCharsets.UTF_8))
        );

        PciIdsRegistry.installForTests(customRegistry);
        PciIdsRegistry retrieved = PciIdsRegistry.get();

        Assertions.assertEquals(Optional.of("Custom Vendor"), retrieved.vendorName("aaaa"));

        // Restore default for other tests
        PciIdsRegistry.installForTests(null);
    }

    @Test
    void parse_deviceWithoutVendor_skipsDevice() throws Exception {
        String orphanDevice = """
            \t1234  Orphan Device
            8086  Intel Corporation
            """;
        PciIdsRegistry orphanRegistry = PciIdsRegistry.parse(
                new ByteArrayInputStream(orphanDevice.getBytes(StandardCharsets.UTF_8))
        );

        // Should have the vendor. orphan device should be skipped
        Assertions.assertEquals(Optional.of("Intel Corporation"), orphanRegistry.vendorName("8086"));
    }

    @Test
    void parse_subsystemWithoutDevice_skipsSubsystem() throws Exception {
        String orphanSubsystem = """
            8086  Intel Corporation
            \t\t8086 5678  Orphan Subsystem
            \t1234  Intel Device One
            """;
        PciIdsRegistry orphanSubRegistry = PciIdsRegistry.parse(
                new ByteArrayInputStream(orphanSubsystem.getBytes(StandardCharsets.UTF_8))
        );

        // Should have vendor and device
        Assertions.assertEquals(Optional.of("Intel Corporation"), orphanSubRegistry.vendorName("8086"));
        Assertions.assertEquals(Optional.of("Intel Device One"), orphanSubRegistry.deviceName("8086", "1234"));
    }
}
