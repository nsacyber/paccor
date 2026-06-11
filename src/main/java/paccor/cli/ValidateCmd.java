package paccor.cli;

import paccor.cert.PlatformCertificate;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import paccor.json.ObjectMapperFactory;
import paccor.normalization.PlatformConfigurationNormalizer;
import paccor.crypto.PcBcContentVerifierProviderBuilder;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import paccor.tcg.credential.CertificateIdentifier;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.PlatformConfiguration;
import paccor.tcg.credential.PlatformConfigurationV2;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitMap;
import paccor.validator.ComponentValidationReport;
import paccor.validator.ComponentMatcher;
import paccor.validator.ComponentValidator;
import paccor.validator.SpecificationValidationReport;
import paccor.validator.SpecificationValidator;

@Command(name = "validate", mixinStandardHelpOptions = true, description = "Validate signature, and optionally components and certificate profile")
public class ValidateCmd implements Callable<Integer>, HasCommonOptions {
    @Mixin private CommonOptions common;

    @Option(names = { "-X", "--x509v2AttrCert"/*backwards compatibility*/, "--pkcPlatformCert" }, description = "Platform certificate file", required = true)
    private File platformCertFile;
    @Option(names = { "-P", "--publicKeyCert"/*backwards compatibility*/, "--issuer-cert" }, description = "Signer certificate file")
    private File signerFile;
    @Option(names = { "-c", "--components-json" }, description = "Components JSON to verify against AC components")
    private File componentsJson;
    @Option(names = { "--component-matcher" }, description = "Component matcher: NORMALIZED (default) or RAW")
    private String componentMatcherName;
    @Option(names = "--prev-pcert", description = "Previous platform certificate file(s). Repeatable. Globs allowed.")
    private List<String> prevPcerts;
    @Override
    public CommonOptions commonOptions() {
        return common;
    }

    @Override
    public Integer call() {
        PlatformCertificate certificate = PlatformCertificate.load(platformCertFile);
        if (certificate == null) {
            if (!common.quiet) {
                System.out.println("Could not read platform certificate provided.");
            }
            return reportOverall(false, ClientExitCodes.USAGE_ERROR).code();
        }

        boolean signatureOk = checkSignatureOptionAndValidate(certificate, signerFile);
        boolean specificationOk = validateSpecification(certificate);
        boolean componentsOk = checkComponentsOptionAndValidate(certificate, componentsJson);

        return reportOverall(componentsOk && signatureOk && specificationOk).code();
    }

    private ContentVerifierProvider buildVerifierProvider(X509CertificateHolder signer) throws OperatorCreationException {
        DefaultDigestAlgorithmIdentifierFinder digFinder = new DefaultDigestAlgorithmIdentifierFinder();
        return new PcBcContentVerifierProviderBuilder(digFinder).build(signer);
    }

    private boolean reportComponents(boolean compsOk) {
        if (!common.quiet) {
            System.out.println("Components validation: " + (compsOk ? "OK" : "FAILED"));
        }
        return compsOk;
    }
    private boolean reportSignature(boolean sigOk) {
        if (!common.quiet) {
            System.out.println("Signature validation: " + (sigOk ? "OK" : "FAILED"));
        }
        return sigOk;
    }
    private boolean reportSpecification(SpecificationValidationReport report) {
        if (!common.quiet) {
            System.out.println("Specification validation: " + (report.ok() ? "OK" : "FAILED"));
        }
        return report.ok();
    }
    private ClientExitCodes reportOverall(boolean ok) {
        return reportOverall(ok, ok ? ClientExitCodes.SUCCESS : ClientExitCodes.VALIDATION_FAILED);
    }
    private ClientExitCodes reportOverall(boolean ok, ClientExitCodes exitCode) {
        if (!common.quiet) {
            System.out.println("Platform Certificate validation: " + (ok ? "OK" : "FAILED"));
        }
        return exitCode;
    }

    private boolean checkSignatureOptionAndValidate(PlatformCertificate certificate, File signerFile) {
        if (signerFile == null || !signerFile.exists()) {
            if (!common.quiet) {
                System.out.println("Signature validation: Skipped. No issuer certificate provided.");
            }
            return false;
        }
        X509CertificateHolder signer = CliHelper.loadPKCSafe(signerFile.getPath());
        if (signer == null) {
            if (!common.quiet) {
                System.out.println("Signature validation: Skipped. Could not read signer certificate provided.");
            }
            return false;
        }

        return reportSignature(validateSignature(certificate, signer));
    }

    private boolean validateSignature(PlatformCertificate certificate, X509CertificateHolder signer) {
        try {
            ContentVerifierProvider cvp = buildVerifierProvider(signer);
            return certificate != null && certificate.isSignatureValid(cvp);
        } catch (OperatorCreationException | CertException ignored) {}
        return false;
    }

    private boolean validateSpecification(PlatformCertificate certificate) {
        SpecificationValidationReport report = SpecificationValidator.validate(certificate);
        if (!report.ok() && shouldPrintDetails()) {
            String detail = report.detail();
            if (!detail.isBlank()) {
                System.out.println(detail);
            }
        }
        return reportSpecification(report);
    }

    private boolean checkComponentsOptionAndValidate(PlatformCertificate certificate, File jsonFile) {
        if (jsonFile == null || !jsonFile.exists()) {
            if (!common.quiet) {
                System.out.println("Component validation: Skipped. No components JSON provided.");
            }
            return false;
        }
        return reportComponents(validateComponents(certificate, jsonFile));
    }

    private boolean validateComponents(PlatformCertificate certificate, File jsonFile) {
        ComponentMatcher matcher = resolveMatcher(componentMatcherName);
        if (certificate.requiresPreviousPlatformCertificates() && !hasPrevPcerts()) {
            if (!common.quiet) {
                System.err.println("Component validation for delta or rebase certificates requires --prev-pcert.");
            }
            return false;
        }
        PlatformConfiguration expectV1 = ObjectMapperFactory.fromJsonSafe(jsonFile, PlatformConfiguration.class);
        PlatformConfigurationV2 expectV2 = ObjectMapperFactory.fromJsonSafe(jsonFile, PlatformConfigurationV2.class);
        PlatformConfigurationV3 expectV3 = ObjectMapperFactory.fromJsonSafe(jsonFile, PlatformConfigurationV3.class);

        List<TraitMap> expected = normalizeExpected(
                expectV1,
                expectV2,
                expectV3,
                certificate.hasAttribute(TCGObjectIdentifier.tcgAtPlatformConfigurationV1),
                certificate.hasAttribute(TCGObjectIdentifier.tcgAtPlatformConfigurationV2));

        PlatformConfigurationV3 actual = certificate.canonicalizedPlatformConfigurationV3();
        PlatformConfigurationV3 materialized = materializeWithPrevious(certificate, actual);
        if (hasPrevPcerts() && materialized == null) {
            return false;
        }
        if (materialized != null) {
            return compareNormalized(expected, PlatformConfigurationNormalizer.componentsForValidation(materialized), matcher);
        }
        return false;
    }

    private List<TraitMap> normalizeExpected(
            PlatformConfiguration expectV1,
            PlatformConfigurationV2 expectV2,
            PlatformConfigurationV3 expectV3,
            boolean requireV1Compatibility,
            boolean requireV2Compatibility) {
        if (requireV1Compatibility && PlatformConfigurationNormalizer.hasContent(expectV1)) {
            return PlatformConfigurationNormalizer.componentsForValidation(expectV1);
        }
        if (requireV1Compatibility && PlatformConfigurationNormalizer.hasContent(expectV3)) {
            PlatformConfiguration downcast = PlatformConfigurationNormalizer.toV1(expectV3);
            if (downcast == null) {
                if (shouldPrintDetails()) {
                    System.out.println("Expected component JSON cannot be represented as PlatformConfiguration.");
                }
                return null;
            }
            return PlatformConfigurationNormalizer.componentsForValidation(downcast);
        }
        if (requireV2Compatibility && PlatformConfigurationNormalizer.hasContent(expectV3)) {
            PlatformConfigurationV2 downcast = PlatformConfigurationNormalizer.toV2(expectV3);
            if (downcast == null) {
                if (shouldPrintDetails()) {
                    System.out.println("Expected component JSON cannot be represented as PlatformConfigurationV2.");
                }
                return null;
            }
            return PlatformConfigurationNormalizer.componentsForValidation(downcast);
        }
        if (PlatformConfigurationNormalizer.hasContent(expectV3)) {
            return PlatformConfigurationNormalizer.componentsForValidation(expectV3);
        }
        if (PlatformConfigurationNormalizer.hasContent(expectV2)) {
            return PlatformConfigurationNormalizer.componentsForValidation(expectV2);
        }
        if (PlatformConfigurationNormalizer.hasContent(expectV1)) {
            return PlatformConfigurationNormalizer.componentsForValidation(expectV1);
        }
        return List.of();
    }

    private boolean compareNormalized(List<TraitMap> expected, List<TraitMap> actual, ComponentMatcher matcher) {
        if (expected == null) {
            return false;
        }
        ComponentValidationReport report = ComponentValidator.compareComponents(expected, actual, matcher);
        if (!report.ok() && shouldPrintDetails()) {
            System.out.println(report.detail());
        }
        return report.ok();
    }

    private boolean shouldPrintDetails() {
        if (common == null || common.logLevel == null) {
            return false;
        }
        String logLevel = common.logLevel.trim().toUpperCase(Locale.ROOT);
        return "DEBUG".equals(logLevel) || "TRACE".equals(logLevel);
    }

    public static ComponentMatcher resolveMatcher() {
        return resolveMatcher("NORMALIZED");
    }

    public static ComponentMatcher resolveMatcher(String componentMatcherName) {
        String name = Optional.ofNullable(componentMatcherName).orElse("NORMALIZED");
        switch (name.toUpperCase(Locale.ROOT)) {
            case "NORMALIZED":
            case "DEFAULT":
            case "PCI_AWARE":
                return ComponentMatcher.NORMALIZED;
            case "RAW":
            case "STRICT":
                return ComponentMatcher.RAW;
            default:
                System.err.println("Unknown --component-matcher: " + name + ", using NORMALIZED");
                return ComponentMatcher.NORMALIZED;
        }
    }

    private PlatformConfigurationV3 materializeWithPrevious(PlatformCertificate certificate, PlatformConfigurationV3 current) {
        if (current == null) return null;
        List<File> files = resolvePrevCertFiles();
        if (files.isEmpty()) return current;

        Map<CertificateIdentifier, PlatformConfigurationV3> resolved = loadPrevCerts(files);
        List<CertificateIdentifierTrait> chain = certificate.previousPlatformCertificateTraits();
        if (chain == null || chain.isEmpty()) {
            return materializeWithoutChain(resolved, current);
        }
        return materializeChain(chain, resolved, current);
    }

    private PlatformConfigurationV3 materializeChain(List<CertificateIdentifierTrait> chain,
                                                     Map<CertificateIdentifier, PlatformConfigurationV3> resolved,
                                                     PlatformConfigurationV3 current) {
        if (chain == null || chain.isEmpty()) {
            return current;
        }

        ChainStart chainStart = resolveChainStart(chain);
        if (chainStart == null) {
            return null;
        }

        PlatformConfigurationV3 accumulated = applyResolvedChain(chain, resolved, chainStart.startIndex());
        return mergeCurrentConfiguration(accumulated, current);
    }

    private PlatformConfigurationV3 materializeWithoutChain(Map<CertificateIdentifier, PlatformConfigurationV3> resolved,
                                                            PlatformConfigurationV3 current) {
        if (resolved == null || resolved.isEmpty()) return current;
        PlatformConfigurationV3 base = resolved.values().stream().findFirst().orElse(null);
        if (base == null) return current;
        if (PlatformConfigurationNormalizer.hasStatusTraits(current)) {
            return ComponentValidator.materializeComponents(base, List.of(current));
        }
        return current;
    }

    private List<File> resolvePrevCertFiles() {
        if (prevPcerts == null || prevPcerts.isEmpty()) return List.of();
        List<File> out = new ArrayList<>();
        for (String spec : prevPcerts) {
            if (spec == null || spec.isBlank()) continue;
            if (hasGlob(spec)) {
                out.addAll(expandGlob(spec));
            } else {
                out.add(new File(spec));
            }
        }
        return out.stream().distinct().toList();
    }

    private boolean hasPrevPcerts() {
        return prevPcerts != null && !prevPcerts.isEmpty();
    }

    private static boolean hasGlob(String spec) {
        return spec.contains("*") || spec.contains("?") || spec.contains("[");
    }

    private static List<File> expandGlob(String pattern) {
        Path full = Paths.get(pattern);
        Path base = findGlobRoot(full);
        String normalizedPattern = pattern.replace("\\", "/");
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + normalizedPattern);

        List<File> out = new ArrayList<>();
        try (Stream<Path> stream = Files.walk(base)) {
            boolean isAbs = full.isAbsolute();
            stream.filter(p -> matcher.matches(isAbs ? p : base.relativize(p)))
                    .forEach(p -> out.add(p.toFile()));
        } catch (Exception ignored) { }
        return out;
    }

    private static Path findGlobRoot(Path path) {
        Path root = path.getRoot();
        Path acc = root;
        for (Path part : path) {
            String s = part.toString();
            if (s.contains("*") || s.contains("?") || s.contains("[")) {
                break;
            }
            acc = (acc == null) ? part : acc.resolve(part);
        }
        return acc != null ? acc : Paths.get(".");
    }

    private Map<CertificateIdentifier, PlatformConfigurationV3> loadPrevCerts(List<File> files) {
        Map<CertificateIdentifier, PlatformConfigurationV3> out = new HashMap<>();
        for (File f : files) {
            loadPreviousCertificate(out, f);
        }
        return out;
    }

    private ChainStart resolveChainStart(List<CertificateIdentifierTrait> chain) {
        int baseIndex = -1;
        int lastRebaseIndex = -1;
        int baseCount = 0;
        for (int i = 0; i < chain.size(); i++) {
            CertificateIdentifierTrait trait = chain.get(i);
            if (trait == null) {
                continue;
            }
            if (ComponentValidator.isBaseTrait(trait)) {
                baseIndex = i;
                baseCount++;
                continue;
            }
            if (ComponentValidator.isRebaseTrait(trait)) {
                lastRebaseIndex = i;
            }
        }

        if (baseCount > 1) {
            printError("Previous platform certificates contain more than one base certificate.");
            return null;
        }

        int startIndex = lastRebaseIndex >= 0 ? lastRebaseIndex : baseIndex;
        if (startIndex < 0) {
            printError("No base or rebase certificate found in PreviousPlatformCertificates.");
            return null;
        }
        return new ChainStart(startIndex);
    }

    private PlatformConfigurationV3 applyResolvedChain(
            List<CertificateIdentifierTrait> chain,
            Map<CertificateIdentifier, PlatformConfigurationV3> resolved,
            int startIndex) {
        PlatformConfigurationV3 accumulated = null;
        for (int i = startIndex; i < chain.size(); i++) {
            ChainProgress progress = applyResolvedTrait(accumulated, chain.get(i), resolved);
            if (progress.failed()) {
                return null;
            }
            accumulated = progress.accumulated();
        }
        return accumulated;
    }

    private ChainProgress applyResolvedTrait(
            PlatformConfigurationV3 accumulated,
            CertificateIdentifierTrait trait,
            Map<CertificateIdentifier, PlatformConfigurationV3> resolved) {
        if (isEmptyTrait(trait)) {
            return ChainProgress.success(accumulated);
        }

        PlatformConfigurationV3 next = resolvePreviousConfiguration(trait, resolved);
        if (next == null) {
            return ChainProgress.failure();
        }
        if (ComponentValidator.isDeltaTrait(trait)) {
            PlatformConfigurationV3 updated = applyDeltaConfiguration(accumulated, next, trait);
            return updated != null ? ChainProgress.success(updated) : ChainProgress.failure();
        }
        if (resetsAccumulatedChain(trait)) {
            return ChainProgress.success(next);
        }
        return ChainProgress.success(accumulated);
    }

    private boolean isEmptyTrait(CertificateIdentifierTrait trait) {
        return trait == null || trait.getTraitValue() == null;
    }

    private boolean resetsAccumulatedChain(CertificateIdentifierTrait trait) {
        return ComponentValidator.isBaseTrait(trait) || ComponentValidator.isRebaseTrait(trait);
    }

    private PlatformConfigurationV3 resolvePreviousConfiguration(
            CertificateIdentifierTrait trait,
            Map<CertificateIdentifier, PlatformConfigurationV3> resolved) {
        if (trait == null || trait.getTraitValue() == null) {
            return null;
        }
        PlatformConfigurationV3 configuration = resolved.get(trait.getTraitValue());
        if (configuration == null) {
            printError("Missing previous platform certificate: " + trait.getTraitValue());
        }
        return configuration;
    }

    private PlatformConfigurationV3 applyDeltaConfiguration(
            PlatformConfigurationV3 accumulated,
            PlatformConfigurationV3 delta,
            CertificateIdentifierTrait trait) {
        if (delta == null) {
            return null;
        }
        if (!PlatformConfigurationNormalizer.hasStatusTraits(delta)) {
            printError("Delta certificate without StatusTrait is not supported: " + trait.getTraitValue());
            return null;
        }
        return accumulated == null ? delta : ComponentValidator.materializeComponents(accumulated, List.of(delta));
    }

    private PlatformConfigurationV3 mergeCurrentConfiguration(
            PlatformConfigurationV3 accumulated,
            PlatformConfigurationV3 current) {
        if (accumulated == null) {
            return current;
        }
        if (PlatformConfigurationNormalizer.hasStatusTraits(current)) {
            return ComponentValidator.materializeComponents(accumulated, List.of(current));
        }
        return current;
    }

    private void loadPreviousCertificate(Map<CertificateIdentifier, PlatformConfigurationV3> out, File file) {
        if (file == null || !file.exists()) {
            return;
        }
        PlatformCertificate certificate = PlatformCertificate.load(file);
        if (certificate != null) {
            putResolvedConfiguration(out, certificate.getCertificateIdentifier(), certificate.canonicalizedPlatformConfigurationV3());
        }
    }

    private void putResolvedConfiguration(
            Map<CertificateIdentifier, PlatformConfigurationV3> out,
            CertificateIdentifier identifier,
            PlatformConfigurationV3 configuration) {
        if (configuration != null) {
            out.put(identifier, configuration);
        }
    }

    private void printError(String message) {
        if (!common.quiet) {
            System.err.println(message);
        }
    }

    private record ChainStart(int startIndex) {}
    private record ChainProgress(PlatformConfigurationV3 accumulated, boolean failed) {
        private static ChainProgress success(PlatformConfigurationV3 accumulated) {
            return new ChainProgress(accumulated, false);
        }

        private static ChainProgress failure() {
            return new ChainProgress(null, true);
        }
    }
}
