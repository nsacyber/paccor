package paccor.validator;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import lombok.Builder;
import org.bouncycastle.cert.X509CertificateHolder;
import paccor.cert.CertType;
import paccor.cert.PlatformCertificate;
import paccor.cert.PlatformCertificateHolders;
import paccor.tcg.credential.CertificateIdentifier;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.validator.PreviousCertificateLoader.ResolvedPrevious;

/** Validates and materializes the previous-platform-certificate chain. */
@Builder
public final class PreviousPlatformCertificateValidator {
    private static final Logger LOGGER = Logger.getLogger(PreviousPlatformCertificateValidator.class.getName());

    private final List<File> previousPlatformCertificates;
    private final X509CertificateHolder issuerCertificate;
    private final List<X509CertificateHolder> trustAnchors;
    private final ComponentMatcher matcher;

    public PlatformConfigurationV3 materialize(PlatformCertificate certificate, PlatformConfigurationV3 current) {
        // A delta without a platform configuration attribute records no component changes.
        PlatformConfigurationV3 leaf = Optional.ofNullable(current)
                .orElseGet(() ->
                        certificate.isDelta()
                                ? PlatformConfigurationV3.builder().build()
                                : null);
        if (leaf == null) {
            return null;
        }

        List<File> files = Optional.ofNullable(previousPlatformCertificates).orElse(List.of());
        if (files.isEmpty()) {
            return certificate.requiresPreviousPlatformCertificates() ? null : leaf;
        }

        List<ResolvedPrevious> resolved = PreviousCertificateLoader.builder()
                .issuerCertificate(issuerCertificate)
                .trustAnchors(trustAnchors)
                .build()
                .load(files);
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
                .filter(previous -> PlatformCertificateHolders.holderMatches(certificate, previous.certificate()))
                .findFirst()
                .map(previous -> mergeCurrent(certificate, previous.configuration(), current))
                .orElseGet(() -> {
                    LOGGER.warning("No previous platform certificate matches the holder of the certificate being validated.");
                    return null;
                });
    }

    private PlatformConfigurationV3 materializeChain(PlatformCertificate certificate, List<CertificateIdentifierTrait> chain, List<ResolvedPrevious> resolved, PlatformConfigurationV3 current) {
        return resolveChainStart(chain)
                .map(start -> applyResolvedChain(chain, resolved, start))
                .filter(progress -> !progress.failed())
                .filter(progress -> certificate.getCertType() != null
                        && progress.configuration() != null
                        && PlatformCertificateHolders.holderConsistent(certificate, progress.anchor()))
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
                || !PlatformCertificateHolders.holderConsistent(previous.certificate(), progress.anchor())) {
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
        if (!certificate.isDelta()) {
            return current;
        }
        return ComponentValidator.materializeComponents(accumulated, List.of(current), componentMatcher())
                .orElse(null);
    }

    /**
     * The chain is applied from its last rebase, or else from its single base.
     * @return index of the entry to start from, or empty if the chain cannot be applied
     */
    private Optional<Integer> resolveChainStart(List<CertificateIdentifierTrait> chain) {
        if (chain.stream().anyMatch(trait -> ComponentValidator.certTypeOf(trait).isEmpty())) {
            LOGGER.warning("Previous platform certificates contain an entry with an unrecognized category.");
            return Optional.empty();
        }
        if (chain.stream().filter(ComponentValidator::isBaseTrait).count() > 1) {
            LOGGER.warning("Previous platform certificates contain more than one base certificate.");
            return Optional.empty();
        }
        return lastIndexOf(chain, CertType.REBASE)
                .or(() -> lastIndexOf(chain, CertType.BASE))
                .or(() -> {
                    LOGGER.warning("No base or rebase certificate found in PreviousPlatformCertificates.");
                    return Optional.empty();
                });
    }

    private static Optional<Integer> lastIndexOf(List<CertificateIdentifierTrait> chain, CertType type) {
        return IntStream.range(0, chain.size()).boxed()
                .filter(index -> ComponentValidator.certTypeOf(chain.get(index)).filter(type::equals).isPresent())
                .reduce((earlier, later) -> later);
    }

    private ComponentMatcher componentMatcher() {
        return Optional.ofNullable(matcher).orElse(ComponentMatcher.NORMALIZED);
    }

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
