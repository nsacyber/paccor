package paccor.cert;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.encoders.Base64;
import paccor.crypto.SignatureProfiles;
import paccor.json.AttributesJsonHelper;
import paccor.json.ExtensionsJsonHelper;
import paccor.json.HardwareManifestJsonHelper;
import paccor.model.PlatformCertificateInformationModel;
import paccor.model.SubjectInfo;

/**
 * Builds the to-be-signed envelope for a new platform certificate from a {@link CertGenRequest}.
 */
public final class CertGenService {
    private CertGenService() {}

    /**
     * @param request generation inputs
     * @return the envelope to write
     * @throws IllegalArgumentException if the inputs cannot produce a certificate
     * @throws Exception if an input file cannot be read
     */
    public static TbsEnvelope generate(CertGenRequest request) throws Exception {
        TbsEnvelope existing = readEnvelope(request.inEnvelope());
        PlatformCertificateInformationModel pi = buildModel(request, existing);
        CertificateProfile profile = CertificateProfile.ofWithDefaults(
                CertSpecVersion.resolve(pi.getTcgCredentialSpecification(), existing == null ? null : existing.getCertSpecVersion()),
                CertificateResolver.resolveKind(request.certKind(), request.holderCert(), existing));
        applyRequest(pi, profile, request);
        CertTypeResolver.applyDefaults(pi, profile, request.certType());
        AlgorithmIdentifier algId = SignatureProfiles.resolve(request.sigProfile(), request.issuerCertificate(), recordedAlgorithm(existing));
        return envelope(profile, pi, algId, request.finalizeTbs());
    }

    private static TbsEnvelope readEnvelope(File envelope) throws Exception {
        return envelope != null && envelope.exists() ? TbsEnvelope.read(envelope) : null;
    }

    private static PlatformCertificateInformationModel buildModel(CertGenRequest request, TbsEnvelope existing) throws Exception {
        PlatformCertificateInformationModel pi = PlatformCertificateInformationModel.loadOrCreate(
                request.platformModelJson(), existing == null ? null : existing.getPlatformInfoJson());
        if (request.attributesJson() != null && request.attributesJson().exists()) {
            pi.applyAttributes(AttributesJsonHelper.read(request.attributesJson()));
        }
        Optional.ofNullable(request.previousPlatformCert())
                .map(PlatformCertificate::loadSafe)
                .ifPresent(previous -> CertificateIdentifierChain.append(pi, previous, true));
        if (request.componentsJson() != null) {
            pi.applyHardwareManifest(HardwareManifestJsonHelper.readComponents(request.componentsJson()));
        }
        return pi;
    }

    private static void applyRequest(PlatformCertificateInformationModel pi, CertificateProfile profile, CertGenRequest request) throws Exception {
        Optional.ofNullable(request.issuerCertificate()).map(CertificateResolver::resolveIssuer).ifPresent(pi::setIssuer);
        Optional.ofNullable(request.holderCert()).ifPresent(holder -> GeneratedHolderResolver.apply(pi, profile, holder, request.certType()));
        if (profile.outputType() == CertKind.PKC) {
            applySubject(pi, request);
        }
        Optional.ofNullable(request.serial()).ifPresent(pi::setCertSerialNumber);
        Optional.ofNullable(request.notBefore()).ifPresent(pi::setNotBefore);
        Optional.ofNullable(request.notAfter()).ifPresent(pi::setNotAfter);
        if (request.extensionsJson() != null) {
            ExtensionAssembler.applyToPlatformInfo(pi, ExtensionsJsonHelper.read(request.extensionsJson()), request.issuerCertificate());
        }
    }

    private static void applySubject(PlatformCertificateInformationModel pi, CertGenRequest request) {
        Optional.ofNullable(request.subjectDn()).ifPresent(dn -> pi.setSubject(withSubjectName(pi.getSubject(), dn)));
        Optional.ofNullable(request.subjectKey()).ifPresent(key -> pi.setSubject(withSubjectKey(pi.getSubject(), key)));
    }

    private static SubjectInfo withSubjectName(SubjectInfo current, String dn) {
        try {
            return SubjectInfo.withName(current, new X500Name(dn));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid subject distinguished name: " + dn, e);
        }
    }

    private static SubjectInfo withSubjectKey(SubjectInfo current, File keyFile) {
        SubjectPublicKeyInfo publicKey = Optional.ofNullable(CertificateResolver.resolveSubjectPublicKeyInfo(keyFile))
                .orElseThrow(() -> new IllegalArgumentException("Could not read subject public key from " + keyFile
                        + ". Expected DER or PEM SubjectPublicKeyInfo."));
        SubjectInfo named = Optional.ofNullable(current)
                .filter(subject -> subject.nameInfo() != null && subject.resolvedSubjectName() != null)
                .orElseThrow(() -> new IllegalArgumentException("A subject name is required with a subject public key. "
                        + "Provide a subject distinguished name, or a platform model that contains one."));
        try {
            return named.withPublicKey(publicKey);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not encode subject public key from " + keyFile + ".", e);
        }
    }

    private static AlgorithmIdentifier recordedAlgorithm(TbsEnvelope existing) {
        try {
            return existing == null ? null : existing.decodeAlgId();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static TbsEnvelope envelope(CertificateProfile profile, PlatformCertificateInformationModel pi,
                                        AlgorithmIdentifier algId, boolean finalizeTbs) throws IOException {
        TbsFinalizer rebuild = TbsFinalizer.rebuildTbsIfPossible(profile, pi, algId);
        TbsFinalizer.maybeFinalize(finalizeTbs, profile, pi, rebuild);
        return TbsEnvelope.builder()
                .type(profile.outputType())
                .certSpecVersion(profile.specVersion())
                .tbsDerB64(rebuild.tbsB64())
                .sha256OfTbs(rebuild.shaHex())
                .sigAlgDerB64(algId != null ? Base64.toBase64String(algId.getEncoded()) : null)
                .platformInfoJson(pi.serializeOrNull())
                .build();
    }
}
