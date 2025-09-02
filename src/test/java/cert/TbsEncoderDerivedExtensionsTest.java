package cert;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.Date;
import model.ExtensionInfo;
import model.HolderInfo;
import model.NameInfo;
import model.PlatformCertificateInformationModel;
import model.SubjectInfo;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Attribute;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.SubjectDirectoryAttributes;
import org.bouncycastle.asn1.x509.TBSCertificate;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import json.HardwareManifestJsonHelper;
import tcg.credential.ASN1Utils;
import tcg.credential.PENTrait;
import tcg.credential.TBBSecurityAssertions;
import tcg.credential.TPMSpecification;
import tcg.credential.TCGObjectIdentifier;
import tcg.credential.TCGPlatformSpecification;
import tcg.credential.TCGSpecificationVersion;
import tcg.credential.TraitMap;
import tcg.credential.UTF8StringTrait;
import tcg.credential.PublicKeyTraitTest;

public class TbsEncoderDerivedExtensionsTest {
    private static final String COMP_JSON_BARE_BONES = "src/test/resources/bare-bones-config/base-bare-bones-componentlist.json";
    private static final String COMP_JSON_V3_ADV = "src/test/resources/sample_testgen1/localhost-componentlistv3adv.json";
    private static final String HOLDER_CERT = "src/test/resources/ek.cer";
    private static final AlgorithmIdentifier SIG_ALG =
            new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption);

    @Test
    void buildTbs_populatesDerivedPkcExtensions() throws Exception {
        PlatformCertificateInformationModel pi = samplePkcModel();

        TbsEncoder encoder = new TbsEncoder(pi, CertificateProfile.platformV2_0Pkc());
        byte[] tbs = encoder.buildTbs(SIG_ALG);

        Assertions.assertNotNull(tbs);
        Assertions.assertNotNull(pi.getSubjectAlternativeName());
        Assertions.assertNotNull(pi.getSubjectKeyIdentifier());
        Assertions.assertNotNull(pi.getBasicConstraints());
        Assertions.assertNotNull(pi.getExtendedKeyUsage());

        BasicConstraints basicConstraints = BasicConstraints.getInstance(
                ASN1Primitive.fromByteArray(Base64.decode(pi.getBasicConstraints().valueDerB64())));
        Assertions.assertFalse(basicConstraints.isCA());
        Assertions.assertTrue(pi.getBasicConstraints().critical());

        ExtendedKeyUsage eku = ExtendedKeyUsage.getInstance(
                ASN1Primitive.fromByteArray(Base64.decode(pi.getExtendedKeyUsage().valueDerB64())));
        Assertions.assertTrue(eku.hasKeyPurposeId(
                KeyPurposeId.getInstance(TCGObjectIdentifier.tcgKpPlatformKeyCertificate)));
    }

    @Test
    void buildTbs_deltaPkcOmitsDeltaSensitiveSubjectDirectoryAttributes() throws Exception {
        PlatformCertificateInformationModel pi = samplePkcModel();
        pi.setIsDelta(true);
        pi.setTcgPlatformSpecification(new TCGPlatformSpecification(
                TCGSpecificationVersion.builder()
                        .majorVersion(new ASN1Integer(2))
                        .minorVersion(new ASN1Integer(0))
                        .revision(new ASN1Integer(43))
                        .build(),
                new DEROctetString(new byte[] {0, 0, 0, 1})));
        pi.setTbbSecurityAssertions(TBBSecurityAssertions.builder().build());

        byte[] tbs = new TbsEncoder(pi, CertificateProfile.platformV2_0Pkc()).buildTbs(SIG_ALG);

        TBSCertificate certificate = TBSCertificate.getInstance(ASN1Primitive.fromByteArray(tbs));
        SubjectDirectoryAttributes attributes = SubjectDirectoryAttributes.getInstance(
                certificate.getExtensions().getExtension(Extension.subjectDirectoryAttributes).getParsedValue());

        Assertions.assertNotNull(attributes);
        Assertions.assertFalse(hasAttribute(attributes, TCGObjectIdentifier.tcgAtTbbSecurityAssertions));
        Assertions.assertFalse(hasAttribute(attributes, TCGObjectIdentifier.tcgAtTcgPlatformSpecification));
        Assertions.assertTrue(hasAttribute(attributes, TCGObjectIdentifier.tcgAtTcgCredentialType));
    }

    @Test
    void buildTbs_v20DerivesTraitBasedSubjectAlternativeName() throws Exception {
        PlatformCertificateInformationModel pi = samplePkcModel();
        pi.applyHardwareManifest(readHardwareManifest(COMP_JSON_BARE_BONES));

        byte[] tbs = new TbsEncoder(pi, CertificateProfile.platformV2_0Pkc()).buildTbs(SIG_ALG);

        Assertions.assertNotNull(tbs);
        GeneralNames names = subjectAlternativeNames(pi);
        Assertions.assertEquals(1, names.getNames().length);
        Assertions.assertEquals(GeneralName.otherName, names.getNames()[0].getTagNo());

        ASN1Sequence otherName = ASN1Sequence.getInstance(names.getNames()[0].getName());
        Assertions.assertEquals(TCGObjectIdentifier.tcgAtPlatformIdentifier, ASN1ObjectIdentifier.getInstance(otherName.getObjectAt(0)));

        ASN1TaggedObject value = ASN1TaggedObject.getInstance(otherName.getObjectAt(1));
        TraitMap platformIdentifier = TraitMap.getInstance(ASN1Sequence.getInstance(value, true));
        Assertions.assertEquals(5, platformIdentifier.flattenTraits().size());
        Assertions.assertTrue(platformIdentifier.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformManufacturer.equals(trait.getTraitCategory())));
        Assertions.assertTrue(platformIdentifier.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformModel.equals(trait.getTraitCategory())));
        Assertions.assertTrue(platformIdentifier.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformVersion.equals(trait.getTraitCategory())));
        Assertions.assertTrue(platformIdentifier.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof UTF8StringTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformSerial.equals(trait.getTraitCategory())));
        Assertions.assertTrue(platformIdentifier.flattenTraits().stream()
                .anyMatch(trait -> trait instanceof PENTrait
                        && TCGObjectIdentifier.tcgTrCatPlatformManufactureridentifier.equals(trait.getTraitCategory())));
    }

    @Test
    void buildTbs_v11DerivesLegacyDirectoryNameSubjectAlternativeName() throws Exception {
        PlatformCertificateInformationModel pi = sampleAcModel();
        pi.applyHardwareManifest(readHardwareManifest(COMP_JSON_BARE_BONES));

        byte[] tbs = new TbsEncoder(pi, CertificateProfile.platformV1_1()).buildTbs(SIG_ALG);

        Assertions.assertNotNull(tbs);
        GeneralNames names = subjectAlternativeNames(pi);
        Assertions.assertEquals(1, names.getNames().length);
        Assertions.assertEquals(GeneralName.directoryName, names.getNames()[0].getTagNo());
    }

    @Test
    void buildTbs_v10PkcUsesLegacyPlatformConfigurationAndOmitsCredentialType() throws Exception {
        PlatformCertificateInformationModel pi = samplePkcModel();
        pi.setTcgCredentialSpecification(TCGSpecificationVersion.builder()
                .majorVersion(new ASN1Integer(1))
                .minorVersion(new ASN1Integer(0))
                .revision(new ASN1Integer(2))
                .build());
        pi.applyHardwareManifest(readHardwareManifest(COMP_JSON_BARE_BONES));
        pi.setTpmManufacturer("Acme TPM");
        pi.setTpmSpecification(TPMSpecification.builder()
                .family(new DERUTF8String("2.0"))
                .level(new ASN1Integer(138))
                .revision(new ASN1Integer(1))
                .build());

        byte[] tbs = new TbsEncoder(pi, CertificateProfile.platformV1_0Pkc()).buildTbs(SIG_ALG);

        Assertions.assertNotNull(tbs);
        TBSCertificate certificate = TBSCertificate.getInstance(ASN1Primitive.fromByteArray(tbs));
        SubjectDirectoryAttributes attributes = SubjectDirectoryAttributes.getInstance(
                certificate.getExtensions().getExtension(Extension.subjectDirectoryAttributes).getParsedValue());
        Assertions.assertTrue(hasAttribute(attributes, TCGObjectIdentifier.tcgAtPlatformConfigurationV1));
        Assertions.assertFalse(hasAttribute(attributes, TCGObjectIdentifier.tcgAtTcgCredentialType));
        Assertions.assertTrue(hasAttribute(attributes, TCGObjectIdentifier.tcgAtTpmManufacturer));
        Assertions.assertTrue(hasAttribute(attributes, TCGObjectIdentifier.tcgAtTpmSpecification));

        GeneralNames names = subjectAlternativeNames(pi);
        Assertions.assertEquals(GeneralName.directoryName, names.getNames()[0].getTagNo());
    }

    @Test
    void buildTbs_v20DerivesTraitBasedSubjectAlternativeNameForAdvancedV3Manifest() throws Exception {
        PlatformCertificateInformationModel pi = samplePkcModel();
        pi.applyHardwareManifest(readHardwareManifest(COMP_JSON_V3_ADV));

        byte[] tbs = new TbsEncoder(pi, CertificateProfile.platformV2_0Pkc()).buildTbs(SIG_ALG);

        Assertions.assertNotNull(tbs);
        GeneralNames names = subjectAlternativeNames(pi);
        Assertions.assertEquals(1, names.getNames().length);
        Assertions.assertEquals(GeneralName.otherName, names.getNames()[0].getTagNo());
    }

    static PlatformCertificateInformationModel samplePkcModel() throws Exception {
        X500Name issuer = new X500Name("CN=Test Issuer");
        X500Name subject = new X500Name("CN=Test Subject");

        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        pi.setIssuer(NameInfo.builder()
                .name(issuer)
                .nameDerB64(Base64.toBase64String(issuer.getEncoded()))
                .build());
        pi.setSubject(SubjectInfo.builder()
                .nameInfo(NameInfo.builder()
                        .name(subject)
                        .nameDerB64(Base64.toBase64String(subject.getEncoded()))
                        .build())
                .subjectPublicKeyInfoDerB64(Base64.toBase64String(PublicKeyTraitTest.PUBLIC_KEY_1.getEncoded()))
                .build());
        pi.setTcgCredentialSpecification(TCGSpecificationVersion.builder()
                .majorVersion(new ASN1Integer(2))
                .minorVersion(new ASN1Integer(0))
                .revision(new ASN1Integer(43))
                .build());
        pi.setCertSerialNumber(BigInteger.ONE);
        pi.setNotBefore(new Date(System.currentTimeMillis()));
        pi.setNotAfter(new Date(System.currentTimeMillis() + 3600000L));
        pi.setPlatformTraits(samplePlatformTraits());
        pi.putExtension(dummyExtension(Extension.authorityKeyIdentifier, "Authority Key Identifier"));
        pi.putExtension(dummyExtension(Extension.certificatePolicies, "Certificate Policies"));
        return pi;
    }

    static PlatformCertificateInformationModel sampleAcModel() throws Exception {
        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        X500Name issuer = new X500Name("CN=Test Issuer");
        HolderInfo holder = CertificateResolver.resolveHolder(new File(HOLDER_CERT), null);

        pi.setIssuer(NameInfo.builder()
                .name(issuer)
                .nameDerB64(Base64.toBase64String(issuer.getEncoded()))
                .build());
        pi.setHolder(holder);
        pi.setTcgCredentialSpecification(TCGSpecificationVersion.builder()
                .majorVersion(new ASN1Integer(2))
                .minorVersion(new ASN1Integer(0))
                .revision(new ASN1Integer(43))
                .build());
        pi.setCertSerialNumber(BigInteger.ONE);
        pi.setNotBefore(new Date(System.currentTimeMillis()));
        pi.setNotAfter(new Date(System.currentTimeMillis() + 3600000L));
        return pi;
    }

    static TraitMap samplePlatformTraits() {
        return TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformManufacturer)
                        .traitValue(ASN1Utils.getUTF8String("Acme"))
                        .build())
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformModel)
                        .traitValue(ASN1Utils.getUTF8String("RoadRunner"))
                        .build())
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformVersion)
                        .traitValue(ASN1Utils.getUTF8String("1.0"))
                        .build())
                .build();
    }

    static ExtensionInfo dummyExtension(ASN1ObjectIdentifier oid, String name) {
        return ExtensionInfo.builder()
                .oid(oid.getId())
                .critical(false)
                .name(name)
                .valueDerB64("AA==")
                .build();
    }

    private static boolean hasAttribute(SubjectDirectoryAttributes attributes, org.bouncycastle.asn1.ASN1ObjectIdentifier oid) {
        for (Object attribute : attributes.getAttributes()) {
            if (((Attribute) attribute).getAttrType().equals(oid)) {
                return true;
            }
        }
        return false;
    }

    private static HardwareManifestJsonHelper readHardwareManifest(String path) {
        File manifestFile = Paths.get(path).toFile();
        Assertions.assertTrue(manifestFile.exists(), "Test file should exist");
        HardwareManifestJsonHelper hw = HardwareManifestJsonHelper.readComponents(manifestFile);
        Assertions.assertNotNull(hw);
        return hw;
    }

    private static GeneralNames subjectAlternativeNames(PlatformCertificateInformationModel pi) {
        Assertions.assertNotNull(pi.getSubjectAlternativeName(), "SAN extension should be populated");
        return GeneralNames.getInstance(Base64.decode(pi.getSubjectAlternativeName().valueDerB64()));
    }
}
