package paccor.validator;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import paccor.normalization.pci.PciIdsRegistry;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.UTF8StringTrait;
import paccor.validator.ComponentMatcher;

public class ComponentMatcherNormalizationTest {

    @BeforeEach
    public void resetPciRegistry() {
        // Ensure tests run with a known tiny pci.ids content
        String ids = "8086  Intel Corporation\n";
        try {
            PciIdsRegistry.installForTests(PciIdsRegistry.parse(new ByteArrayInputStream(ids.getBytes(StandardCharsets.UTF_8))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void manufacturerSynonyms_areEqualAfterNormalization() {
        ComponentMatcher matcher = ComponentMatcher.NORMALIZED;

        // expected vendor uses "Unknown"; actual uses "n/A" with spaces and sample_testgen1 case
        TraitMap expected = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformManufacturer)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("Unknown"))
                        .build())
                .build();
        TraitMap actual = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformManufacturer)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("  n/A  "))
                        .build())
                .build();

        Assertions.assertTrue(matcher.matchV3(List.of(expected), List.of(actual)), "Manufacturer synonyms should normalize to match");
    }

    @Test
    public void pciVendor_nameAndId_matchUsingPciIds() {
        ComponentMatcher matcher = ComponentMatcher.NORMALIZED;

        // expected has vendor name (from pci.ids) with PCI registry OID; actual has hex id
        // Use COMPONENT manufacturer since PciFieldTranslator only works on component fields
        TraitMap expected = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(ComponentMatcher.PCI_REGISTRY_OID)
                        .traitValue(new DERUTF8String("Intel Corporation"))
                        .build())
                .build();
        TraitMap actual = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(ComponentMatcher.PCI_REGISTRY_OID)
                        .traitValue(new DERUTF8String("8086"))
                        .build())
                .build();

        Assertions.assertTrue(matcher.matchV3(List.of(expected), List.of(actual)), "PCI vendor name should canonicalize to vendor ID and match");
        // reversed order should also pass
        Assertions.assertTrue(matcher.matchV3(List.of(actual), List.of(expected)));
    }

    @Test
    public void actualMayContainExtraTraits() {
        ComponentMatcher matcher = new ComponentMatcher(List.of());

        TraitMap expected = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("SomeVendor"))
                        .build())
                .build();
        TraitMap actual = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("SomeVendor"))
                        .build())
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel)
                        .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                        .traitValue(new DERUTF8String("Extra"))
                        .build())
                .build();

        Assertions.assertTrue(matcher.matchV3(List.of(expected), List.of(actual)));
    }
}
