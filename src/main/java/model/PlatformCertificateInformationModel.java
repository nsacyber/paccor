package model;

import cert.CertType;
import cert.CertificateResolver;
import cert.ExtensionContext;
import cert.PlatformCertificate;
import cert.SubjectAlternativeNameHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import json.AttributesJsonHelper;
import json.HardwareManifestJsonHelper;
import json.ResolvedCertificateReferenceMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import normalization.PlatformConfigurationNormalizer;
import tcg.credential.ASN1Utils;
import tcg.credential.EnumWithStringValue;
import tcg.credential.PlatformConfiguration;
import tcg.credential.PlatformConfigurationV2;
import tcg.credential.PlatformConfigurationV3;
import tcg.credential.TBBSecurityAssertions;
import tcg.credential.TCPASpecVersion;
import tcg.credential.TCGCredentialType;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.TCGPlatformSpecification;
import tcg.credential.TCGSpecificationVersion;
import tcg.credential.TPMSecurityAssertions;
import tcg.credential.TPMSpecification;
import tcg.credential.TraitMap;

/**
 * Canonical platform certificate information model.
 * Stores certificate envelope data plus the canonical TCG ASN.1-backed objects used for generation and validation.
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlatformCertificateInformationModel {
    private BigInteger certSerialNumber;
    private Date notBefore;
    private Date notAfter;
    private Boolean isDelta;
    private HolderInfo holder;
    private NameInfo issuer;
    private SubjectInfo subject;
    private TCGPlatformSpecification tcgPlatformSpecification;
    private TCGSpecificationVersion tcgCredentialSpecification;
    private TBBSecurityAssertions tbbSecurityAssertions;
    private TCPASpecVersion tcpaSpecificationVersion;
    private String tpmManufacturer;
    private String tpmModel;
    private String tpmVersion;
    private TPMSpecification tpmSpecification;
    private TPMSecurityAssertions tpmSecurityAssertions;
    private TCGCredentialType tcgCredentialType;
    private TraitMap previousPlatformCertificates;
    private List<CertificateReference> previousPlatformCertificateObjects;
    private TraitMap cryptographicAnchors;
    private List<CertificateReference> cryptographicAnchorObjects;
    private PlatformConfigurationV3 platformConfiguration;
    private TraitMap platformTraits;
    private String platformConfigUri;
    private String componentIdentifiersUri;
    private String platformPropertiesUri;
    private ExtensionInfo subjectAlternativeName;
    private ExtensionInfo authorityKeyIdentifier;
    private ExtensionInfo certificatePolicies;
    private ExtensionInfo keyUsage;
    private ExtensionInfo extendedKeyUsage;
    private ExtensionInfo targetingInformation;
    private ExtensionInfo subjectKeyIdentifier;
    private ExtensionInfo basicConstraints;
    private ExtensionInfo crlDistributionPoints;
    private ExtensionInfo authorityInfoAccess;
    @Singular
    private Map<String, ExtensionInfo> extraExtensions;

    public String describeSubject() {
        if (subject != null) {
            return subject.describe();
        }
        if (holder != null) {
            return holder.describe();
        }
        return "unknown";
    }

    public String describeIssuer() {
        return issuer != null ? issuer.describe() : "unknown";
    }

    public String describeCredSpec() {
        return tcgCredentialSpecification != null ? tcgCredentialSpecification.describe() : "missing";
    }

    public TraitMap getPlatformTraits() {
        return platformTraits == null || platformTraits.isEmpty() ? null : platformTraits;
    }

    public void setPlatformTraits(TraitMap platformTraits) {
        this.platformTraits = platformTraits == null || platformTraits.isEmpty() ? null : platformTraits;
    }

    public void applyAttributes(AttributesJsonHelper attr) {
        if (attr == null) return;
        if (attr.tCGPlatformSpecification() != null) {
            this.tcgPlatformSpecification = attr.tCGPlatformSpecification();
        }
        if (attr.tCGCredentialSpecification() != null) {
            this.tcgCredentialSpecification = attr.tCGCredentialSpecification();
        }
        if (attr.tBBSecurityAssertions() != null) {
            this.tbbSecurityAssertions = attr.tBBSecurityAssertions();
        }
        if (attr.tCPASpecVersion() != null) {
            this.tcpaSpecificationVersion = attr.tCPASpecVersion();
        }
        if (attr.tPMManufacturer() != null) {
            this.tpmManufacturer = attr.tPMManufacturer().getString();
        }
        if (attr.tPMModel() != null) {
            this.tpmModel = attr.tPMModel().getString();
        }
        if (attr.tPMVersion() != null) {
            this.tpmVersion = attr.tPMVersion().getString();
        }
        if (attr.tPMSpecification() != null) {
            this.tpmSpecification = attr.tPMSpecification();
        }
        if (attr.tPMSecurityAssertions() != null) {
            this.tpmSecurityAssertions = attr.tPMSecurityAssertions();
        }
        if (attr.platformConfigUri() != null) {
            this.platformConfigUri = attr.platformConfigUri().getUniformResourceIdentifier().getString();
        }
        if (attr.previousPlatformCertificates() != null) {
            this.previousPlatformCertificates = TraitMap.getInstance(attr.previousPlatformCertificates());
            this.previousPlatformCertificateObjects =
                    ResolvedCertificateReferenceMap.referencesOf(attr.previousPlatformCertificates());
        }
        if (attr.cryptographicAnchors() != null) {
            this.cryptographicAnchors = TraitMap.getInstance(attr.cryptographicAnchors());
            this.cryptographicAnchorObjects =
                    ResolvedCertificateReferenceMap.referencesOf(attr.cryptographicAnchors());
        }
    }

    public void applyHardwareManifest(HardwareManifestJsonHelper hw) {
        if (hw == null) {
            return;
        }
        applySubjectAlternativeName(hw);
        applyPlatformConfiguration(hw);
    }

    private void applySubjectAlternativeName(HardwareManifestJsonHelper hw) {
        if (hw.platformTraits() == null || hw.platformTraits().isEmpty()) {
            return;
        }
        this.platformTraits = hw.platformTraits();
    }

    private void applyPlatformConfiguration(HardwareManifestJsonHelper hw) {
        if (hw.pcV3() != null
                && PlatformConfigurationNormalizer.hasContent(hw.pcV3())
                && (hw.pcV2() == null
                    || !PlatformConfigurationNormalizer.hasContent(hw.pcV2())
                    || hw.pcV3().getPlatformComponents().size() != hw.pcV2().getComponentIdentifiers().size())) {
            applyV3Config(hw.pcV3());
            return;
        }
        if (hw.pcV2() != null && PlatformConfigurationNormalizer.hasContent(hw.pcV2())) {
            applyV2Config(hw.pcV2());
            return;
        }
        if (hw.pcV1() != null && PlatformConfigurationNormalizer.hasContent(hw.pcV1())) {
            applyV1Config(hw.pcV1());
            return;
        }
        if (hw.pcV3() != null && PlatformConfigurationNormalizer.hasContent(hw.pcV3())) {
            applyV3Config(hw.pcV3());
        }
    }

    private Map<String, ExtensionInfo> ensureExtraExtensions() {
        if (this.extraExtensions == null) {
            this.extraExtensions = new HashMap<>();
        }
        return this.extraExtensions;
    }

    public void applyV1Config(PlatformConfiguration configuration) {
        this.platformConfiguration = PlatformConfigurationNormalizer.canonicalize(configuration);
        if (configuration.getPlatformPropertiesUri() != null) {
            this.platformPropertiesUri = configuration.getPlatformPropertiesUri().getUniformResourceIdentifier().getString();
        }
    }

    public void applyV2Config(PlatformConfigurationV2 configuration) {
        this.platformConfiguration = PlatformConfigurationNormalizer.canonicalize(configuration);
        if (configuration.getComponentIdentifiersUri() != null) {
            this.componentIdentifiersUri = configuration.getComponentIdentifiersUri().getUniformResourceIdentifier().getString();
        }
        if (configuration.getPlatformPropertiesUri() != null) {
            this.platformPropertiesUri = configuration.getPlatformPropertiesUri().getUniformResourceIdentifier().getString();
        }
    }

    public void applyV3Config(PlatformConfigurationV3 configuration) {
        this.platformConfiguration = PlatformConfigurationNormalizer.canonicalize(configuration);
    }

    @JsonIgnore
    public Map<String, ExtensionInfo> getExtensions() {
        Map<String, ExtensionInfo> extensions = new LinkedHashMap<>();
        addExtensionIfPresent(extensions, subjectAlternativeName);
        addExtensionIfPresent(extensions, authorityKeyIdentifier);
        addExtensionIfPresent(extensions, certificatePolicies);
        addExtensionIfPresent(extensions, keyUsage);
        addExtensionIfPresent(extensions, extendedKeyUsage);
        addExtensionIfPresent(extensions, targetingInformation);
        addExtensionIfPresent(extensions, subjectKeyIdentifier);
        addExtensionIfPresent(extensions, basicConstraints);
        addExtensionIfPresent(extensions, crlDistributionPoints);
        addExtensionIfPresent(extensions, authorityInfoAccess);
        if (extraExtensions != null) {
            extensions.putAll(extraExtensions);
        }
        return extensions.isEmpty() ? null : extensions;
    }

    @JsonSetter("extensions")
    public void setExtensions(Map<String, ExtensionInfo> extensions) {
        this.subjectAlternativeName = null;
        this.authorityKeyIdentifier = null;
        this.certificatePolicies = null;
        this.keyUsage = null;
        this.extendedKeyUsage = null;
        this.targetingInformation = null;
        this.subjectKeyIdentifier = null;
        this.basicConstraints = null;
        this.crlDistributionPoints = null;
        this.authorityInfoAccess = null;
        this.extraExtensions = null;

        if (extensions == null || extensions.isEmpty()) {
            return;
        }
        extensions.values().forEach(this::putExtension);
    }

    public void putExtension(ExtensionInfo extension) {
        if (extension == null || extension.oid() == null || extension.oid().isBlank()) {
            return;
        }

        switch (EnumWithStringValue.lookupValue(extension.oid(), ExtensionContext.class)) {
            case ExtensionContext.authorityInfoAccess -> authorityInfoAccess = extension;
            case ExtensionContext.authorityKeyIdentifier -> authorityKeyIdentifier = extension;
            case ExtensionContext.basicConstraints -> basicConstraints = extension;
            case ExtensionContext.certificatePolicies -> certificatePolicies = extension;
            case ExtensionContext.cRLDistributionPoints -> crlDistributionPoints = extension;
            case ExtensionContext.extendedKeyUsage -> extendedKeyUsage = extension;
            case ExtensionContext.keyUsage -> keyUsage = extension;
            case ExtensionContext.subjectAlternativeName -> subjectAlternativeName = extension;
            case ExtensionContext.subjectKeyIdentifier -> subjectKeyIdentifier = extension;
            case ExtensionContext.targetInformation -> targetingInformation = extension;
            default -> ensureExtraExtensions().put(extension.oid(), extension);
        }
    }

    public static PlatformCertificateInformationModel from(PlatformCertificate certificate) {
        if (certificate == null) {
            return null;
        }

        PlatformCertificateInformationModel model = new PlatformCertificateInformationModel();
        model.setCertSerialNumber(certificate.serialNumber());
        model.setNotBefore(certificate.extractNotBefore());
        model.setNotAfter(certificate.extractNotAfter());
        model.setIsDelta(certificate.getCertType() == CertType.DELTA);
        model.setHolder(CertificateResolver.resolveHolder(certificate));
        model.setSubject(CertificateResolver.resolveSubject(certificate));
        model.setIssuer(CertificateResolver.resolveIssuer(certificate));
        model.setTcgPlatformSpecification(certificate.platformSpecification());
        model.setTcgCredentialSpecification(certificate.declaredSpecification());
        model.setTbbSecurityAssertions(certificate.attributeValue(
                TCGObjectIdentifier.tcgAtTbbSecurityAssertions,
                TBBSecurityAssertions::getInstance));
        model.setTcpaSpecificationVersion(certificate.attributeValue(
                TCGObjectIdentifier.tcgTcpaSpecVersion,
                TCPASpecVersion::getInstance));
        model.setTpmManufacturer(ASN1Utils.getStringOrDefault(certificate.attributeValue(
                TCGObjectIdentifier.tcgAtTpmManufacturer,
                ASN1Utils::getUTF8String), null));
        model.setTpmModel(ASN1Utils.getStringOrDefault(certificate.attributeValue(
                TCGObjectIdentifier.tcgAtTpmModel,
                ASN1Utils::getUTF8String), null));
        model.setTpmVersion(ASN1Utils.getStringOrDefault(certificate.attributeValue(
                TCGObjectIdentifier.tcgAtTpmVersion,
                ASN1Utils::getUTF8String), null));
        model.setTpmSpecification(certificate.attributeValue(
                TCGObjectIdentifier.tcgAtTpmSpecification,
                TPMSpecification::getInstance));
        model.setTpmSecurityAssertions(certificate.attributeValue(
                TCGObjectIdentifier.tcgAtTpmSecurityAssertions,
                TPMSecurityAssertions::getInstance));
        model.setTcgCredentialType(certificate.getTcgCredentialType());
        model.setPreviousPlatformCertificates(certificate.traitMap(TCGObjectIdentifier.tcgAtPreviousPlatformCertificates));
        model.setCryptographicAnchors(certificate.traitMap(TCGObjectIdentifier.tcgAtCryptographicAnchors));
        model.setPlatformConfiguration(certificate.canonicalizedPlatformConfigurationV3());
        model.setPlatformTraits(SubjectAlternativeNameHelper.extractPlatformTraits(
                certificate.subjectAlternativeNames(),
                certificate.resolvedSpecVersion()));
        if (certificate.platformConfigUri() != null) {
            model.setPlatformConfigUri(certificate.platformConfigUri().getUniformResourceIdentifier().getString());
        }

        Map<String, ExtensionInfo> extensions = ExtensionInfo.extractExtensionInfo(certificate.extractExtensions());
        if (!extensions.isEmpty()) {
            model.setExtensions(extensions);
        }
        return model;
    }

    private static void addExtensionIfPresent(Map<String, ExtensionInfo> extensions, ExtensionInfo extension) {
        if (extension != null && extension.oid() != null && !extension.oid().isBlank()) {
            extensions.put(extension.oid(), extension);
        }
    }
}
