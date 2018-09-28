package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * <pre>
 * URIReference ::= SEQUENCE {
 *      uniformResourceIdentifier IA5String (SIZE (1..URIMAX)),
 *      hashAlgorithm AlgorithmIdentifier OPTIONAL,
 *      hashValue BIT STRING OPTIONAL }
 * </pre>
 */
public class URIReference extends ASN1Object {
	
	// minimum size 1, max 3
	DERIA5String uniformResourceIdentifier;
	AlgorithmIdentifier hashAlgorithm = null; // optional
	DERBitString hashValue = null; // optional

	public URIReference(DERIA5String uniformResourceIdentifier, AlgorithmIdentifier hashAlgorithm,
			DERBitString hashValue) {
		if (uniformResourceIdentifier.toString().length() > Definitions.URIMAX) {
			throw new IllegalArgumentException("Length of uniformResourceIdentifier exceeds URIMAX");
		}
		this.uniformResourceIdentifier = uniformResourceIdentifier;
		this.hashAlgorithm = hashAlgorithm;
		this.hashValue = hashValue;
	}
	
	public static URIReference getInstance(Object obj) {
		if (obj == null || obj instanceof URIReference) {
			return (URIReference) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new URIReference((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private URIReference (ASN1Sequence seq) {
		if (seq.size() < 1 || seq.size() > 3) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		int pos = 0;
		if (elements[pos] instanceof DERIA5String) {
			uniformResourceIdentifier = (DERIA5String) elements[pos];
			if (uniformResourceIdentifier.toString().length() > Definitions.URIMAX) {
				throw new IllegalArgumentException("Length of uniformResourceIdentifier exceeds URIMAX");
			}
			pos++;
		} else {
			throw new IllegalArgumentException("Expected DERIA5String, received " + elements[pos].getClass().getName());
		}
		if (((elements.length - pos) > 0) && elements[pos] instanceof AlgorithmIdentifier) {
			hashAlgorithm = (AlgorithmIdentifier) elements[pos];
			pos++;
		}
		if (((elements.length - pos) > 0) && elements[pos] instanceof DERBitString) {
			hashValue = (DERBitString) elements[pos];
			pos++;
		}
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(uniformResourceIdentifier);
		if (hashAlgorithm != null) {
			vec.add(hashAlgorithm);
		}
		if (hashValue != null) {
			vec.add(hashValue);
		}
		return new DERSequence(vec);
	}

	public DERIA5String getUniformResourceIdentifier() {
		return uniformResourceIdentifier;
	}

	public AlgorithmIdentifier getHashAlgorithm() {
		return hashAlgorithm;
	}

	public DERBitString getHashValue() {
		return hashValue;
	}
}
