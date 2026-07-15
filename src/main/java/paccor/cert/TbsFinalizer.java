package paccor.cert;

import paccor.crypto.AlgorithmSupport;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import paccor.model.ExtensionInfo;
import paccor.model.PlatformCertificateInformationModel;
import paccor.normalization.HexNormalizer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.util.encoders.Base64;
import paccor.tcg.credential.TBBSecurityAssertions;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.ComponentIdentifierV11Trait;
import paccor.tcg.credential.PlatformPropertiesV2;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

@Builder
public record TbsFinalizer(String tbsB64, String shaHex) {
    public static List<String> validateAc(CertificateProfile profile, PlatformCertificateInformationModel pi) {
        List<String> issues = checkCommonFields(pi);
        issues.addAll(checkSpecification(profile, pi));
        issues.addAll(checkAcFields(pi));
        validateTbbSecurityAssertions(profile.specVersion(), pi, issues);
        validateV2Traits(profile.specVersion(), pi, issues);
        mustHaveMandatoryExtensions(pi, issues);
        return issues;
    }

    public static List<String> validatePkc(CertificateProfile profile, PlatformCertificateInformationModel pi) {
        List<String> issues = checkCommonFields(pi);
        issues.addAll(checkSpecification(profile, pi));
        issues.addAll(checkPkcFields(pi));
        validateTbbSecurityAssertions(profile.specVersion(), pi, issues);
        validateV2Traits(profile.specVersion(), pi, issues);
        mustHaveMandatoryExtensions(pi, issues);
        mustHaveExtension(pi.getExtensions(), Extension.subjectKeyIdentifier, "Subject Key Identifier", issues);
        mustHaveExtension(pi.getExtensions(), Extension.basicConstraints, "Basic Constraints", issues, true);
        mustHavePkcExtendedKeyUsage(profile, pi, issues);
        validateBasicConstraints(pi.getExtensions(), issues);
        return issues;
    }

    private static void mustHaveMandatoryExtensions(PlatformCertificateInformationModel pi, List<String> issues) {
        mustHaveExtension(pi.getExtensions(), Extension.authorityKeyIdentifier, "Authority Key Identifier", issues);
        mustHaveExtension(pi.getExtensions(), Extension.certificatePolicies, "Certificate Policies", issues);
        mustHaveExtension(pi.getExtensions(), Extension.subjectAlternativeName, "Subject Alternative Name", issues);
    }

    private static List<String> checkCommonFields(PlatformCertificateInformationModel pi) {
        List<String> issues = new ArrayList<>();
        if (pi.getIssuer() == null || pi.getIssuer().nameDerB64() == null || pi.getIssuer().nameDerB64().isBlank()) {
            issues.add("Issuer is required.");
        }
        if (pi.getTcgCredentialSpecification() == null) {
            issues.add("TCG credential specification is required.");
        }
        if (pi.getCertSerialNumber() == null || pi.getCertSerialNumber().compareTo(BigInteger.ZERO) <= 0) {
            issues.add("Serial is required.");
        }
        if (pi.getNotBefore() == null || pi.getNotAfter() == null) {
            issues.add("Validity is required.");
        } else if (pi.getNotBefore().after(pi.getNotAfter())) {
            issues.add("Validity period is invalid: notBefore is later than notAfter.");
        }
        return issues;
    }

    private static List<String> checkSpecification(CertificateProfile profile, PlatformCertificateInformationModel pi) {
        List<String> issues = new ArrayList<>();
        if (pi == null || pi.getTcgCredentialSpecification() == null) {
            return issues;
        }

        CertSpecVersion inferred = CertSpecVersion.fromTcgSpecVersion(pi.getTcgCredentialSpecification());
        if (inferred == null) {
            issues.add("Unsupported TCG credential specification " + pi.describeCredSpec() + ".");
            return issues;
        }
        if (profile != null && inferred != profile.specVersion()) {
            issues.add("TCG credential specification maps to " + inferred
                    + " but the selected profile is " + profile.specVersion() + ".");
        }
        CertType certType = CertTypeResolver.inferCertType(pi);
        if (profile != null && !CertTypeResolver.supportsCertType(profile.specVersion(), certType)) {
            issues.add("Certificate type " + certType + " is not supported for " + profile.specVersion() + ".");
        }
        return issues;
    }

    private static List<String> checkAcFields(PlatformCertificateInformationModel pi) {
        List<String> issues = new ArrayList<>();
        if (pi.getHolder() == null) {
            issues.add("Holder is required for AC.");
        }
        return issues;
    }

    private static List<String> checkPkcFields(PlatformCertificateInformationModel pi) {
        List<String> issues = new ArrayList<>();
        if (pi.getSubject() == null || pi.getSubject().nameInfo().nameDerB64() == null) {
            issues.add("Subject is required for PKC finalize.");
        }
        if (pi.getSubject() == null || pi.getSubject().subjectPublicKeyInfoDerB64() == null) {
            issues.add("Subject Public Key Info is required for PKC finalize.");
        }
        return issues;
    }

    private static void mustHaveExtension(Map<String, ExtensionInfo> extensions,
                                          ASN1ObjectIdentifier oid,
                                          String name,
                                          List<String> issues) {
        boolean mustBeCritical = oid.equals(Extension.keyUsage) || oid.equals(Extension.targetInformation);
        mustHaveExtension(extensions, oid, name, issues, mustBeCritical);
    }

    private static void mustHaveExtension(Map<String, ExtensionInfo> extensions,
                                          ASN1ObjectIdentifier oid,
                                          String name,
                                          List<String> issues,
                                          boolean mustBeCritical) {
        ExtensionInfo e = (extensions != null) ? extensions.get(oid.getId()) : null;
        if (e == null) {
            issues.add(name + " extension (" + oid.getId() + ") is required.");
        } else if (mustBeCritical != e.critical()) {
            issues.add(name + " criticality must match specification.");
        }
    }

    private static void validateBasicConstraints(Map<String, ExtensionInfo> extensions, List<String> issues) {
        ExtensionInfo info = extensions != null ? extensions.get(Extension.basicConstraints.getId()) : null;
        if (info == null || info.valueDerB64() == null || info.valueDerB64().isBlank()) {
            return;
        }
        try {
            BasicConstraints constraints = BasicConstraints.getInstance(
                    ASN1Primitive.fromByteArray(Base64.decode(info.valueDerB64())));
            if (constraints.isCA()) {
                issues.add("Basic Constraints CA flag must be FALSE for PKC.");
            }
        } catch (Exception e) {
            issues.add("Basic Constraints extension value could not be decoded.");
        }
    }

    private static void mustHavePkcExtendedKeyUsage(
            CertificateProfile profile,
            PlatformCertificateInformationModel pi,
            List<String> issues) {
        Map<String, ExtensionInfo> extensions = pi != null ? pi.getExtensions() : null;
        ExtensionInfo info = extensions != null ? extensions.get(Extension.extendedKeyUsage.getId()) : null;
        if (info == null) {
            issues.add("Extended Key Usage extension (" + Extension.extendedKeyUsage.getId() + ") is required.");
            return;
        }
        if (info.critical()) {
            issues.add("Extended Key Usage criticality must match specification.");
            return;
        }

        try {
            ExtendedKeyUsage usage = ExtendedKeyUsage.getInstance(
                    ASN1Primitive.fromByteArray(Base64.decode(info.valueDerB64())));
            KeyPurposeId expected = KeyPurposeId.getInstance(resolveExpectedPkcKeyPurposeId(profile, pi));
            if (!usage.hasKeyPurposeId(expected)) {
                issues.add("Extended Key Usage must include " + expected.getId() + ".");
            }
        } catch (Exception e) {
            issues.add("Extended Key Usage extension value could not be decoded.");
        }
    }

    private static ASN1ObjectIdentifier resolveExpectedPkcKeyPurposeId(
            CertificateProfile profile,
            PlatformCertificateInformationModel pi) {
        CertType type = CertTypeResolver.inferCertType(pi);
        ASN1ObjectIdentifier oid = CertTypeResolver.toOid(CertKind.PKC, type);
        if (oid != null) {
            return oid;
        }
        if (profile != null && profile.outputType() == CertKind.PKC) {
            return CertTypeResolver.toOid(CertKind.PKC, CertType.BASE);
        }
        return Extension.extendedKeyUsage;
    }

    private static void validateTbbSecurityAssertions(
            CertSpecVersion specVersion,
            PlatformCertificateInformationModel pi,
            List<String> issues) {
        Optional.ofNullable(pi.getTbbSecurityAssertions())
                .filter(_ -> specVersion == CertSpecVersion.V2_0)
                .map(TBBSecurityAssertions::getTraits)
                .ifPresent(traits -> TraitMap.validateTraitMap(traits, "TBBSecurityAssertions", TBBSecurityAssertions.ALLOWED_TRAIT_TYPES, null, issues));
    }

    private static void validateV2Traits(CertSpecVersion specVersion, PlatformCertificateInformationModel pi, List<String> issues) {
        if (specVersion == CertSpecVersion.V2_0) {
            validatePlatformIdentifier(pi, issues);
            validateComponents(pi, issues);
            validatePlatformOwnership(pi, issues);
        }
    }

    private static void validatePlatformIdentifier(PlatformCertificateInformationModel pi, List<String> issues) {
        Optional.ofNullable(pi.getPlatformTraits())
                .filter(traits -> !traits.isEmpty())
                .ifPresentOrElse(
                        traits -> TraitMap.validateTraitMap(traits, "PlatformIdentifier", null,
                                Set.of(TCGObjectIdentifier.tcgTrCatPlatformManufacturer,
                                        TCGObjectIdentifier.tcgTrCatPlatformModel,
                                        TCGObjectIdentifier.tcgTrCatPlatformVersion),
                                issues),
                        () -> issues.add("Subject Alternative Name must contain a PlatformIdentifier sequence with traits.")
                );
    }

    private static void validateComponents(PlatformCertificateInformationModel pi, List<String> issues) {
        Optional.ofNullable(pi.getPlatformConfiguration()).ifPresent(pc -> {
            boolean isDelta = Boolean.TRUE.equals(pi.getIsDelta());
            validateComponentTraits(pc.getPlatformComponents(), isDelta, issues);
            if (isDelta) {
                validatePropertyStatus(pc.getPlatformProperties(), issues);
            }
        });
    }

    private static void validateComponentTraits(List<TraitMap> components, boolean isDelta, List<String> issues) {
        if (components == null) return;
        Set<ASN1ObjectIdentifier> requiredBase = Set.of(
                TCGObjectIdentifier.tcgTrCatComponentClass,
                TCGObjectIdentifier.tcgTrCatComponentManufacturer,
                TCGObjectIdentifier.tcgTrCatComponentModel
        );
        IntStream.range(0, components.size()).forEach(i -> {
            TraitMap component = components.get(i);
            String context = "Component[" + i + "]";
            if (component.containsKey(ComponentIdentifierV11Trait.class)) {
                validateV11Component(component, context, issues);
            } else {
                validateV20Component(component, context, isDelta, requiredBase, issues);
            }
        });
    }

    private static void validateV11Component(TraitMap component, String context, List<String> issues) {
        if (component.size() > 1) {
            issues.add(context + " containing ComponentIdentifierV11Trait SHALL NOT contain any other trait.");
        }
    }

    private static void validateV20Component(
            TraitMap component,
            String context,
            boolean isDelta,
            Set<ASN1ObjectIdentifier> requiredBase,
            List<String> issues) {
        Set<ASN1ObjectIdentifier> required = new HashSet<>(requiredBase);
        if (isDelta) {
            required.add(TCGObjectIdentifier.tcgTrCatComponentStatus);
        }
        TraitMap.validateTraitMap(component, context, null, required, issues);
    }

    private static void validatePropertyStatus(List<PlatformPropertiesV2> properties, List<String> issues) {
        if (properties == null) return;
        IntStream.range(0, properties.size())
                .filter(i -> properties.get(i).getStatus() == null)
                .forEach(i -> issues.add("Property[" + i + "] in Delta Platform Certificate SHALL contain the status field."));
    }

    private static void validatePlatformOwnership(PlatformCertificateInformationModel pi, List<String> issues) {
        Optional.ofNullable(pi.getPlatformOwnership())
                .filter(traits -> !traits.isEmpty())
                .ifPresent(traits -> TraitMap.validateTraitMap(traits, "PlatformOwnership", null, Set.of(TCGObjectIdentifier.tcgTrCatPlatformOwnership), issues));
    }

    /**
     * Build TBS directly from the model and profile.
     */
    public static TbsFinalizer rebuildTbsIfPossible(
            CertificateProfile profile,
            PlatformCertificateInformationModel platformInfo,
            AlgorithmIdentifier algId) {
        String tbsB64 = null;
        String shaHex = null;
        if (algId != null && platformInfo != null && platformInfo.getIssuer() != null) {
            var tbsBuilder = new TbsEncoder(platformInfo, profile);
            byte[] tbs = tbsBuilder.buildTbs(algId);
            tbsB64 = Base64.toBase64String(tbs);
            shaHex = HexNormalizer.toHexString(AlgorithmSupport.sha256(tbs));
        }
        return new TbsFinalizer(tbsB64, shaHex);
    }

    public static void maybeFinalize(boolean finalizeFlag, CertificateProfile profile, PlatformCertificateInformationModel pi, TbsFinalizer rr) {
        if (!finalizeFlag) return;
        List<String> issues = profile.outputType() == CertKind.AC
                ? validateAc(profile, pi)
                : validatePkc(profile, pi);
        if (!issues.isEmpty()) {
            issues.forEach(msg -> System.err.println("Finalize check: " + msg));
            throw new IllegalStateException("Finalize failed: profile constraints not met.");
        }
        if (rr.tbsB64() == null) throw new IllegalStateException("--finalize could not rebuild TBS; check inputs and sig-profile");
    }
}
