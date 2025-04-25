package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * Class name is "PlatformProperties" as opposed to TCG given name of "Properties"
 * to limit potential collision issues with built-in Java class named Properties.
 * 
 * <pre>
 * Properties ::= SEQUENCE {
 *      propertyName UTF8String (SIZE (1..STRMAX)),
 *      propertyValue UTF8String (SIZE (1..STRMAX)),
 *      status [0] IMPLICIT AttributeStatus OPTIONAL }
 * </pre>
 */
public class PlatformPropertiesV2 extends ASN1Object {
	
	// minimum 2, max 3
	DERUTF8String propertyName;
	DERUTF8String propertyValue;
	AttributeStatus status = null; // optional, tagged 0
	
	public static PlatformPropertiesV2 getInstance(Object obj) {
		if (obj == null || obj instanceof PlatformPropertiesV2) {
			return (PlatformPropertiesV2) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new PlatformPropertiesV2((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private PlatformPropertiesV2(ASN1Sequence seq) {
		if ((seq.size() < 2) || (seq.size() > 3)) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		if (elements[0] instanceof DERUTF8String) {
			propertyName = (DERUTF8String) elements[0];
			if (propertyName.toString().length() > Definitions.STRMAX) {
				throw new IllegalArgumentException("Length of propertyName exceeds STRMAX");
			}
		} else {
			throw new IllegalArgumentException("Expected TCGSpecificationVersion, received " + elements[0].getClass().getName());
		}
		if (elements[1] instanceof DERUTF8String) {
			propertyValue = (DERUTF8String) elements[1];
			if (propertyValue.toString().length() > Definitions.STRMAX) {
				throw new IllegalArgumentException("Length of propertyValue exceeds STRMAX");
			}
		} else {
			throw new IllegalArgumentException("Expected DEROctetString, received " + elements[0].getClass().getName());
		}
		int pos = 2;
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 0)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
            if (elementObject instanceof AttributeStatus as) {
                status = as;
            } else {
                throw new IllegalArgumentException("Expected AttributeStatus object, but received " + elements[pos].getClass().getName());
            }
            pos++;
        }
	}

	public PlatformPropertiesV2(DERUTF8String propertyName, DERUTF8String propertyValue) {
		this(propertyName, propertyValue, null);
	}
	
	public PlatformPropertiesV2(DERUTF8String propertyName, DERUTF8String propertyValue, AttributeStatus status) {
        if (propertyName.toString().length() > Definitions.STRMAX) {
            throw new IllegalArgumentException("Length of propertyName exceeds STRMAX");
        }
        if (propertyValue.toString().length() > Definitions.STRMAX) {
            throw new IllegalArgumentException("Length of propertyValue exceeds STRMAX");
        }
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
        this.status = status;
    }

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(propertyName);
		vec.add(propertyValue);
		if (status != null) {
            vec.add(new DERTaggedObject(false, 0, status));
        }
		return new DERSequence(vec);
	}

	public DERUTF8String getPropertyName() {
		return propertyName;
	}

	public DERUTF8String getPropertyValue() {
		return propertyValue;
	}
	
	public AttributeStatus getStatus() {
	    return status;
	}
}
