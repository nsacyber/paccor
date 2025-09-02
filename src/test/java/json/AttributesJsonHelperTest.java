package json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.io.IOException;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tcg.credential.TBBSecurityAssertions;
import tcg.credential.TCGPlatformSpecification;
import tcg.credential.TCGSpecificationVersion;
import tcg.credential.URIReference;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

public class AttributesJsonHelperTest {
    @Test
    public void testLegacy() throws IOException {
        String json = """
                {
                
                    "TCGPLATFORMSPECIFICATION": {
                        "VERSION": {
                            "MAJORVERSION": "1",
                            "MINORVERSION": "3",
                            "REVISION": "22"
                        },
                        "PLATFORMCLASS": "AAAAAQ=="
                    },
                    "TCGCREDENTIALSPECIFICATION": {
                            "MAJORVERSION": "1",
                            "MINORVERSION": "1",
                            "REVISION": "17"
                    },
                    "TBBSECURITYASSERTIONS": {
                        "VERSION": "1",
                        "ISO9000CERTIFIED": "false"
                    }
                }
                """;
        AttributesJsonHelper expected = AttributesJsonHelper.builder()
                .tCGPlatformSpecification(TCGPlatformSpecification.builder()
                        .version(TCGSpecificationVersion.builder()
                                .majorVersion(new ASN1Integer(1))
                                .minorVersion(new ASN1Integer(3))
                                .revision(new ASN1Integer(22))
                                .build())
                        .platformClass(new DEROctetString(new byte[]{0x00, 0x00, 0x00, 0x01}))
                        .build())
                .tCGCredentialSpecification(TCGSpecificationVersion.builder()
                        .majorVersion(new ASN1Integer(1))
                        .minorVersion(new ASN1Integer(1))
                        .revision(new ASN1Integer(17))
                        .build())
                .tBBSecurityAssertions(TBBSecurityAssertions.builder()
                        .version(new ASN1Integer(1))
                        .iso9000Certified(ASN1Boolean.FALSE)
                        .build())
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();

        AttributesJsonHelper actual = mapper.readValue(json, AttributesJsonHelper.class);

        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testCaseInsensitivityAndUnknownIgnored() throws Exception {
        String json = """
                {
                    "tcgPlatformSpecification": {
                        "version": { "majorVersion": "1", "minorVersion": "0", "revision": "2" },
                        "platformClass": "AQID"
                    },
                    "tcgCredentialSpecification": { "majorVersion": "1", "minorVersion": "0", "revision": "2" },
                    "tbbSecurityAssertions": { "version": "1", "iso9000Certified": "false" },
                    "SOME_UNKNOWN": 123
                }
                """;

        AttributesJsonHelper expected = AttributesJsonHelper.builder()
                .tCGPlatformSpecification(TCGPlatformSpecification.builder()
                        .version(TCGSpecificationVersion.builder()
                                .majorVersion(new ASN1Integer(1))
                                .minorVersion(new ASN1Integer(0))
                                .revision(new ASN1Integer(2))
                                .build())
                        .platformClass(new DEROctetString(new byte[]{0x00, 0x01, 0x02, 0x03}))
                        .build())
                .tCGCredentialSpecification(TCGSpecificationVersion.builder()
                        .majorVersion(new ASN1Integer(1))
                        .minorVersion(new ASN1Integer(0))
                        .revision(new ASN1Integer(2))
                        .build())
                .tBBSecurityAssertions(TBBSecurityAssertions.builder()
                        .version(new ASN1Integer(1))
                        .iso9000Certified(ASN1Boolean.FALSE)
                        .build())
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        AttributesJsonHelper actual = mapper.readValue(json, AttributesJsonHelper.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testPlatformConfigUri1() throws Exception {
        String json = """
                {
                    "PLATFORMCONFIGURI": {
                        "UNIFORMRESOURCEIDENTIFIER": "https://example.com/config",
                        "HASHALGORITHM": "1.2.840.113549.2.5",
                        "HASHVALUE": "AQIDBA=="
                    }
                }
                """;

        AttributesJsonHelper expected = AttributesJsonHelper.builder()
                .platformConfigUri(URIReference.builder()
                        .uniformResourceIdentifier(new DERIA5String("https://example.com/config"))
                        .hashAlgorithm(new AlgorithmIdentifier(new ASN1ObjectIdentifier("1.2.840.113549.2.5")))
                        .hashValue(new DERBitString(new byte[]{1,2,3,4}))
                        .build())
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        AttributesJsonHelper actual = mapper.readValue(json, AttributesJsonHelper.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testIso9000CertifiedBoolean() throws Exception {
        String json = """
                {
                    "tbbSecurityAssertions": { "version": "1", "iso9000Certified": true }
                }
                """;

        AttributesJsonHelper expected = AttributesJsonHelper.builder()
                .tBBSecurityAssertions(TBBSecurityAssertions.builder()
                        .version(new ASN1Integer(1))
                        .iso9000Certified(ASN1Boolean.TRUE)
                        .build())
                .build();

        ObjectMapper mapper = JsonMapper.builder().build();
        AttributesJsonHelper actual = mapper.readValue(json, AttributesJsonHelper.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testEmptyJsonDeserializesToNullFields() throws Exception {
        String json = "{}";
        ObjectMapper mapper = JsonMapper.builder().build();
        AttributesJsonHelper actual = mapper.readValue(json, AttributesJsonHelper.class);
        AttributesJsonHelper expected = AttributesJsonHelper.builder().build();
        Assertions.assertEquals(expected, actual);
    }
}
