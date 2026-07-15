package paccor.json;

import java.io.IOException;
import java.util.List;
import paccor.json.AttributesJsonHelper;
import paccor.json.ObjectMapperFactory;
import paccor.json.ResolvedCertificateReferenceMap;
import paccor.json.schema.ComponentSchema;
import paccor.model.CertificateReference;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.CommonCriteriaMeasures;
import paccor.tcg.credential.EvaluationAssuranceLevel;
import paccor.tcg.credential.EvaluationStatus;
import paccor.tcg.credential.FIPSLevel;
import paccor.tcg.credential.MeasurementRootType;
import paccor.tcg.credential.SecurityLevel;
import paccor.tcg.credential.TBBSecurityAssertions;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TCGPlatformSpecification;
import paccor.tcg.credential.TCGSpecificationVersion;
import paccor.tcg.credential.URIReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class AttributesJsonHelperTest {
    private static final String TEST_PLATFORM_CERT = "src/test/resources/sample_testgen1/platform_cert.20250909102720.crt";
    private static final String TEST_PUBLIC_KEY_CERT = "src/test/resources/TestCA.cert.example.pem";

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

        ObjectMapper mapper = ObjectMapperFactory.create();

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

        ObjectMapper mapper = ObjectMapperFactory.create();
        AttributesJsonHelper actual = mapper.readValue(json, AttributesJsonHelper.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testLegacyTpmFieldsDeserialize() throws Exception {
        String json = """
                {
                    "tcpaSpecVersion": { "major": "1", "minor": "2" },
                    "tpmManufacturer": "Acme",
                    "tpmModel": "RoadRunner",
                    "tpmVersion": "7.1",
                    "tpmSpecification": {
                        "family": "2.0",
                        "level": "138",
                        "revision": "1"
                    },
                    "tpmSecurityAssertions": {
                        "version": "0",
                        "fieldUpgradable": true
                    }
                }
                """;

        AttributesJsonHelper actual = ObjectMapperFactory.create().readValue(json, AttributesJsonHelper.class);

        Assertions.assertEquals(new DERUTF8String("Acme"), actual.tPMManufacturer());
        Assertions.assertEquals(new DERUTF8String("RoadRunner"), actual.tPMModel());
        Assertions.assertEquals(new DERUTF8String("7.1"), actual.tPMVersion());
        Assertions.assertNotNull(actual.tCPASpecVersion());
        Assertions.assertEquals(1, actual.tCPASpecVersion().getMajor().getValue().intValueExact());
        Assertions.assertEquals(2, actual.tCPASpecVersion().getMinor().getValue().intValueExact());
        Assertions.assertNotNull(actual.tPMSpecification());
        Assertions.assertEquals(new DERUTF8String("2.0"), actual.tPMSpecification().getFamily());
        Assertions.assertEquals(138, actual.tPMSpecification().getLevel().getValue().intValueExact());
        Assertions.assertEquals(1, actual.tPMSpecification().getRevision().getValue().intValueExact());
        Assertions.assertNotNull(actual.tPMSecurityAssertions());
        Assertions.assertEquals(0, actual.tPMSecurityAssertions().getVersion().getValue().intValueExact());
        Assertions.assertEquals(ASN1Boolean.TRUE, actual.tPMSecurityAssertions().getFieldUpgradable());
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

        ObjectMapper mapper = ObjectMapperFactory.create();
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

        ObjectMapper mapper = ObjectMapperFactory.create();
        AttributesJsonHelper actual = mapper.readValue(json, AttributesJsonHelper.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testEmptyJsonDeserializesToNullFields() throws Exception {
        String json = "{}";
        ObjectMapper mapper = ObjectMapperFactory.create();
        AttributesJsonHelper actual = mapper.readValue(json, AttributesJsonHelper.class);
        AttributesJsonHelper expected = AttributesJsonHelper.builder().build();
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testPreviousPlatformCertificatesSupportsFileEntries() throws Exception {
        String json = """
                {
                    "previousPlatformCertificates": [
                        { "FILE": "%s" }
                    ]
                }
                """.formatted(TEST_PLATFORM_CERT);

        ObjectMapper mapper = JsonMapper.builder().build();
        AttributesJsonHelper actual = mapper.readValue(json, AttributesJsonHelper.class);

        Assertions.assertNotNull(actual.previousPlatformCertificates());
        List<?> traits = actual.previousPlatformCertificates().get(CertificateIdentifierTrait.class);
        Assertions.assertNotNull(traits);
        Assertions.assertEquals(1, traits.size());

        List<?> refs = ResolvedCertificateReferenceMap.referencesOf(actual.previousPlatformCertificates());
        Assertions.assertEquals(1, refs.size());
        CertificateReference reference = (CertificateReference) refs.getFirst();
        Assertions.assertEquals(TCGObjectIdentifier.tcgTrCatPlatformCertificate, reference.toTrait().getTraitCategory());
    }

    @Test
    public void testCryptographicAnchorsSupportsFileEntries() throws Exception {
        String json = """
                {
                    "cryptographicAnchors": [
                        { "FILE": "%s" }
                    ]
                }
                """.formatted(TEST_PUBLIC_KEY_CERT);

        ObjectMapper mapper = ObjectMapperFactory.create();
        AttributesJsonHelper actual = mapper.readValue(json, AttributesJsonHelper.class);

        Assertions.assertNotNull(actual.cryptographicAnchors());
        List<?> traits = actual.cryptographicAnchors().get(CertificateIdentifierTrait.class);
        Assertions.assertNotNull(traits);
        Assertions.assertEquals(1, traits.size());
        CertificateIdentifierTrait trait = (CertificateIdentifierTrait) traits.getFirst();
        Assertions.assertEquals(TCGObjectIdentifier.tcgTrCatGenericCertificate, trait.getTraitCategory());

        List<?> refs = ResolvedCertificateReferenceMap.referencesOf(actual.cryptographicAnchors());
        Assertions.assertEquals(1, refs.size());
        CertificateReference reference = (CertificateReference) refs.getFirst();
        Assertions.assertEquals(TCGObjectIdentifier.tcgTrCatGenericCertificate, reference.toTrait().getTraitCategory());
    }

    // Fields of TBBSecurityAssertions FOR TEST
    public enum Json {
        VERSION,
        ASSURANCELEVEL,
        EVALUATIONSTATUS,
        PLUS,
        STRENGTHOFFUNCTION,
        LEVEL,
        PROFILEOID,
        PROFILEURI,
        TARGETOID,
        TARGETURI,
        CCINFO,
        FIPSLEVEL,
        RTMTYPE,
        ISO9000CERTIFIED,
        ISO9000URI;
    }

    @Test
    public void testTBBFromJson() throws Exception {
        final String version = "1";
        final String iso9000Certified = "FALSE";
        final String ccVersion = "2.2";
        final int assuranceLevel = 6;
        final String evaluationStatus = "evaluationInProgress";
        final String plus = "FALSE";
        final String sof = "high";
        final String profileOid = "1.2.1.3.4";
        final String profileURI = "./enterprise-numbers";
        final String profileAlg = "2.16.840.1.101.3.4.2.1";
        final String profileHash = "FsMOcUPnfEjmF+vn+6sCr0UDqLmETPolZGx79QtH2CY=";
        final String targetOid = "2.2.3.5.5";
        final String targetURI = "./referenceoptions.sh";
        final String targetAlg = "2.16.840.1.101.3.4.2.1";
        final String targetHash = "ERuruGz0beU6AjqOaLKX3RFRNLp8s88htnelUexPHHY=";
        final String fipsVersion = "140-2";
        final int fipsLevel = 3;
        final String fipsPlus = "FALSE";
        final String rtmType = "static";
        final String iso9000URI = "./referenceoptions.sh";
        final String jsonData =
                "{"
                        +       "    \"" + Json.VERSION.name() + "\": \"" + version + "\","
                        +       "    \"" + Json.CCINFO.name() + "\": {"
                        +       "        \"" + Json.VERSION.name() + "\": \"" + ccVersion + "\","
                        +       "        \"" + Json.ASSURANCELEVEL.name() + "\": \"" + assuranceLevel + "\","
                        +       "        \"" + Json.EVALUATIONSTATUS.name() + "\": \"" + evaluationStatus + "\","
                        +       "        \"" + Json.PLUS.name() + "\": \"" + plus + "\","
                        +       "        \"" + Json.STRENGTHOFFUNCTION.name() + "\": \"" + sof + "\","
                        +       "        \"" + Json.PROFILEOID.name() + "\": \"" + profileOid + "\","
                        +       "        \"" + Json.PROFILEURI.name() + "\": {"
                        +       "            \"" + ComponentSchema.UNIFORM_RESOURCE_IDENTIFIER + "\": \"" + profileURI + "\","
                        +       "            \"" + ComponentSchema.HASH_ALGORITHM + "\": \"" + profileAlg + "\","
                        +       "            \"" + ComponentSchema.HASH_VALUE + "\": \"" + profileHash + "\""
                        +       "        },"
                        +       "        \"" + Json.TARGETOID.name() + "\": \"" + targetOid + "\","
                        +       "        \"" + Json.TARGETURI.name() + "\": {"
                        +       "            \"" + ComponentSchema.UNIFORM_RESOURCE_IDENTIFIER + "\": \"" + targetURI + "\","
                        +       "            \"" + ComponentSchema.HASH_ALGORITHM + "\": \"" + targetAlg + "\","
                        +       "            \"" + ComponentSchema.HASH_VALUE + "\": \"" + targetHash + "\""
                        +       "        }"
                        +       "    },"
                        +       "    \"" + Json.FIPSLEVEL.name() + "\": {"
                        +       "        \"" + Json.VERSION.name() + "\": \"" + fipsVersion + "\","
                        +       "        \"" + Json.LEVEL.name() + "\": \"" + fipsLevel + "\","
                        +       "        \"" + Json.PLUS.name() + "\": \"" + fipsPlus + "\""
                        +       "    },"
                        +       "    \"" + Json.RTMTYPE.name() + "\": \"" + rtmType + "\","
                        +       "    \"" + Json.ISO9000CERTIFIED.name() + "\": \"" + iso9000Certified + "\","
                        +       "    \"" + Json.ISO9000URI.name() + "\": \"" + iso9000URI + "\""
                        +       "}";
        TBBSecurityAssertions tbsa = ObjectMapperFactory.fromJsonSafe(jsonData, TBBSecurityAssertions.class);
        Assertions.assertEquals(new ASN1Integer(Integer.parseInt(version)), tbsa.getVersion().orElse(null));
        CommonCriteriaMeasures ccInfo = tbsa.getCcInfo().orElseThrow();
        Assertions.assertEquals(ccVersion, ccInfo.getVersion().getString());
        Assertions.assertEquals(EvaluationAssuranceLevel.getInstance(assuranceLevel), ccInfo.getAssuranceLevel());
        Assertions.assertEquals(EvaluationStatus.getInstance(evaluationStatus), ccInfo.getEvaluationStatus());
        Assertions.assertEquals(Boolean.valueOf(plus), ccInfo.getPlus().isTrue());
        Assertions.assertEquals(profileOid, ccInfo.getProfileOid().getId());
        Assertions.assertEquals(profileURI, ccInfo.getProfileUri().getUniformResourceIdentifier().getString());
        Assertions.assertEquals(profileAlg, ccInfo.getProfileUri().getHashAlgorithm().getAlgorithm().getId());
        Assertions.assertArrayEquals(Base64.decode(profileHash), ccInfo.getProfileUri().getHashValue().getOctets());
        Assertions.assertEquals(targetOid, ccInfo.getTargetOid().getId());
        Assertions.assertEquals(targetURI, ccInfo.getTargetUri().getUniformResourceIdentifier().getString());
        Assertions.assertEquals(targetAlg, ccInfo.getTargetUri().getHashAlgorithm().getAlgorithm().getId());
        Assertions.assertArrayEquals(Base64.decode(targetHash), ccInfo.getTargetUri().getHashValue().getOctets());
        FIPSLevel fips = tbsa.getFipsLevel().orElseThrow();
        Assertions.assertEquals(fipsVersion, fips.getVersion().getString());
        Assertions.assertEquals(SecurityLevel.getInstance(fipsLevel), fips.getLevel());
        Assertions.assertEquals(Boolean.valueOf(fipsPlus), fips.getPlus().isTrue());
        Assertions.assertEquals(MeasurementRootType.getInstance(rtmType), tbsa.getRtmType().orElseThrow());
        Assertions.assertEquals(Boolean.valueOf(iso9000Certified), tbsa.getIso9000Certified().isTrue());
        Assertions.assertEquals(iso9000URI, tbsa.getIso9000Uri().map(s -> s.getString()).orElse(null));
    }
}
