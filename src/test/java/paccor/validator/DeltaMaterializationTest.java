package paccor.validator;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.cert.CertType;
import paccor.normalization.ComponentCanonicalizer;
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
    void addedIdenticalSerialLessComponentIsAppended() {
        Optional<List<TraitMap>> result = apply(
                config(component("DIMM", null, null), component("DIMM", null, null)),
                config(component("DIMM", null, ADDED)),
                ComponentMatcher.NORMALIZED);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(3, result.get().size());
    }

    @Test
    void removedMatchingComponentIsRemoved() {
        Optional<List<TraitMap>> result = apply(
                config(component("DIMM", "A", null), component("DIMM", "B", null)),
                config(component("DIMM", "A", REMOVED)),
                ComponentMatcher.NORMALIZED);

        Assertions.assertTrue(result.isPresent());
        assertSameComponents(traits(component("DIMM", "B", null)), result.get());
    }

    @Test
    void modifiedReplacesMatchingComponent() {
        Optional<List<TraitMap>> result = apply(
                config(component("Acme", "BIOS", null, "1.0", null)),
                config(component("Acme", "BIOS", null, "2.0", MODIFIED)),
                ComponentMatcher.NORMALIZED);

        Assertions.assertTrue(result.isPresent());
        assertSameComponents(traits(component("Acme", "BIOS", null, "2.0", null)), result.get());
    }

    @Test
    void removedWithNoMatch_fails() {
        Assertions.assertTrue(apply(
                config(component("DIMM", "A", null)),
                config(component("DIMM", "Z", REMOVED)),
                ComponentMatcher.NORMALIZED).isEmpty());
    }

    @Test
    void modifiedWithNoMatch_fails() {
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
    void removedWithoutManufacturerOrModel_fails() {
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
    void removedIdentityComparisonFollowsMatcherNormalization() {
        PlatformConfigurationV3 base = config(component("Acme Corp", "DIMM", "A", null, null));
        PlatformConfigurationV3 delta = config(component(" ACME  CORP ", "dimm", "A", null, REMOVED));

        Assertions.assertEquals(Optional.of(0), apply(base, delta, ComponentMatcher.NORMALIZED).map(List::size));
        Assertions.assertTrue(apply(base, delta, ComponentMatcher.RAW).isEmpty());
    }

    @Test
    void deltaWithoutComponentsKeepsPreviousComponents() {
        PlatformConfigurationV3 base = config(component("DIMM", "A", null));

        Optional<List<TraitMap>> result = apply(base, PlatformConfigurationV3.builder().build(), ComponentMatcher.NORMALIZED);

        Assertions.assertTrue(result.isPresent());
        assertSameComponents(traits(component("DIMM", "A", null)), result.get());
    }

    // ===== Component comparison =====

    @Test
    void compareRequiresEveryCertificateComponentOnPlatform() {
        ComponentValidationReport report = ComponentValidator.compareComponents(
                traits(component("DIMM", "A", null)),
                traits(component("DIMM", "A", null), component("NIC", "N", null)),
                ComponentMatcher.NORMALIZED);

        Assertions.assertFalse(report.ok());
        Assertions.assertTrue(report.detail().contains("Certificate component not found on platform"), report.detail());
    }

    @Test
    void compareDuplicateManifestEntryCannotSatisfyTwoCertificateEntries() {
        Assertions.assertFalse(ComponentValidator.compareComponents(
                traits(component("DIMM", "A", null), component("DIMM", "A", null)),
                traits(component("DIMM", "A", null), component("DIMM", "B", null)),
                ComponentMatcher.NORMALIZED).ok());
    }

    @Test
    void compareSerialLessDuplicatesMustMatchCount() {
        Assertions.assertFalse(ComponentValidator.compareComponents(
                traits(component("DIMM", null, null), component("DIMM", null, null), component("DIMM", null, null)),
                traits(component("DIMM", null, null), component("DIMM", null, null), component("NIC", "N", null)),
                ComponentMatcher.NORMALIZED).ok());
    }

    @Test
    void matchComponentReportingFewerTraitsThanCertifiedDoesNotPair() {
        List<TraitMap> reported = traits(component("DIMM", null, null));
        List<TraitMap> certified = traits(component("DIMM", "2", null));

        Assertions.assertFalse(ComponentMatcher.RAW.match(reported, certified).complete());
    }

    // ===== Certificate references =====

    private static ComponentIdentifierV2 withPlatformCert(ComponentIdentifierV2 component, int serial) {
        return component.toBuilder()
                .componentPlatformCert(CertificateIdentifier.builder()
                        .genericCertIdentifier(new IssuerSerial(new GeneralNames(new GeneralName(new X500Name("CN=OEM CA"))), BigInteger.valueOf(serial)))
                        .build())
                .build();
    }

    @Test
    void matchCertificateReferenceNotReportedByPlatformIsAccepted() {
        List<TraitMap> reported = traits(component("NIC", "N1", null));
        List<TraitMap> certified = traits(withPlatformCert(component("NIC", "N1", null), 7));

        Assertions.assertTrue(ComponentMatcher.NORMALIZED.match(reported, certified).complete());
    }

    @Test
    void matchCertificateReferenceReportedByPlatformMustMatch() {
        List<TraitMap> certified = traits(withPlatformCert(component("NIC", "N1", null), 7));

        Assertions.assertTrue(ComponentMatcher.NORMALIZED.match(
                traits(withPlatformCert(component("NIC", "N1", null), 7)), certified).complete());
        Assertions.assertFalse(ComponentMatcher.NORMALIZED.match(
                traits(withPlatformCert(component("NIC", "N1", null), 8)), certified).complete());
    }

    @Test
    void matchPairsWithTheCandidateWhoseReferenceMatches() {
        // Two certified components with the same hardware identity, told apart only by their reference.
        List<TraitMap> certified = traits(
                withPlatformCert(component("NIC", null, null), 7),
                withPlatformCert(component("NIC", null, null), 8));
        List<TraitMap> reported = traits(
                withPlatformCert(component("NIC", null, null), 8),
                component("NIC", null, null));

        Assertions.assertTrue(ComponentMatcher.NORMALIZED.match(reported, certified).complete());
    }

    @Test
    void matchReferenceCheckIsReplaceable() {
        ComponentMatcher strict = new ComponentMatcher(ComponentCanonicalizer.NORMALIZED,
                (reported, certified) -> reported.references().equals(certified.references()));
        List<TraitMap> certified = traits(withPlatformCert(component("NIC", "N1", null), 7));

        Assertions.assertFalse(strict.match(traits(component("NIC", "N1", null)), certified).complete());
    }

    @Test
    void matchReportsUnpairedComponentsOnBothSides() {
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
