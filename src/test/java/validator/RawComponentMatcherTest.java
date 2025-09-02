package validator;

import java.util.List;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tcg.credential.ComponentClassTraitTest;
import tcg.credential.ComponentIdentifierV2;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.TraitMap;
import tcg.credential.UTF8StringTrait;

public class RawComponentMatcherTest {
    private final ComponentMatcher matcher = ComponentMatcher.RAW;

    public static final ComponentIdentifierV2 createTestComponentV2(String manufacturer) {
        return RawComponentMatcherTest.createTestComponentV2(manufacturer, "testModel");
    }

    public static final ComponentIdentifierV2 createTestComponentV2(String manufacturer, String model) {
        return ComponentIdentifierV2.builder()
                .componentClass(ComponentClassTraitTest.COMPONENT_CLASS_2)
                .componentManufacturer(new DERUTF8String(manufacturer))
                .componentModel(new DERUTF8String(model))
                .build();
    }
    
    @Test
    void matchesIdenticalComponents() {
        ComponentIdentifierV2 comp1 = createTestComponentV2("vendor1", "model1");
        ComponentIdentifierV2 comp2 = createTestComponentV2("vendor1", "model1");

        boolean result = matcher.matchV2(List.of(comp1), List.of(comp2));
        Assertions.assertTrue(result);
    }

    @Test
    void rejectsDifferentManufacturers() {
        ComponentIdentifierV2 expected = createTestComponentV2("vendor1", "model1");
        ComponentIdentifierV2 actual = createTestComponentV2("vendor2", "model1");

        boolean result = matcher.matchV2(List.of(expected), List.of(actual));
        Assertions.assertFalse(result);
    }

    @Test
    void expectedSubsetOfActual() {
        ComponentIdentifierV2 comp1 = createTestComponentV2("vendor1", "model1");
        ComponentIdentifierV2 comp2 = createTestComponentV2("vendor2", "model2");

        boolean result = matcher.matchV2(List.of(comp1), List.of(comp1, comp2));
        Assertions.assertTrue(result);
    }

    @Test
    void expectedLargerThanActual() {
        ComponentIdentifierV2 comp1 = createTestComponentV2("vendor1", "model1");
        ComponentIdentifierV2 comp2 = createTestComponentV2("vendor2", "model2");

        boolean result = matcher.matchV2(List.of(comp1, comp2), List.of(comp1));
        Assertions.assertFalse(result);
    }

    @Test
    void emptyExpected() {
        ComponentIdentifierV2 comp1 = createTestComponentV2("vendor1", "model1");
        boolean result = matcher.matchV2(List.of(), List.of(comp1));
        Assertions.assertTrue(result);
    }

    @Test
    void v3_allowsActualSupersetOfExpectedTraits() {
        TraitMap expected = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("vendor1"))
                        .build())
                .build();
        TraitMap actual = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("vendor1"))
                        .build())
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("model1"))
                        .build())
                .build();

        Assertions.assertTrue(matcher.matchV3(List.of(expected), List.of(actual)));
    }

    @Test
    void v3_rejectsActualMissingExpectedTraits() {
        TraitMap expected = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("vendor1"))
                        .build())
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("model1"))
                        .build())
                .build();
        TraitMap actual = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("vendor1"))
                        .build())
                .build();

        Assertions.assertFalse(matcher.matchV3(List.of(expected), List.of(actual)));
    }

}
