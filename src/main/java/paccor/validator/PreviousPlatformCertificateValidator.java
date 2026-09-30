package paccor.validator;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import lombok.Builder;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import paccor.cert.CertSpecVersion;
import paccor.cert.CertType;
import paccor.cert.PlatformCertificate;
import paccor.cli.GlobFileResolver;
import paccor.crypto.IssuerCertificateChecker;
import paccor.tcg.credential.CertificateIdentifier;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.PlatformConfigurationV3;

/** Validates and materializes the previous-platform-certificate chain. */
@Builder
public final class PreviousPlatformCertificateValidator {
    private static final Logger LOGGER = Logger.getLogger(PreviousPlatformCertificateValidator.class.getName());

    private final List<String> previousPlatformCertificates;
    private final X509CertificateHolder issuerCertificate;
    private final List<X509CertificateHolder> trustAnchors;
    private final ComponentMatcher matcher;

    public PlatformConfigurationV3 materialize(PlatformCertificate certificate, PlatformConfigurationV3 current) {
        PlatformConfigurationV3 leaf = Optional.ofNullable(current)
                .orElseGet(() -> isDelta(certificate) ? emptyConfiguration() : null);
        if (leaf == null) {
            return null;
        }

        List<File> files = GlobFileResolver.resolve(previousPlatformCertificates);
        if (files.isEmpty()) {
            return certificate.requiresPreviousPlatformCertificates() ? null : leaf;
        }

        List<ResolvedPrevious> resolved = loadPrevious(files);
        List<CertificateIdentifierTrait> chain = certificate.previousPlatformCertificateTraits();
        // When a chain is present it is authoritative; a chain that fails must not fall back to holder matching.
        return chain != null && !chain.isEmpty()
                ? materializeChain(certificate, chain, resolved, leaf)
                : materializeWithoutChain(certificate, resolved, leaf);
    }

    private PlatformConfigurationV3 materializeWithoutChain(PlatformCertificate certificate, List<ResolvedPrevious> resolved, PlatformConfigurationV3 current) {
        if (!certificate.requiresPreviousPlatformCertificates()) {
            return current;
        }
        return resolved.stream()
                .filter(previous -> holderMatches(certificate, previous.certificate()))
                .findFirst()
                .map(previous -> mergeCurrent(certificate, previous.configuration(), current))
                .orElseGet(() -> {
                    LOGGER.warning("No previous platform certificate matches the holder of the certificate being validated.");
                    return null;
                });
    }

    private PlatformConfigurationV3 materializeChain(PlatformCertificate certificate, List<CertificateIdentifierTrait> chain, List<ResolvedPrevious> resolved, PlatformConfigurationV3 current) {
        return resolveChainStart(chain)
                .map(start -> applyResolvedChain(chain, resolved, start.index()))
                .filter(progress -> !progress.failed())
                .filter(progress -> currentType(certificate).isPresent()
                        && progress.configuration() != null
                        && holderConsistent(certificate, progress.anchor()))
                .map(progress -> mergeCurrent(certificate, progress.configuration(), current))
                .orElse(null);
    }

    private ChainProgress applyResolvedChain(List<CertificateIdentifierTrait> chain, List<ResolvedPrevious> resolved, int start) {
        ChainProgress progress = ChainProgress.initial();
        for (int index = start; index < chain.size() && !progress.failed(); index++) {
            progress = applyTrait(progress, chain.get(index), resolved);
        }
        return progress;
    }

    private ChainProgress applyTrait(ChainProgress progress, CertificateIdentifierTrait trait, List<ResolvedPrevious> resolved) {
        return Optional.ofNullable(trait)
                .flatMap(value -> resolved.stream()
                        .filter(r -> r.certificate().identifies(value.getTraitValue()))
                        .findFirst())
                .map(previous -> applyResolvedTrait(progress, trait, previous))
                .orElseGet(() -> Optional.ofNullable(trait)
                        .map(value -> missingTrait(value.getTraitValue()))
                        .orElse(progress));
    }

    private ChainProgress applyResolvedTrait(ChainProgress progress, CertificateIdentifierTrait trait, ResolvedPrevious previous) {
        Optional<CertType> declared = ComponentValidator.certTypeOf(trait);
        if (declared.isEmpty()) {
            LOGGER.warning("Previous platform certificate entry has an unrecognized category "
                    + trait.getTraitCategory() + ": " + trait.getTraitValue());
            return ChainProgress.failure();
        }
        CertType actual = previous.certificate().getCertType();
        if (actual != null && actual != declared.get()) {
            LOGGER.warning("Previous platform certificate is listed as " + declared.get()
                    + " but its credential type is " + actual + ": " + trait.getTraitValue());
            return ChainProgress.failure();
        }
        if (declared.get() != CertType.DELTA) {
            return ChainProgress.success(previous.configuration(), previous.certificate());
        }
        if (progress.configuration() == null || progress.anchor() == null
                || !holderConsistent(previous.certificate(), progress.anchor())) {
            LOGGER.warning("Previous delta certificate does not identify the certificate it is applied on top of: "
                    + trait.getTraitValue());
            return ChainProgress.failure();
        }
        return ComponentValidator.materializeComponents(progress.configuration(), List.of(previous.configuration()), componentMatcher())
                .map(configuration -> ChainProgress.success(configuration, progress.anchor()))
                .orElseGet(() -> {
                    LOGGER.warning("Previous delta certificate could not be applied: " + trait.getTraitValue());
                    return ChainProgress.failure();
                });
    }

    private ChainProgress missingTrait(CertificateIdentifier identifier) {
        LOGGER.warning("Missing previous platform certificate: " + identifier);
        return ChainProgress.failure();
    }

    private PlatformConfigurationV3 mergeCurrent(PlatformCertificate certificate, PlatformConfigurationV3 accumulated, PlatformConfigurationV3 current) {
        if (!isDelta(certificate)) {
            return current;
        }
        return ComponentValidator.materializeComponents(accumulated, List.of(current), componentMatcher())
                .orElse(null);
    }

    private Optional<ChainStart> resolveChainStart(List<CertificateIdentifierTrait> chain) {
        int base = -1;
        int rebase = -1;
        int baseCount = 0;
        for (int index = 0; index < chain.size(); index++) {
            CertificateIdentifierTrait trait = chain.get(index);
            if (ComponentValidator.isBaseTrait(trait)) {
                base = index;
                baseCount++;
            } else if (ComponentValidator.isRebaseTrait(trait)) {
                rebase = index;
            } else if (!ComponentValidator.isDeltaTrait(trait)) {
                LOGGER.warning("Previous platform certificates contain an entry with an unrecognized category: "
                        + (trait == null ? "null" : trait.getTraitCategory()));
                return Optional.empty();
            }
        }
        if (baseCount > 1) {
            LOGGER.warning("Previous platform certificates contain more than one base certificate.");
            return Optional.empty();
        }
        final int resolvedRebase = rebase;
        final int resolvedBase = base;

        return Optional.of(resolvedRebase)
                .filter(index -> index >= 0)
                .or(() -> Optional.of(resolvedBase).filter(index -> index >= 0))
                .map(ChainStart::new)
                .or(() -> {
                    LOGGER.warning("No base or rebase certificate found in PreviousPlatformCertificates.");
                    return Optional.empty();
                });
    }

    private List<ResolvedPrevious> loadPrevious(List<File> files) {
        return files.stream()
                .filter(this::isReadablePreviousFile)
                .map(this::loadPrevious)
                .filter(Objects::nonNull)
                .filter(previous -> isPreviousSignatureValid(previous.file(), previous.certificate()))
                .map(this::resolvePrevious)
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean isReadablePreviousFile(File file) {
        if (file == null || !file.exists()) {
            LOGGER.warning("Rejected previous platform certificate file " + file + ": file does not exist.");
            return false;
        }
        return true;
    }

    private LoadedPrevious loadPrevious(File file) {
        PlatformCertificate certificate = PlatformCertificate.loadSafe(file);
        if (certificate == null) {
            LOGGER.warning("Rejected previous platform certificate file " + file + ": could not be parsed.");
            return null;
        }
        return new LoadedPrevious(file, certificate);
    }

    private ResolvedPrevious resolvePrevious(LoadedPrevious previous) {
        // A delta without a platform configuration attribute records no component changes.
        PlatformConfigurationV3 configuration = Optional.ofNullable(previous.certificate().canonicalizedPlatformConfigurationV3())
                .orElseGet(() -> isDelta(previous.certificate()) ? emptyConfiguration() : null);
        if (configuration == null) {
            LOGGER.warning("Rejected previous platform certificate file " + previous.file()
                    + ": no supported platform configuration was found.");
            return null;
        }
        return new ResolvedPrevious(previous.certificate(), configuration);
    }

    private boolean isPreviousSignatureValid(File file, PlatformCertificate certificate) {
        IssuerCertificateChecker checker = new IssuerCertificateChecker();
        if (issuerCertificate != null && acceptsFromIssuer(certificate, issuerCertificate, checker)) {
            return true;
        }
        if (trustAnchors != null && trustAnchors.stream()
                .anyMatch(anchor -> acceptsFromIssuer(certificate, anchor, checker))) {
            return true;
        }

        String reason = issuerCertificate == null && (trustAnchors == null || trustAnchors.isEmpty())
                ? "no issuer certificate or trust anchors were configured"
                : "the previous certificate signature did not verify with a configured issuer, "
                + "or that issuer did not have a valid path to the configured trust anchors";
        LOGGER.warning("Rejected previous platform certificate file " + file + ": " + reason + ".");
        return false;
    }

    private boolean acceptsFromIssuer(
            PlatformCertificate certificate,
            X509CertificateHolder issuer,
            IssuerCertificateChecker checker) {
        if (!checker.validateSignature(certificate, issuer)) {
            return false;
        }
        return trustAnchors == null
                || trustAnchors.isEmpty()
                || checker.validateTrustPath(issuer, trustAnchors);
    }

    private static boolean holderMatches(PlatformCertificate delta, PlatformCertificate target) {
        if (delta == null || target == null || !delta.isAttributeCertificate()) return false;
        try {
            AttributeCertificateHolder holder = delta.getAttributeCertificate().getHolder();
            if (target.isPublicKeyCertificate()) {
                return (holder.getSerialNumber() != null || holder.getObjectDigest() != null)
                        && holder.match(target.getPublicKeyCertificate());
            }
            return target.isAttributeCertificate()
                    && holder.getSerialNumber() != null
                    && holder.getIssuer() != null
                    && holder.getSerialNumber().equals(target.serialNumber())
                    && Arrays.stream(holder.getIssuer()).anyMatch(issuer -> targetIssuer(target, issuer));
        } catch (Exception ignored) {
            return false;
        }
    }

    private static Optional<AttributeCertificateHolder> roTHolder(PlatformCertificate certificate) {
        return Optional.ofNullable(certificate)
                .filter(PlatformCertificate::isAttributeCertificate)
                .map(PlatformCertificate::getAttributeCertificate)
                .map(X509AttributeCertificateHolder::getHolder)
                .filter(holder -> holder.getSerialNumber() != null
                        && holder.getIssuer() != null);
    }

    private static boolean sameRoTHolder(PlatformCertificate a, PlatformCertificate b) {
        return roTHolder(a)
                .flatMap(left -> roTHolder(b)
                        .map(right -> Objects.equals(
                                left.getSerialNumber(),
                                right.getSerialNumber())
                                && Arrays.equals(
                                left.getIssuer(),
                                right.getIssuer())))
                .orElse(false);
    }

    /**
     * V1.1 deltas name the certificate they update as their holder. V2.0 deltas and rebases share
     * that certificate's holder.
     */
    private static boolean holderConsistent(PlatformCertificate delta, PlatformCertificate anchor) {
        if (delta != null && delta.resolvedSpecVersion() == CertSpecVersion.V1_1) {
            return holderMatches(delta, anchor);
        }
        return holderConsistentV2(delta, anchor);
    }

    private static boolean holderConsistentV2(PlatformCertificate delta, PlatformCertificate anchor) {
        if (delta == null || anchor == null) {
            return false;
        }
        // PKC has no Holder
        if (!delta.isAttributeCertificate() && !anchor.isAttributeCertificate()) {
            return true;
        }
        if (delta.isAttributeCertificate() != anchor.isAttributeCertificate()) {
            return false;
        }
        return sameRoTHolder(delta, anchor);
    }

    private static boolean targetIssuer(PlatformCertificate target, X500Name issuer) {
        return Arrays.asList(target.getAttributeCertificate().getIssuer().getNames()).contains(issuer);
    }

    private static Optional<CertType> currentType(PlatformCertificate certificate) {
        return Optional.ofNullable(certificate).map(PlatformCertificate::getCertType);
    }

    private static boolean isDelta(PlatformCertificate certificate) {
        return currentType(certificate).filter(CertType.DELTA::equals).isPresent();
    }

    private static PlatformConfigurationV3 emptyConfiguration() {
        return PlatformConfigurationV3.builder().build();
    }

    private ComponentMatcher componentMatcher() {
        return Optional.ofNullable(matcher).orElse(ComponentMatcher.NORMALIZED);
    }

    private record LoadedPrevious(File file, PlatformCertificate certificate) {}
    private record ResolvedPrevious(PlatformCertificate certificate, PlatformConfigurationV3 configuration) {}
    private record ChainStart(int index) {}
    private record ChainProgress(PlatformConfigurationV3 configuration, PlatformCertificate anchor, boolean failed) {
        private static ChainProgress initial() {
            return new ChainProgress(null, null, false);
        }

        private static ChainProgress success(PlatformConfigurationV3 configuration, PlatformCertificate anchor) {
            return new ChainProgress(configuration, anchor, false);
        }

        private static ChainProgress failure() {
            return new ChainProgress(null, null, true);
        }
    }
}
