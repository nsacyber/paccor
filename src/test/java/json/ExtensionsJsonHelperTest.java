package json;

import cli.CliHelper;
import java.io.IOException;
import java.util.List;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.PolicyQualifierId;
import org.bouncycastle.asn1.x509.PolicyQualifierInfo;
import org.bouncycastle.asn1.x509.ReasonFlags;
import org.bouncycastle.asn1.x509.TargetInformation;
import org.bouncycastle.asn1.x509.UserNotice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tcg.credential.ASN1Utils;
import tools.jackson.core.exc.JacksonIOException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class ExtensionsJsonHelperTest {
    @Test
    public void testLegacy() throws IOException {
        String json = """
                {
                    "CERTIFICATEPOLICIES": [
                        {
                            "POLICYIDENTIFIER": "1.2.3",
                            "POLICYQUALIFIERS": [
                                {
                                    "POLICYQUALIFIERID": "USERNOTICE",
                                    "QUALIFIER": "TCG Trusted Platform Endorsement"
                                }
                            ]
                        }
                    ]
                }
                """;
        ExtensionsJsonHelper expected = ExtensionsJsonHelper.builder()
                .certificatePolicies(certificatePolicies(policyInformation(
                        "1.2.3",
                        new PolicyQualifierInfo(PolicyQualifierId.id_qt_unotice,
                                new UserNotice(null, "TCG Trusted Platform Endorsement")))))
                .build();

        JsonMapper.Builder mapperBuilder = JsonMapper.builder();
        ObjectMapper mapper = mapperBuilder.build();

        ExtensionsJsonHelper actual = mapper.readValue(json, ExtensionsJsonHelper.class);

        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testReadAuthorityInfoAccessAliasAndCase() throws Exception {
        String json = """
            {
              "AUTHORITYINFOACCESS": [
                { "ACCESSMETHOD": "OCSP", "ACCESSLOCATION": "CN=Ocsp Responder" },
                { "ACCESSMETHOD": "CAISSUERS", "ACCESSLOCATION": "CN=CA Issuers" }
              ]
            }
            """;

        ExtensionsJsonHelper expected = ExtensionsJsonHelper.builder()
                .authorityInformationAccess(authorityInfoAccess(
                        new AccessDescription(AccessDescription.id_ad_ocsp,
                                new GeneralName(new X500Name("CN=Ocsp Responder"))),
                        new AccessDescription(AccessDescription.id_ad_caIssuers,
                                new GeneralName(new X500Name("CN=CA Issuers")))))
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        ExtensionsJsonHelper actual = mapper.readValue(json, ExtensionsJsonHelper.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testReadAuthorityInfoAccessUriLocation() throws Exception {
        String json = """
            {
              "AUTHORITYINFOACCESS": [
                { "ACCESSMETHOD": "OCSP", "ACCESSLOCATION": "http://ocsp.example.com" }
              ]
            }
            """;

        ExtensionsJsonHelper expected = ExtensionsJsonHelper.builder()
                .authorityInformationAccess(authorityInfoAccess(
                        new AccessDescription(AccessDescription.id_ad_ocsp,
                                new GeneralName(GeneralName.uniformResourceIdentifier, "http://ocsp.example.com"))))
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        ExtensionsJsonHelper actual = mapper.readValue(json, ExtensionsJsonHelper.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testCrlDistAliasAndCase() throws Exception {
        String json = """
            {
              "CRLDISTRIBUTION": [
                {
                  "DISTRIBUTIONNAME": { "TYPE": 0, "NAME": "CN=DP1" },
                  "REASON": 0,
                  "ISSUER": "CN=CRL Issuer"
                }
              ]
            }
            """;

        ExtensionsJsonHelper expected = ExtensionsJsonHelper.builder()
                .crlDistPoint(crlDistPoint(new DistributionPoint(
                        new DistributionPointName(0, new GeneralNames(new GeneralName(new X500Name("CN=DP1")))),
                        new ReasonFlags(0),
                        new GeneralNames(new GeneralName(new X500Name("CN=CRL Issuer"))))))
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        ExtensionsJsonHelper actual = mapper.readValue(json, ExtensionsJsonHelper.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testCertificatePoliciesMultiplePoliciesAndQualifiers() throws Exception {
        String json = """
            {
              "certificatePolicies": [
                {
                  "policyIdentifier": "1.2.3.4",
                  "policyQualifiers": [
                    { "policyQualifierId": "CPS", "qualifier": "http://example.com/cps" }
                  ]
                },
                {
                  "policyIdentifier": "2.999.1",
                  "policyQualifiers": [
                    { "policyQualifierId": "USERNOTICE", "qualifier": "Notice text" }
                  ]
                }
              ]
            }
            """;

        ExtensionsJsonHelper expected = ExtensionsJsonHelper.builder()
                .certificatePolicies(certificatePolicies(
                        policyInformation("1.2.3.4",
                                new PolicyQualifierInfo(PolicyQualifierId.id_qt_cps,
                                        new DERIA5String("http://example.com/cps"))),
                        policyInformation("2.999.1",
                                new PolicyQualifierInfo(PolicyQualifierId.id_qt_unotice,
                                        new UserNotice(null, "Notice text")))))
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        ExtensionsJsonHelper actual = mapper.readValue(json, ExtensionsJsonHelper.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testKeyUsageNamesArray() throws Exception {
        String json = """
            {
              "keyUsage": ["digitalSignature", "keyEncipherment"]
            }
            """;

        // digitalSignature (1) + keyEncipherment (4) = 5
        ExtensionsJsonHelper expected = ExtensionsJsonHelper.builder()
                .keyUsage(new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment))
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        ExtensionsJsonHelper actual = mapper.readValue(json, ExtensionsJsonHelper.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testKeyUsageIntAndHexEqual() throws Exception {
        String jsonInt = """
            { "keyUsage": 5 }
            """;
        String jsonHex = """
            { "keyUsage": "0x05" }
            """;

        KeyUsage expectedKU = new KeyUsage(5);

        ObjectMapper mapper = JsonMapper.builder().build();
        ExtensionsJsonHelper actualInt = mapper.readValue(jsonInt, ExtensionsJsonHelper.class);
        ExtensionsJsonHelper actualHex = mapper.readValue(jsonHex, ExtensionsJsonHelper.class);

        Assertions.assertEquals(expectedKU, actualInt.keyUsage());
        Assertions.assertEquals(expectedKU, actualHex.keyUsage());
        Assertions.assertEquals(actualInt, actualHex);
    }

    @Test
    public void testKeyUsageEmpty() {
        String json = """
            { "keyUsage": {} }
            """;

        ObjectMapper mapper = JsonMapper.builder().build();
        Assertions.assertThrows(JacksonIOException.class, () -> mapper.readValue(json, ExtensionsJsonHelper.class));
    }

    @Test
    public void testTargetInformationEmptyArray() throws Exception {
        String json = """
            { "TARGETINFORMATION": [] }
            """;

        ExtensionsJsonHelper expected = ExtensionsJsonHelper.builder()
                .targetingInformation(targetInformation()) // empty
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        ExtensionsJsonHelper actual = mapper.readValue(json, ExtensionsJsonHelper.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testIgnoresDerivedExtensionFields() throws Exception {
        String json = """
            {
              "aki": { "der": "AA==" },
              "eku": ["1.2.3.4"],
              "ski": "AQ==",
              "basicConstraints": true,
              "keyUsage": ["digitalSignature"]
            }
            """;

        ExtensionsJsonHelper expected = ExtensionsJsonHelper.builder()
                .keyUsage(new KeyUsage(KeyUsage.digitalSignature))
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        ExtensionsJsonHelper actual = mapper.readValue(json, ExtensionsJsonHelper.class);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testAllSupportedFields() throws Exception {
        String json = """
            {
              "CERTIFICATEPOLICIES": [
                {
                  "POLICYIDENTIFIER": "1.2.840.113549.1.1.5",
                  "POLICYQUALIFIERS": [
                    { "POLICYQUALIFIERID": "CPS", "QUALIFIER": "https://example.com/cps" }
                  ]
                }
              ],
              "authorityInfoAccess": [
                { "accessMethod": "OCSP", "accessLocation": "CN=OCSP A" }
              ],
              "CRLDISTRIBUTION": [
                {
                  "distributionName": { "type": 0, "name": "CN=DP A" },
                  "reason": 0,
                  "issuer": "CN=Issuer A"
                }
              ],
              "KEYUSAGE": ["digitalSignature", "keyEncipherment"],
              "targetInformation": [ { "FILE": "src/test/resources/ek_cert_2187.der" } ]
            }
            """;

        ExtensionsJsonHelper expected = ExtensionsJsonHelper.builder()
                .certificatePolicies(certificatePolicies(policyInformation(
                        "1.2.840.113549.1.1.5",
                        new PolicyQualifierInfo(PolicyQualifierId.id_qt_cps,
                                new DERIA5String("https://example.com/cps")))))
                .authorityInformationAccess(authorityInfoAccess(
                        new AccessDescription(AccessDescription.id_ad_ocsp,
                                new GeneralName(new X500Name("CN=OCSP A")))))
                .crlDistPoint(crlDistPoint(new DistributionPoint(
                        new DistributionPointName(0, new GeneralNames(new GeneralName(new X500Name("CN=DP A")))),
                        new ReasonFlags(0),
                        new GeneralNames(new GeneralName(new X500Name("CN=Issuer A"))))))
                .keyUsage(new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment))
                .targetingInformation(targetInformation(TargetingInformationJson.fromCertificate(
                        CliHelper.loadCert("src/test/resources/ek_cert_2187.der", CliHelper.x509type.CERTIFICATE))))
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        ExtensionsJsonHelper actual = mapper.readValue(json, ExtensionsJsonHelper.class);

        Assertions.assertEquals(expected, actual);
    }

    private static CertificatePolicies certificatePolicies(PolicyInformation... policies) {
        return CertificatePolicies.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(List.of(policies))));
    }

    private static PolicyInformation policyInformation(String oid, PolicyQualifierInfo... qualifiers) {
        return new PolicyInformation(
                new ASN1ObjectIdentifier(oid),
                new DERSequence(ASN1Utils.toASN1EncodableVector(List.of(qualifiers))));
    }

    private static AuthorityInformationAccess authorityInfoAccess(AccessDescription... descriptions) {
        return AuthorityInformationAccess.getInstance(
                new DERSequence(ASN1Utils.toASN1EncodableVector(List.of(descriptions))));
    }

    private static CRLDistPoint crlDistPoint(DistributionPoint... points) {
        return CRLDistPoint.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(List.of(points))));
    }

    private static TargetInformation targetInformation(org.bouncycastle.asn1.x509.Target... targets) {
        return TargetInformation.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(List.of(targets))));
    }
}
