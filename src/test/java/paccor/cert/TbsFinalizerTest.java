package paccor.cert;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import paccor.model.ExtensionInfo;
import paccor.model.HolderInfo;
import paccor.model.NameInfo;
import paccor.model.PlatformCertificateInformationModel;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.tcg.credential.ComponentIdentifierV11Trait;
import paccor.tcg.credential.ComponentIdentifierV2;
import paccor.tcg.credential.ComponentClass;
import paccor.tcg.credential.UTF8StringTrait;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.PlatformConfigurationV3;
import paccor.tcg.credential.PlatformPropertiesV2;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERUTF8String;
import paccor.tcg.credential.TCGSpecificationVersion;

public class TbsFinalizerTest {
    private static final AlgorithmIdentifier SIG_ALG =
            new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption);

    @Test
    void validateAc_identifiesMissingFields() {
        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        CertificateProfile profile = CertificateProfile.platformV2_0Ac();

        List<String> issues = TbsFinalizer.validateAc(profile, pi);
        
        Assertions.assertTrue(issues.contains("Issuer is required."));
        Assertions.assertTrue(issues.contains("TCG credential specification is required."));
        Assertions.assertTrue(issues.contains("Serial is required."));
        Assertions.assertTrue(issues.contains("Validity is required."));
        Assertions.assertTrue(issues.contains("Holder is required for AC."));
        Assertions.assertTrue(issues.contains("Authority Key Identifier extension (" + Extension.authorityKeyIdentifier.getId() + ") is required."));
        Assertions.assertTrue(issues.contains("Certificate Policies extension (" + Extension.certificatePolicies.getId() + ") is required."));
        Assertions.assertTrue(issues.contains("Subject Alternative Name extension (" + Extension.subjectAlternativeName.getId() + ") is required."));
    }

    @Test
    void validatePkc_identifiesMissingFields() {
        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        CertificateProfile profile = CertificateProfile.platformV2_0Pkc();

        List<String> issues = TbsFinalizer.validatePkc(profile, pi);

        Assertions.assertTrue(issues.contains("Issuer is required."));
        Assertions.assertTrue(issues.contains("TCG credential specification is required."));
        Assertions.assertTrue(issues.contains("Serial is required."));
        Assertions.assertTrue(issues.contains("Validity is required."));
        Assertions.assertTrue(issues.contains("Subject is required for PKC finalize."));
        Assertions.assertTrue(issues.contains("Subject Public Key Info is required for PKC finalize."));
        Assertions.assertTrue(issues.contains("Authority Key Identifier extension (" + Extension.authorityKeyIdentifier.getId() + ") is required."));
        Assertions.assertTrue(issues.contains("Certificate Policies extension (" + Extension.certificatePolicies.getId() + ") is required."));
        Assertions.assertTrue(issues.contains("Subject Alternative Name extension (" + Extension.subjectAlternativeName.getId() + ") is required."));
        Assertions.assertTrue(issues.contains("Subject Key Identifier extension (" + Extension.subjectKeyIdentifier.getId() + ") is required."));
        Assertions.assertTrue(issues.contains("Basic Constraints extension (" + Extension.basicConstraints.getId() + ") is required."));
        Assertions.assertTrue(issues.contains("Extended Key Usage extension (" + Extension.extendedKeyUsage.getId() + ") is required."));
    }

    @Test
    void checksValidityPeriod() {
        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        pi.setIssuer(NameInfo.fromDerB64("MBExDzANBgNVBAMMBlRlc3RDQQ=="));
        pi.setTcgCredentialSpecification(
                TCGSpecificationVersion.builder()
                        .majorVersion(new ASN1Integer(2))
                        .minorVersion(new ASN1Integer(0))
                        .revision(new ASN1Integer(43))
                        .build());
        pi.setCertSerialNumber(BigInteger.TEN);

        // Set After earlier than Before - Error!
        long now = System.currentTimeMillis();
        pi.setNotBefore(new Date(now + 10000)); // Later than after
        pi.setNotAfter(new Date(now));

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV2_0Ac(), pi);
        Assertions.assertTrue(issues.contains("Validity period is invalid: notBefore is later than notAfter."));

        issues = TbsFinalizer.validatePkc(CertificateProfile.platformV2_0Pkc(), pi);
        Assertions.assertTrue(issues.contains("Validity period is invalid: notBefore is later than notAfter."));
    }
    
    @Test
    void validateAcFull() {
        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        pi.setIssuer(NameInfo.fromDerB64("MBExDzANBgNVBAMMBlRlc3RDQQ=="));
        pi.setTcgCredentialSpecification(
                TCGSpecificationVersion.builder()
                        .majorVersion(new ASN1Integer(2))
                        .minorVersion(new ASN1Integer(0))
                        .revision(new ASN1Integer(43))
                        .build());
        pi.setCertSerialNumber(BigInteger.TEN);
        pi.setNotBefore(new Date(System.currentTimeMillis()));
        pi.setNotAfter(new Date(System.currentTimeMillis() + 3600000));
        pi.setHolder(new HolderInfo(null, null));
        pi.setPlatformTraits(TbsEncoderDerivedExtensionsTest.samplePlatformTraits());

        pi.putExtension(TbsEncoderDerivedExtensionsTest.dummyExtension(
                Extension.authorityKeyIdentifier, "Authority Key Identifier"));
        pi.putExtension(TbsEncoderDerivedExtensionsTest.dummyExtension(
                Extension.certificatePolicies, "Certificate Policies"));
        pi.putExtension(TbsEncoderDerivedExtensionsTest.dummyExtension(
                Extension.subjectAlternativeName, "Subject Alternative Name"));

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV2_0Ac(), pi);
        Assertions.assertTrue(issues.isEmpty(), "Should have no issues but got: " + issues);
    }

    @Test
    void validatePkc_flagsMissingDerivedExtensions() throws Exception {
        PlatformCertificateInformationModel pi = TbsEncoderDerivedExtensionsTest.samplePkcModel();
        pi.setSubjectAlternativeName(TbsEncoderDerivedExtensionsTest.dummyExtension(
                Extension.subjectAlternativeName, "Subject Alternative Name"));

        List<String> issues = TbsFinalizer.validatePkc(CertificateProfile.platformV2_0Pkc(), pi);

        Assertions.assertTrue(issues.contains("Subject Key Identifier extension (" + Extension.subjectKeyIdentifier.getId() + ") is required."));
        Assertions.assertTrue(issues.contains("Basic Constraints extension (" + Extension.basicConstraints.getId() + ") is required."));
        Assertions.assertTrue(issues.contains("Extended Key Usage extension (" + Extension.extendedKeyUsage.getId() + ") is required."));
    }

    @Test
    void validatePkc_acceptsDerivedExtensionsFromEncoder() throws Exception {
        PlatformCertificateInformationModel pi = TbsEncoderDerivedExtensionsTest.samplePkcModel();

        new TbsEncoder(pi, CertificateProfile.platformV2_0Pkc()).buildTbs(SIG_ALG);
        List<String> issues = TbsFinalizer.validatePkc(CertificateProfile.platformV2_0Pkc(), pi);

        Assertions.assertTrue(issues.isEmpty(), "Should have no issues but got: " + issues);
    }

    @Test
    void validatePkc_rejectsMalformedBasicConstraints() throws Exception {
        PlatformCertificateInformationModel pi = TbsEncoderDerivedExtensionsTest.samplePkcModel();
        new TbsEncoder(pi, CertificateProfile.platformV2_0Pkc()).buildTbs(SIG_ALG);
        pi.setBasicConstraints(ExtensionInfo.builder()
                .oid(Extension.basicConstraints.getId())
                .critical(true)
                .name("Basic Constraints")
                .valueDerB64("AA==")
                .build());

        List<String> issues = TbsFinalizer.validatePkc(CertificateProfile.platformV2_0Pkc(), pi);

        Assertions.assertTrue(issues.contains("Basic Constraints extension value could not be decoded."));
    }

    @Test
    void validatePkc_rejectsWrongExtendedKeyUsageAndCaFlag() throws Exception {
        PlatformCertificateInformationModel pi = TbsEncoderDerivedExtensionsTest.samplePkcModel();
        new TbsEncoder(pi, CertificateProfile.platformV2_0Pkc()).buildTbs(SIG_ALG);
        pi.setBasicConstraints(ExtensionInfo.builder()
                .oid(Extension.basicConstraints.getId())
                .critical(true)
                .name("Basic Constraints")
                .valueDerB64(Base64.toBase64String(new BasicConstraints(true).getEncoded()))
                .build());
        pi.setExtendedKeyUsage(ExtensionInfo.builder()
                .oid(Extension.extendedKeyUsage.getId())
                .critical(false)
                .name("Extended Key Usage")
                .valueDerB64(Base64.toBase64String(new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth).getEncoded()))
                .build());

        List<String> issues = TbsFinalizer.validatePkc(CertificateProfile.platformV2_0Pkc(), pi);

        Assertions.assertTrue(issues.contains("Basic Constraints CA flag must be FALSE for PKC."));
        Assertions.assertTrue(issues.contains("Extended Key Usage must include " + KeyPurposeId.getInstance(
                CertTypeResolver.toOid(CertKind.PKC, CertType.BASE)).getId() + "."));
    }

    @Test
    void validateAc_v20_requiresPlatformTraits() {
        PlatformCertificateInformationModel pi = sampleV20Model();
        pi.setPlatformTraits(null); // Missing required traits

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV2_0Ac(), pi);
        Assertions.assertTrue(issues.contains("Subject Alternative Name must contain a PlatformIdentifier sequence with traits."));
    }

    @Test
    void validateAc_v20_checksRequiredPlatformTraits() {
        PlatformCertificateInformationModel pi = sampleV20Model();
        TraitMap traits = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformManufacturer)
                        .traitValue(new DERUTF8String("Manufacturer"))
                        .build())
                .build();
        pi.setPlatformTraits(traits); // Missing Model and Version

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV2_0Ac(), pi);
        Assertions.assertTrue(issues.contains("PlatformIdentifier is missing required trait category: " + TCGObjectIdentifier.tcgTrCatPlatformModel.getId()));
        Assertions.assertTrue(issues.contains("PlatformIdentifier is missing required trait category: " + TCGObjectIdentifier.tcgTrCatPlatformVersion.getId()));
    }

    @Test
    void validateAc_v20_checksComponentV11Exclusivity() {
        PlatformCertificateInformationModel pi = sampleV20Model();
        pi.setPlatformTraits(samplePlatformTraits());

        ComponentIdentifierV11Trait v11 = ComponentIdentifierV11Trait.builder()
                .traitValue(ComponentIdentifierV2.builder()
                        .componentClass(new ComponentClass(TCGObjectIdentifier.tcgRegistryComponentClassTcg, new DEROctetString(new byte[] {0, 0, 0, 1})))
                        .componentManufacturer(new DERUTF8String("man"))
                        .componentModel(new DERUTF8String("mod"))
                        .build())
                .build();

        TraitMap component = TraitMap.builder()
                .trait(v11)
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentClass)
                        .traitValue(new DERUTF8String("another"))
                        .build())
                .build();

        pi.setPlatformConfiguration(PlatformConfigurationV3.builder()
                .platformComponent(component)
                .build());

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV2_0Ac(), pi);
        Assertions.assertTrue(issues.contains("Component[0] containing ComponentIdentifierV11Trait SHALL NOT contain any other trait."));
    }

    @Test
    void validateAc_v20_checksRequiredComponentTraits() {
        PlatformCertificateInformationModel pi = sampleV20Model();
        pi.setPlatformTraits(samplePlatformTraits());

        TraitMap component = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer)
                        .traitValue(new DERUTF8String("man"))
                        .build())
                .build();

        pi.setPlatformConfiguration(PlatformConfigurationV3.builder()
                .platformComponent(component)
                .build());

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV2_0Ac(), pi);
        Assertions.assertTrue(issues.contains("Component[0] is missing required trait category: " + TCGObjectIdentifier.tcgTrCatComponentClass.getId()));
        Assertions.assertTrue(issues.contains("Component[0] is missing required trait category: " + TCGObjectIdentifier.tcgTrCatComponentModel.getId()));
    }

    @Test
    void validateAc_v20_deltaRequiresComponentStatusAndPropertyStatus() {
        PlatformCertificateInformationModel pi = sampleV20Model();
        pi.setPlatformTraits(samplePlatformTraits());
        pi.setIsDelta(true);

        TraitMap component = TraitMap.builder()
                .trait(UTF8StringTrait.builder().traitCategory(TCGObjectIdentifier.tcgTrCatComponentClass).traitValue(new DERUTF8String("c")).build())
                .trait(UTF8StringTrait.builder().traitCategory(TCGObjectIdentifier.tcgTrCatComponentManufacturer).traitValue(new DERUTF8String("m")).build())
                .trait(UTF8StringTrait.builder().traitCategory(TCGObjectIdentifier.tcgTrCatComponentModel).traitValue(new DERUTF8String("m")).build())
                .build(); // Missing Status

        pi.setPlatformConfiguration(PlatformConfigurationV3.builder()
                .platformComponent(component)
                .platformProperty(PlatformPropertiesV2.builder()
                        .propertyName(new DERUTF8String("prop"))
                        .propertyValue(new DERUTF8String("val"))
                        .build()) // Missing Status
                .build());

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV2_0Ac(), pi);
        Assertions.assertTrue(issues.contains("Component[0] is missing required trait category: " + TCGObjectIdentifier.tcgTrCatComponentStatus.getId()));
        Assertions.assertTrue(issues.contains("Property[0] in Delta Platform Certificate SHALL contain the status field."));
    }

    @Test
    void validateAc_v20_checksPlatformOwnershipTrait() {
        PlatformCertificateInformationModel pi = sampleV20Model();
        pi.setPlatformTraits(samplePlatformTraits());

        TraitMap ownership = TraitMap.builder()
                .trait(UTF8StringTrait.builder()
                        .traitCategory(TCGObjectIdentifier.tcgTrCatPlatformManufacturer) // Wrong category
                        .traitValue(new DERUTF8String("val"))
                        .build())
                .build();
        pi.setPlatformOwnership(ownership);

        List<String> issues = TbsFinalizer.validateAc(CertificateProfile.platformV2_0Ac(), pi);
        Assertions.assertTrue(issues.contains("PlatformOwnership is missing required trait category: " + TCGObjectIdentifier.tcgTrCatPlatformOwnership.getId()));
    }

    private PlatformCertificateInformationModel sampleV20Model() {
        PlatformCertificateInformationModel pi = new PlatformCertificateInformationModel();
        pi.setIssuer(NameInfo.fromDerB64("MBExDzANBgNVBAMMBlRlc3RDQQ=="));
        pi.setTcgCredentialSpecification(
                TCGSpecificationVersion.builder()
                        .majorVersion(new ASN1Integer(2))
                        .minorVersion(new ASN1Integer(0))
                        .revision(new ASN1Integer(43))
                        .build());
        pi.setCertSerialNumber(BigInteger.TEN);
        pi.setNotBefore(new Date());
        pi.setNotAfter(new Date(System.currentTimeMillis() + 3600000));
        pi.setHolder(new HolderInfo(null, null));
        pi.putExtension(TbsEncoderDerivedExtensionsTest.dummyExtension(Extension.authorityKeyIdentifier, "AKI"));
        pi.putExtension(TbsEncoderDerivedExtensionsTest.dummyExtension(Extension.certificatePolicies, "CP"));
        pi.putExtension(TbsEncoderDerivedExtensionsTest.dummyExtension(Extension.subjectAlternativeName, "SAN"));
        return pi;
    }

    private TraitMap samplePlatformTraits() {
        return TbsEncoderDerivedExtensionsTest.samplePlatformTraits();
    }

    @Test
    void validateV10RejectsDeltaAndRebaseInputs() throws Exception {
        PlatformCertificateInformationModel pi = TbsEncoderDerivedExtensionsTest.samplePkcModel();
        pi.setTcgCredentialSpecification(
                TCGSpecificationVersion.builder()
                        .majorVersion(new ASN1Integer(1))
                        .minorVersion(new ASN1Integer(0))
                        .revision(new ASN1Integer(2))
                        .build());
        pi.setIsDelta(Boolean.TRUE);

        List<String> issues = TbsFinalizer.validatePkc(CertificateProfile.platformV1_0Pkc(), pi);
        Assertions.assertTrue(issues.contains("Certificate type DELTA is not supported for 1.0."));
    }
}
