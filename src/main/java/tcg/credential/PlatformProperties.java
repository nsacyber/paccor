package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * Class name is "PlatformProperties" as opposed to TCG given name of "Properties"
 * to limit potential collision issues with built-in Java class named Properties.
 * 
 * <pre>
 * Properties ::= SEQUENCE {
 *      propertyName UTF8String (SIZE (1..STRMAX)),
 *      propertyValue UTF8String (SIZE (1..STRMAX)) }
 * </pre>
 */
public class PlatformProperties extends ASN1Object {
	
	// minimum 2, max, 2
	DERUTF8String propertyName;
	DERUTF8String propertyValue;
	
	public static PlatformProperties getInstance(Object obj) {
		if (obj == null || obj instanceof PlatformProperties) {
			return (PlatformProperties) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new PlatformProperties((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private PlatformProperties(ASN1Sequence seq) {
		if (seq.size() != 2) {
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
	}

	public PlatformProperties(DERUTF8String propertyName, DERUTF8String propertyValue) {
		if (propertyName.toString().length() > Definitions.STRMAX) {
			throw new IllegalArgumentException("Length of propertyName exceeds STRMAX");
		}
		if (propertyValue.toString().length() > Definitions.STRMAX) {
			throw new IllegalArgumentException("Length of propertyValue exceeds STRMAX");
		}
		this.propertyName = propertyName;
		this.propertyValue = propertyValue;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(propertyName);
		vec.add(propertyValue);
		return new DERSequence(vec);
	}

	public DERUTF8String getPropertyName() {
		return propertyName;
	}

	public DERUTF8String getPropertyValue() {
		return propertyValue;
	}
}
