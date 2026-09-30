package paccor.tcg.credential;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests for DEFAULT-field decoding, extra-element tolerance, collection size limits, and duplicate-tag
 * handling in the ASN.1 layer.
 */
class DecodingHardeningTest {

    @Test
    void iso9000Certification_uriWithOmittedDefaultBoolean_isDecoded() {
        ISO9000Certification decoded = ISO9000Certification.fromASN1Sequence(
                new DERSequence(new DERIA5String("https://example.com/iso")));

        Assertions.assertEquals(ASN1Boolean.FALSE, decoded.getIso9000Certified());
        Assertions.assertEquals("https://example.com/iso", decoded.getIso9000Uri().getString());
    }

    @Test
    void iso9000Certification_booleanAndUri_areDecoded() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(ASN1Boolean.TRUE);
        vec.add(new DERIA5String("https://example.com/iso"));

        ISO9000Certification decoded = ISO9000Certification.fromASN1Sequence(new DERSequence(vec));

        Assertions.assertEquals(ASN1Boolean.TRUE, decoded.getIso9000Certified());
        Assertions.assertEquals("https://example.com/iso", decoded.getIso9000Uri().getString());
    }

    @Test
    void tpmSecurityAssertions_omittedVersion_decodesAsV1AndRoundTrips() throws Exception {
        TPMSecurityAssertions decoded = TPMSecurityAssertions.fromASN1Sequence(new DERSequence());

        Assertions.assertEquals(new ASN1Integer(0), decoded.getVersion());
        Assertions.assertArrayEquals(new DERSequence().getEncoded(), decoded.toASN1Primitive().getEncoded());
    }

    @Test
    void parseTaggedElements_rejectsDuplicateTags() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(new DERTaggedObject(false, 0, new DERUTF8String("a")));
        vec.add(new DERTaggedObject(false, 0, new DERUTF8String("b")));

        Assertions.assertThrows(IllegalArgumentException.class, () -> ASN1Utils.parseTaggedElements(new DERSequence(vec)));
    }

    @Test
    void structuralSequence_withElementsFromANewerVersion_isToleratedWithWarning() {
        // Sequences have no upper bound so certificates from a newer minor version still decode.
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(ASN1Boolean.TRUE);
        vec.add(new DERIA5String("https://example.com/iso"));
        vec.add(new DERUTF8String("field from a future version"));

        List<LogRecord> records = captureDefinitionsLog(() -> {
            ISO9000Certification decoded = ISO9000Certification.fromASN1Sequence(new DERSequence(vec));
            Assertions.assertEquals(ASN1Boolean.TRUE, decoded.getIso9000Certified());
            Assertions.assertEquals("https://example.com/iso", decoded.getIso9000Uri().getString());
        });

        Assertions.assertTrue(records.stream().anyMatch(logRecord -> logRecord.getLevel() == Level.WARNING
                && logRecord.getMessage().equals("ISO9000Certification has 3 elements but only 2 are defined. The extra elements were ignored.")),
                records.stream().map(LogRecord::getMessage).toList().toString());
    }

    @Test
    void structuralSequence_withinDefinedSize_logsNothing() {
        List<LogRecord> records = captureDefinitionsLog(() ->
                ISO9000Certification.fromASN1Sequence(new DERSequence(new DERIA5String("https://example.com/iso"))));

        Assertions.assertTrue(records.isEmpty(), records.stream().map(LogRecord::getMessage).toList().toString());
    }

    private static List<LogRecord> captureDefinitionsLog(Runnable action) {
        List<LogRecord> records = new CopyOnWriteArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord logRecord) {
                records.add(logRecord);
            }

            @Override
            public void flush() {}

            @Override
            public void close() {}
        };
        Logger logger = Logger.getLogger(Definitions.class.getName());
        logger.addHandler(handler);
        try {
            action.run();
        } finally {
            logger.removeHandler(handler);
        }
        return records;
    }

    @Test
    void relevantManifests_withTooManyUrls_isRejected() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        DERSequence uri = new DERSequence(new DERIA5String("https://example.com/m"));
        for (int i = 0; i <= Definitions.MAX_COLLECTION_ELEMENTS; i++) {
            vec.add(uri);
        }

        Assertions.assertThrows(IllegalArgumentException.class, () -> TCGRelevantManifests.fromASN1Sequence(new DERSequence(vec)));
    }

    @Test
    void platformConfigurationV3_emptyComponentList_isOmittedAndDecodesToNoComponents() throws Exception {
        PlatformConfigurationV3 empty = PlatformConfigurationV3.builder().build();
        Assertions.assertArrayEquals(new DERSequence().getEncoded(), empty.toASN1Primitive().getEncoded());

        // A non-conforming encoder may still emit [0] SEQUENCE {}; it must not become one empty component.
        PlatformConfigurationV3 decoded = PlatformConfigurationV3.fromASN1Sequence(
                new DERSequence(new DERTaggedObject(false, 0, new DERSequence())));
        Assertions.assertTrue(decoded.getPlatformComponents().isEmpty());
    }

    @Test
    void platformConfigurationV2_emptyLists_areOmitted() throws Exception {
        Assertions.assertArrayEquals(new DERSequence().getEncoded(),
                PlatformConfigurationV2.builder().build().toASN1Primitive().getEncoded());
    }
}
