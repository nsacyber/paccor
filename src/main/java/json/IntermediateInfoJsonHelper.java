package json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.asn1.x509.Holder;
import org.bouncycastle.cert.AttributeCertificateHolder;
import org.bouncycastle.util.encoders.Base64;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class IntermediateInfoJsonHelper {

    public enum InfoJson {
        HOLDER,
        ATTRIBUTES,
        EXTENSIONS;
    }
    public enum HolderJson {
        VALUE;
    }
    public enum AttributesJson {
        TYPE,
        VALUE;
    }
    public enum ExtensionsJson {
        OID,
        ISCRITICAL,
        VALUE;
    }
    
    public static final Holder readHolder(final JsonNode refNode) {
        Holder holder = null;
        
        if (refNode.has(HolderJson.VALUE.name())) {
            JsonNode valueNode = refNode.get(HolderJson.VALUE.name());
            
            holder = Holder.getInstance(Base64.decode(valueNode.asText()));
        }
        
        return holder;
    }
    
    public static final HashMap<ASN1ObjectIdentifier, Vector<String>> readAttributes(final JsonNode refNode) {
        HashMap<ASN1ObjectIdentifier, Vector<String>> map = new HashMap<ASN1ObjectIdentifier, Vector<String>>();
        
        if (refNode.isArray()) {
            Iterator<JsonNode> attributeList = refNode.elements();
            while (attributeList.hasNext()) {
                final JsonNode attributeNode = attributeList.next();
                if (attributeNode.has(AttributesJson.TYPE.name()) && attributeNode.has(AttributesJson.VALUE.name())) {
                    JsonNode typeNode = refNode.get(AttributesJson.TYPE.name());
                    JsonNode valueNode = refNode.get(AttributesJson.VALUE.name());
                    
                    ASN1ObjectIdentifier type = new ASN1ObjectIdentifier(typeNode.asText());
                    Vector<String> values = new Vector<String>();
                    if (map.containsKey(type)) {
                        values = map.get(type);
                    }
                    values.add(valueNode.asText());
                    map.put(type, values);
                }
            }
        }
        
        return map;
    }
    
    public static final Extensions readExtensions(final JsonNode refNode) {
        Vector<Extension> extensions = new Vector<Extension>();
        
        if (refNode.isArray()) {
            Iterator<JsonNode> extensionList = refNode.elements();
            while (extensionList.hasNext()) {
                final JsonNode extensionNode = extensionList.next();
                if (extensionNode.has(ExtensionsJson.OID.name()) && extensionNode.has(ExtensionsJson.ISCRITICAL.name()) && extensionNode.has(ExtensionsJson.VALUE.name())) {
                    JsonNode oidNode = refNode.get(ExtensionsJson.OID.name());
                    JsonNode isCriticalNode = refNode.get(ExtensionsJson.ISCRITICAL.name());
                    JsonNode valueNode = refNode.get(ExtensionsJson.VALUE.name());
                    
                    ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(oidNode.asText());
                    boolean critical = false;
                    if (isCriticalNode != null) {
                        if (isCriticalNode.isBoolean()) {
                            critical = isCriticalNode.asBoolean();
                        } else if (isCriticalNode.isInt()) {
                            critical = isCriticalNode.asInt() != 0;   
                        }
                    }
                    byte[] value = Base64.decode(valueNode.asText());
                    
                    extensions.add(new Extension(oid, critical, value)); 
                }
            }
        }
        
        return new Extensions(extensions.toArray(new Extension[extensions.size()]));
    }
}
