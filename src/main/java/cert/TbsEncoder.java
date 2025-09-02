package cert;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import model.ExtensionInfo;
import model.PlatformCertificateInformationModel;
import normalization.PlatformConfigurationNormalizer;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.Holder;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.SubjectDirectoryAttributes;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.cert.AttributeCertificateIssuer;
import org.bouncycastle.cert.X509v2AttributeCertificateBuilder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.bc.BcX509ExtensionUtils;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.util.encoders.Base64;
import tcg.credential.ASN1Utils;
import tcg.credential.PlatformConfiguration;
import tcg.credential.PlatformConfigurationV2;
import tcg.credential.PlatformConfigurationV3;
import tcg.credential.TCGCredentialType;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.TraitMap;
import tcg.credential.URIReference;

/**
 * Consolidates TBS (To Be Signed) generation logic for both Attribute Certificates
 * and Public Key Certificates.
 */
public class TbsEncoder {
    private final PlatformCertificateInformationModel platformInfo;
    private final CertificateProfile profile;

    /**
     * Create a TbsBuilder with platform information and certificate profile.
     *
     * @param platformInfo Platform information to encode
     * @param profile Certificate profile (spec version + output type)
     */
    public TbsEncoder(PlatformCertificateInformationModel platformInfo, CertificateProfile profile) {
        this.platformInfo = platformInfo;
        this.profile = profile;
        profile.validate(); // Ensure profile is valid
    }

    /**
     * Build TBS bytes for the configured certificate type.
     *
     * @param sigAlg Signature algorithm identifier
     * @return TBS bytes (ACInfo or TBSCertificate)
     * @throws IllegalStateException if required fields are missing
     */
    public byte[] buildTbs(AlgorithmIdentifier sigAlg) {
        validatePlatformInfo();
        ensureSubjectAlternativeName();
        ensureDerivedExtensions();

        return profile.outputType() == CertKind.AC
                ? buildAc(sigAlg)
                : buildPkc(sigAlg);
    }

    /**
     * Build ACInfo for Attribute Certificates.
     *
     * @param sigAlg Signature algorithm
     * @return ACInfo bytes
     */
    private byte[] buildAc(AlgorithmIdentifier sigAlg) {
        Holder holder = resolveHolder();
        X500Name issuerName = resolveIssuerName();
        TbsValidity validity = resolveValidity();
        X509v2AttributeCertificateBuilder builder = new X509v2AttributeCertificateBuilder(
                createAttributeCertificateHolder(holder),
                new AttributeCertificateIssuer(issuerName),
                validity.serial(),
                validity.notBefore(),
                validity.notAfter());

        addAttributes(builder);
        addExtensions(builder);
        return captureAcInfo(sigAlg, builder);
    }

    /**
     * Build TBSCertificate for Public Key Certificates (X.509).
     *
     * @param sigAlg Signature algorithm
     * @return TBSCertificate bytes
     */
    private byte[] buildPkc(AlgorithmIdentifier sigAlg) {
        X500Name issuerName = resolveIssuerName();
        X500Name subjectName = resolveSubjectName();
        SubjectPublicKeyInfo spki = resolveSubjectPublicKeyInfo();
        TbsValidity validity = resolveValidity();
        X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                issuerName,
                validity.serial(),
                validity.notBefore(),
                validity.notAfter(),
                subjectName,
                spki
        );

        addAttributes(builder);
        addExtensions(builder);
        return captureCertificate(sigAlg, builder);
    }

    private TbsValidity resolveValidity() {
        Date notBefore = Optional.ofNullable(platformInfo.getNotBefore()).orElseGet(Date::new);
        Date notAfter = Optional.ofNullable(platformInfo.getNotAfter())
                .orElseGet(() -> new Date(notBefore.getTime() + 86400000L * 365));
        BigInteger serial = Optional.ofNullable(platformInfo.getCertSerialNumber()).orElse(BigInteger.ZERO);
        return new TbsValidity(serial, notBefore, notAfter);
    }

    private Holder resolveHolder() {
        if (platformInfo.getHolder() == null) {
            throw new IllegalStateException("Holder information required for AC");
        }
        Holder holder = platformInfo.getHolder().holder();
        if (holder == null && platformInfo.getHolder().holderDerB64() != null) {
            try {
                holder = Holder.getInstance(ASN1Primitive.fromByteArray(Base64.decode(platformInfo.getHolder().holderDerB64())));
            } catch (Exception ignored) { }
        }
        if (holder == null) {
            throw new IllegalStateException("Could not resolve Holder");
        }
        return holder;
    }

    private X500Name resolveIssuerName() {
        if (platformInfo.getIssuer() == null) {
            throw new IllegalStateException("Issuer information required");
        }
        X500Name issuerName = platformInfo.getIssuer().name();
        if (issuerName == null && platformInfo.getIssuer().nameDerB64() != null) {
            issuerName = X500Name.getInstance(Base64.decode(platformInfo.getIssuer().nameDerB64()));
        }
        if (issuerName == null) {
            throw new IllegalStateException("Could not resolve Issuer");
        }
        return issuerName;
    }

    private X500Name resolveSubjectName() {
        if (platformInfo.getSubject() == null) {
            throw new IllegalStateException("Subject information required for PKC");
        }
        X500Name subjectName = platformInfo.getSubject().nameInfo().name();
        if (subjectName == null && platformInfo.getSubject().nameInfo().nameDerB64() != null) {
            subjectName = X500Name.getInstance(Base64.decode(platformInfo.getSubject().nameInfo().nameDerB64()));
        }
        if (subjectName == null) {
            throw new IllegalStateException("Could not resolve Subject");
        }
        return subjectName;
    }

    private SubjectPublicKeyInfo resolveSubjectPublicKeyInfo() {
        if (platformInfo.getSubject() == null || platformInfo.getSubject().subjectPublicKeyInfoDerB64() == null) {
            throw new IllegalStateException("Subject Public Key Info is required for PKC");
        }
        return SubjectPublicKeyInfo.getInstance(Base64.decode(platformInfo.getSubject().subjectPublicKeyInfoDerB64()));
    }

    private byte[] captureAcInfo(AlgorithmIdentifier sigAlg, X509v2AttributeCertificateBuilder builder) {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ContentSigner capturingSigner = captureSigner(sigAlg, buf);
        builder.build(capturingSigner);
        return buf.toByteArray();
    }

    private byte[] captureCertificate(AlgorithmIdentifier sigAlg, X509v3CertificateBuilder builder) {
        final ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ContentSigner capturingSigner = captureSigner(sigAlg, buf);
        builder.build(capturingSigner);
        return buf.toByteArray();
    }

    // Attributes
    private void addAttributes(X509v2AttributeCertificateBuilder builder) {
        for (CertificateAttribute attribute : collectAttributes(CertKind.AC)) {
            builder.addAttribute(attribute.oid(), attribute.value());
        }
    }

    private void addAttributes(X509v3CertificateBuilder builder) {
        SubjectDirectoryAttributes attributes = buildSubjectDirectoryAttributes();
        if (attributes != null) {
            addExtensions((_, _, _) ->
                    builder.addExtension(
                            ExtensionContext.subjectDirectoryAttributes.getOid(),
                            ExtensionContext.subjectDirectoryAttributes.isCritical(),
                            attributes));
        }
    }

    private List<CertificateAttribute> collectAttributes(CertKind target) {
        ASN1ObjectIdentifier credentialType = resolveCredentialTypeOid();
        List<CertificateAttribute> attributes = new ArrayList<>();

        if (profile.specVersion() != CertSpecVersion.V1_0) {
            addAttribute(attributes, TCGObjectIdentifier.tcgAtTcgCredentialType, new TCGCredentialType(credentialType));
        }
        if (!CertTypeResolver.isDeltaOid(credentialType)) {
            addAttribute(attributes, TCGObjectIdentifier.tcgAtTbbSecurityAssertions, platformInfo.getTbbSecurityAssertions());
            addAttribute(attributes, TCGObjectIdentifier.tcgAtTcgPlatformSpecification, platformInfo.getTcgPlatformSpecification());
        }
        addAttribute(attributes, TCGObjectIdentifier.tcgAtTcgCredentialSpecification, platformInfo.getTcgCredentialSpecification());
        addLegacyV1Attributes(attributes);
        addPlatformConfigurationAttribute(attributes, target);
        URIReference uri = null;
        if (platformInfo.getPlatformConfigUri() != null) {
            uri = URIReference.builder()
                    .uniformResourceIdentifier(ASN1Utils.getIA5String(platformInfo.getPlatformConfigUri()))
                    .build();
        }
        addAttribute(attributes, TCGObjectIdentifier.tcgAtPlatformConfigUri, uri);
        addAttribute(attributes, TCGObjectIdentifier.tcgAtPreviousPlatformCertificates, platformInfo.getPreviousPlatformCertificates());
        addAttribute(attributes, TCGObjectIdentifier.tcgAtCryptographicAnchors, platformInfo.getCryptographicAnchors());
        return attributes;
    }

    private void addPlatformConfigurationAttribute(List<CertificateAttribute> attributes, CertKind kind) {
        ASN1Encodable platformConfiguration = buildPlatformConfiguration();
        if (platformConfiguration == null) {
            return;
        }
        ASN1ObjectIdentifier attributeOid = profile.specVersion().getPlatformConfigOid();
        attributes.add(new CertificateAttribute(attributeOid, platformConfiguration));
    }

    private void addAttribute(
            List<CertificateAttribute> attributes,
            ASN1ObjectIdentifier oid,
            ASN1Encodable value) {
        if (value != null) {
            attributes.add(new CertificateAttribute(oid, value));
        }
    }

    private SubjectDirectoryAttributes buildSubjectDirectoryAttributes() {
        List<CertificateAttribute> attributes = collectAttributes(CertKind.PKC);
        if (attributes.isEmpty()) {
            return null;
        }

        Vector<Attribute> attrs = new Vector<>();
        for (CertificateAttribute attribute : attributes) {
            attrs.add(new Attribute(attribute.oid(), new DERSet(attribute.value())));
        }
        return new SubjectDirectoryAttributes(attrs);
    }

    private ASN1ObjectIdentifier resolveCredentialTypeOid() {
        if (profile.specVersion() == CertSpecVersion.V1_0) {
            return profile.outputType() == CertKind.PKC
                    ? TCGObjectIdentifier.tcgKpPlatformKeyCertificate
                    : TCGObjectIdentifier.tcgKpPlatformAttributeCertificate;
        }
        if (platformInfo.getTcgCredentialType() != null) {
            return platformInfo.getTcgCredentialType().getCertificateType();
        }
        CertType inferred = CertTypeResolver.inferCertType(platformInfo);
        ASN1ObjectIdentifier oid = CertTypeResolver.toOid(profile.outputType(), inferred);
        return (oid != null)
                ? oid
                : (profile.outputType() == CertKind.PKC
                    ? TCGObjectIdentifier.tcgKpPlatformKeyCertificate
                    : TCGObjectIdentifier.tcgKpPlatformAttributeCertificate);
    }

    private ASN1Encodable buildPlatformConfiguration() {
        PlatformConfigurationV3 canonical = platformInfo.getPlatformConfiguration();
        if (!PlatformConfigurationNormalizer.hasContent(canonical)) {
            return null;
        }

        if (profile.specVersion() == CertSpecVersion.V1_0) {
            return buildPlatformConfigurationV1(canonical);
        }

        if (profile.specVersion() == CertSpecVersion.V1_1) {
            return buildPlatformConfigurationV2(canonical);
        }

        return PlatformConfigurationNormalizer.normalizeForCertificateOutput(canonical);
    }

    private ASN1Encodable buildPlatformConfigurationV1(PlatformConfigurationV3 canonical) {
        PlatformConfiguration configuration = PlatformConfigurationNormalizer.toV1(canonical);
        if (configuration == null) {
            throw new IllegalStateException("PlatformConfigurationV3 cannot be represented as PlatformConfiguration.");
        }
        return configuration.toBuilder()
                .platformPropertiesUri(platformInfo.getPlatformPropertiesUri() != null
                        ? new URIReference(new DERIA5String(platformInfo.getPlatformPropertiesUri()), null, null)
                        : null)
                .build();
    }

    private ASN1Encodable buildPlatformConfigurationV2(PlatformConfigurationV3 canonical) {
        PlatformConfigurationV2 configuration = PlatformConfigurationNormalizer.toV2(canonical);
        if (configuration == null) {
            throw new IllegalStateException("PlatformConfigurationV3 cannot be represented as PlatformConfigurationV2.");
        }
        return configuration.toBuilder()
                .componentIdentifiersUri(platformInfo.getComponentIdentifiersUri() != null ? new URIReference(new DERIA5String(platformInfo.getComponentIdentifiersUri()), null, null) : null)
                .platformPropertiesUri(platformInfo.getPlatformPropertiesUri() != null ? new URIReference(new DERIA5String(platformInfo.getPlatformPropertiesUri()), null, null) : null)
                .build();
    }

    private void addLegacyV1Attributes(List<CertificateAttribute> attributes) {
        if (profile.specVersion() != CertSpecVersion.V1_0) {
            return;
        }

        addAttribute(attributes, TCGObjectIdentifier.tcgTcpaSpecVersion, platformInfo.getTcpaSpecificationVersion());
        addAttribute(attributes, TCGObjectIdentifier.tcgAtTpmManufacturer, ASN1Utils.getUTF8String(platformInfo.getTpmManufacturer()));
        addAttribute(attributes, TCGObjectIdentifier.tcgAtTpmModel, ASN1Utils.getUTF8String(platformInfo.getTpmModel()));
        addAttribute(attributes, TCGObjectIdentifier.tcgAtTpmVersion, ASN1Utils.getUTF8String(platformInfo.getTpmVersion()));
        addAttribute(attributes, TCGObjectIdentifier.tcgAtTpmSpecification, platformInfo.getTpmSpecification());
        addAttribute(attributes, TCGObjectIdentifier.tcgAtTpmSecurityAssertions, platformInfo.getTpmSecurityAssertions());
    }

    private void ensureDerivedExtensions() {
        if (profile.outputType() != CertKind.PKC) {
            return;
        }

        SubjectPublicKeyInfo spki = resolveSubjectPublicKeyInfo();
        ensureSubjectKeyIdentifier(spki);
        ensureBasicConstraints();
        ensureExtendedKeyUsage();
    }

    private void ensureSubjectAlternativeName() {
        TraitMap traits = platformInfo.getPlatformTraits();
        if (traits == null || profile == null) {
            return;
        }
        try {
            GeneralNames san = profile.specVersion() == CertSpecVersion.V2_0
                    ? SubjectAlternativeNameHelper.buildPlatformIdentifier(traits)
                    : SubjectAlternativeNameHelper.buildLegacy(traits);
            if (san == null) {
                return;
            }

            platformInfo.putExtension(ExtensionInfo.builder()
                    .oid(ExtensionContext.subjectAlternativeName.getOid().getId())
                    .valueDerB64(Base64.toBase64String(san.getEncoded()))
                    .critical(ExtensionContext.subjectAlternativeName.isCritical())
                    .name(ExtensionContext.subjectAlternativeName.getDisplayName())
                    .build());
        } catch (Exception ignored) {
        }
    }

    private void ensureSubjectKeyIdentifier(SubjectPublicKeyInfo spki) {
        if (platformInfo.getSubjectKeyIdentifier() != null || spki == null) {
            return;
        }
        try {
            SubjectKeyIdentifier ski = new BcX509ExtensionUtils().createSubjectKeyIdentifier(spki);
            platformInfo.putExtension(ExtensionInfo.builder()
                    .oid(ExtensionContext.subjectKeyIdentifier.getOid().getId())
                    .valueDerB64(Base64.toBase64String(ski.getEncoded()))
                    .critical(ExtensionContext.subjectKeyIdentifier.isCritical())
                    .name(ExtensionContext.subjectKeyIdentifier.getDisplayName())
                    .build());
        } catch (Exception ignored) {
        }
    }

    private void ensureBasicConstraints() {
        if (platformInfo.getBasicConstraints() != null) {
            return;
        }
        try {
            BasicConstraints constraints = new BasicConstraints(false);
            platformInfo.putExtension(ExtensionInfo.builder()
                    .oid(ExtensionContext.basicConstraints.getOid().getId())
                    .valueDerB64(Base64.toBase64String(constraints.getEncoded()))
                    .critical(ExtensionContext.basicConstraints.isCritical())
                    .name(ExtensionContext.basicConstraints.getDisplayName())
                    .build());
        } catch (Exception ignored) {
        }
    }

    private void ensureExtendedKeyUsage() {
        if (platformInfo.getExtendedKeyUsage() != null) {
            return;
        }
        try {
            KeyPurposeId purposeId = KeyPurposeId.getInstance(resolveCredentialTypeOid());
            ExtendedKeyUsage usage = new ExtendedKeyUsage(purposeId);
            platformInfo.putExtension(ExtensionInfo.builder()
                    .oid(ExtensionContext.extendedKeyUsage.getOid().getId())
                    .valueDerB64(Base64.toBase64String(usage.getEncoded()))
                    .critical(ExtensionContext.extendedKeyUsage.isCritical())
                    .name(ExtensionContext.extendedKeyUsage.getDisplayName())
                    .build());
        } catch (Exception ignored) {
        }
    }

    private void addExtensions(X509v2AttributeCertificateBuilder builder) {
        addExtensions(builder::addExtension);
    }

    private void addExtensions(X509v3CertificateBuilder builder) {
        addExtensions(builder::addExtension);
    }

    private void addExtensions(ExtensionWriter extensionWriter) {
        if (platformInfo.getExtensions() == null) return;
        platformInfo.getExtensions().values().forEach(extInfo -> {
            try {
                ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(extInfo.oid());
                byte[] value = Base64.decode(extInfo.valueDerB64());
                extensionWriter.add(oid, extInfo.critical(), ASN1Primitive.fromByteArray(value));
            } catch (Exception ignored) {
                // Skip malformed extensions
            }
        });
    }

    private ContentSigner captureSigner(AlgorithmIdentifier sigAlg, final ByteArrayOutputStream buf) {
        return new ContentSigner() {
            @Override public AlgorithmIdentifier getAlgorithmIdentifier() { return sigAlg; }
            @Override public OutputStream getOutputStream() { return buf; }
            @Override public byte[] getSignature() { return new byte[]{0}; }
        };
    }

    private static AttributeCertificateHolder createAttributeCertificateHolder(Holder holder) {
        try {
            Constructor<AttributeCertificateHolder> constructor =
                    AttributeCertificateHolder.class.getDeclaredConstructor(ASN1Sequence.class);
            constructor.setAccessible(true);
            return constructor.newInstance((ASN1Sequence) holder.toASN1Primitive());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create AttributeCertificateHolder", e);
        }
    }

    /**
     * Validate that {@code PlatformCertificateInformationModel} has required fields.
     */
    private void validatePlatformInfo() {
        if (platformInfo == null) {
            throw new IllegalStateException("PlatformCertificateInformationModel is required");
        }

        if (profile.outputType() == CertKind.AC) {
            if (platformInfo.getHolder() == null) {
                throw new IllegalStateException("Holder is required for Attribute Certificates");
            }
        } else {
            if (platformInfo.getSubject() == null) {
                throw new IllegalStateException("Subject is required for Public Key Certificates");
            }
        }

        if (platformInfo.getIssuer() == null) {
            throw new IllegalStateException("Issuer is required");
        }
    }
}
