package paccor.cli;

import paccor.cert.PlatformCertificate;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import java.security.GeneralSecurityException;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertStore;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;
import paccor.cli.pv.ReadableFileConverter;
import paccor.json.HardwareManifestJsonHelper;
import paccor.normalization.PlatformConfigurationNormalizer;
import paccor.crypto.PcBcContentVerifierProviderBuilder;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.openssl.PEMParser;
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

    @Option(names = { CliOptionNames.PLATFORM_CERT_FILE_SHORT, CliOptionNames.X509V2_ATTR_CERT_LONG/*backwards compatibility*/, CliOptionNames.PKC_PLATFORM_CERT_LONG }, description = "Platform certificate file", required = true, converter = ReadableFileConverter.class)
    private File platformCertFile;
    @Option(names = { CliOptionNames.ISSUER_CERT_SHORT, CliOptionNames.ISSUER_CERT_LONG, CliOptionNames.PUBLIC_KEY_CERT_LONG/*backwards compatibility*/ }, description = "Signer certificate file", converter = ReadableFileConverter.class)
    private File signerFile;
    @Option(names = { CliOptionNames.COMPONENTS_JSON_SHORT, CliOptionNames.COMPONENTS_JSON_LONG }, description = "Components JSON to verify against AC components", converter = ReadableFileConverter.class)
    private File componentsJson;
    @Option(names = CliOptionNames.COMPONENT_MATCHER_LONG, description = "Component matcher: NORMALIZED (default) or RAW")
    private String componentMatcherName;
    @Option(names = CliOptionNames.PREV_PCERT_LONG, description = "Previous platform certificate file(s). Repeatable. Globs allowed.")
    private List<String> previousPlatformCertsList;
    @Option(names = CliOptionNames.TRUST_ANCHOR_LONG, description = "Trust-anchor certificate bundle(s). Repeatable. Globs allowed.")
    private List<String> trustAnchorList;
    @Option(names = CliOptionNames.CRL_LONG, description = "CRL file(s) for platform-certificate revocation checking. Repeatable. Globs allowed.")
    private List<String> crlList;
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
        boolean trustOk = checkTrustAnchorOptionAndValidate(signerFile);
        boolean crlOk = checkCrlOptionAndValidate(certificate, signerFile);
        boolean specificationOk = validateSpecification(certificate);
        boolean componentsOk = checkComponentsOptionAndValidate(certificate, componentsJson);

        return reportOverall(componentsOk && signatureOk && trustOk && crlOk && specificationOk).code();
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

    private boolean checkTrustAnchorOptionAndValidate(File signerFile) {
        if (trustAnchorList == null || trustAnchorList.isEmpty()) return true;
        if (signerFile == null || !signerFile.exists()) {
            return reportTrust(false, "No issuer certificate was provided.");
        }
        X509CertificateHolder signer = CliHelper.loadPKCSafe(signerFile.getPath());
        if (signer == null) return reportTrust(false, "Could not read issuer certificate.");
        boolean ok = validateTrustPath(signer, resolveFiles(trustAnchorList));
        return reportTrust(ok, ok ? null : "Issuer certificate does not chain to a supplied trust anchor.");
    }

    private boolean reportTrust(boolean ok, String detail) {
        if (!common.quiet) {
            System.out.println("Trust-anchor validation: " + (ok ? "OK" : "FAILED"));
            if (!ok && detail != null && shouldPrintDetails()) System.out.println(detail);
        }
        return ok;
    }

    private boolean validateTrustPath(X509CertificateHolder signer, List<File> files) {
        try {
            X509Certificate target = toJcaCertificate(signer);
            List<X509Certificate> all = loadJcaCertificates(files);
            Set<TrustAnchor> anchors = new HashSet<>();
            List<X509Certificate> intermediates = new ArrayList<>();
            for (X509Certificate cert : all) {
                if (isSelfSigned(cert)) anchors.add(new TrustAnchor(cert, null));
                else intermediates.add(cert);
            }
            if (anchors.isEmpty()) return false;
            for (TrustAnchor anchor : anchors) {
                if (target.equals(anchor.getTrustedCert())) return true;
            }
            java.security.cert.X509CertSelector selector = new java.security.cert.X509CertSelector();
            selector.setCertificate(target);
            PKIXBuilderParameters params = new PKIXBuilderParameters(anchors, selector);
            params.setRevocationEnabled(false);
            params.addCertStore(CertStore.getInstance("Collection",
                    new CollectionCertStoreParameters(intermediates)));
            CertPathBuilder.getInstance("PKIX").build(params);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean checkCrlOptionAndValidate(PlatformCertificate certificate, File signerFile) {
        if (crlList == null || crlList.isEmpty()) return true;
        if (signerFile == null || !signerFile.exists()) return reportCrl(false, "No issuer certificate was provided.");
        X509CertificateHolder signer = CliHelper.loadPKCSafe(signerFile.getPath());
        if (signer == null) return reportCrl(false, "Could not read issuer certificate.");
        boolean ok = validateCrls(certificate, signer, resolveFiles(crlList));
        return reportCrl(ok, ok ? null : "No applicable valid CRL was found, or the platform certificate is revoked.");
    }

    private boolean reportCrl(boolean ok, String detail) {
        if (!common.quiet) {
            System.out.println("CRL validation: " + (ok ? "OK" : "FAILED"));
            if (!ok && detail != null && shouldPrintDetails()) System.out.println(detail);
        }
        return ok;
    }

    private boolean validateCrls(PlatformCertificate platform, X509CertificateHolder signer, List<File> files) {
        try {
            X509Certificate issuer = toJcaCertificate(signer);
            java.math.BigInteger serial = platform.isPublicKeyCertificate()
                    ? platform.getPublicKeyCertificate().getSerialNumber()
                    : platform.getAttributeCertificate().getSerialNumber();
            X500Principal issuerName = issuer.getSubjectX500Principal();
            boolean applicable = false;
            for (X509CRL crl : loadCrls(files)) {
                if (!crl.getIssuerX500Principal().equals(issuerName)) continue;
                if (crl.getThisUpdate() == null || crl.getThisUpdate().after(new java.util.Date())) continue;
                if (crl.getNextUpdate() != null && crl.getNextUpdate().before(new java.util.Date())) continue;
                crl.verify(issuer.getPublicKey());
                applicable = true;
                if (crl.getRevokedCertificate(serial) != null) return false;
            }
            return applicable;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static X509Certificate toJcaCertificate(X509CertificateHolder holder)
            throws GeneralSecurityException, IOException {
        return (X509Certificate) CertificateFactory.getInstance("X.509")
                .generateCertificate(new ByteArrayInputStream(holder.getEncoded()));
    }

    private static boolean isSelfSigned(X509Certificate certificate) {
        try {
            certificate.verify(certificate.getPublicKey());
            return certificate.getSubjectX500Principal().equals(certificate.getIssuerX500Principal());
        } catch (GeneralSecurityException ignored) {
            return false;
        }
    }

    private static List<X509Certificate> loadJcaCertificates(List<File> files) throws Exception {
        List<X509Certificate> out = new ArrayList<>();
        for (File file : files) for (X509CertificateHolder holder : loadCertificateBundle(file)) out.add(toJcaCertificate(holder));
        return out;
    }

    private static List<X509CertificateHolder> loadCertificateBundle(File file) throws Exception {
        List<X509CertificateHolder> out = new ArrayList<>();
        byte[] bytes = Files.readAllBytes(file.toPath());
        try (PEMParser parser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.US_ASCII))) {
            Object object;
            while ((object = parser.readObject()) != null) if (object instanceof X509CertificateHolder holder) out.add(holder);
        } catch (Exception ignored) { }
        if (out.isEmpty()) out.add(new X509CertificateHolder(bytes));
        return out;
    }

    private static List<X509CRL> loadCrls(List<File> files) throws Exception {
        List<X509CRL> out = new ArrayList<>();
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        for (File file : files) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            boolean parsedPem = false;
            try (PEMParser parser = new PEMParser(new InputStreamReader(new ByteArrayInputStream(bytes), StandardCharsets.US_ASCII))) {
                Object object;
                while ((object = parser.readObject()) != null) {
                    if (object instanceof X509CRLHolder holder) {
                        out.add((X509CRL) factory.generateCRL(new ByteArrayInputStream(holder.getEncoded())));
                        parsedPem = true;
                    }
                }
            } catch (Exception ignored) { }
            if (!parsedPem) {
                Collection<? extends java.security.cert.CRL> crls = factory.generateCRLs(new ByteArrayInputStream(bytes));
                for (java.security.cert.CRL crl : crls) if (crl instanceof X509CRL x509crl) out.add(x509crl);
            }
        }
        return out;
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
        HardwareManifestJsonHelper manifest = HardwareManifestJsonHelper.readComponents(jsonFile);
        if (manifest == null) {
            return false;
        }

        List<TraitMap> expected = normalizeExpected(
                manifest.pcV1(),
                manifest.pcV2(),
                manifest.pcV3(),
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
        return tryV1Compat(expectV1, expectV3, requireV1Compatibility)
                .or(() -> tryV2Compat(expectV3, requireV2Compatibility))
                .or(() -> tryDirect(expectV3, expectV2, expectV1))
                .orElse(List.of());
    }

    private Optional<List<TraitMap>> tryV1Compat(
            PlatformConfiguration expectV1,
            PlatformConfigurationV3 expectV3,
            boolean requireV1Compatibility) {
        if (!requireV1Compatibility) return Optional.empty();
        if (PlatformConfigurationNormalizer.hasContent(expectV1)) {
            return Optional.of(PlatformConfigurationNormalizer.componentsForValidation(expectV1));
        }
        if (PlatformConfigurationNormalizer.hasContent(expectV3)) {
            return Optional.ofNullable(PlatformConfigurationNormalizer.toV1(expectV3))
                    .map(PlatformConfigurationNormalizer::componentsForValidation)
                    .or(() -> {
                        if (shouldPrintDetails()) {
                            System.out.println("Expected component JSON cannot be represented as PlatformConfiguration.");
                        }
                        return Optional.empty();
                    });
        }
        return Optional.empty();
    }

    private Optional<List<TraitMap>> tryV2Compat(
            PlatformConfigurationV3 expectV3,
            boolean requireV2Compatibility) {
        if (!requireV2Compatibility || !PlatformConfigurationNormalizer.hasContent(expectV3)) {
            return Optional.empty();
        }
        return Optional.ofNullable(PlatformConfigurationNormalizer.toV2(expectV3))
                .map(PlatformConfigurationNormalizer::componentsForValidation)
                .or(() -> {
                    if (shouldPrintDetails()) {
                        System.out.println("Expected component JSON cannot be represented as PlatformConfigurationV2.");
                    }
                    return Optional.empty();
                });
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
        return resolveFiles(previousPlatformCertsList);
    }

    private static List<File> resolveFiles(List<String> specs) {
        if (specs == null || specs.isEmpty()) return List.of();
        List<File> out = new ArrayList<>();
        for (String spec : specs) {
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
        return previousPlatformCertsList != null && !previousPlatformCertsList.isEmpty();
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
