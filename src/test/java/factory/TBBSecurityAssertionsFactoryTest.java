package factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.util.encoders.Base64;
import org.testng.Assert;
import org.testng.annotations.Test;
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
        Assert.assertEquals(tbsa.getVersion(), new ASN1Integer(Integer.valueOf(version)));
        CommonCriteriaMeasures ccInfo = tbsa.getCcInfo();
        Assert.assertEquals(ccInfo.getVersion().getString(), ccVersion);
        Assert.assertEquals(ccInfo.getAssuranceLevel(), new EvaluationAssuranceLevel(assuranceLevel));
        Assert.assertEquals(ccInfo.getEvaluationStatus(), new EvaluationStatus(evaluationStatus));
        Assert.assertEquals(ccInfo.getPlus().isTrue(), Boolean.valueOf(plus).booleanValue());
        Assert.assertEquals(ccInfo.getProfileOid().getId(), profileOid);
        Assert.assertEquals(ccInfo.getProfileUri().getUniformResourceIdentifier().getString(), profileURI);
        Assert.assertEquals(ccInfo.getProfileUri().getHashAlgorithm().getAlgorithm().getId(), profileAlg);
        Assert.assertEquals(Base64.encode(ccInfo.getProfileUri().getHashValue().getBytes()), profileHash.getBytes());
        Assert.assertEquals(ccInfo.getTargetOid().getId(), targetOid);
        Assert.assertEquals(ccInfo.getTargetUri().getUniformResourceIdentifier().getString(), targetURI);
        Assert.assertEquals(ccInfo.getTargetUri().getHashAlgorithm().getAlgorithm().getId(), targetAlg);
        Assert.assertEquals(Base64.encode(ccInfo.getTargetUri().getHashValue().getBytes()), targetHash.getBytes());
        FIPSLevel fips = tbsa.getFipsLevel();
        Assert.assertEquals(fips.getVersion().getString(), fipsVersion);
        Assert.assertEquals(fips.getLevel(), new SecurityLevel(fipsLevel));
        Assert.assertEquals(fips.getPlus().isTrue(), Boolean.valueOf(fipsPlus).booleanValue());
        Assert.assertEquals(tbsa.getRtmType(), new MeasurementRootType(rtmType));
        Assert.assertEquals(tbsa.getIso9000Certified().isTrue(), Boolean.valueOf(iso9000Certified).booleanValue());
        Assert.assertEquals(tbsa.getIso9000Uri().getString(), iso9000URI);
    }    
}
