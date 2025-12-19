package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * PlatformManufacturerId ATTRIBUTE ::= {
 *      WITH SYNTAX ManufacturerId
 *      ID tcg-at-platformManufacturerId }
 * 
 * ManufacturerId ::= SEQUENCE {
 *      manufacturerIdentifier PrivateEnterpriseNumber }
 * 
 * enterprise OBJECT IDENTIFIER :: = {iso(1) identified-organization(3) dod(6) internet(1) private(4) enterprise(1)}
 * 
 * PrivateEnterpriseNumber OBJECT IDENTIFIER :: = { enterprise private-enterprise-number }
 * </pre>
 */
public class ManufacturerId extends ASN1Object {
	
	ASN1ObjectIdentifier manufacturerIdentifier;

	public ManufacturerId(ASN1ObjectIdentifier manufacturerIdentifier) {
		this.manufacturerIdentifier = manufacturerIdentifier;
	}
	
	public static ManufacturerId getInstance(Object obj) {
		if (obj == null || obj instanceof ManufacturerId) {
			return (ManufacturerId) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return new ManufacturerId(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private ManufacturerId(ASN1Sequence seq) {
		if (seq.size() != 1) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		if (elements[0] instanceof ASN1ObjectIdentifier oid) {
			manufacturerIdentifier = oid;
		} else {
			throw new IllegalArgumentException("Expected ASN1ObjectIdentifier, received " + elements[0].getClass().getName());
		}
	}

	@Override
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(manufacturerIdentifier);
		return new DERSequence(vec);
	}

	public ASN1ObjectIdentifier getManufacturerIdentifier() {
		return manufacturerIdentifier;
	}
}
