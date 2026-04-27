package normalization.pci;

import java.util.Optional;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERUTF8String;
import normalization.HexNormalizer;
import normalization.TraitValueTranslator;
import tcg.credential.TCGObjectIdentifier;

/**
 * Translator for PCIe component fields with compound colon-delimited format.
 * Activates when traitRegistry == tcgRegistryComponentClassPcie.
 * Field formats per TCG PCIe Component Class Registry v1 r18:
 * - Manufacturer: VendorID:SubsysVendorID:VPD_MN
 * - Model: DeviceID:SubsysID:VPD_PN
 * - Serial: SerialNumber:VPD_SN
 * - Revision: RevisionID
 * Hex IDs are normalized (lowercase, zero-padded) and translated via pci.ids.
 * VPD strings are preserved as-is (case-sensitive, no synonym normalization).
 * This translator serves as a template for implementing other registry-specific translators
 * (SMBIOS, enterprise-numbers, etc.).
 */
public final class PciFieldTranslator implements TraitValueTranslator {

    private final PciIdsRegistry registry;

    public PciFieldTranslator() {
        this.registry = PciIdsRegistry.get();
    }

    /**
     * Constructor for testing with a custom registry.
     */
    public PciFieldTranslator(PciIdsRegistry registry) {
        this.registry = registry != null ? registry : PciIdsRegistry.get();
    }

    @Override
    public boolean supports(ASN1ObjectIdentifier traitId,
                           ASN1ObjectIdentifier traitCategory,
                           ASN1ObjectIdentifier traitRegistry) {
        // Activate only for PCIe component class registry
        return TCGObjectIdentifier.tcgRegistryComponentClassPcie.equals(traitRegistry);
    }

    @Override
    public ASN1Object translate(ASN1ObjectIdentifier traitId,
                               ASN1ObjectIdentifier traitCategory,
                               ASN1ObjectIdentifier traitRegistry,
                               ASN1Object rawValue) {
        if (!(rawValue instanceof ASN1UTF8String utf8)) {
            return rawValue;
        }

        String input = utf8.getString();
        if (input.isEmpty()) {
            return rawValue;
        }

        String normalized;

        // Manufacturer: VendorID:SubsysVendorID:VPD_MN
        if (TCGObjectIdentifier.tcgTrCatComponentManufacturer.equals(traitCategory)) {
            normalized = normalizeManufacturerField(input);
        }
        // Model: DeviceID:SubsysID:VPD_PN
        else if (TCGObjectIdentifier.tcgTrCatComponentModel.equals(traitCategory)) {
            normalized = normalizeModelField(input);
        }
        // Serial: SerialNumber:VPD_SN
        else if (TCGObjectIdentifier.tcgTrCatComponentSerial.equals(traitCategory)) {
            normalized = normalizeSerialField(input);
        }
        // Revision: RevisionID (2 bytes hex)
        else if (TCGObjectIdentifier.tcgTrCatComponentRevision.equals(traitCategory)) {
            normalized = HexNormalizer.normalize(input, 2);
        }
        else {
            // Not a PCIe field we handle
            return rawValue;
        }

        return new DERUTF8String(normalized);
    }

    /**
     * Normalize manufacturer field: VendorID:SubsysVendorID:VPD_MN
     * Format: <pre>{@code 2 byte hex : 2 byte hex : string}</pre>
     *
     * Hex IDs are normalized and optionally translated via pci.ids.
     * VPD string (MN = Manufacturer Name) is preserved as-is.
     */
    private String normalizeManufacturerField(String input) {
        String[] parts = input.split(":", 3);

        String vendorId = "";
        String subsysVendorId = "";
        String vpdMN = "";

        if (parts.length > 0) {
            // translateVendorId handles both hex IDs and vendor names
            vendorId = translateVendorId(parts[0].trim());
        }

        if (parts.length > 1) {
            // Subsystem vendor IDs are also vendor IDs
            subsysVendorId = translateVendorId(parts[1].trim());
        }

        if (parts.length > 2) {
            vpdMN = parts[2]; // VPD preserved as-is (no trim, no case change)
        }

        return vendorId + ":" + subsysVendorId + ":" + vpdMN;
    }

    /**
     * Normalize model field: DeviceID:SubsysID:VPD_PN
     * Format: <pre>{@code 2 byte hex : 2 byte hex : string}</pre>
     *
     * Hex IDs are normalized. Device IDs are context-dependent on vendor,
     * so we normalize but don't translate without vendor context.
     * VPD string (PN = Part Number) is preserved as-is.
     */
    private String normalizeModelField(String input) {
        String[] parts = input.split(":", 3);

        String deviceId = "";
        String subsysId = "";
        String vpdPN = "";

        if (parts.length > 0) {
            // Device ID translation requires vendor context
            // Keep as normalized hex
            deviceId = HexNormalizer.normalize(parts[0].trim(), 2);
        }

        if (parts.length > 1) {
            // Subsystem ID translation requires vendor+device context
            // Keep as normalized hex
            subsysId = HexNormalizer.normalize(parts[1].trim(), 2);
        }

        if (parts.length > 2) {
            vpdPN = parts[2]; // VPD preserved as-is
        }

        return deviceId + ":" + subsysId + ":" + vpdPN;
    }

    /**
     * Normalize serial field: SerialNumber:VPD_SN
     * Format: <pre>{@code 8 byte hex : string}</pre>
     *
     * Serial number is hex, VPD string (SN = Serial Number) is preserved as-is.
     */
    private String normalizeSerialField(String input) {
        String[] parts = input.split(":", 2);

        String serialNum = "";
        String vpdSN = "";

        if (parts.length > 0) {
            serialNum = HexNormalizer.normalize(parts[0].trim(), 8);
        }

        if (parts.length > 1) {
            vpdSN = parts[1]; // VPD preserved as-is
        }

        return serialNum + ":" + vpdSN;
    }

    /**
     * Translate vendor ID or vendor name to canonical hex ID via pci.ids.
     * If input is already a valid hex ID, this returns normalized form.
     * If input is a vendor name, looks up the ID.
     * If no match is found, returns normalized hex or input as-is.
     */
    private String translateVendorId(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        Optional<String> vendorId = registry.canonicalVendorId(input);
        if (vendorId.isPresent()) {
            return vendorId.get();
        }

        // No match found - try normalizing as hex anyway
        // (handles malformed hex that HexNormalizer can fix)
        String normalized = HexNormalizer.normalize(input, 2);
        if (!normalized.equals("0000")) {
            return normalized;
        }

        // Complete failure, return input as-is
        return input;
    }
}
