package json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.ReasonFlags;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import factory.AuthorityInfoAccessFactory;
import factory.CertificatePoliciesFactory;
import factory.PolicyInformationFactory;
import factory.AuthorityInfoAccessFactory.ElementJson;
import factory.AuthorityInfoAccessFactory.MethodJson;

public class OtherExtensionsJsonHelper {
    public enum Json {
        CERTIFICATEPOLICIES,
        AUTHORITYINFOACCESS,
        CRLDISTRIBUTION; 
    }
    
    public enum CrlJson {
        DISTRIBUTIONNAME,
        TYPE,
        NAME,
        REASON,
        ISSUER;
    }
    
    public static final CertificatePoliciesFactory policiesFromJsonFile(final String filename) {
        CertificatePoliciesFactory cpf = CertificatePoliciesFactory.create();
        
        try {
            final String jsonData = new String(Files.readAllBytes(Paths.get(filename)));
            ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode root = objectMapper.readTree(jsonData);
            if (root.has(Json.CERTIFICATEPOLICIES.name())) {
                final JsonNode certPoliciesNode = root.get(Json.CERTIFICATEPOLICIES.name());
                if (certPoliciesNode.isArray()) {
                    for (final JsonNode policyInfoNode : certPoliciesNode) {
                        PolicyInformationFactory pif = PolicyInformationFactory.fromJsonNode(policyInfoNode);
                        cpf.addPolicyInformation(pif.build());
                    }
                }
            }
        } catch (IOException e) {
            // catch file read error
        }
        
        return cpf;
    }    
    
    public static final AuthorityInfoAccessFactory accessesFromJsonFile(final String filename) {
        AuthorityInfoAccessFactory aiaf = AuthorityInfoAccessFactory.create();
        
        try {
            final String jsonData = new String(Files.readAllBytes(Paths.get(filename)));
            ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode root = objectMapper.readTree(jsonData);
            if (root.has(Json.AUTHORITYINFOACCESS.name())) {
                final JsonNode aiaNode = root.get(Json.AUTHORITYINFOACCESS.name());
                if (aiaNode.isArray()) {
                    for (final JsonNode elementNode : aiaNode) {
                        if (elementNode.has(ElementJson.ACCESSMETHOD.name()) && elementNode.has(ElementJson.ACCESSLOCATION.name())) {
                            final JsonNode methodNode = elementNode.get(ElementJson.ACCESSMETHOD.name());
                            final JsonNode locationNode = elementNode.get(ElementJson.ACCESSLOCATION.name());
                            
                            aiaf.addElement(MethodJson.valueOf(methodNode.asText()), new GeneralName(new X500Name(locationNode.asText())));
                        }
                    }
                }
            }
        } catch (IOException e) {
            // catch file read error
        }
            
        return aiaf;
    }
    
    // Profile states exactly 1 dist point
    public static final CRLDistPoint crlFromJsonFile(final String filename) {
        CRLDistPoint cdf = null;
        
        try {
            final String jsonData = new String(Files.readAllBytes(Paths.get(filename)));
            ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode root = objectMapper.readTree(jsonData);
            if (root.has(Json.CRLDISTRIBUTION.name())) { // place array here if ever more than 1 dist point allowed
                final JsonNode crlNode = root.get(Json.CRLDISTRIBUTION.name());
                
                if (crlNode.has(CrlJson.DISTRIBUTIONNAME.name()) && crlNode.has(CrlJson.REASON.name()) && crlNode.has(CrlJson.ISSUER.name())) {
                    final JsonNode distNameNode = crlNode.get(CrlJson.DISTRIBUTIONNAME.name());
                    final JsonNode reasonNode = crlNode.get(CrlJson.REASON.name());
                    final JsonNode issuerNode = crlNode.get(CrlJson.ISSUER.name());
                    
                    DistributionPointName dpn = null;
                    if (distNameNode.has(CrlJson.TYPE.name()) && distNameNode.has(CrlJson.NAME.name())) {
                        final JsonNode typeNode = distNameNode.get(CrlJson.TYPE.name());
                        final JsonNode nameNode = distNameNode.get(CrlJson.NAME.name());
                        
                        dpn = new DistributionPointName(typeNode.asInt(), new GeneralNames(new GeneralName(new X500Name(nameNode.asText()))));
                    }
                    
                    cdf = new CRLDistPoint(new DistributionPoint[]{new DistributionPoint(dpn, new ReasonFlags(reasonNode.asInt()), new GeneralNames(new GeneralName(new X500Name(issuerNode.asText()))))});
                }
            }
        } catch (IOException e) {
            // catch file read error
        }
            
        return cdf;
    }
}
