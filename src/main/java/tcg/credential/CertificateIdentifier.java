package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.IssuerSerial;

/**
 * <pre>
 * CertificateIdentifier ::= SEQUENCE {
 *      attributeCertIdentifier [0] IMPLICIT AttributeCertificateIdentifier OPTIONAL,
 *      genericCertIdentifier [1] IMPLICIT IssuerSerial OPTIONAL }
 * </pre>
 */
public class CertificateIdentifier extends ASN1Object {
    
    // minimum 0, maximum 2
    AttributeCertificateIdentifier attributeCertIdentifier = null; // optional, tagged 0
    IssuerSerial genericCertIdentifier = null; // optional, tagged 1
    
    public static CertificateIdentifier getInstance(Object obj) {
        if (obj == null || obj instanceof ComponentClass) {
            return (CertificateIdentifier) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new CertificateIdentifier((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }
    
    private CertificateIdentifier(ASN1Sequence seq) {
        if (seq.size() > 2) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        ASN1Object[] elements = (ASN1Object[]) seq.toArray();
        int pos = 0;
        if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject ) && (((ASN1TaggedObject)elements[pos]).getTagNo() == 0)) {
            if ((ASN1Object)((ASN1TaggedObject)elements[pos]).getObject() instanceof AttributeCertificateIdentifier) {
                attributeCertIdentifier = (AttributeCertificateIdentifier)(ASN1Object)((ASN1TaggedObject)elements[pos]).getObject();
            } else {
                throw new IllegalArgumentException("Expected AttributeCertificateIdentifier object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
        if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject ) && (((ASN1TaggedObject)elements[pos]).getTagNo() == 1)) {
            if ((ASN1Object)((ASN1TaggedObject)elements[pos]).getObject() instanceof IssuerSerial) {
                genericCertIdentifier = (IssuerSerial)(ASN1Object)((ASN1TaggedObject)elements[pos]).getObject();
            } else {
                throw new IllegalArgumentException("Expected IssuerSerial object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
        if ((elements.length - pos) > 0) {
            throw new IllegalArgumentException("Too many elements in CertificateIdentifier");
        }
    }

    public CertificateIdentifier(AttributeCertificateIdentifier attributeCertIdentifier, IssuerSerial genericCertIdentifier) {
        this.attributeCertIdentifier = attributeCertIdentifier;
        this.genericCertIdentifier = genericCertIdentifier;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(attributeCertIdentifier);
        vec.add(genericCertIdentifier);
        return new DERSequence(vec);
    }

    public AttributeCertificateIdentifier getAttributeCertIdentifier() {
        return attributeCertIdentifier;
    }

    public IssuerSerial getGenericCertIdentifier() {
        return genericCertIdentifier;
    }
}
