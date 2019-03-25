package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
 * ComponentAddress ::= SEQUENCE {
 *      addressType AddressType,
 *      addressValue UTF8String (SIZE (1..STRMAX)) }
 * 
 * AddressType ::= OBJECT IDENTIFIER (tcg-address-ethernetmac | tcg-address-wlanmac | tcg-addressbluetoothmac)
 * </pre>
 */
public class ComponentAddress extends ASN1Object {
	
	//minimum 2, max 2
	ASN1ObjectIdentifier addressType;
	DERUTF8String addressValue;
	
	public static ComponentAddress getInstance(Object obj) {
		if (obj == null || obj instanceof ComponentAddress) {
			return (ComponentAddress) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new ComponentAddress((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private ComponentAddress(ASN1Sequence seq) {
		if (seq.size() != 2) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		if (elements[0] instanceof ASN1ObjectIdentifier) {
			addressType = (ASN1ObjectIdentifier) elements[0];
		} else {
			throw new IllegalArgumentException("Expected ASN1ObjectIdentifier, received " + elements[0].getClass().getName());
		}
		if (elements[1] instanceof DERUTF8String) {
			addressValue = (DERUTF8String) elements[1];
		} else {
			throw new IllegalArgumentException("Expected DERUTF8String, received " + elements[1].getClass().getName());
		}
	}

	public ComponentAddress(ASN1ObjectIdentifier addressType, DERUTF8String addressValue) {
		this.addressType = addressType;
		this.addressValue = addressValue;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(addressType);
		vec.add(addressValue);
		return new DERSequence(vec);
	}

	public ASN1ObjectIdentifier getAddressType() {
		return addressType;
	}

	public DERUTF8String getAddressValue() {
		return addressValue;
	}
}
