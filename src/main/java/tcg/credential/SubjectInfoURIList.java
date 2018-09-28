package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * SubjectInfoURIList ::=
 *      SEQUENCE SIZE (1..REFMAX) OF HashedSubjectInfoURI
 * </pre>
 */
public class SubjectInfoURIList extends ASN1Object {
	
	HashedSubjectInfoURI[] list = null;
	
	public static SubjectInfoURIList getInstance(Object obj) {
		if (obj == null || obj instanceof SubjectInfoURIList) {
			return (SubjectInfoURIList) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new SubjectInfoURIList((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private SubjectInfoURIList(ASN1Sequence seq) {
		if (seq.size() < 0) { // enforce refmax
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		list = new HashedSubjectInfoURI[elements.length];
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof HashedSubjectInfoURI) {
				list[i] = (HashedSubjectInfoURI) elements[i];
			} else {
				throw new IllegalArgumentException("Expected HashedSubjectInfoURI, received " + elements[i].getClass().getName());
			}
		}
	}

	public SubjectInfoURIList(HashedSubjectInfoURI[] manifests) {
		// enforce refmax
		this.list = manifests;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		if (list != null) {
			for(int i = 0; i < list.length; i++) {
				vec.add(list[i]);
			}
		}
		return new DERSequence(vec);
	}

	public HashedSubjectInfoURI[] getManifests() {
		return list;
	}
}
