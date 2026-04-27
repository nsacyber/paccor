package cli;

import cert.CertKind;
import cert.CertSpecVersion;
import cert.CertType;
import cert.CertificateProfile;
import cert.CertificateResolver;
import cert.CertTypeResolver;
import cert.ExtensionAssembler;
import cert.PlatformCertificate;
import cert.TbsFinalizer;
import cert.TbsEnvelope;
import cli.pv.BigIntegerConverter;
import cli.pv.CertKindConverter;
import cli.pv.CertTypeConverter;
import cli.pv.DateConverter;
import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import json.AttributesJsonHelper;
import json.ExtensionsJsonHelper;
import json.HardwareManifestJsonHelper;
import json.ObjectMapperFactory;
import model.PlatformCertificateInformationModel;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.encoders.Base64;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import crypto.SignatureProfiles;
import tcg.credential.CertificateIdentifier;
import tcg.credential.CertificateIdentifierTrait;
import tcg.credential.TCGCredentialType;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.TCGSpecificationVersion;
import tcg.credential.TraitMap;

/**
 * Generate the PlatformCertificateInformationModel using direct import or JSON data files.
 * Build the to-be-signed envelope from the model.
 */
@Command(name = "certgen", mixinStandardHelpOptions = true, description = "Generate Platform Certificate data")
public class CertGenCmd implements Callable<Integer>, HasCommonOptions {
    @Mixin
    private CommonOptions common;

    // JSON data
    @Option(names = { "-p", "--attributes-json" }, description = "Attributes JSON file")
    private File attrsJson;

    @Option(names = { "-c", "--components-json" }, description = "Hardware manifest components JSON file")
    private File componentsJson;

    @Option(names = { "-x", "--extensions-json" }, description = "Extensions JSON file")
    private File extJson;

    @Option(names = "--in-platform-model", description = "Existing model data from JSON")
    private File platformInfoJson;

    @Option(names = "--in", description = "Existing to-be-signed data to merge from JSON")
    private File inJson;

    // Most relevant certificates. Other certificates may be specified in the JSON.
    @Option(names = { "-P", "--issuer-cert" }, description = "Issuer certificate file")
    private File issuerCert;

    @Option(names = { "-e", "--holder-cert" }, description = "Holder/Subject certificate file")
    private File holderCert;

    // Platform Certificate options required prior to finalization
    @Option(names = { "--kind", "--cert-kind" }, description = "Certificate output kind (AC, PKC)", converter = {CertKindConverter.class})
    private CertKind certKind;

    @Option(names = { "--type", "--cert-type" }, description = "Platform certificate type (base, delta, rebase)", converter = {CertTypeConverter.class})
    private CertType certType;

    @Option(names = { "-N", "--serial" }, description = "Certificate serial number", converter = {BigIntegerConverter.class})
    private BigInteger serial;

    @Option(names = { "-b", "--not-before" }, description = DateConverter.DATE_FORMAT, converter = {DateConverter.class})
    private Date notBefore;

    @Option(names = { "-a", "--not-after" }, description = DateConverter.DATE_FORMAT, converter = {DateConverter.class})
    private Date notAfter;

    @Option(names = "--sig-profile", description = "Signature profile ID")
    private String sigProfile;

    // Output options
    @Option(names = { "-f", "--out" }, required = true, description = "Model data and context in JSON. Can be given to the assemble command")
    private File outJson;

    @Option(names = "--finalize", description = "Validate model data and context prior to output")
    private boolean finalizeFlag;

    @Option(names = "--overwrite-in-place", description = "Allow in-place overwrite when --in equals --out.")
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
            applyHolderOrSubject(pi, profile);
            maybeAttachPreviousPlatformCertificates(pi, profile);
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
        if (pi.getPreviousPlatformCertificates() != null) return;
        if (profile.outputType() != CertKind.AC) return;

        PlatformCertificate pc = PlatformCertificate.loadSafe(holderCert);
        if (pc == null || pc.certKind() != CertKind.AC) return;
        CertificateIdentifier id = pc.getCertificateIdentifier();
        CertType prevType = pc.getCertType();

        ASN1ObjectIdentifier category = Optional.ofNullable(CertTypeResolver.toTraitCategory(prevType))
                .orElse(TCGObjectIdentifier.tcgTrCatPlatformCertificate);

        CertificateIdentifierTrait trait = CertificateIdentifierTrait.builder()
                .traitCategory(category)
                .traitValue(id)
                .build();

        pi.setPreviousPlatformCertificates(TraitMap.fromTraits(List.of(trait)));
    }
}
