package factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tcg.credential.CommonCriteriaMeasures;
import tcg.credential.EvaluationAssuranceLevel;
import tcg.credential.EvaluationStatus;
import tcg.credential.FIPSLevel;
import tcg.credential.MeasurementRootType;
import tcg.credential.SecurityLevel;
import tcg.credential.TBBSecurityAssertions;

public class TBBSecurityAssertionsFactoryTest {

    @Test
    public void testFromJson() throws Exception {
        final String version = "1";
        final String iso9000Certified = "FALSE";
        final String ccVersion = "2.2";
        final String assuranceLevel = "6";
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
        final String fipsLevel = "3";
        final String fipsPlus = "FALSE";
        final String rtmType = "static";
        final String iso9000URI = "./referenceoptions.sh";
        final String jsonData = 
        "{"
+       "    \"" + TBBSecurityAssertionsFactory.Json.VERSION.name() + "\": \"" + version + "\","
+       "    \"" + TBBSecurityAssertionsFactory.Json.CCINFO.name() + "\": {"
+       "        \"" + CommonCriteriaMeasuresFactory.Json.VERSION.name() + "\": \"" + ccVersion + "\","
+       "        \"" + CommonCriteriaMeasuresFactory.Json.ASSURANCELEVEL.name() + "\": \"" + assuranceLevel + "\","
+       "        \"" + CommonCriteriaMeasuresFactory.Json.EVALUATIONSTATUS.name() + "\": \"" + evaluationStatus + "\","
+       "        \"" + CommonCriteriaMeasuresFactory.Json.PLUS.name() + "\": \"" + plus + "\","
+       "        \"" + CommonCriteriaMeasuresFactory.Json.STRENGTHOFFUNCTION.name() + "\": \"" + sof + "\","
+       "        \"" + CommonCriteriaMeasuresFactory.Json.PROFILEOID.name() + "\": \"" + profileOid + "\","
+       "        \"" + CommonCriteriaMeasuresFactory.Json.PROFILEURI.name() + "\": {"
+       "            \"" + URIReferenceFactory.Json.UNIFORMRESOURCEIDENTIFIER.name() + "\": \"" + profileURI + "\","
+       "            \"" + URIReferenceFactory.Json.HASHALGORITHM.name() + "\": \"" + profileAlg + "\","
+       "            \"" + URIReferenceFactory.Json.HASHVALUE.name() + "\": \"" + profileHash + "\""
+       "        },"
+       "        \"" + CommonCriteriaMeasuresFactory.Json.TARGETOID.name() + "\": \"" + targetOid + "\","
+       "        \"" + CommonCriteriaMeasuresFactory.Json.TARGETURI.name() + "\": {"
+       "            \"" + URIReferenceFactory.Json.UNIFORMRESOURCEIDENTIFIER.name() + "\": \"" + targetURI + "\","
+       "            \"" + URIReferenceFactory.Json.HASHALGORITHM.name() + "\": \"" + targetAlg + "\","
+       "            \"" + URIReferenceFactory.Json.HASHVALUE.name() + "\": \"" + targetHash + "\""
+       "        }"
+       "    },"
+       "    \"" + TBBSecurityAssertionsFactory.Json.FIPSLEVEL.name() + "\": {"
+       "        \"" + TBBSecurityAssertionsFactory.FipsJson.VERSION.name() + "\": \"" + fipsVersion + "\","
+       "        \"" + TBBSecurityAssertionsFactory.FipsJson.LEVEL.name() + "\": \"" + fipsLevel + "\","
+       "        \"" + TBBSecurityAssertionsFactory.FipsJson.PLUS.name() + "\": \"" + fipsPlus + "\""
+       "    },"
+       "    \"" + TBBSecurityAssertionsFactory.Json.RTMTYPE.name() + "\": \"" + rtmType + "\","
+       "    \"" + TBBSecurityAssertionsFactory.Json.ISO9000CERTIFIED.name() + "\": \"" + iso9000Certified + "\","
+       "    \"" + TBBSecurityAssertionsFactory.Json.ISO9000URI.name() + "\": \"" + iso9000URI + "\""
+       "}";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonData);
        TBBSecurityAssertions tbsa =
                TBBSecurityAssertionsFactory.create()
                .fromJsonNode(root)
                .build();
        Assertions.assertEquals(tbsa.getVersion(), new ASN1Integer(Integer.valueOf(version)));
        CommonCriteriaMeasures ccInfo = tbsa.getCcInfo();
        Assertions.assertEquals(ccVersion, ccInfo.getVersion().getString());
        Assertions.assertEquals(new EvaluationAssuranceLevel(assuranceLevel), ccInfo.getAssuranceLevel());
        Assertions.assertEquals(new EvaluationStatus(evaluationStatus), ccInfo.getEvaluationStatus());
        Assertions.assertEquals(Boolean.valueOf(plus).booleanValue(), ccInfo.getPlus().isTrue());
        Assertions.assertEquals(profileOid, ccInfo.getProfileOid().getId());
        Assertions.assertEquals(profileURI, ccInfo.getProfileUri().getUniformResourceIdentifier().getString());
        Assertions.assertEquals(profileAlg, ccInfo.getProfileUri().getHashAlgorithm().getAlgorithm().getId());
        Assertions.assertArrayEquals(profileHash.getBytes(), Base64.encode(ccInfo.getProfileUri().getHashValue().getBytes()));
        Assertions.assertEquals(targetOid, ccInfo.getTargetOid().getId());
        Assertions.assertEquals(targetURI, ccInfo.getTargetUri().getUniformResourceIdentifier().getString());
        Assertions.assertEquals(targetAlg, ccInfo.getTargetUri().getHashAlgorithm().getAlgorithm().getId());
        Assertions.assertArrayEquals(targetHash.getBytes(), Base64.encode(ccInfo.getTargetUri().getHashValue().getBytes()));
        FIPSLevel fips = tbsa.getFipsLevel();
        Assertions.assertEquals(fipsVersion, fips.getVersion().getString());
        Assertions.assertEquals(new SecurityLevel(fipsLevel), fips.getLevel());
        Assertions.assertEquals(Boolean.valueOf(fipsPlus).booleanValue(), fips.getPlus().isTrue());
        Assertions.assertEquals(new MeasurementRootType(rtmType), tbsa.getRtmType());
        Assertions.assertEquals(Boolean.valueOf(iso9000Certified).booleanValue(), tbsa.getIso9000Certified().isTrue());
        Assertions.assertEquals(iso9000URI, tbsa.getIso9000Uri().getString());
    }    
}
