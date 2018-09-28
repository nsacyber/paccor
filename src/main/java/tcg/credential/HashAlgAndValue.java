package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * HashAlgAndValue ::= SEQUENCE {
 *      hashAlg AlgorithmIdentifier,
 *      hashValue OCTET STRING }
 * </pre>
 */
public class HashAlgAndValue extends ASN1Object {
	
	ASN1ObjectIdentifier hashAlg;
	DEROctetString hashValue;
	
	public static HashAlgAndValue getInstance(Object obj) {
		if (obj == null || obj instanceof HashAlgAndValue) {
			return (HashAlgAndValue) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new HashAlgAndValue((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private HashAlgAndValue(ASN1Sequence seq) {
		if (seq.size() != 2) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		if (elements[0] instanceof ASN1ObjectIdentifier) {
			hashAlg = (ASN1ObjectIdentifier) elements[0];
		} else {
			throw new IllegalArgumentException("Expected ASN1ObjectIdentifier, received " + elements[0].getClass().getName());
		}
		if (elements[1] instanceof DEROctetString) {
			hashValue = (DEROctetString) elements[1];
		} else {
			throw new IllegalArgumentException("Expected DEROctetString, received " + elements[0].getClass().getName());
		}
	}

	public HashAlgAndValue(ASN1ObjectIdentifier hashAlg, DEROctetString hashValue) {
		this.hashAlg = hashAlg;
		this.hashValue = hashValue;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(hashAlg);
		vec.add(hashValue);
		return new DERSequence(vec);
	}

	public ASN1ObjectIdentifier getHashAlg() {
		return hashAlg;
	}

	public DEROctetString getHashValue() {
		return hashValue;
	}
}
