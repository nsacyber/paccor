package tcg.credential;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ASN1UtilsTest {
    @Test
    public void testToASN1EncodableVectorNullInput() {
        ASN1EncodableVector result = ASN1Utils.toASN1EncodableVector(null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testToASN1EncodableVectorEmptyList() {
        List<ASN1Object> input = List.of();

        ASN1EncodableVector result = ASN1Utils.toASN1EncodableVector(input);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testToASN1EncodableVectorValidInput() throws IOException {
        ASN1Object obj1 = new DEROctetString(FIPSLevelTraitTest.FIPS_1_SEC_LEVEL.getEncoded());
        ASN1Object obj2 = new DEROctetString(ASN1ObjectTraitTest.ASN1_DATE.getEncoded());
        TraitSequence obj3 = TraitSequenceTest.sampleTraitSequence1();
        List<ASN1Object> input = List.of(obj1, obj2, obj3);

        ASN1EncodableVector result = ASN1Utils.toASN1EncodableVector(input);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(input.size(), result.size());
        Assertions.assertEquals(obj1, result.get(0));
        Assertions.assertEquals(obj2, result.get(1));
        Assertions.assertEquals(obj3, result.get(2));
    }

    @Test
    public void testParseTaggedElementsNullInput() {
        Map<Integer, ASN1Object> result = ASN1Utils.parseTaggedElements(null);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testParseTaggedElementsEmptySequence() {
        ASN1Sequence emptySeq = new DERSequence();
        Map<Integer, ASN1Object> result = ASN1Utils.parseTaggedElements(emptySeq);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testParseTaggedElementsTrait1() {
        Trait<?, ?> fips = FIPSLevelTraitTest.sampleFIPSLevelTrait1(); // This function returns a trait with a description and no descriptionURI.
        Map<Integer, ASN1Object> result = ASN1Utils.parseTaggedElements((ASN1Sequence) fips.toASN1Primitive());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.containsKey(0));
        Assertions.assertEquals(fips.getDescription(), result.get(0));
    }

    @Test
    public void testParseTaggedElementsTbbSecurityAssertions1() {
        TBBSecurityAssertions tbb = TBBSecurityAssertions.builder()
                .rtmType(new MeasurementRootType(MeasurementRootType.Enumerated.nonHost.getValue()))
                .build();
        Map<Integer, ASN1Object> result = ASN1Utils.parseTaggedElements((ASN1Sequence) tbb.toASN1Primitive());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.containsKey(2));
        Assertions.assertEquals(tbb.getRtmType(), result.get(2));
    }

    @Test
    public void testListUntaggedElementsNullInput() {
        List<ASN1Object> result = ASN1Utils.listUntaggedElements(null);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testListUntaggedElementsEmptySequence() {
        ASN1Sequence emptySeq = new DERSequence();
        List<ASN1Object> result = ASN1Utils.listUntaggedElements(emptySeq);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testListUntaggedElementsTrait1() {
        Trait<?, ?> fips = FIPSLevelTraitTest.sampleFIPSLevelTrait1(); // All untagged elements required.
        List<ASN1Object> result = ASN1Utils.listUntaggedElements((ASN1Sequence)fips.toASN1Primitive());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(4, result.size());
        Assertions.assertEquals(fips.getTraitId(), result.get(0));
        Assertions.assertEquals(fips.getTraitCategory(), result.get(1));
        Assertions.assertEquals(fips.getTraitRegistry(), result.get(2));
        Assertions.assertEquals(fips.getTraitValue(), result.get(3));
    }

    @Test
    public void testListUntaggedElementsTbbSecurityAssertions1() {
        TBBSecurityAssertions tbb = TBBSecurityAssertions.builder()
                .rtmType(new MeasurementRootType(MeasurementRootType.Enumerated.nonHost.getValue()))
                .build();
        List<ASN1Object> result = ASN1Utils.listUntaggedElements((ASN1Sequence)tbb.toASN1Primitive());

        Assertions.assertNotNull(result);
        Assertions.assertEquals(0, result.size());
    }

    @Test
    public void testSafeGetDefaultElementFromNullSequence() {
        FIPSLevel defaultAnswer = FIPSLevelTraitTest.FIPS_1;
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetDefaultElementFromSequence(null, 0, defaultAnswer, instanceMethod);

        Assertions.assertEquals(defaultAnswer, result);
    }

    @Test
    public void testSafeGetDefaultElementFromEmptySequence() {
        ASN1Sequence emptySeq = new DERSequence();
        FIPSLevel defaultAnswer = FIPSLevelTraitTest.FIPS_1;
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetDefaultElementFromSequence(emptySeq, 0, defaultAnswer, instanceMethod);

        Assertions.assertEquals(defaultAnswer, result);
    }

    @Test
    public void testSafeGetDefaultElementFromSequenceConversionFail() {
        ASN1Object obj = ASN1ObjectTraitTest.ASN1_DATE;
        FIPSLevel defaultAnswer = FIPSLevelTraitTest.FIPS_1;
        ASN1Sequence seq = new DERSequence(obj);
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetDefaultElementFromSequence(seq, 0, defaultAnswer, instanceMethod);

        Assertions.assertEquals(defaultAnswer, result);
    }

    @Test
    public void testSafeGetDefaultElementFromSequence1() {
        FIPSLevel fips = FIPSLevelTraitTest.FIPS_1;
        FIPSLevel defaultAnswer = null;
        ASN1Sequence seq = new DERSequence(fips);
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetDefaultElementFromSequence(seq, 0, defaultAnswer, instanceMethod);

        Assertions.assertEquals(fips, result);
    }

    @Test
    public void testSafeGetDefaultElementFromSequenceBadIndex() {
        FIPSLevel fips = FIPSLevelTraitTest.FIPS_1;
        FIPSLevel defaultAnswer = null;
        ASN1Sequence seq = new DERSequence(fips);
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetDefaultElementFromSequence(seq, 1, defaultAnswer, instanceMethod);

        Assertions.assertNull(result);
    }

    @Test
    public void testSafeGetDefaultElementNullObject() {
        FIPSLevel defaultAnswer = FIPSLevelTraitTest.FIPS_1;
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetDefaultElement(null, defaultAnswer, instanceMethod);

        Assertions.assertEquals(defaultAnswer, result);
    }

    @Test
    public void testSafeGetDefaultElementConversionFail() {
        ASN1Object obj = ASN1ObjectTraitTest.ASN1_DATE;
        FIPSLevel defaultAnswer = FIPSLevelTraitTest.FIPS_1;
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        ASN1Object result = ASN1Utils.safeGetDefaultElement(obj, defaultAnswer, instanceMethod);

        Assertions.assertEquals(defaultAnswer, result);
    }

    @Test
    public void testSafeGetDefaultElement1() {
        FIPSLevel fips = FIPSLevelTraitTest.FIPS_1;
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetDefaultElement(fips.toASN1Primitive(), null, instanceMethod);

        Assertions.assertEquals(fips, result);
    }

    @Test
    public void testSafeGetFirstInstanceFromNullRange() {
        FIPSLevel defaultFips = FIPSLevelTraitTest.FIPS_1;
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(null, 0, 1, defaultFips, instanceMethod);

        Assertions.assertEquals(defaultFips, result);
    }

    @Test
    public void testSafeGetFirstInstanceFromEmptyRange() {
        FIPSLevel defaultFips = FIPSLevelTraitTest.FIPS_1;
        ASN1Sequence seq = new DERSequence();
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 100, 1000, defaultFips, instanceMethod);

        Assertions.assertEquals(defaultFips, result);
    }

    @Test
    public void testSafeGetFirstInstanceFromBadRange() {
        FIPSLevel defaultFips = FIPSLevelTraitTest.FIPS_1;
        ASN1Sequence seq = new DERSequence(defaultFips);
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 100, 1000, defaultFips, instanceMethod);

        Assertions.assertEquals(defaultFips, result);
    }

    @Test
    public void testSafeGetFirstInstanceFromRange1() {
        SecurityLevel secLevel = FIPSLevelTraitTest.FIPS_1_SEC_LEVEL;
        ASN1GeneralizedTime time = ASN1ObjectTraitTest.ASN1_DATE;
        FIPSLevel fips1 = FIPSLevelTraitTest.FIPS_1;
        ASN1Sequence seq = new DERSequence(new ASN1Object[]{secLevel, fips1, time});
        Function<Object, SecurityLevel> instanceMethod = SecurityLevel::getInstance;

        SecurityLevel result = ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 0, 1, null, instanceMethod);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(secLevel, result);
    }

    @Test
    public void testSafeGetFirstInstanceFromRange2() {
        SecurityLevel secLevel = FIPSLevelTraitTest.FIPS_1_SEC_LEVEL;
        ASN1GeneralizedTime time = ASN1ObjectTraitTest.ASN1_DATE;
        FIPSLevel fips1 = FIPSLevelTraitTest.FIPS_1;
        ASN1Sequence seq = new DERSequence(new ASN1Object[]{secLevel, fips1, time});
        Function<Object, FIPSLevel> instanceMethod = FIPSLevel::getInstance;

        FIPSLevel result = ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 0, 1, null, instanceMethod);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(fips1, result);
    }

    @Test
    public void testSafeGetFirstInstanceFromRange3() {
        SecurityLevel secLevel = FIPSLevelTraitTest.FIPS_1_SEC_LEVEL;
        ASN1GeneralizedTime time = ASN1ObjectTraitTest.ASN1_DATE;
        FIPSLevel fips1 = FIPSLevelTraitTest.FIPS_1;
        ASN1Sequence seq = new DERSequence(new ASN1Object[]{secLevel, fips1, time});
        Function<Object, ASN1GeneralizedTime> instanceMethod = ASN1GeneralizedTime::getInstance;

        ASN1GeneralizedTime result = ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 0, 2, null, instanceMethod);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(time, result);
    }

    @Test
    public void testMapTraitSequence() {
        TraitSequence traitSeq = TraitSequenceTest.sampleTraitSequence1();
        FIPSLevelTrait trait1 = FIPSLevelTraitTest.sampleFIPSLevelTrait1();
        ASN1ObjectTrait trait2 = ASN1ObjectTraitTest.sampleASN1ObjectTrait1();

        Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> expectedMap = new HashMap<>();
        expectedMap.put(FIPSLevelTrait.class, List.of(trait1));
        expectedMap.put(ASN1ObjectTrait.class, List.of(trait2));

        List<Function<Object, ? extends Trait<?, ?>>> expectedConversionMethods = List.of(FIPSLevelTrait::getInstance, ASN1ObjectTrait::getInstance);

        Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> actualMap = ASN1Utils.mapTraitSequence(traitSeq, expectedConversionMethods);

        Assertions.assertNotNull(expectedMap);
        Assertions.assertNotNull(actualMap);
        Assertions.assertEquals(expectedMap.size(), actualMap.size());
        expectedMap.forEach((key, value) -> {
            Assertions.assertTrue(actualMap.containsKey(key));
            List<Trait<?, ?>> actualList = actualMap.get(key);
            Assertions.assertEquals(value.size(), actualList.size());
            Assertions.assertTrue(
                    IntStream.range(0, value.size())
                            .allMatch(i -> value.get(i).equals(actualList.get(i)))
            );
        });
    }

    @Test
    public void testTryConvertTraitNullParams() {
        Optional<Trait<?, ?>> result = ASN1Utils.tryConvertTrait(null, (List<Function<Object, ? extends Trait<?, ?>>>) null);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testTryConvertTraitNullTrait() {
        List<Function<Object, ? extends Trait<?, ?>>> conversionMethods = List.of(FIPSLevelTrait::getInstance);
        Optional<Trait<?, ?>> result = ASN1Utils.tryConvertTrait(null, conversionMethods);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testTryConvertTraitNullMethods() {
        Trait<?, ?> inputTrait = FIPSLevelTraitTest.sampleFIPSLevelTrait1();
        Optional<Trait<?, ?>> result = ASN1Utils.tryConvertTrait(inputTrait, (List<Function<Object, ? extends Trait<?, ?>>>) null);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testTryConvertTrait1() {
        Trait<?, ?> inputTrait = FIPSLevelTraitTest.sampleFIPSLevelTrait1();
        List<Function<Object, ? extends Trait<?, ?>>> conversionMethods = List.of(
                FIPSLevelTrait::getInstance,
                ASN1ObjectTrait::getInstance
        );

        Optional<Trait<?, ?>> result = ASN1Utils.tryConvertTrait(inputTrait, conversionMethods);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(inputTrait, result.get());
    }

    @Test
    public void testTryConvertTraitConversionFail() {
        Trait<?, ?> inputTrait = FIPSLevelTraitTest.sampleFIPSLevelTrait1();
        ASN1ObjectTrait copy = ASN1ObjectTrait.builder().cloneTraitDescriptors(inputTrait).traitValue(inputTrait.getTraitValue()).build();
        List<Function<Object, ? extends Trait<?, ?>>> conversionMethods = List.of(
                obj -> null // Purposely failing method
        );

        Optional<Trait<?, ?>> result = ASN1Utils.tryConvertTrait(inputTrait, conversionMethods);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(copy, result.get());
    }

    @Test
    public void testTryConvertTrait2() {
        Trait<?, ?> inputTrait = FIPSLevelTraitTest.sampleFIPSLevelTrait1();
        Function<Object, ? extends Trait<?, ?>> conversionMethod = FIPSLevelTrait::getInstance;

        Optional<Trait<?, ?>> result = ASN1Utils.tryConvertTrait(inputTrait, conversionMethod);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(inputTrait, result.get());
    }

    @Test
    public void testTryConvertTrait3() {
        FIPSLevelTrait inputTrait = FIPSLevelTraitTest.sampleFIPSLevelTrait1();
        Function<Object, FIPSLevelTrait> conversionMethod = FIPSLevelTrait::getInstance;

        Optional<Trait<?, ?>> result = ASN1Utils.tryConvertTrait(inputTrait, conversionMethod);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(inputTrait, result.get());
    }

    @Test
    public void testTryConvertTraitFail1() {
        Trait<?, ?> inputTrait = FIPSLevelTraitTest.sampleFIPSLevelTrait1();

        Optional<Trait<?, ?>> result = ASN1Utils.tryConvertTrait(inputTrait, (Function<Object, ? extends Trait<?, ?>>) null);

        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    public void testResizeOctetsNullArray() {
        int sequenceSize = 4;
        byte[] result = ASN1Utils.resizeOctets(sequenceSize, (byte[]) null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(sequenceSize, result.length);
        Assertions.assertArrayEquals(new byte[]{0, 0, 0, 0}, result);
    }

    @Test
    public void testResizeOctetsEmptyArray() {
        int sequenceSize = 4;
        byte[] result = ASN1Utils.resizeOctets(sequenceSize, new byte[0]);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(sequenceSize, result.length);
        Assertions.assertArrayEquals(new byte[]{0, 0, 0, 0}, result);
    }

    @Test
    public void testResizeOctetsSmallerArray() {
        int sequenceSize = 4;
        byte[] input = new byte[]{1, 2};
        byte[] result = ASN1Utils.resizeOctets(sequenceSize, input);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(sequenceSize, result.length);
        Assertions.assertArrayEquals(new byte[]{0, 0, 1, 2}, result);
    }

    @Test
    public void testResizeOctets1() {
        int sequenceSize = 4;
        byte[] input = new byte[]{1, 2, 3, 4};
        byte[] result = ASN1Utils.resizeOctets(sequenceSize, input);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(sequenceSize, result.length);
        Assertions.assertArrayEquals(input, result);
    }

    @Test
    public void testResizeOctetsBiggerArray() {
        int sequenceSize = 4;
        byte[] input = new byte[]{1, 2, 3, 4, 5, 6};
        byte[] result = ASN1Utils.resizeOctets(sequenceSize, input);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(sequenceSize, result.length);
        Assertions.assertArrayEquals(new byte[]{3, 4, 5, 6}, result);
    }

    @Test
    public void testResizeOctetsNullOctets() {
        int sequenceSize = 4;
        ASN1OctetString result = ASN1Utils.resizeOctets(sequenceSize, (ASN1OctetString) null);

        Assertions.assertNotNull(result);
        Assertions.assertArrayEquals(new byte[]{0, 0, 0, 0}, result.getOctets());
    }

    @Test
    public void testResizeOctets2() {
        int sequenceSize = 4;
        ASN1OctetString input = new DEROctetString(new byte[]{1, 2, 3, 4});
        ASN1OctetString result = ASN1Utils.resizeOctets(sequenceSize, input);

        Assertions.assertNotNull(result);
        Assertions.assertArrayEquals(input.getOctets(), result.getOctets());
    }

    @Test
    public void testResizeOctetsHex() {
        int sequenceSize = 4;
        String hexValue = "0A1B2C";
        ASN1OctetString result = ASN1Utils.resizeOctets(sequenceSize, hexValue);

        Assertions.assertNotNull(result);
        Assertions.assertArrayEquals(new byte[]{0, 10, 27, 44}, result.getOctets());
    }
}
