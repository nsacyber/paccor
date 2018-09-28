package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * TCGRelevantCredentials::=
 *      SEQUENCE SIZE (1..REFMAX) OF HashedSubjectInfoURI
 * </pre>
 */
public class TCGRelevantCredentials extends ASN1Object {
	
	HashedSubjectInfoURI[] credentials = null;
	
	public static TCGRelevantCredentials getInstance(Object obj) {
		if (obj == null || obj instanceof TCGRelevantCredentials) {
			return (TCGRelevantCredentials) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new TCGRelevantCredentials((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private TCGRelevantCredentials(ASN1Sequence seq) {
		if (seq.size() < 0) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		credentials = new HashedSubjectInfoURI[elements.length];
		if (credentials.length > Definitions.REFMAX) {
			throw new IllegalArgumentException("Number of HashedSubjectInfoURI exceeds REFMAX");
		}
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof HashedSubjectInfoURI) {
				credentials[i] = (HashedSubjectInfoURI) elements[i];
			} else {
				throw new IllegalArgumentException("Expected HashedSubjectInfoURI, received " + elements[i].getClass().getName());
			}
		}
	}

	public TCGRelevantCredentials(HashedSubjectInfoURI[] manifests) {
		if (credentials.length > Definitions.REFMAX) {
			throw new IllegalArgumentException("Number of HashedSubjectInfoURI exceeds REFMAX");
		}
		this.credentials = manifests;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		if (credentials != null) {
			for(int i = 0; i < credentials.length; i++) {
				vec.add(credentials[i]);
			}
		}
		return new DERSequence(vec);
	}

	public HashedSubjectInfoURI[] getManifests() {
		return credentials;
	}
}
