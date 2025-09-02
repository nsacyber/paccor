package cert;

import cli.CliHelper;
import java.io.File;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Vector;
import java.util.function.Function;
import lombok.Getter;
import lombok.NonNull;
import model.CertificateReference;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectDirectoryAttributes;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.CertException;
import org.bouncycastle.operator.ContentVerifierProvider;
import normalization.PlatformConfigurationNormalizer;
import tcg.credential.ASN1Utils;
import tcg.credential.CertificateIdentifier;
import tcg.credential.CertificateIdentifierTrait;
import tcg.credential.PlatformConfiguration;
import tcg.credential.PlatformConfigurationV2;
import tcg.credential.PlatformConfigurationV3;
import tcg.credential.TCGCredentialType;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.TCGPlatformSpecification;
import tcg.credential.TCGSpecificationVersion;
import tcg.credential.TraitMap;
import tcg.credential.URIReference;

/**
 * Unified AC/PKC facade for reading platform-certificate data without
 * branching at each call site.
 */
@Getter
public final class PlatformCertificate {
    private final File file;
    private final CertKind certKind;
    private final CertSpecVersion certSpecVersion;
    private final CertType certType;
    private final TCGCredentialType tcgCredentialType;
    private final CertificateIdentifier certificateIdentifier;
    private final X509AttributeCertificateHolder attributeCertificate;
    private final X509CertificateHolder publicKeyCertificate;

    private PlatformCertificate(
            File file,
            CertKind certKind,
            CertSpecVersion certSpecVersion,
            CertType certType,
            TCGCredentialType tcgCredentialType,
            CertificateIdentifier certificateIdentifier,
            X509AttributeCertificateHolder attributeCertificate,
            X509CertificateHolder publicKeyCertificate) {
        this.file = file;
        this.certKind = certKind;
        this.certSpecVersion = certSpecVersion;
        this.certType = certType;
        this.tcgCredentialType = tcgCredentialType;
        this.certificateIdentifier = certificateIdentifier;
        this.attributeCertificate = attributeCertificate;
        this.publicKeyCertificate = publicKeyCertificate;
    }

    public static PlatformCertificate load(@NonNull File file) {
        X509AttributeCertificateHolder ac = CliHelper.loadACSafe(file.getPath());
        if (ac != null) {
            return fromAttributeCertificate(file, ac);
        }

        X509CertificateHolder pkc = CliHelper.loadPKCSafe(file.getPath());
        if (pkc != null) {
            return fromPublicKeyCertificate(file, pkc);
        }
        return null;
    }

    public static PlatformCertificate loadSafe(@NonNull File file) {
        try {
            return load(file);
        } catch (Exception ignored) {}
        return null;
    }

    private static PlatformCertificate fromAttributeCertificate(
            @NonNull File file,
            @NonNull X509AttributeCertificateHolder ac) {
        return inspect(file, CertKind.AC, ac, null);
    }

    private static PlatformCertificate fromPublicKeyCertificate(
            @NonNull File file,
            @NonNull X509CertificateHolder pkc) {
        return inspect(file, CertKind.PKC, null, pkc);
    }

    public boolean isAttributeCertificate() {
        return attributeCertificate != null;
    }

    public boolean isPublicKeyCertificate() {
        return publicKeyCertificate != null;
    }

    public CertKind certKind() {
        return certKind;
    }

    public CertSpecVersion resolvedSpecVersion() {
        return certSpecVersion;
    }

    public CertificateReference toReference() {
        return toReference(null);
    }

    public CertificateReference toReference(ASN1ObjectIdentifier traitCategory) {
        return CertificateReference.builder()
                .file(file != null ? file.getPath() : null)
                .certKind(certKind)
                .certSpecVersion(certSpecVersion)
                .certType(certType)
                .tcgCredentialType(tcgCredentialType)
                .certificateIdentifier(certificateIdentifier)
                .traitCategory(traitCategory)
                .build();
    }

    public BigInteger serialNumber() {
        return isAttributeCertificate()
                ? attributeCertificate.getSerialNumber()
                : publicKeyCertificate.getSerialNumber();
    }

    public Attribute getAttribute(@NonNull ASN1ObjectIdentifier oid) {
        if (isAttributeCertificate()) {
            return getAttributeFromAttributeCertificate(attributeCertificate, oid);
        }
        if (isPublicKeyCertificate()) {
            return getAttributeFromPublicKeyCertificate(publicKeyCertificate, oid);
        }
        return null;
    }

    public boolean hasAttribute(@NonNull ASN1ObjectIdentifier oid) {
        Attribute attribute = getAttribute(oid);
        return attribute != null && attribute.getAttrValues() != null && attribute.getAttrValues().size() > 0
                && attribute.getAttrValues().getObjectAt(0) != null;
    }

    public <T> T attributeValue(ASN1ObjectIdentifier oid, Function<Object, T> decoder) {
        Attribute attribute = getAttribute(oid);
        if (attribute == null || attribute.getAttrValues() == null || attribute.getAttrValues().size() == 0) {
            return null;
        }
        ASN1Encodable value = attribute.getAttrValues().getObjectAt(0);
        try {
            return decoder.apply(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    private TCGCredentialType credentialType() {
        return attributeValue(TCGObjectIdentifier.tcgAtTcgCredentialType, TCGCredentialType::getInstance);
    }

    public TCGSpecificationVersion declaredSpecification() {
        return attributeValue(TCGObjectIdentifier.tcgAtTcgCredentialSpecification, TCGSpecificationVersion::getInstance);
    }

    public boolean hasPcv1() {
        return hasAttribute(TCGObjectIdentifier.tcgAtPlatformConfigurationV1);
    }

    public boolean hasPcv2() {
        return hasAttribute(TCGObjectIdentifier.tcgAtPlatformConfigurationV2);
    }

    public boolean hasPcv3() {
        return hasAttribute(TCGObjectIdentifier.tcgAtPlatformConfigurationV3);
    }

    public CertSpecVersion actualSpecVersion() {
        if (hasPcv1()) return CertSpecVersion.V1_0;
        if (hasPcv2()) return CertSpecVersion.V1_1;
        if (hasPcv3()) return CertSpecVersion.V2_0;
        return null;
    }

    public Extension getExtension(@NonNull ASN1ObjectIdentifier oid) {
        if (publicKeyCertificate != null) {
            return publicKeyCertificate.getExtension(oid);
        }
        if (attributeCertificate != null && attributeCertificate.getExtensions() != null) {
            return attributeCertificate.getExtensions().getExtension(oid);
        }
        return null;
    }

    public GeneralNames subjectAlternativeNames() {
        ASN1Primitive parsed = Optional.ofNullable(getExtension(ExtensionContext.subjectAlternativeName.getOid()))
                .map(Extension::getParsedValue)
                .map(ASN1Primitive.class::cast)
                .orElse(null);
        if (parsed == null) {
            return null;
        }
        try {
            return GeneralNames.getInstance(parsed);
        } catch (Exception ignored) {
            return null;
        }
    }

    public TCGPlatformSpecification platformSpecification() {
        return attributeValue(TCGObjectIdentifier.tcgAtTcgPlatformSpecification, TCGPlatformSpecification::getInstance);
    }

    public URIReference platformConfigUri() {
        return attributeValue(TCGObjectIdentifier.tcgAtPlatformConfigUri, URIReference::getInstance);
    }

    public TraitMap traitMap(@NonNull ASN1ObjectIdentifier oid) {
        return attributeValue(oid, TraitMap::getInstance);
    }

    public PlatformConfigurationV3 canonicalizedPlatformConfigurationV3() {
        if (hasPcv1()) {
            return PlatformConfigurationNormalizer.canonicalize(platformConfigurationV1());
        }
        if (hasPcv2()) {
            return PlatformConfigurationNormalizer.canonicalize(platformConfigurationV2());
        }
        if (hasPcv3()) {
            return PlatformConfigurationNormalizer.canonicalize(platformConfigurationV3());
        }
        return null;
    }

    public List<CertificateIdentifierTrait> previousPlatformCertificateTraits() {
        Attribute attr = getAttribute(TCGObjectIdentifier.tcgAtPreviousPlatformCertificates);
        boolean proceed = Optional.ofNullable(attr)
                .map(Attribute::getAttrValues)
                .map(values -> values.size() > 0)
                .orElse(false);
        if (!proceed) {
            return List.of();
        }

        ASN1Encodable value = attr.getAttrValues().getObjectAt(0);
        ASN1Sequence sequence = ASN1Utils.getSequence(value);
        if (sequence == null) {
            return List.of();
        }

        return TraitMap.fromASN1Sequence(sequence).get(CertificateIdentifierTrait.class);
    }

    public boolean requiresPreviousPlatformCertificates() {
        if (!previousPlatformCertificateTraits().isEmpty()) {
            return true;
        }
        return certType == CertType.DELTA || certType == CertType.REBASE;
    }

    public boolean isSignatureValid(ContentVerifierProvider verifierProvider) throws CertException {
        if (publicKeyCertificate != null) {
            return publicKeyCertificate.isSignatureValid(verifierProvider);
        }
        if (attributeCertificate != null) {
            return attributeCertificate.isSignatureValid(verifierProvider);
        }
        return false;
    }

    public PlatformConfiguration platformConfigurationV1() {
        return attributeValue(TCGObjectIdentifier.tcgAtPlatformConfigurationV1, PlatformConfiguration::getInstance);
    }

    public PlatformConfigurationV2 platformConfigurationV2() {
        return attributeValue(TCGObjectIdentifier.tcgAtPlatformConfigurationV2, PlatformConfigurationV2::getInstance);
    }

    public PlatformConfigurationV3 platformConfigurationV3() {
        return attributeValue(TCGObjectIdentifier.tcgAtPlatformConfigurationV3, PlatformConfigurationV3::getInstance);
    }

    private static PlatformCertificate inspect(
            File file,
            CertKind certKind,
            X509AttributeCertificateHolder ac,
            X509CertificateHolder pkc) {
        PlatformCertificate certificate = new PlatformCertificate(file, certKind, null, null, null, null, ac, pkc);
        TCGCredentialType credentialType = certificate.credentialType();
        TCGSpecificationVersion declaredSpec = certificate.declaredSpecification();
        CertSpecVersion actualSpec = certificate.actualSpecVersion();
        CertSpecVersion expectedSpec = CertSpecVersion.fromTcgSpecVersion(declaredSpec);
        CertSpecVersion resolvedSpec = actualSpec != null ? actualSpec : expectedSpec;
        CertType certType = credentialType != null
                ? CertTypeResolver.fromOid(credentialType.getCertificateType())
                : (resolvedSpec == CertSpecVersion.V1_0 ? CertType.BASE : null);
        CertificateIdentifier certificateIdentifier = ac != null
                ? CertificateIdentifier.fromAC(ac)
                : CertificateIdentifier.fromPKC(pkc);
        return new PlatformCertificate(
                file,
                certKind,
                resolvedSpec,
                certType,
                credentialType,
                certificateIdentifier,
                ac,
                pkc);
    }

    public Date extractNotBefore() {
        if (isPublicKeyCertificate()) {
            return getPublicKeyCertificate().getNotBefore();
        }
        if (isAttributeCertificate()) {
            return getAttributeCertificate().getNotBefore();
        }
        return null;
    }

    public Date extractNotAfter() {
        if (isPublicKeyCertificate()) {
            return getPublicKeyCertificate().getNotAfter();
        }
        if (isAttributeCertificate()) {
            return getAttributeCertificate().getNotAfter();
        }
        return null;
    }

    public Extensions extractExtensions() {
        return isPublicKeyCertificate()
                ? getPublicKeyCertificate().getExtensions()
                : isAttributeCertificate()
                    ? getAttributeCertificate().getExtensions()
                    : null;
    }

    private static Attribute getAttributeFromAttributeCertificate(
            @NonNull X509AttributeCertificateHolder ac,
            @NonNull ASN1ObjectIdentifier oid) {
        Attribute[] attributes = ac.getAttributes(oid);
        for (Attribute attribute : attributes) {
            if (attribute != null && attribute.getAttrType().equals(oid)) {
                return attribute;
            }
        }
        return null;
    }

    private static Attribute getAttributeFromPublicKeyCertificate(
            @NonNull X509CertificateHolder pkc,
            @NonNull ASN1ObjectIdentifier oid) {
        Extension extension = pkc.getExtension(Extension.subjectDirectoryAttributes);
        if (extension == null || extension.getParsedValue() == null) {
            return null;
        }
        Vector<?> attributes = SubjectDirectoryAttributes.getInstance(extension.getParsedValue()).getAttributes();
        return attributes.stream()
                .filter(Attribute.class::isInstance)
                .map(Attribute.class::cast)
                .filter(attribute -> Objects.equals(attribute.getAttrType(), oid))
                .findFirst()
                .orElse(null);
    }
}
