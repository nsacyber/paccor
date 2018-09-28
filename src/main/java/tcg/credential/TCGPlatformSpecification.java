package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * tCGPlatformSpecification ATTRIBUTE ::= {
 *      WITH SYNTAX TCGPlatformSpecification
 *      ID tcg-at-tcgPlatformSpecification }
 * 
 * TCGPlatformSpecification ::= SEQUENCE {
 *      Version TCGSpecificationVersion,
 *      platformClass OCTET STRING SIZE(4) }
 * </pre>
 */
public class TCGPlatformSpecification extends ASN1Object {
	
	// minimum 2, maximum 2
	TCGSpecificationVersion version;
	DEROctetString platformClass;
	
	public static TCGPlatformSpecification getInstance(Object obj) {
		if (obj == null || obj instanceof TCGPlatformSpecification) {
			return (TCGPlatformSpecification) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new TCGPlatformSpecification((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private TCGPlatformSpecification(ASN1Sequence seq) {
		if (seq.size() != 2) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		if (elements[0] instanceof TCGSpecificationVersion) {
			version = (TCGSpecificationVersion) elements[0];
		} else {
			throw new IllegalArgumentException("Expected TCGSpecificationVersion, received " + elements[0].getClass().getName());
		}
		if (elements[1] instanceof DEROctetString) {
			platformClass = (DEROctetString) elements[1];
			if (platformClass.getOctets().length != 4) {
				throw new IllegalArgumentException("platformClass must have exactly 4 bytes. Received bytes: " + platformClass.getOctets().length);
			}
		} else {
			throw new IllegalArgumentException("Expected DEROctetString, received " + elements[0].getClass().getName());
		}
	}
	
	public TCGPlatformSpecification(TCGSpecificationVersion version, DEROctetString platformClass) {
		if (platformClass.getOctets().length != 4) {
			throw new IllegalArgumentException("platformClass must be exactly 4 bytes. Bytes received: " + platformClass.getOctets().length);
		}
		this.version = version;
		this.platformClass = platformClass;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(version);
		vec.add(platformClass);
		return new DERSequence(vec);
	}

	public TCGSpecificationVersion getVersion() {
		return version;
	}

	public DEROctetString getPlatformClass() {
		return platformClass;
	}

}
