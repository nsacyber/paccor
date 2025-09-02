package tcg.credential;

import java.util.List;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TraitSequenceTest {
    public static final TraitSequence sampleTraitSequence1() {
        return TraitSequence.builder()
                .trait(FIPSLevelTraitTest.sampleFIPSLevelTrait1())
                .trait(ASN1ObjectTraitTest.sampleASN1ObjectTrait1())
                .build();
    }

    @Test
    public void testBuildSampleTraitSequence1() {
        TraitSequence traitSeq = sampleTraitSequence1();
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
        Assertions.assertEquals(trait1, traitSeq.getTraits().get(FIPSLevelTrait.class).get(0));
        Assertions.assertEquals(trait2, traitSeq.getTraits().get(ASN1ObjectTrait.class).get(0));
    }

    @Test
    public void testFromASN1Sequence() {
        TraitSequence traitSeq = sampleTraitSequence1();
        FIPSLevelTrait trait1 = FIPSLevelTraitTest.sampleFIPSLevelTrait1();
        ASN1ObjectTrait trait2 = ASN1ObjectTraitTest.sampleASN1ObjectTrait1();
        ASN1Sequence seq = new DERSequence(ASN1Utils.toASN1EncodableVector(List.of(trait1.toASN1Primitive(), trait2.toASN1Primitive())));

        TraitSequence traitSeq2 = TraitSequence.fromASN1Sequence(seq);
        Assertions.assertNotNull(traitSeq2);
        Assertions.assertEquals(traitSeq, traitSeq2);
    }
}
