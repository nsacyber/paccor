package paccor.cert;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import paccor.model.CertificateReference;
import paccor.model.HolderInfo;
import paccor.model.PlatformCertificateInformationModel;

/**
 * Sets the holder (attribute certificates) or subject (public key certificates) of a certificate being
 * generated from the certificate given with --holder-cert. For deltas and rebases that certificate is
 * the previous platform certificate, and it is also added to the previous-certificate chain.
 */
public final class GeneratedHolderResolver {
    private GeneratedHolderResolver() {}

    /**
     * @param pi platform model to update
     * @param profile specification version and output kind
     * @param holderCert EK certificate, or the previous platform certificate for a delta or rebase
     * @param override certificate type requested on the command line, or null to infer it
     */
    public static void apply(PlatformCertificateInformationModel pi, CertificateProfile profile, File holderCert, CertType override) {
        if (profile.outputType() != CertKind.AC) {
            pi.setSubject(CertificateResolver.resolveSubject(holderCert));
            return;
        }
        PlatformCertificate previous = PlatformCertificate.loadSafe(holderCert);
        attachPrevious(pi, profile, previous, requestedType(pi, override));

        CertType requestedType = requestedType(pi, override);
        pi.setHolder(v2DeltaHolder(pi, profile, previous, requestedType)
                .or(() -> v1PreviousHolder(profile, previous, holderCert, requestedType))
                .orElseGet(() -> CertificateResolver.resolveHolder(holderCert, holderCert)));
    }

    private static CertType requestedType(PlatformCertificateInformationModel pi, CertType override) {
        return Optional.ofNullable(override).orElseGet(() -> CertTypeResolver.inferCertType(pi));
    }

    private static void attachPrevious(PlatformCertificateInformationModel pi, CertificateProfile profile,
                                       PlatformCertificate previous, CertType requestedType) {
        boolean v2Chain = profile.specVersion() == CertSpecVersion.V2_0 && requestedType != CertType.BASE;
        boolean attachable = previous != null
                && previous.certKind() == CertKind.AC
                && (v2Chain || pi.getPreviousPlatformCertificates() == null);
        if (attachable) {
            CertificateIdentifierChain.append(pi, previous, v2Chain);
        }
    }

    /**
     * A V2.0 delta holder is the holder of the referenced base or rebase certificate, not a newly
     * constructed reference to the previous certificate.
     */
    private static Optional<HolderInfo> v2DeltaHolder(PlatformCertificateInformationModel pi, CertificateProfile profile,
                                                      PlatformCertificate previous, CertType requestedType) {
        boolean applies = profile.specVersion() == CertSpecVersion.V2_0 && requestedType == CertType.DELTA && previous != null;
        return applies ? latestBaseOrRebaseHolder(pi, previous) : Optional.empty();
    }

    /**
     * Before V2.0, a delta holder names the previous platform certificate itself.
     */
    private static Optional<HolderInfo> v1PreviousHolder(CertificateProfile profile, PlatformCertificate previous,
                                                         File holderCert, CertType requestedType) {
        boolean applies = profile.specVersion() != CertSpecVersion.V2_0
                && requestedType != CertType.BASE
                && previous != null
                && previous.isAttributeCertificate();
        return applies ? Optional.ofNullable(CertificateResolver.resolvePlatformCertificateHolder(holderCert)) : Optional.empty();
    }

    private static Optional<HolderInfo> latestBaseOrRebaseHolder(PlatformCertificateInformationModel pi, PlatformCertificate supplied) {
        return newestFirst(pi.getPreviousPlatformCertificateObjects())
                .filter(reference -> reference != null && isBaseOrRebase(reference.certType()))
                .map(GeneratedHolderResolver::holderFromReference)
                .flatMap(Optional::stream)
                .findFirst()
                .or(() -> Optional.ofNullable(supplied)
                        .filter(certificate -> isBaseOrRebase(certificate.getCertType()))
                        .map(CertificateResolver::resolveHolder));
    }

    private static Stream<CertificateReference> newestFirst(List<CertificateReference> references) {
        List<CertificateReference> reversed = new ArrayList<>(Optional.ofNullable(references).orElse(List.of()));
        Collections.reverse(reversed);
        return reversed.stream();
    }

    private static boolean isBaseOrRebase(CertType type) {
        return type == CertType.BASE || type == CertType.REBASE;
    }

    private static Optional<HolderInfo> holderFromReference(CertificateReference reference) {
        return Optional.ofNullable(reference.file())
                .map(File::new)
                .map(PlatformCertificate::loadSafe)
                .map(CertificateResolver::resolveHolder);
    }
}
