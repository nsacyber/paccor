package json;

import cli.CliHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import factory.AuthorityInfoAccessFactory;
import factory.CRLDistPointFactory;
import factory.CertificatePoliciesFactory;
import factory.DistributionPointFactory;
import factory.PolicyInformationFactory;
import factory.TargetingInformationFactory;
import java.io.IOException;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.ReasonFlags;
import org.bouncycastle.asn1.x509.UserNotice;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
                .certificatePolicies(CertificatePoliciesFactory.create()
                        .addPolicyInformation(PolicyInformationFactory.create()
                                .policyIdentifier(new ASN1ObjectIdentifier("1.2.3"))
                                .addQualifier(PolicyInformationFactory.QualifierJson.USERNOTICE, new UserNotice(null, "TCG Trusted Platform Endorsement"))
                                .build())
                        .build())
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
                .authorityInformationAccess(AuthorityInfoAccessFactory.create()
                        .addElement(AuthorityInfoAccessFactory.MethodJson.OCSP,
                                new GeneralName(new X500Name("CN=Ocsp Responder")))
                        .addElement(AuthorityInfoAccessFactory.MethodJson.CAISSUERS,
                                new GeneralName(new X500Name("CN=CA Issuers")))
                        .build())
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
                .crlDistPoint(CRLDistPointFactory.create()
                        .addDistributionPoint(DistributionPointFactory.create()
                                        .distributionPointName(new DistributionPointName(0,
                                                new GeneralNames(
                                                        new GeneralName(
                                                                new X500Name("CN=DP1")))))
                                        .reasons(new ReasonFlags(0))
                                        .cRLIssuer(new GeneralNames(
                                                new GeneralName(
                                                        new X500Name("CN=CRL Issuer"))))
                                        .build())
                                .build())
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
                .certificatePolicies(CertificatePoliciesFactory.create()
                        .addPolicyInformation(PolicyInformationFactory.create()
                                .policyIdentifier(new ASN1ObjectIdentifier("1.2.3.4"))
                                .addQualifier(PolicyInformationFactory.QualifierJson.CPS,
                                        new DERIA5String("http://example.com/cps"))
                                 .build())
                        .addPolicyInformation(PolicyInformationFactory.create()
                                .policyIdentifier(new ASN1ObjectIdentifier("2.999.1"))
                                .addQualifier(PolicyInformationFactory.QualifierJson.USERNOTICE,
                                        new UserNotice(null, "Notice text"))
                                .build())
                        .build())
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

        Assertions.assertEquals(expectedKU, actualInt.getKeyUsage());
        Assertions.assertEquals(expectedKU, actualHex.getKeyUsage());
        Assertions.assertEquals(actualInt, actualHex);
    }

    @Test
    public void testKeyUsageEmpty() {
        String json = """
            { "keyUsage": {} }
            """;

        ObjectMapper mapper = JsonMapper.builder().build();
        Assertions.assertThrows(IOException.class, () -> mapper.readValue(json, ExtensionsJsonHelper.class));
    }

    @Test
    public void testTargetInformationEmptyArray() throws Exception {
        String json = """
            { "TARGETINFORMATION": [] }
            """;

        ExtensionsJsonHelper expected = ExtensionsJsonHelper.builder()
                .targetingInformation(factory.TargetingInformationFactory.create().build()) // empty
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
                .certificatePolicies(CertificatePoliciesFactory.create()
                        .addPolicyInformation(PolicyInformationFactory.create()
                                .policyIdentifier(new ASN1ObjectIdentifier("1.2.840.113549.1.1.5"))
                                .addQualifier(PolicyInformationFactory.QualifierJson.CPS,
                                        new DERIA5String("https://example.com/cps"))
                                .build())
                        .build())
                .authorityInformationAccess(AuthorityInfoAccessFactory.create()
                        .addElement(AuthorityInfoAccessFactory.MethodJson.OCSP,
                                new GeneralName(new X500Name("CN=OCSP A")))
                        .build())
                .crlDistPoint(CRLDistPointFactory.create()
                        .addDistributionPoint(DistributionPointFactory.create()
                                .distributionPointName(new DistributionPointName(0,
                                        new GeneralNames(new GeneralName(new X500Name("CN=DP A")))))
                                .reasons(new ReasonFlags(0))
                                .cRLIssuer(new GeneralNames(new GeneralName(new X500Name("CN=Issuer A"))))
                                .build())
                        .build())
                .keyUsage(new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment))
                .targetingInformation(TargetingInformationFactory.create()
                        .addCertificate(CliHelper.loadCert("src/test/resources/ek_cert_2187.der", CliHelper.x509type.CERTIFICATE))
                        .build())
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        ExtensionsJsonHelper actual = mapper.readValue(json, ExtensionsJsonHelper.class);

        Assertions.assertEquals(expected, actual);
    }
}
