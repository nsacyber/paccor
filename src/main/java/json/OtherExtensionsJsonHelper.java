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
import org.bouncycastle.cert.X509CertificateHolder;

import cli.CliHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import factory.AuthorityInfoAccessFactory;
import factory.CertificatePoliciesFactory;
import factory.PolicyInformationFactory;
import factory.TargetingInformationFactory;
import factory.AuthorityInfoAccessFactory.ElementJson;
import factory.AuthorityInfoAccessFactory.MethodJson;

public class OtherExtensionsJsonHelper {
    public enum Json {
        CERTIFICATEPOLICIES,
        AUTHORITYINFOACCESS,
        CRLDISTRIBUTION,
        TARGETINGINFORMATION; 
    }
    
    public enum CrlJson {
        DISTRIBUTIONNAME,
        TYPE,
        NAME,
        REASON,
        ISSUER;
    }
    
    public enum TargetInformationJson {
        FILE;
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
    
    public static final TargetingInformationFactory ekTargetsFromJsonFile(final String filename) {
        TargetingInformationFactory tif = TargetingInformationFactory.create();
        
        try {
            final String jsonData = new String(Files.readAllBytes(Paths.get(filename)));
            ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode root = objectMapper.readTree(jsonData);
            if (root.has(Json.TARGETINGINFORMATION.name())) {
                final JsonNode tiNode = root.get(Json.TARGETINGINFORMATION.name());
                if (tiNode.isArray()) {
                    for (final JsonNode target : tiNode) {
                        if (target.has(TargetInformationJson.FILE.name())) {
                            final JsonNode fileNode = target.get(TargetInformationJson.FILE.name());
                            final String targetFilename = fileNode.asText();
                            X509CertificateHolder cert = (X509CertificateHolder)CliHelper.loadCert(targetFilename, CliHelper.x509type.CERTIFICATE);
                            tif.addCertificate(cert);
                        }
                    }
                }
            }
        } catch (IOException e) {
            // catch file read error
        }
        
        return tif;
    }
}
