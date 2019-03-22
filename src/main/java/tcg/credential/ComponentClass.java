package tcg.credential;

import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
 * ComponentClass ::= SEQUENCE {
 *      componentClassRegistry ComponentClassRegistry,
 *      componentClassValue OCTET STRING SIZE(4) }
 * 
 * ComponentClassRegistry ::= OBJECT IDENTIFIER ( tcg-registry-componentClass-tcg | tcg-registry-componentClass-ietf | tcg-registry-componentClass-dmtf )
 * </pre>
 */
public class ComponentClass extends ASN1Object {

    // minimum 2, maximum 2
    ASN1ObjectIdentifier componentClassRegistry;
    ASN1OctetString componentClassValue;
    
    public static ComponentClass getInstance(Object obj) {
        if (obj == null || obj instanceof ComponentClass) {
            return (ComponentClass) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new ComponentClass((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }
    
    private ComponentClass(ASN1Sequence seq) {
        if (seq.size() != 2) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        ASN1Object[] elements = (ASN1Object[]) seq.toArray();
        if (elements[0] instanceof ASN1ObjectIdentifier) {
            componentClassRegistry = (ASN1ObjectIdentifier) elements[0];
        } else {
            throw new IllegalArgumentException("Expected ASN1ObjectIdentifier, received " + elements[0].getClass().getName());
        }
        if (elements[1] instanceof ASN1OctetString) {
            componentClassValue = (ASN1OctetString) elements[1];
        } else {
            throw new IllegalArgumentException("Expected ASN1OctetString, received " + elements[1].getClass().getName());
        }
    }
    
    public ComponentClass(ASN1ObjectIdentifier componentClassRegistry, String componentClassValueHex) {
        if (componentClassRegistry == null || componentClassValueHex == null) {
            throw  new IllegalArgumentException("One of the parameters was null.");
        }
        
        BigInteger value = new BigInteger(componentClassValueHex, 16);
        if (value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new IllegalArgumentException("Component class value too large.  Given registry: " + componentClassRegistry.getId() + ", value: " + componentClassValueHex);
        }
        
        this.componentClassRegistry = componentClassRegistry;
        this.componentClassValue = new DEROctetString(value.toByteArray());
    }

    public ComponentClass(ASN1ObjectIdentifier componentClassRegistry, ASN1OctetString componentClassValue) {
        this.componentClassRegistry = componentClassRegistry;
        this.componentClassValue = componentClassValue;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(componentClassRegistry);
        vec.add(componentClassValue);
        return new DERSequence(vec);
    }

    public ASN1ObjectIdentifier getComponentClassRegistry() {
        return componentClassRegistry;
    }

    public ASN1OctetString getComponentClassValue() {
        return componentClassValue;
    }
}
