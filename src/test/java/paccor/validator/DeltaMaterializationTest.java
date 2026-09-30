package paccor.validator;

import java.util.List;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.cert.CertType;
import paccor.normalization.PlatformConfigurationNormalizer;
import paccor.tcg.credential.AttributeStatus;
import paccor.tcg.credential.CertificateIdentifier;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.ComponentClass;
import paccor.tcg.credential.ComponentIdentifierV2;
import paccor.tcg.credential.PlatformConfigurationV2;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.StatusTrait;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitMap;

/**
 * Unit tests for applying delta component changes and for one-to-one component comparison.
 */
public class DeltaMaterializationTest {
    private static final AttributeStatus ADDED = new AttributeStatus(AttributeStatus.Enumerated.added.getValue());
    private static final AttributeStatus MODIFIED = new AttributeStatus(AttributeStatus.Enumerated.modified.getValue());
    private static final AttributeStatus REMOVED = new AttributeStatus(AttributeStatus.Enumerated.removed.getValue());

    private static ComponentIdentifierV2 component(String manufacturer, String model, String serial, String revision, AttributeStatus status) {
        ComponentIdentifierV2.ComponentIdentifierV2Builder builder = ComponentIdentifierV2.builder()
                .componentClass(new ComponentClass(TCGObjectIdentifier.tcgRegistryComponentClassTcg,
                        new DEROctetString(new byte[]{0, 6, 0, 1})))
                .componentManufacturer(new DERUTF8String(manufacturer))
                .componentModel(new DERUTF8String(model));
        Optional.ofNullable(serial).map(DERUTF8String::new).ifPresent(builder::componentSerial);
        Optional.ofNullable(revision).map(DERUTF8String::new).ifPresent(builder::componentRevision);
        Optional.ofNullable(status).ifPresent(builder::status);
        return builder.build();
    }

    private static ComponentIdentifierV2 component(String model, String serial, AttributeStatus status) {
        return component("Acme", model, serial, null, status);
    }

    private static PlatformConfigurationV3 config(ComponentIdentifierV2... components) {
        return PlatformConfigurationNormalizer.canonicalize(
                PlatformConfigurationV2.builder().componentIdentifiers(List.of(components)).build());
    }

    private static List<TraitMap> traits(ComponentIdentifierV2... components) {
        return PlatformConfigurationNormalizer.componentsForValidation(config(components));
    }

    private static Optional<List<TraitMap>> apply(PlatformConfigurationV3 base, PlatformConfigurationV3 delta, ComponentMatcher matcher) {
        return ComponentValidator.materializeComponents(base, List.of(delta), matcher)
                .map(PlatformConfigurationNormalizer::componentsForValidation);
    }

    private static void assertSameComponents(List<TraitMap> expected, List<TraitMap> actual) {
        ComponentValidationReport report = ComponentValidator.compareComponents(expected, actual, ComponentMatcher.RAW);
        Assertions.assertTrue(report.ok(), report.detail());
    }

    // ===== Delta application =====

    @Test
    void added_identicalSerialLessComponent_isAppended() {
        Optional<List<TraitMap>> result = apply(
                config(component("DIMM", null, null), component("DIMM", null, null)),
                config(component("DIMM", null, ADDED)),
                ComponentMatcher.NORMALIZED);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(3, result.get().size());
    }

    @Test
    void removed_matchingComponent_isRemoved() {
        Optional<List<TraitMap>> result = apply(
                config(component("DIMM", "A", null), component("DIMM", "B", null)),
                config(component("DIMM", "A", REMOVED)),
                ComponentMatcher.NORMALIZED);

        Assertions.assertTrue(result.isPresent());
        assertSameComponents(traits(component("DIMM", "B", null)), result.get());
    }

    @Test
    void modified_replacesMatchingComponent() {
        Optional<List<TraitMap>> result = apply(
                config(component("Acme", "BIOS", null, "1.0", null)),
                config(component("Acme", "BIOS", null, "2.0", MODIFIED)),
                ComponentMatcher.NORMALIZED);

        Assertions.assertTrue(result.isPresent());
        assertSameComponents(traits(component("Acme", "BIOS", null, "2.0", null)), result.get());
    }

    @Test
    void removed_withNoMatch_fails() {
        Assertions.assertTrue(apply(
                config(component("DIMM", "A", null)),
                config(component("DIMM", "Z", REMOVED)),
                ComponentMatcher.NORMALIZED).isEmpty());
    }

    @Test
    void modified_withNoMatch_fails() {
        Assertions.assertTrue(apply(
                config(component("DIMM", "A", null)),
                config(component("DIMM", "Z", MODIFIED)),
                ComponentMatcher.NORMALIZED).isEmpty());
    }

    @Test
    void componentWithoutStatus_fails() {
        Assertions.assertTrue(apply(
                config(component("DIMM", "A", null)),
                config(component("DIMM", "B", ADDED), component("NIC", "C", null)),
                ComponentMatcher.NORMALIZED).isEmpty());
    }

    @Test
    void removed_withoutManufacturerOrModel_fails() {
        PlatformConfigurationV3 delta = PlatformConfigurationV3.builder()
                .platformComponent(TraitMap.builder()
                        .trait(StatusTrait.builder()
                                .traitCategory(TCGObjectIdentifier.tcgTrCatComponentStatus)
                                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                                .traitValue(REMOVED)
                                .build())
                        .build())
                .build();

        Assertions.assertTrue(apply(config(component("DIMM", "A", null)), delta, ComponentMatcher.NORMALIZED).isEmpty());
    }

    @Test
    void removed_identityComparisonFollowsMatcherNormalization() {
        PlatformConfigurationV3 base = config(component("Acme Corp", "DIMM", "A", null, null));
        PlatformConfigurationV3 delta = config(component(" ACME  CORP ", "dimm", "A", null, REMOVED));

        Assertions.assertEquals(Optional.of(0), apply(base, delta, ComponentMatcher.NORMALIZED).map(List::size));
        Assertions.assertTrue(apply(base, delta, ComponentMatcher.RAW).isEmpty());
    }

    @Test
    void deltaWithoutComponents_keepsPreviousComponents() {
        PlatformConfigurationV3 base = config(component("DIMM", "A", null));

        Optional<List<TraitMap>> result = apply(base, PlatformConfigurationV3.builder().build(), ComponentMatcher.NORMALIZED);

        Assertions.assertTrue(result.isPresent());
        assertSameComponents(traits(component("DIMM", "A", null)), result.get());
    }

    // ===== Component comparison =====

    @Test
    void compare_requiresEveryCertificateComponentOnPlatform() {
        ComponentValidationReport report = ComponentValidator.compareComponents(
                traits(component("DIMM", "A", null)),
                traits(component("DIMM", "A", null), component("NIC", "N", null)),
                ComponentMatcher.NORMALIZED);

        Assertions.assertFalse(report.ok());
        Assertions.assertTrue(report.detail().contains("Certificate component not found on platform"), report.detail());
    }

    @Test
    void compare_duplicateManifestEntryCannotSatisfyTwoCertificateEntries() {
        Assertions.assertFalse(ComponentValidator.compareComponents(
                traits(component("DIMM", "A", null), component("DIMM", "A", null)),
                traits(component("DIMM", "A", null), component("DIMM", "B", null)),
                ComponentMatcher.NORMALIZED).ok());
    }

    @Test
    void compare_serialLessDuplicatesMustMatchCount() {
        Assertions.assertFalse(ComponentValidator.compareComponents(
                traits(component("DIMM", null, null), component("DIMM", null, null), component("DIMM", null, null)),
                traits(component("DIMM", null, null), component("DIMM", null, null), component("NIC", "N", null)),
                ComponentMatcher.NORMALIZED).ok());
    }

    @Test
    void match_findsAssignmentThatGreedyFirstMatchWouldMiss() {
        // The less specific expected entry must not take the only candidate for the more specific one.
        List<TraitMap> expected = traits(component("Acme", "DIMM", null, null, null), component("DIMM", "2", null));
        List<TraitMap> actual = traits(component("DIMM", "2", null), component("DIMM", "1", null));

        ComponentMatcher.MatchResult result = ComponentMatcher.RAW.match(expected, actual);

        Assertions.assertTrue(result.complete());
    }

    @Test
    void match_reportsUnpairedComponentsOnBothSides() {
        ComponentMatcher.MatchResult result = ComponentMatcher.RAW.match(
                traits(component("DIMM", "A", null), component("GPU", "G", null)),
                traits(component("DIMM", "A", null), component("NIC", "N", null)));

        Assertions.assertEquals(1, result.unmatchedExpected().size());
        Assertions.assertEquals(1, result.unmatchedActual().size());
    }

    // ===== Previous certificate categories =====

    @Test
    void certTypeOf_mapsRecognizedCategoriesOnly() {
        CertificateIdentifier identifier = CertificateIdentifier.builder().build();
        Assertions.assertEquals(Optional.of(CertType.BASE), ComponentValidator.certTypeOf(trait(TCGObjectIdentifier.tcgTrCatPlatformCertificate, identifier)));
        Assertions.assertEquals(Optional.of(CertType.DELTA), ComponentValidator.certTypeOf(trait(TCGObjectIdentifier.tcgTrCatDeltaPlatformCertificate, identifier)));
        Assertions.assertEquals(Optional.of(CertType.REBASE), ComponentValidator.certTypeOf(trait(TCGObjectIdentifier.tcgTrCatRebasePlatformCertificate, identifier)));
        Assertions.assertEquals(Optional.empty(), ComponentValidator.certTypeOf(trait(TCGObjectIdentifier.tcgTrCatGenericCertificate, identifier)));
        Assertions.assertEquals(Optional.empty(), ComponentValidator.certTypeOf(null));
    }

    private static CertificateIdentifierTrait trait(ASN1ObjectIdentifier category, CertificateIdentifier identifier) {
        return CertificateIdentifierTrait.builder()
                .traitCategory(category)
                .traitRegistry(TCGObjectIdentifier.tcgTrRegNone)
                .traitValue(identifier)
                .build();
    }
}
