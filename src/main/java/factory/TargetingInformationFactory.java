package factory;

import java.util.Arrays;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.Target;
import org.bouncycastle.asn1.x509.TargetInformation;
import org.bouncycastle.cert.X509CertificateHolder;

/**
 * Functions to help manage the creation of the AC targeting extension.
 */
public class TargetingInformationFactory {
    
private Vector<Target> elements = new Vector<Target>();
    
    private TargetingInformationFactory() {
        elements = new Vector<Target>();
    }
    
    /**
     * Begin defining the target information extension.
     */
    public static final TargetingInformationFactory create() {
        TargetingInformationFactory tif = new TargetingInformationFactory();
        return tif;
    }
    
    /**
     * Add a target element.
     * @param element {@link Target}
     */
    public final TargetingInformationFactory addElement(final Target element) {
        elements.add(element);
        return this;
    }
    
    /**
     * 
     * @param cert
     * @return
     */
    public final TargetingInformationFactory addCertificate(final X509CertificateHolder cert) {
        X500Name subjectName = cert.getSubject();
        
        if (subjectName == null || cert.getSerialNumber() == null) {
            throw new IllegalArgumentException("The target information extension cannot use "
                    + "the provided certificate.  It is missing vital information.");
        }
        
        // The EKC serial number MUST be included as an RDN at oid id-at-serialnumber
        if (subjectName.getRDNs(BCStyle.SERIALNUMBER).length == 0) {
            // Add the certificate serial number
            
            // Convert the serial number from BigInteger to a RDN
            DERUTF8String serialNumber = new DERUTF8String(cert.getSerialNumber().toString());
            AttributeTypeAndValue serialNumberATV = new AttributeTypeAndValue(BCStyle.SERIALNUMBER, serialNumber);
            RDN serialNumberRdn = new RDN(serialNumberATV);
            
            // Add the RDN to the X500Name
            Vector<RDN> rdns = new Vector<RDN>();
            rdns.addAll(Arrays.asList(subjectName.getRDNs()));
            rdns.add(serialNumberRdn);
            
            subjectName = new X500Name(rdns.toArray(new RDN[rdns.size()]));
        }
        
        // Add the new Target to the extension
        Target target = new Target(Target.targetName, new GeneralName(subjectName));
        addElement(target);
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link TargetInformation}
     */
    public final TargetInformation build() {
        if (elements.isEmpty()) {
            return null;
        }
        Target[] targets = elements.toArray(new Target[elements.size()]);
        TargetInformation ti = new TargetInformation(targets);
        return ti;
    }
}
