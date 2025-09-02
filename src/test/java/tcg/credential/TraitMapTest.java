package tcg.credential;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TraitMapTest {
    public static final TraitMap sampleTraitMap1() {
        return TraitMap.builder()
                .trait(FIPSLevelTraitTest.sampleFIPSLevelTrait1())
                .trait(ASN1ObjectTraitTest.sampleASN1ObjectTrait1())
                .build();
    }

    public static final TraitMap sampleTraitMapAllTypes() {
        return TraitMap.builder()
                .trait(BooleanTraitTest.sampleBooleanTrait1())
                .trait(CertificateIdentifierTraitTest.sampleCertificateIdentifierTrait1())
                .trait(CommonCriteriaTraitTest.sampleCommonCriteriaTrait1())
                .trait(ComponentClassTraitTest.sampleComponentClassTrait1())
                .trait(ComponentIdentifierV11TraitTest.sampleComponentIdentifierV11Trait1())
                .trait(CountryOfOriginTraitTest.sampleCountryOfOriginTrait1())
                .trait(EntityGeoLocationTraitTest.sampleEntityGeoLocationTrait1())
                .trait(FIPSLevelTraitTest.sampleFIPSLevelTrait1())
                .trait(IA5StringTraitTest.sampleIA5StringTrait1())
                .trait(ISO9000TraitTest.sampleISO9000Trait1())
                .trait(NetworkMACTraitTest.sampleNetworkMACTrait1())
                .trait(OIDTraitTest.sampleOIDTrait1())
                .trait(PEMCertStringTraitTest.samplePEMCertStringTrait1())
                .trait(PENTraitTest.samplePENTrait1())
                .trait(PlatformFirmwareCapabilitiesTraitTest.samplePlatformFirmwareCapabilitiesTrait1())
                .trait(PlatformFirmwareSignatureVerificationTraitTest.samplePlatformFirmwareSignatureVerificationTrait1())
                .trait(PlatformFirmwareUpdateComplianceTraitTest.samplePlatformFirmwareUpdateComplianceTrait1())
                .trait(PlatformHardwareCapabilitiesTraitTest.samplePlatformHardwareCapabilitiesTrait1())
                .trait(PublicKeyTraitTest.samplePublicKeyTrait1())
                .trait(RTMTraitTest.sampleRTMTrait1())
                .trait(StatusTraitTest.sampleStatusTrait1())
                .trait(URITraitTest.sampleURITrait1())
                .trait(UTF8StringTraitTest.sampleUTF8StringTrait1())
                .build();
    }

    @Test
    public void testBuildSampleTraitMap1() {
        TraitMap traitSeq = sampleTraitMap1();
        Trait<?, ?> trait1 = FIPSLevelTraitTest.sampleFIPSLevelTrait1();
        Trait<?, ?> trait2 = ASN1ObjectTraitTest.sampleASN1ObjectTrait1();
        Assertions.assertNotNull(traitSeq);
        Assertions.assertNotNull(trait1);
        Assertions.assertNotNull(trait2);
        Assertions.assertEquals(2, traitSeq.getTraits().size());
        Assertions.assertTrue(traitSeq.getTraits().containsKey(FIPSLevelTrait.class));
        Assertions.assertTrue(traitSeq.getTraits().containsKey(ASN1ObjectTrait.class));
        Assertions.assertEquals(1, traitSeq.getTraits().get(FIPSLevelTrait.class).size());
        Assertions.assertEquals(1, traitSeq.getTraits().get(ASN1ObjectTrait.class).size());
        Assertions.assertEquals(trait1, traitSeq.getTraits().get(FIPSLevelTrait.class).getFirst());
        Assertions.assertEquals(trait2, traitSeq.getTraits().get(ASN1ObjectTrait.class).getFirst());
    }

    @Test
    public void testBuildSampleTraitMapAllTypes() {
        TraitMap traitSeq = sampleTraitMapAllTypes();
        Assertions.assertNotNull(traitSeq);
        Assertions.assertEquals(23, traitSeq.size());
    }

    @Test
    public void testFromASN1Sequence() {
        TraitMap traitSeq = sampleTraitMap1();
        FIPSLevelTrait trait1 = FIPSLevelTraitTest.sampleFIPSLevelTrait1();
        ASN1ObjectTrait trait2 = ASN1ObjectTraitTest.sampleASN1ObjectTrait1();
        ASN1Sequence seq = new DERSequence(ASN1Utils.toASN1EncodableVector(List.of(trait1.toASN1Primitive(), trait2.toASN1Primitive())));

        TraitMap traitSeq2 = TraitMap.fromASN1Sequence(seq);
        Assertions.assertNotNull(traitSeq2);
        Assertions.assertEquals(traitSeq, traitSeq2);
    }

    @Test
    public void testRoundTripASN1() {
        TraitMap original = sampleTraitMap1();
        ASN1Sequence asn1 = (ASN1Sequence) original.toASN1Primitive();
        TraitMap restored = TraitMap.getInstance(asn1);

        Assertions.assertEquals(original, restored);
        Assertions.assertEquals(original.size(), restored.size());
    }

    @Test
    public void testPutCreatesNewList() {
        TraitMap traitSeq = TraitMap.builder().trait(FIPSLevelTraitTest.sampleFIPSLevelTrait1()).build();
        List<Trait<?, ?>> uriTraits = Collections.singletonList(URITraitTest.sampleURITrait1());
        traitSeq.put(URITrait.class, uriTraits);

        Assertions.assertTrue(traitSeq.containsKey(URITrait.class));
        Assertions.assertEquals(1, traitSeq.get(URITrait.class).size());
    }

    @Test
    public void testPutAppends() {
        TraitMap traitSeq = TraitMap.builder().trait(FIPSLevelTraitTest.sampleFIPSLevelTrait1()).build();
        FIPSLevelTrait fips2 = FIPSLevelTraitTest.sampleFIPSLevelTrait2();
        traitSeq.put(FIPSLevelTrait.class, Collections.singletonList(fips2));

        List<FIPSLevelTrait> fipsTraits = traitSeq.get(FIPSLevelTrait.class);
        Assertions.assertEquals(2, fipsTraits.size());
    }

    @Test
    public void testPutAll() {
        TraitMap traitSeq = TraitMap.builder().trait(FIPSLevelTraitTest.sampleFIPSLevelTrait1()).build();
        Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> newTraits = new HashMap<>();
        newTraits.put(URITrait.class, Collections.singletonList(URITraitTest.sampleURITrait1()));
        newTraits.put(StatusTrait.class, Collections.singletonList(StatusTraitTest.sampleStatusTrait1()));

        traitSeq.putAll(newTraits);

        Assertions.assertEquals(3, traitSeq.size());
        Assertions.assertTrue(traitSeq.containsKey(URITrait.class));
        Assertions.assertTrue(traitSeq.containsKey(StatusTrait.class));
    }

    @Test
    public void testPutAllAppends() {
        TraitMap traitSeq = TraitMap.builder().trait(FIPSLevelTraitTest.sampleFIPSLevelTrait1()).build();
        FIPSLevelTrait fips2 = FIPSLevelTraitTest.sampleFIPSLevelTrait2();
        Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> newTraits = new HashMap<>();
        newTraits.put(FIPSLevelTrait.class, Collections.singletonList(fips2));

        traitSeq.putAll(newTraits);

        Assertions.assertEquals(2, traitSeq.size());
        Assertions.assertEquals(2, traitSeq.get(FIPSLevelTrait.class).size());
    }

    @Test
    public void testPutNullValue() {
        TraitMap traitSeq = TraitMap.builder().trait(FIPSLevelTraitTest.sampleFIPSLevelTrait1()).build();
        int originalSize = traitSeq.get(FIPSLevelTrait.class).size();
        traitSeq.put(FIPSLevelTrait.class, null);

        Assertions.assertEquals(originalSize, traitSeq.get(FIPSLevelTrait.class).size());
    }

    @Test
    public void testBooleanTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(BooleanTraitTest.sampleBooleanTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(BooleanTrait.class));
        Assertions.assertEquals(1, seq.get(BooleanTrait.class).size());
    }

    @Test
    public void testCertificateIdentifierTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(CertificateIdentifierTraitTest.sampleCertificateIdentifierTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(CertificateIdentifierTrait.class));
    }

    @Test
    public void testCommonCriteriaTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(CommonCriteriaTraitTest.sampleCommonCriteriaTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(CommonCriteriaTrait.class));
    }

    @Test
    public void testComponentClassTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(ComponentClassTraitTest.sampleComponentClassTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(ComponentClassTrait.class));
    }

    @Test
    public void testComponentIdentifierV11Trait() {
        TraitMap seq = TraitMap.builder()
                .trait(ComponentIdentifierV11TraitTest.sampleComponentIdentifierV11Trait1())
                .build();
        Assertions.assertTrue(seq.containsKey(ComponentIdentifierV11Trait.class));
    }

    @Test
    public void testCountryOfOriginTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(CountryOfOriginTraitTest.sampleCountryOfOriginTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(CountryOfOriginTrait.class));
    }

    @Test
    public void testEntityGeoLocationTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(EntityGeoLocationTraitTest.sampleEntityGeoLocationTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(EntityGeoLocationTrait.class));
    }

    @Test
    public void testFIPSLevelTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(FIPSLevelTraitTest.sampleFIPSLevelTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(FIPSLevelTrait.class));
    }

    @Test
    public void testIA5StringTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(IA5StringTraitTest.sampleIA5StringTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(IA5StringTrait.class));
    }

    @Test
    public void testISO9000Trait() {
        TraitMap seq = TraitMap.builder()
                .trait(ISO9000TraitTest.sampleISO9000Trait1())
                .build();
        Assertions.assertTrue(seq.containsKey(ISO9000Trait.class));
    }

    @Test
    public void testNetworkMACTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(NetworkMACTraitTest.sampleNetworkMACTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(NetworkMACTrait.class));
    }

    @Test
    public void testOIDTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(OIDTraitTest.sampleOIDTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(OIDTrait.class));
    }

    @Test
    public void testPEMCertStringTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(PEMCertStringTraitTest.samplePEMCertStringTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(PEMCertStringTrait.class));
    }

    @Test
    public void testPENTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(PENTraitTest.samplePENTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(PENTrait.class));
    }

    @Test
    public void testPlatformFirmwareCapabilitiesTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(PlatformFirmwareCapabilitiesTraitTest.samplePlatformFirmwareCapabilitiesTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(PlatformFirmwareCapabilitiesTrait.class));
    }

    @Test
    public void testPlatformFirmwareSignatureVerificationTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(PlatformFirmwareSignatureVerificationTraitTest.samplePlatformFirmwareSignatureVerificationTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(PlatformFirmwareSignatureVerificationTrait.class));
    }

    @Test
    public void testPlatformFirmwareUpdateComplianceTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(PlatformFirmwareUpdateComplianceTraitTest.samplePlatformFirmwareUpdateComplianceTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(PlatformFirmwareUpdateComplianceTrait.class));
    }

    @Test
    public void testPlatformHardwareCapabilitiesTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(PlatformHardwareCapabilitiesTraitTest.samplePlatformHardwareCapabilitiesTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(PlatformHardwareCapabilitiesTrait.class));
    }

    @Test
    public void testPublicKeyTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(PublicKeyTraitTest.samplePublicKeyTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(PublicKeyTrait.class));
    }

    @Test
    public void testRTMTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(RTMTraitTest.sampleRTMTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(RTMTrait.class));
    }

    @Test
    public void testStatusTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(StatusTraitTest.sampleStatusTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(StatusTrait.class));
    }

    @Test
    public void testURITrait() {
        TraitMap seq = TraitMap.builder()
                .trait(URITraitTest.sampleURITrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(URITrait.class));
    }

    @Test
    public void testUTF8StringTrait() {
        TraitMap seq = TraitMap.builder()
                .trait(UTF8StringTraitTest.sampleUTF8StringTrait1())
                .build();
        Assertions.assertTrue(seq.containsKey(UTF8StringTrait.class));
    }

    // Empty/null checks

    @Test
    @SuppressWarnings("all") // testing multiple empty methods
    public void testEmptySequence1() {
        TraitMap empty = TraitMap.builder().build();
        Assertions.assertTrue(empty.isEmpty());
        Assertions.assertEquals(0, empty.size());
        Assertions.assertEquals(0, empty.keySet().size());
        Assertions.assertEquals(0, empty.values().size());
        Assertions.assertEquals(0, empty.entrySet().size());
    }

    @Test
    @SuppressWarnings("all") // testing multiple null methods
    public void testGetInstanceNull() {
        TraitMap seq = TraitMap.getInstance(null);
        Assertions.assertNull(seq);
    }

    @Test
    public void testGetInstanceTraitMap() {
        TraitMap original = sampleTraitMap1();
        TraitMap same = TraitMap.getInstance(original);
        Assertions.assertSame(original, same);
    }

    @Test
    public void testMultipleTraitsOfSameType() {
        FIPSLevelTrait fips1 = FIPSLevelTraitTest.sampleFIPSLevelTrait1();
        FIPSLevelTrait fips2 = FIPSLevelTraitTest.sampleFIPSLevelTrait2();

        TraitMap seq = TraitMap.builder()
                .trait(fips1)
                .trait(fips2)
                .build();

        Assertions.assertEquals(2, seq.size());
        Assertions.assertEquals(2, seq.get(FIPSLevelTrait.class).size());
        Assertions.assertTrue(seq.get(FIPSLevelTrait.class).contains(fips1));
        Assertions.assertTrue(seq.get(FIPSLevelTrait.class).contains(fips2));
    }

    @Test
    public void testToBuilder() {
        TraitMap original = sampleTraitMap1();
        TraitMap copy = original.toBuilder().build();
        Assertions.assertEquals(original, copy);
    }

    // Other Map methods

    @Test
    public void testRemove() {
        TraitMap traitSeq = sampleTraitMap1();
        Assertions.assertTrue(traitSeq.containsKey(FIPSLevelTrait.class));
        List<Trait<?, ?>> removed = traitSeq.remove(FIPSLevelTrait.class);

        Assertions.assertNotNull(removed);
        Assertions.assertFalse(traitSeq.containsKey(FIPSLevelTrait.class));
        Assertions.assertEquals(1, traitSeq.size());
    }

    @Test
    public void testRemoveNonExistent() {
        TraitMap traitSeq = sampleTraitMap1();
        List<Trait<?, ?>> removed = traitSeq.remove(URITrait.class);
        Assertions.assertNull(removed);
    }

    @Test
    @SuppressWarnings("all") // testing multiple empty methods
    public void testClear() {
        TraitMap traitSeq = sampleTraitMap1();
        Assertions.assertFalse(traitSeq.isEmpty());
        traitSeq.clear();
        Assertions.assertTrue(traitSeq.isEmpty());
        Assertions.assertEquals(0, traitSeq.size());
    }

    @Test
    public void testSize() {
        TraitMap traitSeq = sampleTraitMap1();
        Assertions.assertEquals(2, traitSeq.size());
    }

    @Test
    public void testIsNotEmpty() {
        TraitMap traitSeq = sampleTraitMap1();
        Assertions.assertFalse(traitSeq.isEmpty());
    }

    @Test
    public void testContainsKey() {
        TraitMap traitSeq = sampleTraitMap1();
        Assertions.assertTrue(traitSeq.containsKey(FIPSLevelTrait.class));
        Assertions.assertTrue(traitSeq.containsKey(ASN1ObjectTrait.class));
        Assertions.assertFalse(traitSeq.containsKey(URITrait.class));
    }

    @Test
    public void testContainsValue() {
        TraitMap traitSeq = sampleTraitMap1();
        List<FIPSLevelTrait> fipsTraits = traitSeq.get(FIPSLevelTrait.class);
        Assertions.assertTrue(traitSeq.containsValue(fipsTraits));

        List<Trait<?, ?>> nonExistent = Collections.singletonList(URITraitTest.sampleURITrait1());
        Assertions.assertFalse(traitSeq.containsValue(nonExistent));
    }

    @Test
    public void testGet() {
        TraitMap traitSeq = sampleTraitMap1();
        List<FIPSLevelTrait> fipsTraits = traitSeq.get(FIPSLevelTrait.class);
        Assertions.assertNotNull(fipsTraits);
        Assertions.assertEquals(1, fipsTraits.size());

        List<URITrait> nonExistent = traitSeq.get(URITrait.class);
        Assertions.assertNotNull(nonExistent);
        Assertions.assertEquals(0, nonExistent.size());
    }

    @Test
    public void testKeySet() {
        TraitMap traitSeq = sampleTraitMap1();
        Set<Class<? extends Trait<?, ?>>> keys = traitSeq.keySet();
        Assertions.assertEquals(2, keys.size());
        Assertions.assertTrue(keys.contains(FIPSLevelTrait.class));
        Assertions.assertTrue(keys.contains(ASN1ObjectTrait.class));
    }

    @Test
    public void testValues() {
        TraitMap traitSeq = sampleTraitMap1();
        Collection<List<Trait<?, ?>>> values = traitSeq.values();
        Assertions.assertEquals(2, values.size());
        Assertions.assertTrue(values.stream().noneMatch(List::isEmpty));
    }

    @Test
    public void testEntrySet() {
        TraitMap traitSeq = sampleTraitMap1();
        Set<Map.Entry<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>>> entries = traitSeq.entrySet();
        Assertions.assertEquals(2, entries.size());

        entries.forEach(entry -> {
            Assertions.assertNotNull(entry.getKey());
            Assertions.assertNotNull(entry.getValue());
            Assertions.assertFalse(entry.getValue().isEmpty());
        });
    }
}
