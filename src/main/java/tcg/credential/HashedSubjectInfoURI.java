package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * HashedSubjectInfoURI ::= SEQUENCE {
 *      documentURI IA5String (SIZE (1..URIMAX)),
 *      documentAccessInfo OBJECT IDENTIFIER OPTIONAL,
 *      documentHashInfo HashAlgAndValue OPTIONAL }
 * </pre>
 */
public class HashedSubjectInfoURI extends ASN1Object {
	
	// minimum 1, maximum 3
	DERIA5String documentURI;
	ASN1ObjectIdentifier documentAccessInfo = null; // optional
	HashAlgAndValue documentHashInfo = null; // optional
	
	public static HashedSubjectInfoURI getInstance(Object obj) {
		if (obj == null || obj instanceof HashedSubjectInfoURI) {
			return (HashedSubjectInfoURI) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new HashedSubjectInfoURI((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private HashedSubjectInfoURI(ASN1Sequence seq) {
		if (seq.size() < 1 || seq.size() > 3) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		int pos = 0;
		if (elements[pos] instanceof DERIA5String) {
			documentURI = (DERIA5String) elements[pos];
			if (documentURI.toString().length() > Definitions.URIMAX) {
				throw new IllegalArgumentException("Length of documentURI exceeds URIMAX");
			}
			pos++;
		} else {
			throw new IllegalArgumentException("Expected DERIA5String, received " + elements[pos].getClass().getName());
		}
		if (((elements.length - pos) > 0) && elements[pos] instanceof ASN1ObjectIdentifier) {
			documentAccessInfo = (ASN1ObjectIdentifier) elements[pos];
			pos++;
		}
		if (((elements.length - pos) > 0) && elements[pos] instanceof HashAlgAndValue) {
			documentHashInfo = (HashAlgAndValue) elements[pos];
			pos++;
		}
		if ((elements.length - pos) > 0) {
			throw new IllegalArgumentException("Too many elements in HashedSubjectInfoURI");
		}
	}

	public HashedSubjectInfoURI(DERIA5String documentURI, ASN1ObjectIdentifier documentAccessInfo,
			HashAlgAndValue documentHashInfo) {
		if (documentURI.toString().length() > Definitions.URIMAX) {
			throw new IllegalArgumentException("Length of documentURI exceeds URIMAX");
		}
		this.documentURI = documentURI;
		this.documentAccessInfo = documentAccessInfo;
		this.documentHashInfo = documentHashInfo;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(documentURI);
		if (documentAccessInfo != null) {
			vec.add(documentAccessInfo);
		}
		if (documentHashInfo != null) {
			vec.add(documentHashInfo);
		}
		return new DERSequence(vec);
	}

	public DERIA5String getDocumentURI() {
		return documentURI;
	}

	public ASN1ObjectIdentifier getDocumentAccessInfo() {
		return documentAccessInfo;
	}

	public HashAlgAndValue getDocumentHashInfo() {
		return documentHashInfo;
	}
}
