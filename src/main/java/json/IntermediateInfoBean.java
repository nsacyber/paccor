package json;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.util.encoders.Base64;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

public class IntermediateInfoBean {
    private String holder;
    private Map<String, String> attributes;
    private Map<String, String> extensions;
    private boolean delta;
    
    public IntermediateInfoBean() {
        holder = "";
        attributes = new Hashtable<String, String>();
        extensions = new Hashtable<String, String>();
        delta = false;
    }
    
    public String getHolder() {
        return holder;
    }
    public void setHolder(String holder) {
        this.holder = holder;
    }
    public boolean isDelta() {
        return delta;
    }
    public void setDelta(boolean delta) {
        this.delta = delta;
    }
    public Map<String, String> getAttributes() {
        return attributes;
    }
    public void addAttribute(String key, String value) {
        attributes.put(key, value);
    }
    public void addAttribute(ASN1ObjectIdentifier oid, ASN1Object value) throws IOException {
        attributes.put(oid.getId(), new String(Base64.toBase64String(value.getEncoded())));
    }
    public Map<String, String> getExtensions() {
        return extensions;
    }
    public void addExtension(String key, String value) {
        extensions.put(key, value);
    }
    public void addExtension(ASN1ObjectIdentifier oid, Extension ext) throws IOException { 
        extensions.put(oid.getId(), Base64.toBase64String(ext.getEncoded()));
    }
    public Hashtable<ASN1ObjectIdentifier, ASN1Object> convertAttributes() throws IOException {
        Hashtable<ASN1ObjectIdentifier, ASN1Object> table = new Hashtable<ASN1ObjectIdentifier, ASN1Object>();
        
        for (final String key : attributes.keySet()) {
            ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(key);
            ASN1Object obj = ASN1Primitive.fromByteArray(Base64.decode(attributes.get(key)));
            table.put(oid, obj);
        }
        
        return table;
    }
    public Hashtable<ASN1ObjectIdentifier, Extension> convertExtentions() throws IOException {
        Hashtable<ASN1ObjectIdentifier, Extension> table = new Hashtable<ASN1ObjectIdentifier, Extension>();
        
        for (final String key : extensions.keySet()) {
            ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(key);
            Extension obj = Extension.getInstance(Base64.decode(extensions.get(key)));
            table.put(oid, obj);
        }
        
        return table;
    }
}
