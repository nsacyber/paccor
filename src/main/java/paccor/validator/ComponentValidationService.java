package paccor.validator;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import paccor.cert.PlatformCertificate;
import paccor.cli.GlobFileResolver;
import paccor.json.HardwareManifestJsonHelper;
import paccor.normalization.PlatformConfigurationNormalizer;
import paccor.tcg.credential.CertificateIdentifier;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.PlatformConfiguration;
import paccor.tcg.credential.PlatformConfigurationV2;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitMap;

/** Validates expected components, including previous-platform-certificate materialization. */
@Builder
public final class ComponentValidationService {
    private final boolean quiet;
    private final String logLevel;
    private final List<String> previousPlatformCertificates;

    public boolean validate(PlatformCertificate certificate, File jsonFile, String matcherName) {
        if (jsonFile == null || !jsonFile.exists()) {
            report("Component validation: Skipped. No components JSON provided.");
            return false;
        }
        if (certificate.requiresPreviousPlatformCertificates() && !hasPreviousCertificates()) {
            error("Component validation for delta or rebase certificates requires --prev-pcert.");
            return false;
        }
        HardwareManifestJsonHelper manifest = HardwareManifestJsonHelper.readComponents(jsonFile);
        if (manifest == null) return false;

        ComponentMatcher matcher = ValidateMatcher.resolve(matcherName);
        List<TraitMap> expected = normalizeExpected(
                manifest.pcV1(), manifest.pcV2(), manifest.pcV3(),
                certificate.hasAttribute(TCGObjectIdentifier.tcgAtPlatformConfigurationV1),
                certificate.hasAttribute(TCGObjectIdentifier.tcgAtPlatformConfigurationV2));
        PlatformConfigurationV3 actual = certificate.canonicalizedPlatformConfigurationV3();
        PlatformConfigurationV3 materialized = materializeWithPrevious(certificate, actual);
        boolean valid = !hasPreviousCertificates() || materialized != null;
        boolean result = valid && Optional.ofNullable(materialized)
                .map(configuration -> compare(expected,
                        PlatformConfigurationNormalizer.componentsForValidation(configuration), matcher))
                .orElse(false);
        report("Components validation: " + (result ? "OK" : "FAILED"));
        return result;
    }

    private boolean compare(List<TraitMap> expected, List<TraitMap> actual, ComponentMatcher matcher) {
        ComponentValidationReport report = ComponentValidator.compareComponents(expected, actual, matcher);
        if (!report.ok() && detailsEnabled()) System.out.println(report.detail());
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
        if (current == null) return null;
        List<File> files = GlobFileResolver.resolve(previousPlatformCertificates);
        if (files.isEmpty()) return current;
        Map<CertificateIdentifier, PlatformConfigurationV3> resolved = loadPrevious(files);
        List<CertificateIdentifierTrait> chain = certificate.previousPlatformCertificateTraits();
        return chain == null || chain.isEmpty()
                ? materializeWithoutChain(resolved, current)
                : materializeChain(chain, resolved, current);
    }

    private PlatformConfigurationV3 materializeWithoutChain(
            Map<CertificateIdentifier, PlatformConfigurationV3> resolved,
            PlatformConfigurationV3 current) {
        PlatformConfigurationV3 base = resolved.values().stream().findFirst().orElse(null);
        if (base == null) return current;
        return PlatformConfigurationNormalizer.hasStatusTraits(current)
                ? ComponentValidator.materializeComponents(base, List.of(current))
                : current;
    }

    private PlatformConfigurationV3 materializeChain(
            List<CertificateIdentifierTrait> chain,
            Map<CertificateIdentifier, PlatformConfigurationV3> resolved,
            PlatformConfigurationV3 current) {
        ChainStart start = resolveChainStart(chain);
        if (start == null) return null;
        PlatformConfigurationV3 accumulated = applyResolvedChain(chain, resolved, start.index());
        return mergeCurrent(accumulated, current);
    }

    private PlatformConfigurationV3 applyResolvedChain(
            List<CertificateIdentifierTrait> chain,
            Map<CertificateIdentifier, PlatformConfigurationV3> resolved,
            int start) {
        PlatformConfigurationV3 accumulated = null;
        for (int index = start; index < chain.size(); index++) {
            ChainProgress progress = applyTrait(accumulated, chain.get(index), resolved);
            if (progress.failed()) return null;
            accumulated = progress.configuration();
        }
        return accumulated;
    }

    private ChainProgress applyTrait(
            PlatformConfigurationV3 accumulated,
            CertificateIdentifierTrait trait,
            Map<CertificateIdentifier, PlatformConfigurationV3> resolved) {
        if (trait == null || trait.getTraitValue() == null) return ChainProgress.success(accumulated);
        PlatformConfigurationV3 next = resolved.get(trait.getTraitValue());
        if (next == null) {
            error("Missing previous platform certificate: " + trait.getTraitValue());
            return ChainProgress.failure();
        }
        if (ComponentValidator.isDeltaTrait(trait)) {
            if (!PlatformConfigurationNormalizer.hasStatusTraits(next)) {
                error("Delta certificate without StatusTrait is not supported: " + trait.getTraitValue());
                return ChainProgress.failure();
            }
            return ChainProgress.success(accumulated == null
                    ? next
                    : ComponentValidator.materializeComponents(accumulated, List.of(next)));
        }
        return ComponentValidator.isBaseTrait(trait) || ComponentValidator.isRebaseTrait(trait)
                ? ChainProgress.success(next)
                : ChainProgress.success(accumulated);
    }

    private PlatformConfigurationV3 mergeCurrent(PlatformConfigurationV3 accumulated, PlatformConfigurationV3 current) {
        if (accumulated == null) return current;
        return PlatformConfigurationNormalizer.hasStatusTraits(current)
                ? ComponentValidator.materializeComponents(accumulated, List.of(current))
                : current;
    }

    private ChainStart resolveChainStart(List<CertificateIdentifierTrait> chain) {
        int base = -1;
        int rebase = -1;
        int baseCount = 0;
        for (int index = 0; index < chain.size(); index++) {
            CertificateIdentifierTrait trait = chain.get(index);
            if (trait == null) continue;
            if (ComponentValidator.isBaseTrait(trait)) {
                base = index;
                baseCount++;
            } else if (ComponentValidator.isRebaseTrait(trait)) {
                rebase = index;
            }
        }
        if (baseCount > 1) {
            error("Previous platform certificates contain more than one base certificate.");
            return null;
        }
        int start = rebase >= 0 ? rebase : base;
        if (start < 0) {
            error("No base or rebase certificate found in PreviousPlatformCertificates.");
            return null;
        }
        return new ChainStart(start);
    }

    private Map<CertificateIdentifier, PlatformConfigurationV3> loadPrevious(List<File> files) {
        Map<CertificateIdentifier, PlatformConfigurationV3> resolved = new HashMap<>();
        files.stream()
                .filter(file -> file != null && file.exists())
                .map(PlatformCertificate::load)
                .filter(java.util.Objects::nonNull)
                .forEach(certificate -> Optional.ofNullable(certificate.canonicalizedPlatformConfigurationV3())
                        .ifPresent(configuration -> resolved.put(certificate.getCertificateIdentifier(), configuration)));
        return resolved;
    }

    private boolean hasPreviousCertificates() {
        return previousPlatformCertificates != null && !previousPlatformCertificates.isEmpty();
    }

    private boolean detailsEnabled() {
        String level = Optional.ofNullable(logLevel).orElse("").trim().toUpperCase();
        return "DEBUG".equals(level) || "TRACE".equals(level);
    }

    private void report(String message) {
        if (!quiet) System.out.println(message);
    }

    private void error(String message) {
        if (!quiet) System.err.println(message);
    }

    private record ChainStart(int index) {}
    private record ChainProgress(PlatformConfigurationV3 configuration, boolean failed) {
        private static ChainProgress success(PlatformConfigurationV3 configuration) {
            return new ChainProgress(configuration, false);
        }

        private static ChainProgress failure() {
            return new ChainProgress(null, true);
        }
    }

    private static final class ValidateMatcher {
        private static ComponentMatcher resolve(String name) {
            return Optional.ofNullable(name)
                    .map(value -> value.toUpperCase(java.util.Locale.ROOT))
                    .filter(value -> value.equals("RAW") || value.equals("STRICT"))
                    .map(value -> ComponentMatcher.RAW)
                    .orElse(ComponentMatcher.NORMALIZED);
        }
    }
}
