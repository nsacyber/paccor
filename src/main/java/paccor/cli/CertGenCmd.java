package paccor.cli;

import paccor.cert.CertKind;
import paccor.cert.CertSpecVersion;
import paccor.cert.CertType;
import paccor.cert.CertificateProfile;
import paccor.cert.CertificateResolver;
import paccor.cert.CertificateIdentifierChain;
import paccor.cert.CertTypeResolver;
import paccor.cert.ExtensionAssembler;
import paccor.cert.PlatformCertificate;
import paccor.cert.TbsFinalizer;
import paccor.cert.TbsEnvelope;
import paccor.cli.pv.BigIntegerConverter;
import paccor.cli.pv.CertKindConverter;
import paccor.cli.pv.CertTypeConverter;
import paccor.cli.pv.DateConverter;
import paccor.cli.pv.OutFileConverter;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.concurrent.Callable;
import paccor.cli.pv.ReadableFileConverter;
import paccor.json.AttributesJsonHelper;
import paccor.json.ExtensionsJsonHelper;
import paccor.json.HardwareManifestJsonHelper;
import paccor.json.ObjectMapperFactory;
import paccor.model.PlatformCertificateInformationModel;
import paccor.model.HolderInfo;
import paccor.model.CertificateReference;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.encoders.Base64;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import paccor.crypto.SignatureProfiles;
import paccor.tcg.credential.TCGCredentialType;
import paccor.tcg.credential.TCGSpecificationVersion;

/**
 * Generate the PlatformCertificateInformationModel using direct import or JSON data files.
 * Build the to-be-signed envelope from the model.
 */
@Command(name = "certgen", mixinStandardHelpOptions = true, description = "Generate Platform Certificate data")
public class CertGenCmd implements Callable<Integer>, HasCommonOptions {
    @Mixin
    private CommonOptions common;

    // JSON data
    @Option(names = { CliOptionNames.ATTRIBUTES_JSON_SHORT, CliOptionNames.ATTRIBUTES_JSON_LONG }, description = "Attributes JSON file", converter = ReadableFileConverter.class)
    private File attrsJson;

    @Option(names = { CliOptionNames.COMPONENTS_JSON_SHORT, CliOptionNames.COMPONENTS_JSON_LONG }, description = "Hardware manifest components JSON file", converter = ReadableFileConverter.class)
    private File componentsJson;

    @Option(names = { CliOptionNames.EXTENSIONS_JSON_SHORT, CliOptionNames.EXTENSIONS_JSON_LONG }, description = "Extensions JSON file", converter = ReadableFileConverter.class)
    private File extJson;

    @Option(names = CliOptionNames.IN_PLATFORM_MODEL_LONG, description = "Existing model data from JSON", converter = ReadableFileConverter.class)
    private File platformInfoJson;

    @Option(names = CliOptionNames.IN_LONG, description = "Existing to-be-signed data to merge from JSON", converter = ReadableFileConverter.class)
    private File inJson;

    @Option(names = CliOptionNames.PREV_PCERT_LONG,
            description = "Single previous platform certificate used as the V2.0 chain seed. Use previousPlatformCertificates JSON for additional entries.",
            split = ",")
    private List<String> previousPlatformCerts;

    // Most relevant certificates. Other certificates may be specified in the JSON.
    @Option(names = { CliOptionNames.ISSUER_CERT_SHORT, CliOptionNames.ISSUER_CERT_LONG }, description = "Issuer certificate file", converter = ReadableFileConverter.class)
    private File issuerCert;

    @Option(names = { CliOptionNames.HOLDER_CERT_SHORT, CliOptionNames.HOLDER_CERT_LONG }, description = "Holder/Subject certificate file", converter = ReadableFileConverter.class)
    private File holderCert;

    // Platform Certificate options required prior to finalization
    @Option(names = { CliOptionNames.CERT_KIND_LONG_ALT, CliOptionNames.CERT_KIND_LONG }, description = "Certificate output kind (AC, PKC)", converter = {CertKindConverter.class})
    private CertKind certKind;

    @Option(names = { CliOptionNames.CERT_TYPE_LONG_ALT, CliOptionNames.CERT_TYPE_LONG }, description = "Platform certificate type (base, delta, rebase)", converter = {CertTypeConverter.class})
    private CertType certType;

    @Option(names = { CliOptionNames.SERIAL_SHORT, CliOptionNames.SERIAL_LONG }, description = "Certificate serial number", converter = {BigIntegerConverter.class})
    private BigInteger serial;

    @Option(names = { CliOptionNames.NOT_BEFORE_SHORT, CliOptionNames.NOT_BEFORE_LONG }, description = DateConverter.DATE_FORMAT, converter = {DateConverter.class})
    private Date notBefore;

    @Option(names = { CliOptionNames.NOT_AFTER_SHORT, CliOptionNames.NOT_AFTER_LONG }, description = DateConverter.DATE_FORMAT, converter = {DateConverter.class})
    private Date notAfter;

    @Option(names = CliOptionNames.SIG_PROFILE_LONG, description = "Signature profile ID")
    private String sigProfile;

    // Output options
    @Option(names = { CliOptionNames.FILE_OUT_SHORT, CliOptionNames.FILE_OUT_LONG }, required = true, description = "Model data and context in JSON. Can be given to the assemble command", converter = OutFileConverter.class)
    private File outJson;

    @Option(names = CliOptionNames.FINALIZE_LONG, description = "Validate model data and context prior to output")
    private boolean finalizeFlag;

    @Option(names = CliOptionNames.OVERWRITE_IN_PLACE_LONG, description = "Allow in-place overwrite when --in equals --out.")
    private boolean overwriteInPlace;

    @Override
    public CommonOptions commonOptions() {
        return common;
    }

    @Override
    public Integer call() throws Exception {
        if (!validateOutputPath()) {
            return ClientExitCodes.USAGE_ERROR.code();
        }
        if (previousPlatformCerts != null && previousPlatformCerts.size() > 1) {
            common.printError("--prev-pcert accepts one chain seed; use previousPlatformCertificates JSON for additional history.");
            return ClientExitCodes.USAGE_ERROR.code();
        }

        final TbsEnvelope existingEnv = (inJson != null && inJson.exists()) ? TbsEnvelope.read(inJson) : null;
        CertKind resolvedType = CertificateResolver.resolveKind(certKind, holderCert, existingEnv);
        PlatformCertificateInformationModel pi = buildPlatformInfo(existingEnv);

        CertificateProfile profile;
        try {
            profile = resolveProfile(pi, existingEnv, resolvedType);
        } catch (IllegalArgumentException e) {
            common.printError(e.getMessage());
            return ClientExitCodes.USAGE_ERROR.code();
        }

        applyConvenienceOverrides(pi, profile);
        try {
            applyCredentialTypeDefaults(pi, profile);
        } catch (IllegalArgumentException e) {
            common.printError(e.getMessage());
            return ClientExitCodes.USAGE_ERROR.code();
        }
        AlgorithmIdentifier algId = normalizeAlgorithmIdentifier(resolveAlg(existingEnv));

        TbsFinalizer rebuild = TbsFinalizer.rebuildTbsIfPossible(
                profile,
                pi,
                algId
        );

        TbsFinalizer.maybeFinalize(finalizeFlag, profile, pi, rebuild);

        TbsEnvelope env = TbsEnvelope.builder()
                .type(profile.outputType())
                .certSpecVersion(profile.specVersion())
                .tbsDerB64(rebuild.tbsB64())
                .sha256OfTbs(rebuild.shaHex())
                .sigAlgDerB64(algId != null ? Base64.toBase64String(algId.getEncoded()) : null)
                .platformInfoJson(serializePlatformInfo(pi))
                .build();

        ObjectMapperFactory.write(outJson, env);
        common.printInfo("Wrote TBS envelope to " + outJson.getAbsolutePath());
        return ClientExitCodes.SUCCESS.code();
    }

    private boolean validateOutputPath() {
        if (inJson == null || outJson == null || overwriteInPlace) {
            return true;
        }
        try {
            if (Files.isSameFile(inJson.toPath(), outJson.toPath())) {
                return rejectInPlaceOverwrite();
            }
        } catch (Exception ignored) {
            if (inJson.getAbsolutePath().equals(outJson.getAbsolutePath())) {
                return rejectInPlaceOverwrite();
            }
        }
        return true;
    }

    private boolean rejectInPlaceOverwrite() {
        common.printError("Refusing to overwrite input file. Use --overwrite-in-place for in-place update.");
        return false;
    }

    private PlatformCertificateInformationModel buildPlatformInfo(TbsEnvelope existingEnv) throws Exception {
        PlatformCertificateInformationModel pi = loadOrCreatePi(existingEnv);
        applyAttributes(pi);
        if (componentsJson != null) {
            pi.applyHardwareManifest(HardwareManifestJsonHelper.readComponents(componentsJson));
        }
        return pi;
    }

    private void applyAttributes(PlatformCertificateInformationModel pi) throws Exception {
        AttributesJsonHelper attributes = (attrsJson != null && attrsJson.exists()) ? AttributesJsonHelper.read(attrsJson) : null;
        if (attributes != null) {
            pi.applyAttributes(attributes);
        }
        appendExplicitPreviousPlatformCertificates(pi);
    }

    private CertificateProfile resolveProfile(
            PlatformCertificateInformationModel pi,
            TbsEnvelope existingEnv,
            CertKind resolvedType) {
        CertSpecVersion resolvedSpec = resolveSpecVersion(pi.getTcgCredentialSpecification(), existingEnv);
        return CertificateProfile.ofWithDefaults(resolvedSpec, resolvedType);
    }

    private void applyConvenienceOverrides(PlatformCertificateInformationModel pi, CertificateProfile profile) throws Exception {
        if (issuerCert != null) {
            pi.setIssuer(CertificateResolver.resolveIssuer(issuerCert));
        }
        if (holderCert != null) {
            maybeAttachPreviousPlatformCertificates(pi, profile);
            applyHolderOrSubject(pi, profile);
        }
        if (serial != null) {
            pi.setCertSerialNumber(serial);
        }
        if (notBefore != null) {
            pi.setNotBefore(notBefore);
        }
        if (notAfter != null) {
            pi.setNotAfter(notAfter);
        }
        if (extJson != null) {
            ExtensionAssembler.applyToPlatformInfo(pi, ExtensionsJsonHelper.read(extJson), CliHelper.loadCertSafe(issuerCert, CliHelper.x509type.CERTIFICATE));
        }
    }

    private void applyHolderOrSubject(PlatformCertificateInformationModel pi, CertificateProfile profile) {
        if (profile.outputType() == CertKind.AC) {
            CertType requestedType = certType != null ? certType : CertTypeResolver.inferCertType(pi);
            PlatformCertificate previous = PlatformCertificate.loadSafe(holderCert);
            if (profile.specVersion() == CertSpecVersion.V2_0
                    && requestedType == CertType.DELTA
                    && previous != null) {
                HolderInfo baseHolder = resolveLatestBaseOrRebaseHolder(pi, previous);
                if (baseHolder != null) {
                    pi.setHolder(baseHolder);
                    return;
                }
            }
            if (profile.specVersion() != CertSpecVersion.V2_0
                    && requestedType != CertType.BASE
                    && previous != null
                    && previous.isAttributeCertificate()) {
                HolderInfo previousHolder = CertificateResolver.resolvePlatformCertificateHolder(holderCert);
                if (previousHolder != null) {
                    pi.setHolder(previousHolder);
                    return;
                }
            }
            pi.setHolder(CertificateResolver.resolveHolder(holderCert, holderCert));
            return;
        }
        pi.setSubject(CertificateResolver.resolveSubject(holderCert));
    }

    private void applyCredentialTypeDefaults(PlatformCertificateInformationModel pi, CertificateProfile profile) {
        CertType effectiveType = certType != null ? certType : CertTypeResolver.inferCertType(pi);
        if (!CertTypeResolver.supportsCertType(profile.specVersion(), effectiveType)) {
            throw new IllegalArgumentException(profile.specVersion() + " does not support " + effectiveType + " certificates.");
        }

        TCGCredentialType resolved = CertTypeResolver.resolveTcgCredentialType(pi, profile.outputType(), certType, profile.specVersion());
        if (resolved != null) {
            pi.setTcgCredentialType(resolved);
        } else if (profile.specVersion() == CertSpecVersion.V1_0) {
            pi.setTcgCredentialType(null);
        }

        if (profile.specVersion() == CertSpecVersion.V1_0) {
            pi.setIsDelta(Boolean.FALSE);
        } else if (pi.getTcgCredentialType() != null) {
            pi.setIsDelta(CertTypeResolver.isDeltaOid(pi.getTcgCredentialType().getCertificateType()));
        } else if (pi.getIsDelta() == null) {
            pi.setIsDelta(CertTypeResolver.isDeltaCredential(pi, profile.outputType(), certType));
        }
    }

    private AlgorithmIdentifier normalizeAlgorithmIdentifier(AlgorithmIdentifier algId) {
        if (algId != null && algId.getParameters() == null) {
            return new AlgorithmIdentifier(algId.getAlgorithm(), DERNull.INSTANCE);
        }
        return algId;
    }

    private String serializePlatformInfo(PlatformCertificateInformationModel pi) {
        try {
            return ObjectMapperFactory.get().writeValueAsString(pi);
        } catch (Exception e) {
            common.printError("Warning: failed to serialize PlatformCertificateInformationModel: " + e.getMessage());
            return null;
        }
    }

    private CertSpecVersion resolveSpecVersion(TCGSpecificationVersion declaredSpec, TbsEnvelope existingEnv) {
        if (declaredSpec != null) {
            CertSpecVersion inferred = CertSpecVersion.fromTcgSpecVersion(declaredSpec);
            if (inferred == null) {
                throw new IllegalArgumentException(
                        "Unsupported TCG credential specification " + declaredSpec.describe() + ".");
            }
            return inferred;
        }
        if (existingEnv != null && existingEnv.getCertSpecVersion() != null) {
            return existingEnv.getCertSpecVersion();
        }
        return CertSpecVersion.V2_0;
    }

    private PlatformCertificateInformationModel loadOrCreatePi(TbsEnvelope existingEnv) {
        if (platformInfoJson != null && platformInfoJson.exists()) {
            try {
                return ObjectMapperFactory.get().readValue(platformInfoJson, PlatformCertificateInformationModel.class);
            } catch (Exception e) {
                common.printError("Warning: failed to load PlatformCertificateInformationModel from " + platformInfoJson + ": " + e.getMessage());
            }
        }
        if (existingEnv != null && existingEnv.getPlatformInfoJson() != null) {
            try {
                return ObjectMapperFactory.get().readValue(existingEnv.getPlatformInfoJson(), PlatformCertificateInformationModel.class);
            } catch (Exception ignored) { }
        }
        return new PlatformCertificateInformationModel();
    }

    private AlgorithmIdentifier resolveAlg(TbsEnvelope env) throws Exception {
        if (sigProfile != null && !sigProfile.isBlank()) {
            return SignatureProfiles.algIdFor(sigProfile);
        }
        if (issuerCert != null) {
            return SignatureProfiles.inferAlgIdFromIssuer(CliHelper.loadPKC(issuerCert.getPath()));
        }
        if (env != null && env.getSigAlgDerB64() != null) {
            try {
                return AlgorithmIdentifier.getInstance(ASN1Primitive.fromByteArray(Base64.decode(env.getSigAlgDerB64())));
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void maybeAttachPreviousPlatformCertificates(PlatformCertificateInformationModel pi, CertificateProfile profile) {
        if (pi == null || holderCert == null || profile == null) return;
        if (profile.outputType() != CertKind.AC) return;

        PlatformCertificate pc = PlatformCertificate.loadSafe(holderCert);
        if (pc == null || pc.certKind() != CertKind.AC) return;
        CertType requestedType = certType != null ? certType : CertTypeResolver.inferCertType(pi);

        if (profile.specVersion() != CertSpecVersion.V2_0
                || requestedType == CertType.BASE) {
            if (pi.getPreviousPlatformCertificates() != null) return;
        }

        CertificateIdentifierChain.append(
                pi,
                pc,
                profile.specVersion() == CertSpecVersion.V2_0 && requestedType != CertType.BASE);
    }

    private void appendExplicitPreviousPlatformCertificates(PlatformCertificateInformationModel pi) {
        if (previousPlatformCerts == null || previousPlatformCerts.isEmpty()) return;
        for (File file : GlobFileResolver.resolve(previousPlatformCerts)) {
            PlatformCertificate certificate = PlatformCertificate.loadSafe(file);
            if (certificate == null || certificate.getCertificateIdentifier() == null) continue;
            CertificateIdentifierChain.append(pi, certificate, true);
        }
    }

    /**
     * V2.0 delta holder is the holder of the referenced Base/Rebase
     * certificate, not a newly constructed reference to the -e certificate.
     */
    private HolderInfo resolveLatestBaseOrRebaseHolder(
            PlatformCertificateInformationModel pi,
            PlatformCertificate supplied) {
        return reversedReferences(pi)
                .flatMap(List::stream)
                .filter(this::isBaseOrRebase)
                .map(this::holderFromReference)
                .flatMap(Optional::stream)
                .findFirst()
                .or(() -> holderFromBaseOrRebase(supplied))
                .orElse(null);
    }

    private Stream<List<CertificateReference>> reversedReferences(PlatformCertificateInformationModel pi) {
        return Optional.ofNullable(pi.getPreviousPlatformCertificateObjects())
                .map(ArrayList::new)
                .map(this::reverse)
                .stream();
    }

    private List<CertificateReference> reverse(List<CertificateReference> references) {
        Collections.reverse(references);
        return references;
    }

    private boolean isBaseOrRebase(CertificateReference reference) {
        return reference != null
                && (reference.certType() == CertType.BASE || reference.certType() == CertType.REBASE);
    }

    private Optional<HolderInfo> holderFromReference(CertificateReference reference) {
        return Optional.ofNullable(reference.file())
                .map(File::new)
                .map(PlatformCertificate::loadSafe)
                .map(CertificateResolver::resolveHolder);
    }

    private Optional<HolderInfo> holderFromBaseOrRebase(PlatformCertificate certificate) {
        return Optional.ofNullable(certificate)
                .filter(candidate -> candidate.getCertType() == CertType.BASE
                        || candidate.getCertType() == CertType.REBASE)
                .map(CertificateResolver::resolveHolder);
    }
}
