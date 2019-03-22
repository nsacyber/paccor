package tcg.credential;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
 * ComponentIdentifier ::= SEQUENCE {
 *      componentClass ComponentClass,
 *      componentManufacturer UTF8String (SIZE (1..STRMAX)),
 *      componentModel UTF8String (SIZE (1..STRMAX)),
 *      componentSerial [0] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      componentRevision [1] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      componentManufacturerId [2] IMPLICIT PrivateEnterpriseNumber OPTIONAL,
 *      fieldReplaceable [3] IMPLICIT BOOLEAN OPTIONAL,
 *      componentAddresses [4] IMPLICIT SEQUENCE(SIZE(1..MAX)) OF ComponentAddress OPTIONAL,
 *      componentPlatformCert [5] IMPLICIT CertificateIdentifier OPTIONAL,
 *      componentPlatformCertUri [6] IMPLICIT URIReference OPTIONAL,
 *      status [7] IMPLICIT AttributeStatus OPTIONAL }
 * </pre>
 */
public class ComponentIdentifierV2 extends ASN1Object {

    //minimum 3, max 11
    ComponentClass componentClass;
    DERUTF8String componentManufacturer;
    DERUTF8String componentModel;
    DERUTF8String componentSerial = null; // optional, tagged 0
    DERUTF8String componentRevision = null; // optional, tagged 1
    ASN1ObjectIdentifier componentManufacturerId = null; // optional, tagged 2
    ASN1Boolean fieldReplaceable = null; // optional, tagged 3
    ComponentAddress[] componentAddress = null; // optional, tagged 4, sequence of 1 to max length
    CertificateIdentifier componentPlatformCert = null; // optional, tagged 5
    URIReference componentPlatformCertUri = null; // optional, tagged 6
    AttributeStatus status = null; //optional, tagged 7
    
    public static ComponentIdentifierV2 getInstance(Object obj) {
        if (obj == null || obj instanceof ComponentIdentifierV2) {
            return (ComponentIdentifierV2) obj;
        }
        if (obj instanceof ASN1Sequence) {
            return new ComponentIdentifierV2((ASN1Sequence)obj);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }
    
    private ComponentIdentifierV2 (ASN1Sequence seq) {
        if (seq.size() < 3 || seq.size() > 11) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }
        ASN1Object[] elements = (ASN1Object[]) seq.toArray();
        int pos = 0;
        if (elements[pos] instanceof ComponentClass) {
            componentClass = (ComponentClass) elements[pos];
            pos++;
        } else {
            throw new IllegalArgumentException("Expected ComponentClass, but received " + elements[pos].getClass().getName());
        }
        if (elements[pos] instanceof DERUTF8String) {
            componentManufacturer = (DERUTF8String) elements[pos];
            if (componentManufacturer.toString().length() > Definitions.STRMAX) {
                throw new IllegalArgumentException("Length of componentManufacturer exceeds STRMAX");
            }
            pos++;
        } else {
            throw new IllegalArgumentException("Expected DERUTF8String, but received " + elements[pos].getClass().getName());
        }
        if (elements[pos] instanceof DERUTF8String) {
            componentModel = (DERUTF8String) elements[pos];
            if (componentModel.toString().length() > Definitions.STRMAX) {
                throw new IllegalArgumentException("Length of componentModel exceeds STRMAX");
            }
            pos++;
        } else {
            throw new IllegalArgumentException("Expected DERUTF8String, but received " + elements[pos].getClass().getName());
        }
        if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject ) && (((ASN1TaggedObject)elements[pos]).getTagNo() == 0)) {
            if ((ASN1Object)((ASN1TaggedObject)elements[pos]).getObject() instanceof DERUTF8String) {
                componentSerial = (DERUTF8String)(ASN1Object)((ASN1TaggedObject)elements[pos]).getObject();
                if (componentSerial.toString().length() > Definitions.STRMAX) {
                    throw new IllegalArgumentException("Length of componentSerial exceeds STRMAX");
                }
            } else {
                throw new IllegalArgumentException("Expected DERUTF8String object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
        if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject ) && (((ASN1TaggedObject)elements[pos]).getTagNo() == 1)) {
            if ((ASN1Object)((ASN1TaggedObject)elements[pos]).getObject() instanceof DERUTF8String) {
                componentRevision = (DERUTF8String)(ASN1Object)((ASN1TaggedObject)elements[pos]).getObject();
                if (componentRevision.toString().length() > Definitions.STRMAX) {
                    throw new IllegalArgumentException("Length of componentRevision exceeds STRMAX");
                }
            } else {
                throw new IllegalArgumentException("Expected DERUTF8String object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
        if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject ) && (((ASN1TaggedObject)elements[pos]).getTagNo() == 2)) {
            if ((ASN1Object)((ASN1TaggedObject)elements[pos]).getObject() instanceof ASN1ObjectIdentifier) {
                componentManufacturerId = (ASN1ObjectIdentifier)(ASN1Object)((ASN1TaggedObject)elements[pos]).getObject();
            } else {
                throw new IllegalArgumentException("Expected ASN1ObjectIdentifier object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
        if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject ) && (((ASN1TaggedObject)elements[pos]).getTagNo() == 3)) {
            if ((ASN1Object)((ASN1TaggedObject)elements[pos]).getObject() instanceof ASN1Boolean) {
                fieldReplaceable = (ASN1Boolean)(ASN1Object)((ASN1TaggedObject)elements[pos]).getObject();
            } else {
                throw new IllegalArgumentException("Expected ASN1Boolean object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
        if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject ) && (((ASN1TaggedObject)elements[pos]).getTagNo() == 4)) {
            if ((ASN1Object)((ASN1TaggedObject)elements[pos]).getObject() instanceof ASN1Sequence) {
                ASN1Sequence tempSeq = (ASN1Sequence)(ASN1Object)((ASN1TaggedObject)elements[pos]).getObject();
                // check for configmax size
                ASN1Object[] tempElements = (ASN1Object[]) tempSeq.toArray();
                componentAddress = new ComponentAddress[tempElements.length];
                for(int i = 0; i < tempElements.length; i++) {
                    if (tempElements[i] instanceof ComponentAddress) {
                        componentAddress[i] = (ComponentAddress) tempElements[i];
                    } else {
                        throw new IllegalArgumentException("Expected ComponentAddress, received " + tempElements[i].getClass().getName());
                    }
                }
            } else {
                throw new IllegalArgumentException("Expected ASN1Sequence object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
        if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject ) && (((ASN1TaggedObject)elements[pos]).getTagNo() == 5)) {
            if ((ASN1Object)((ASN1TaggedObject)elements[pos]).getObject() instanceof CertificateIdentifier) {
                componentPlatformCert = (CertificateIdentifier)(ASN1Object)((ASN1TaggedObject)elements[pos]).getObject();
            } else {
                throw new IllegalArgumentException("Expected CertificateIdentifier object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
        if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject ) && (((ASN1TaggedObject)elements[pos]).getTagNo() == 6)) {
            if ((ASN1Object)((ASN1TaggedObject)elements[pos]).getObject() instanceof URIReference) {
                componentPlatformCertUri = (URIReference)(ASN1Object)((ASN1TaggedObject)elements[pos]).getObject();
            } else {
                throw new IllegalArgumentException("Expected URIReference object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
        if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject ) && (((ASN1TaggedObject)elements[pos]).getTagNo() == 7)) {
            if ((ASN1Object)((ASN1TaggedObject)elements[pos]).getObject() instanceof AttributeStatus) {
                status = (AttributeStatus)(ASN1Object)((ASN1TaggedObject)elements[pos]).getObject();
            } else {
                throw new IllegalArgumentException("Expected AttributeStatus object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
        if ((elements.length - pos) > 0) {
            throw new IllegalArgumentException("Too many elements in ComponentIdentifiers");
        }
    }
    
    public ComponentIdentifierV2(ComponentClass componentClass, DERUTF8String componentManufacturer, DERUTF8String componentModel,
            DERUTF8String componentSerial, DERUTF8String componentRevision,
            ASN1ObjectIdentifier componentManufacturerId, ASN1Boolean fieldReplaceable,
            ComponentAddress[] componentAddress, CertificateIdentifier componentPlatformCert,
            URIReference componentPlatformCertUri, AttributeStatus status) {
        if (componentClass == null) {
            throw new IllegalArgumentException("A ComponentIdentifier did not specify a component class.");
        }
        if (componentManufacturer == null) {
            throw new IllegalArgumentException("A ComponentIdentifier did not specify a manufacturer name.");
        }
        if (componentModel == null) {
            throw new IllegalArgumentException("A ComponentIdentifier did not specify a model name.");
        }
        if (componentManufacturer != null && componentManufacturer.toString().length() > Definitions.STRMAX) {
            throw new IllegalArgumentException("Length of componentManufacturer exceeds STRMAX");
        }
        if (componentModel != null && componentModel.toString().length() > Definitions.STRMAX) {
            throw new IllegalArgumentException("Length of componentModel exceeds STRMAX");
        }
        if (componentSerial != null && componentSerial.toString().length() > Definitions.STRMAX) {
            throw new IllegalArgumentException("Length of componentSerial exceeds STRMAX");
        }
        if (componentRevision != null && componentRevision.toString().length() > Definitions.STRMAX) {
            throw new IllegalArgumentException("Length of componentRevision exceeds STRMAX");
        }
        this.componentClass = componentClass;
        this.componentManufacturer = componentManufacturer;
        this.componentModel = componentModel;
        this.componentSerial = componentSerial;
        this.componentRevision = componentRevision;
        this.componentManufacturerId = componentManufacturerId;
        this.fieldReplaceable = fieldReplaceable;
        this.componentAddress = componentAddress;
        this.componentPlatformCert = componentPlatformCert;
        this.componentPlatformCertUri = componentPlatformCertUri;
        this.status = status;
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(componentClass);
        vec.add(componentManufacturer);
        vec.add(componentModel);
        if (componentSerial != null) {
            vec.add(new DERTaggedObject(false, 0, componentSerial));
        }
        if (componentRevision != null) {
            vec.add(new DERTaggedObject(false, 1, componentRevision));
        }
        if (componentManufacturerId != null) {
            vec.add(new DERTaggedObject(false, 2, componentManufacturerId));
        }
        if (fieldReplaceable != null) {
            vec.add(new DERTaggedObject(false, 3, fieldReplaceable));
        }
        if (componentAddress != null && componentAddress.length > 0) {
            ASN1EncodableVector vec2 = new ASN1EncodableVector();
            for (int i = 0; i < componentAddress.length; i++) {
                vec2.add(componentAddress[i]);
            }
            vec.add(new DERTaggedObject(false, 4, new DERSequence(vec2)));
        }
        if (componentPlatformCert != null) {
            vec.add(new DERTaggedObject(false, 5, componentPlatformCert));
        }
        if (componentPlatformCertUri != null) {
            vec.add(new DERTaggedObject(false, 6, componentPlatformCertUri));
        }
        if (status != null) {
            vec.add(new DERTaggedObject(false, 7, status));
        }
        return new DERSequence(vec);
    }
    
    public ComponentClass getComponentClass() {
        return componentClass;
    }

    public DERUTF8String getComponentManufacturer() {
        return componentManufacturer;
    }


    public DERUTF8String getComponentModel() {
        return componentModel;
    }


    public DERUTF8String getComponentSerial() {
        return componentSerial;
    }

    public DERUTF8String getComponentRevision() {
        return componentRevision;
    }


    public ASN1ObjectIdentifier getComponentManufacturerId() {
        return componentManufacturerId;
    }
    
    public ASN1Boolean getFieldReplaceable() {
        return fieldReplaceable;
    }

    public ComponentAddress[] getComponentAddress() {
        return componentAddress;
    }
    
    public CertificateIdentifier getComponentPlatformCert() {
        return componentPlatformCert;
    }
    
    public URIReference getComponentPlatformCertUri() {
        return componentPlatformCertUri;
    }
    
    public AttributeStatus getStatus() {
        return status;
    }
}
