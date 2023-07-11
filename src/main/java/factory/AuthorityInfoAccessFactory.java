package factory;

import java.util.Vector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.GeneralName;

/**
 * Functions to help manage the creation of the authority info access extension.
 */
public class AuthorityInfoAccessFactory {
    /**
     * fields of the authority info access object
     */
    public enum ElementJson {
        ACCESSMETHOD,
        ACCESSLOCATION;
    }
    
    /**
     * access method options
     */
    public enum MethodJson {
        OCSP(AccessDescription.id_ad_ocsp),
        CAISSUERS(AccessDescription.id_ad_caIssuers);
        
        private ASN1ObjectIdentifier oid;
        
        private MethodJson(ASN1ObjectIdentifier oid) {
            this.oid = oid;
        }
        
        public ASN1ObjectIdentifier getOid() {
            return oid;
        }
    }
    
    private Vector<AccessDescription> elements = new Vector<AccessDescription>();
    
    private AuthorityInfoAccessFactory() {
        elements = new Vector<AccessDescription>();
    }
    
    /**
     * Begin defining the authority info access extension.
     * @return A new AuthorityInfoAccessFactory builder.
     */
    public static final AuthorityInfoAccessFactory create() {
        AuthorityInfoAccessFactory aiaf = new AuthorityInfoAccessFactory();
        return aiaf;
    }
    
    /**
     * Add a method and location element.
     * @param element {@link AccessDescription}
     * @return The AuthorityInfoAccessFactory object with a new element added.
     */
    public final AuthorityInfoAccessFactory addElement(final AccessDescription element) {
        elements.add(element);
        return this;
    }
    
    /**
     * Add an element using the enumerated methods.
     * @param type {@link AuthorityInfoAccessFactory.MethodJson}
     * @param location {@link GeneralName}
     * @return The AuthorityInfoAccessFactory object with a new element added.
     */
    public final AuthorityInfoAccessFactory addElement(final MethodJson type, final GeneralName location) {
        elements.add(new AccessDescription(type.getOid(), location));
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link AuthorityInformationAccess}
     */
    public final AuthorityInformationAccess build() {
        if (elements.isEmpty()) {
            return null;
        }
        AccessDescription[] ads = elements.toArray(new AccessDescription[elements.size()]);
        AuthorityInformationAccess aia = new AuthorityInformationAccess(ads);
        return aia;
    }
}
