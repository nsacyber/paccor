package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * <pre>
 * AttributeCertificateIdentifier ::= SEQUENCE {
 *      hashAlgorithm AlgorithmIdentifier,
 *      hashOverSignatureValue OCTET STRING }
 * </pre>
 */
public class AttributeCertificateIdentifier extends ASN1Object {

    // minimum 2, maximum 2
    AlgorithmIdentifier hashAlgorithm;
    ASN1OctetString hashOverSignatureValue;
    
    public static AttributeCertificateIdentifier getInstance(Object obj) {
        if (obj == null || obj instanceof AttributeCertificateIdentifier) {
            return (AttributeCertificateIdentifier) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new AttributeCertificateIdentifier((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }
    
    private AttributeCertificateIdentifier(ASN1Sequence seq) {
        if (seq.size() != 2) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        ASN1Object[] elements = (ASN1Object[]) seq.toArray();
        if (elements[0] instanceof AlgorithmIdentifier) {
            hashAlgorithm = (AlgorithmIdentifier) elements[0];
        } else {
            throw new IllegalArgumentException("Expected ASN1ObjectIdentifier, received " + elements[0].getClass().getName());
        }
        if (elements[1] instanceof ASN1OctetString) {
            hashOverSignatureValue = (ASN1OctetString) elements[1];
        } else {
            throw new IllegalArgumentException("Expected ASN1OctetString, received " + elements[1].getClass().getName());
        }
    }

    public AttributeCertificateIdentifier(AlgorithmIdentifier hashAlgorithm, ASN1OctetString hashOverSignatureValue) {
        this.hashAlgorithm = hashAlgorithm;
        this.hashOverSignatureValue = hashOverSignatureValue;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(hashAlgorithm);
        vec.add(hashOverSignatureValue);
        return new DERSequence(vec);
    }

    public AlgorithmIdentifier getHashAlgorithm() {
        return hashAlgorithm;
    }

    public ASN1OctetString getHashOverSignatureValue() {
        return hashOverSignatureValue;
    }
}
