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
import org.bouncycastle.cert.X509CertificateHolder;
import paccor.cert.CertType;
import paccor.cert.PlatformCertificate;
import paccor.cli.GlobFileResolver;
import paccor.crypto.IssuerCertificateChecker;
import paccor.normalization.PlatformConfigurationNormalizer;
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

    public PlatformConfigurationV3 materialize(PlatformCertificate certificate, PlatformConfigurationV3 current) {
        if (current == null) {
            return null;
        }
        List<File> files = GlobFileResolver.resolve(previousPlatformCertificates);
        if (files.isEmpty()) {
            return current;
        }

        List<ResolvedPrevious> resolved = loadPrevious(files);
        List<CertificateIdentifierTrait> chain = certificate.previousPlatformCertificateTraits();
        return Optional.ofNullable(chain)
                .filter(values -> !values.isEmpty())
                .map(values -> materializeChain(certificate, values, resolved, current))
                .orElseGet(() -> materializeWithoutChain(certificate, resolved, current));
    }

    private PlatformConfigurationV3 materializeWithoutChain(PlatformCertificate certificate, List<ResolvedPrevious> resolved, PlatformConfigurationV3 current) {
        return resolved.stream()
                .findFirst()
                .filter(previous -> currentType(certificate)
                        .map(CertType.DELTA::equals)
                        .map(delta -> !delta || holderMatches(certificate, previous.certificate()))
                        .orElse(true))
                .map(ResolvedPrevious::configuration)
                .filter(PlatformConfigurationNormalizer::hasContent)
                .map(base -> PlatformConfigurationNormalizer.hasStatusTraits(current)
                        ? ComponentValidator.materializeComponents(base, List.of(current))
                        : current)
                .orElseGet(() -> Optional.ofNullable(current)
                        .filter(_ -> currentType(certificate)
                                .map(CertType.DELTA::equals)
                                .map(delta -> !delta)
                                .orElse(true))
                        .orElse(null));
    }

    private PlatformConfigurationV3 materializeChain(PlatformCertificate certificate, List<CertificateIdentifierTrait> chain, List<ResolvedPrevious> resolved, PlatformConfigurationV3 current) {
        return resolveChainStart(chain)
                .map(start -> applyResolvedChain(chain, resolved, start.index()))
                .filter(progress -> !progress.failed())
                .filter(progress -> currentType(certificate)
                        .map(CertType.DELTA::equals)
                        .map(delta -> !delta || (progress.configuration() != null
                                && holderMatches(certificate, progress.anchor())))
                        .orElse(true))
                .map(ChainProgress::configuration)
                .map(accumulated -> mergeCurrent(accumulated, current))
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
        PlatformConfigurationV3 next = previous.configuration();
        if (ComponentValidator.isDeltaTrait(trait)) {
            return Optional.of(next)
                    .filter(PlatformConfigurationNormalizer::hasStatusTraits)
                    .filter(_ -> progress.anchor() != null
                            && holderMatches(previous.certificate(), progress.anchor()))
                    .map(value -> ChainProgress.success(
                            Optional.ofNullable(progress.configuration())
                                    .map(configuration -> ComponentValidator.materializeComponents(
                                            configuration, List.of(value)))
                                    .orElse(value),
                            progress.anchor()))
                    .orElseGet(() -> {
                        LOGGER.warning("Previous delta certificate is invalid or does not identify the certificate it is applied on top: "
                                + trait.getTraitValue());
                        return ChainProgress.failure();
                    });
        }
        return Optional.of(trait)
                .filter(value -> ComponentValidator.isBaseTrait(value) || ComponentValidator.isRebaseTrait(value))
                .map(_ -> ChainProgress.success(next, previous.certificate()))
                .orElseGet(() -> ChainProgress.success(progress.configuration(), progress.anchor()));
    }

    private ChainProgress missingTrait(CertificateIdentifier identifier) {
        LOGGER.warning("Missing previous platform certificate: " + identifier);
        return ChainProgress.failure();
    }

    private PlatformConfigurationV3 mergeCurrent(PlatformConfigurationV3 accumulated, PlatformConfigurationV3 current) {
        return Optional.ofNullable(accumulated)
                .filter(_ -> PlatformConfigurationNormalizer.hasStatusTraits(current))
                .map(value -> ComponentValidator.materializeComponents(value, List.of(current)))
                .orElse(current);
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
                .filter(file -> file != null && file.exists())
                .map(PlatformCertificate::loadSafe)
                .filter(Objects::nonNull)
                .filter(this::isPreviousSignatureValid)
                .map(certificate -> Optional.ofNullable(certificate.canonicalizedPlatformConfigurationV3())
                        .map(cfg -> new ResolvedPrevious(certificate, cfg))
                        .orElse(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private boolean isPreviousSignatureValid(PlatformCertificate certificate) {
        IssuerCertificateChecker checker = new IssuerCertificateChecker();
        return Optional.ofNullable(issuerCertificate)
                .map(issuer -> checker.validateSignature(certificate, issuer))
                .orElse(false)
                || Optional.ofNullable(trustAnchors)
                        .stream()
                        .flatMap(List::stream)
                        .anyMatch(anchor -> checker.validateSignature(certificate, anchor));
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

    private static boolean targetIssuer(PlatformCertificate target, X500Name issuer) {
        return Arrays.asList(target.getAttributeCertificate().getIssuer().getNames()).contains(issuer);
    }

    private static Optional<CertType> currentType(PlatformCertificate certificate) {
        return Optional.ofNullable(certificate).map(PlatformCertificate::getCertType);
    }

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
