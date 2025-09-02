package json;

import com.fasterxml.jackson.databind.JsonNode;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.util.encoders.Base64;
import tcg.credential.TCGPlatformSpecification;
import tcg.credential.TCGSpecificationVersion;

/**
 * @deprecated see AttributesJsonHelper
 */
public class PolicyReferenceJsonHelper {
    public enum PolicyRefJson {
        TCGPLATFORMSPECIFICATION,
        TCGCREDENTIALSPECIFICATION,
        TBBSECURITYASSERTIONS,
        PLATFORMCONFIGURI;
    }
    public enum SpecificationVersionJson {
        MAJORVERSION,
        MINORVERSION,
        REVISION;
    }
    public enum PlatformSpecificationJson {
        PLATFORMCLASS,
        VERSION;
    }
    
    public static final TCGPlatformSpecification platformSpec(final JsonNode refNode) {
        TCGSpecificationVersion version = null;
        TCGPlatformSpecification platformSpec = null;
        if (refNode.has(PlatformSpecificationJson.VERSION.name()) && refNode.has(PlatformSpecificationJson.PLATFORMCLASS.name())) {
            JsonNode platformClassNode = refNode.get(PlatformSpecificationJson.PLATFORMCLASS.name());
            JsonNode versionNode = refNode.get(PlatformSpecificationJson.VERSION.name());
            version = credentialSpec(versionNode);
            final byte[] platformClass = Base64.decode(platformClassNode.asText());
            
            platformSpec = new TCGPlatformSpecification(version, new DEROctetString(platformClass));
        } else {
            // error for required field
        }
        return platformSpec;
    }
    
    public static final TCGSpecificationVersion credentialSpec(final JsonNode refNode) {
        TCGSpecificationVersion version = null;
        if (refNode.has(SpecificationVersionJson.MAJORVERSION.name()) && refNode.has(SpecificationVersionJson.MINORVERSION.name()) && refNode.has(SpecificationVersionJson.REVISION.name())) {
            JsonNode majorVersionNode = refNode.get(SpecificationVersionJson.MAJORVERSION.name());
            JsonNode minorVersionNode = refNode.get(SpecificationVersionJson.MINORVERSION.name());
            JsonNode revisionNode = refNode.get(SpecificationVersionJson.REVISION.name());
            
            version = TCGSpecificationVersion.builder()
                        .majorVersion(new ASN1Integer(majorVersionNode.asInt()))
                        .minorVersion(new ASN1Integer(minorVersionNode.asInt()))
                        .revision(new ASN1Integer(revisionNode.asInt())).build();
        } else {
            // error for required fields
        }
        return version;
    }
}
