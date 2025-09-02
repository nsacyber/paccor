package cli;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.Trait;
import tcg.credential.TraitCollection;
import tcg.credential.TraitMap;
import tcg.credential.UTF8StringTrait;
import validator.ComponentMatcher;

public class ValidateCmdTest {
    @Test
    public void testValidateWrongSigner() {
        File cert = new File("src/test/resources/TestCA.cert.example.pem");
        File otherSigner = new File("src/test/resources/ca_2187.crt");
        int code = new CommandLine(new RootCmd()).execute(
                "validate",
                "-X", cert.getAbsolutePath(),
                "--publicKeyCert", otherSigner.getAbsolutePath()
        );
        Assertions.assertEquals(ClientExitCodes.VALIDATION_FAILED.code(), code);
    }

    @Test
    public void testResolveMatcher_variants() {
        ComponentMatcher m0 = ValidateCmd.resolveMatcher(null);
        Assertions.assertSame(ComponentMatcher.NORMALIZED, m0);

        ComponentMatcher m1 = ValidateCmd.resolveMatcher("normalized");
        Assertions.assertSame(ComponentMatcher.NORMALIZED, m1);

        ComponentMatcher m2 = ValidateCmd.resolveMatcher("pci_aware");
        Assertions.assertSame(ComponentMatcher.NORMALIZED, m2);

        ComponentMatcher m3 = ValidateCmd.resolveMatcher("RAW");
        Assertions.assertSame(ComponentMatcher.RAW, m3);

        ComponentMatcher m4 = ValidateCmd.resolveMatcher("STRICT");
        Assertions.assertSame(ComponentMatcher.RAW, m4);

        ComponentMatcher m5 = ValidateCmd.resolveMatcher("not_a_real_policy");
        Assertions.assertSame(ComponentMatcher.NORMALIZED, m5);
    }

    @Test
    public void testFlattenTraits() {

        // empty TraitMap (null map) -> empty list
        TraitMap empty = TraitMap.builder().build();
        Assertions.assertEquals(List.of(), empty.flattenTraits());

        // TraitMap with multiple trait lists and a null entry should be filtered
        Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> map = new HashMap<>();
        UTF8StringTrait t1 = UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgAtPlatformManufacturerStr)
                .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                .traitValue(new DERUTF8String("A")).build();
        UTF8StringTrait t2 = UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgAtPlatformManufacturerStr)
                .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                .traitValue(new DERUTF8String("B")).build();
        map.put(UTF8StringTrait.class, new ArrayList<>(Arrays.asList(t1, null, t2)));
        TraitMap seq = TraitMap.builder().traits(map).build();

        List<Trait<?, ?>> flat = seq.flattenTraits();
        // Current implementation flattens lists and only filters out null lists, not null elements
        Assertions.assertEquals(3, flat.size());
        Assertions.assertTrue(flat.contains(t1));
        Assertions.assertTrue(flat.contains(t2));
        Assertions.assertTrue(flat.contains(null));
    }

    @Test
    public void testTraitsSetEquals_multiset() {
        Trait<?, ?> a1 = UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgAtPlatformManufacturerStr)
                .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                .traitValue(new DERUTF8String("X")).build();
        Trait<?, ?> a2 = UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgAtPlatformManufacturerStr)
                .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                .traitValue(new DERUTF8String("Y")).build();
        Trait<?, ?> a3 = UTF8StringTrait.builder()
                .traitCategory(TCGObjectIdentifier.tcgAtPlatformManufacturerStr)
                .traitRegistry(TCGObjectIdentifier.tcgRegistry)
                .traitValue(new DERUTF8String("Z")).build();

        TraitCollection a1List = TraitCollection.fromTraits(List.of(a1));
        TraitCollection a2List = TraitCollection.fromTraits(List.of(a2));
        TraitCollection a1a2List = TraitCollection.fromTraits(List.of(a1, a2));
        TraitCollection a1a2a2List = TraitCollection.fromTraits(List.of(a1, a2, a2));
        TraitCollection a2a3List = TraitCollection.fromTraits(List.of(a2, a3));
        TraitCollection a1a2a3List = TraitCollection.fromTraits(List.of(a1, a2, a3));

        // exact match
        Assertions.assertTrue(a1a2List.containsAll(a1a2List));

        // superset contains subset (extra trait allowed)
        Assertions.assertTrue(a1a2a3List.containsAll(a1a2List));
        Assertions.assertFalse(a1a2List.containsAll(a1a2a2List)); // Only remove one from superset

        // missing required element
        Assertions.assertFalse(a1List.containsAll(a1a2List));

        // duplicate requirement: subset has two equal items; superset needs two occurrences
        Assertions.assertTrue(a2a3List.containsAll(a2a3List));
        Assertions.assertFalse(a2List.containsAll(a2a3List));
    }
}
