package validator;

import java.util.List;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tcg.credential.ComponentClass;
import tcg.credential.ComponentIdentifierV2;
import tcg.credential.PlatformConfigurationV3;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.TraitMap;
import tcg.credential.UTF8StringTrait;

/**
 * Unit tests for raw and normalized component matching.
 */
public class ComponentMatcherTest {

    private ComponentIdentifierV2 mkV2(String manufacturer, String model) {
        return ComponentIdentifierV2.builder()
                .componentClass(new ComponentClass(TCGObjectIdentifier.tcgRegistryComponentClassTcg,
                        new DEROctetString(new byte[]{0,0,0,1})))
                .componentManufacturer(new DERUTF8String(manufacturer))
                .componentModel(new DERUTF8String(model))
                .build();
    }

    @Test
    void rawMatcher_v2_requires_exactMatchForExpectedComponents() {
        ComponentMatcher matcher = ComponentMatcher.RAW;
        ComponentIdentifierV2 e1 = mkV2("Acme", "ModelA");
        ComponentIdentifierV2 e2 = mkV2("Acme", "ModelB");
        // actual has both expected plus one extra
        ComponentIdentifierV2 a1 = mkV2("Acme", "ModelA");
        ComponentIdentifierV2 a2 = mkV2("Acme", "ModelB");
        ComponentIdentifierV2 a3 = mkV2("Acme", "ModelC");
        Assertions.assertTrue(matcher.matchV2(List.of(e1, e2), List.of(a1, a2, a3)));
        // missing one expected -> fail
        Assertions.assertFalse(matcher.matchV2(List.of(e1, e2), List.of(a1)));
        // manufacturer mismatch -> fail
        Assertions.assertFalse(matcher.matchV2(List.of(e1), List.of(mkV2("Other", "ModelA"))));
        // model mismatch -> fail
        Assertions.assertFalse(matcher.matchV2(List.of(e1), List.of(mkV2("Acme", "ModelX"))));
    }

    @Test
    void normalizedMatcher_v2_normalizesManufacturerSynonyms() {
        ComponentMatcher matcher = ComponentMatcher.NORMALIZED;
        ComponentIdentifierV2 expected = mkV2("Unknown", "M");
        // actual blank considered equal
        Assertions.assertTrue(matcher.matchV2(List.of(expected), List.of(mkV2("", "M"))));
        // actual n/a considered equal (case-insensitive)
        Assertions.assertTrue(matcher.matchV2(List.of(expected), List.of(mkV2("n/A", "M"))));
        // exact non-blank still equal
        Assertions.assertTrue(matcher.matchV2(List.of(expected), List.of(mkV2("Unknown", "M"))));
        // sample_testgen1 model -> still fail
        Assertions.assertFalse(matcher.matchV2(List.of(expected), List.of(mkV2("", "X"))));
    }

    @Test
    void rawMatcher_v3_usesExpectedSubsetContainment() {
        UTF8StringTrait t1 = UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .traitValue(new DERUTF8String("A"))
                .build();
        UTF8StringTrait t2 = UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .traitValue(new DERUTF8String("B"))
                .build();
        TraitMap tsExpected1 = TraitMap.builder().trait(t1).trait(t2).build();
        TraitMap tsExpected2 = TraitMap.builder().trait(t1) // missing t2
                .build();

        TraitMap tsActual1 = TraitMap.builder().trait(t1).trait(t2).build();
        TraitMap tsActual2 = TraitMap.builder().trait(t1) // missing t2
                .build();

        ComponentMatcher matcher = ComponentMatcher.RAW;
        PlatformConfigurationV3 pcExpected1 = PlatformConfigurationV3.builder().platformComponent(tsExpected1).build();
        PlatformConfigurationV3 pcExpected2 = PlatformConfigurationV3.builder().platformComponent(tsExpected2).build();
        PlatformConfigurationV3 pcActual1 = PlatformConfigurationV3.builder().platformComponent(tsActual1).build();
        PlatformConfigurationV3 pcActual2 = PlatformConfigurationV3.builder().platformComponent(tsActual2).build();

        Assertions.assertTrue(matcher.matchV3(pcExpected1.getPlatformComponents(), pcActual1.getPlatformComponents()));
        Assertions.assertFalse(matcher.matchV3(pcExpected1.getPlatformComponents(), pcActual2.getPlatformComponents()));
        Assertions.assertTrue(matcher.matchV3(pcExpected2.getPlatformComponents(), pcActual1.getPlatformComponents()));
        Assertions.assertTrue(matcher.matchV3(pcExpected2.getPlatformComponents(), pcActual2.getPlatformComponents()));
    }

    // normalizeTraitValue method removed - normalization now happens via translators during matching
}
