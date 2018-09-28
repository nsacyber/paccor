package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * TCGRelevantManifests::=
 *      SEQUENCE SIZE (1..REFMAX) OF HashedSubjectInfoURI
 * </pre>
 */
public class TCGRelevantManifests extends ASN1Object {
	
	HashedSubjectInfoURI[] manifests = null;
	
	public static TCGRelevantManifests getInstance(Object obj) {
		if (obj == null || obj instanceof TCGRelevantManifests) {
			return (TCGRelevantManifests) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new TCGRelevantManifests((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private TCGRelevantManifests(ASN1Sequence seq) {
		if (seq.size() < 0) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		manifests = new HashedSubjectInfoURI[elements.length];
		if (manifests.length > Definitions.REFMAX) {
			throw new IllegalArgumentException("Number of HashedSubjectInfoURI exceeds REFMAX");
		}
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof HashedSubjectInfoURI) {
				manifests[i] = (HashedSubjectInfoURI) elements[i];
			} else {
				throw new IllegalArgumentException("Expected HashedSubjectInfoURI, received " + elements[i].getClass().getName());
			}
		}
	}

	public TCGRelevantManifests(HashedSubjectInfoURI[] manifests) {
		if (manifests.length > Definitions.REFMAX) {
			throw new IllegalArgumentException("Number of HashedSubjectInfoURI exceeds REFMAX");
		}
		this.manifests = manifests;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		if (manifests != null) {
			for(int i = 0; i < manifests.length; i++) {
				vec.add(manifests[i]);
			}
		}
		return new DERSequence(vec);
	}

	public HashedSubjectInfoURI[] getManifests() {
		return manifests;
	}
}
