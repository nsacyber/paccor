package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * tCGCredentialType ATTRIBUTE ::= {
 *      WITH SYNTAX TCGCredentialType
 *      ID tcg-at-tcgCredentialType }
 * 
 * TCGCredentialType ::= SEQUENCE {
 *      certificateType CredentialType }
 *      
 * CredentialType ::= OBJECT IDENTIFIER (tcg-kp-PlatformAttributeCertificate | tcg-kp-DeltaPlatformAttributeCertificate)
 * </pre>
 */
public class TCGCredentialType extends ASN1Object {
    
    ASN1ObjectIdentifier credentialType;
    
    public static TCGCredentialType getInstance(Object obj) {
        if (obj == null || obj instanceof TCGCredentialType) {
            return (TCGCredentialType) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new TCGCredentialType((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }
    
    private TCGCredentialType(ASN1Sequence seq) {
        if (seq.size() != 1) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        ASN1Object[] elements = (ASN1Object[]) seq.toArray();
        if (elements[0] instanceof ASN1ObjectIdentifier) {
            credentialType = (ASN1ObjectIdentifier) elements[0];
        } else {
            throw new IllegalArgumentException("Expected ASN1ObjectIdentifier, received " + elements[0].getClass().getName());
        }
    }

    public TCGCredentialType(ASN1ObjectIdentifier credentialType) {
        this.credentialType = credentialType;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(credentialType);
        return new DERSequence(vec);
    }

    public ASN1ObjectIdentifier getCredentialType() {
        return credentialType;
    }
}
