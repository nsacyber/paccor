package paccor.validator;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import lombok.Builder;
import org.bouncycastle.cert.X509CertificateHolder;
import paccor.cert.PlatformCertificate;
import paccor.cert.SubjectAlternativeNameHelper;
import paccor.json.HardwareManifestJsonHelper;
import paccor.normalization.PlatformConfigurationNormalizer;
import paccor.tcg.credential.PlatformConfiguration;
import paccor.tcg.credential.PlatformConfigurationV2;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitMap;

/** Validates expected components, including previous-platform-certificate materialization. */
@Builder
public final class ComponentValidationService {
    private static final Logger LOGGER = Logger.getLogger(ComponentValidationService.class.getName());

    private final List<String> previousPlatformCertificates;
    private final X509CertificateHolder issuerCertificate;
    private final List<X509CertificateHolder> trustAnchors;

    public boolean validate(PlatformCertificate certificate, File jsonFile, String matcherName) {
        if (jsonFile == null || !jsonFile.exists()) {
            LOGGER.info("Component validation: Skipped. No components JSON provided.");
            return false;
        }
        boolean previousCertificatesProvided = hasPreviousCertificates();
        if (certificate.requiresPreviousPlatformCertificates() && !previousCertificatesProvided) {
            LOGGER.warning("Component validation for delta or rebase certificates requires --prev-pcert.");
            return false;
        }
        HardwareManifestJsonHelper manifest = HardwareManifestJsonHelper.readComponents(jsonFile);
        if (manifest == null) return false;

        ComponentMatcher matcher = ValidateMatcher.resolve(matcherName);
        boolean platformIdentifiersOk = comparePlatformIdentifiers(
                manifest.platformTraits(),
                SubjectAlternativeNameHelper.extractPlatformTraits(
                        certificate.subjectAlternativeNames(), certificate.resolvedSpecVersion()),
                matcher);
        List<TraitMap> expected = normalizeExpected(
                manifest.pcV1(), manifest.pcV2(), manifest.pcV3(),
                certificate.hasAttribute(TCGObjectIdentifier.tcgAtPlatformConfigurationV1),
                certificate.hasAttribute(TCGObjectIdentifier.tcgAtPlatformConfigurationV2));
        PlatformConfigurationV3 actual = certificate.canonicalizedPlatformConfigurationV3();
        PlatformConfigurationV3 materialized = materializeWithPrevious(certificate, actual);
        boolean valid = !previousCertificatesProvided || materialized != null;
        boolean result = platformIdentifiersOk && valid && Optional.ofNullable(materialized)
                .map(configuration -> compare(expected,
                        PlatformConfigurationNormalizer.componentsForValidation(configuration), matcher))
                .orElse(false);
        LOGGER.info("Components validation: " + (result ? "OK" : "FAILED"));
        return result;
    }

    private boolean comparePlatformIdentifiers(
            TraitMap expected,
            TraitMap actual,
            ComponentMatcher matcher) {
        if (expected == null || expected.isEmpty()) {
            return true;
        }
        boolean matches = actual != null && !actual.isEmpty()
                && matcher.matchV3(List.of(expected), List.of(actual));
        if (!matches) LOGGER.fine(() -> "Platform identifier validation failed; expected=" + expected
                + ", actual=" + actual);
        return matches;
    }

    private boolean compare(List<TraitMap> expected, List<TraitMap> actual, ComponentMatcher matcher) {
        ComponentValidationReport report = ComponentValidator.compareComponents(expected, actual, matcher);
        if (!report.ok()) LOGGER.fine(report.detail());
        return report.ok();
    }

    private List<TraitMap> normalizeExpected(
            PlatformConfiguration expectV1,
            PlatformConfigurationV2 expectV2,
            PlatformConfigurationV3 expectV3,
            boolean requireV1Compatibility,
            boolean requireV2Compatibility) {
        return tryV1Compat(expectV1, expectV3, requireV1Compatibility)
                .or(() -> tryV2Compat(expectV3, requireV2Compatibility))
                .or(() -> tryDirect(expectV3, expectV2, expectV1))
                .orElse(List.of());
    }

    private Optional<List<TraitMap>> tryV1Compat(
            PlatformConfiguration expectV1,
            PlatformConfigurationV3 expectV3,
            boolean required) {
        if (!required) return Optional.empty();
        if (PlatformConfigurationNormalizer.hasContent(expectV1)) {
            return Optional.of(PlatformConfigurationNormalizer.componentsForValidation(expectV1));
        }
        return Optional.ofNullable(expectV3)
                .filter(PlatformConfigurationNormalizer::hasContent)
                .map(PlatformConfigurationNormalizer::toV1)
                .map(PlatformConfigurationNormalizer::componentsForValidation);
    }

    private Optional<List<TraitMap>> tryV2Compat(PlatformConfigurationV3 expectV3, boolean required) {
        if (!required) return Optional.empty();
        return Optional.ofNullable(expectV3)
                .filter(PlatformConfigurationNormalizer::hasContent)
                .map(PlatformConfigurationNormalizer::toV2)
                .map(PlatformConfigurationNormalizer::componentsForValidation);
    }

    private Optional<List<TraitMap>> tryDirect(
            PlatformConfigurationV3 expectV3,
            PlatformConfigurationV2 expectV2,
            PlatformConfiguration expectV1) {
        return Optional.ofNullable(expectV3)
                .filter(PlatformConfigurationNormalizer::hasContent)
                .map(PlatformConfigurationNormalizer::componentsForValidation)
                .or(() -> Optional.ofNullable(expectV2)
                        .filter(PlatformConfigurationNormalizer::hasContent)
                        .map(PlatformConfigurationNormalizer::componentsForValidation))
                .or(() -> Optional.ofNullable(expectV1)
                        .filter(PlatformConfigurationNormalizer::hasContent)
                        .map(PlatformConfigurationNormalizer::componentsForValidation));
    }

    private PlatformConfigurationV3 materializeWithPrevious(PlatformCertificate certificate, PlatformConfigurationV3 current) {
        return PreviousPlatformCertificateValidator.builder()
                .previousPlatformCertificates(previousPlatformCertificates)
                .issuerCertificate(issuerCertificate)
                .trustAnchors(trustAnchors)
                .build()
                .materialize(certificate, current);
    }

    private boolean hasPreviousCertificates() {
        return Optional.ofNullable(previousPlatformCertificates)
                .map(values -> !values.isEmpty())
                .orElse(false);
    }

    private static final class ValidateMatcher {
        private static ComponentMatcher resolve(String name) {
            return Optional.ofNullable(name)
                    .map(value -> value.toUpperCase(java.util.Locale.ROOT))
                    .filter(value -> value.equals("RAW") || value.equals("STRICT"))
                    .map(_ -> ComponentMatcher.RAW)
                    .orElse(ComponentMatcher.NORMALIZED);
        }
    }
}
