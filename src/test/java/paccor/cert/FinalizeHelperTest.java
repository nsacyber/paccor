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
import paccor.tcg.credential.TCGSpecificationVersion;

public class FinalizeHelperTest {
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
