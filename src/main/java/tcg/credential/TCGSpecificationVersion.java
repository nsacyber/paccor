package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * tCGCredentialSpecification ATTRIBUTE ::= {
 *      WITH SYNTAX TCGSpecificationVersion
 *      ID tcg-at-tcgCredentialSpecification }
 * 
 * TCGSpecificationVersion ::= SEQUENCE {
 *      majorVersion INTEGER,
 *      minorVersion INTEGER,
 *      revision INTEGER }
 * </pre>
 */
public class TCGSpecificationVersion extends ASN1Object {
	
	// minimum 3, maximum 3
	ASN1Integer majorVersion;
	ASN1Integer minorVersion;
	ASN1Integer revision;
	
	public static TCGSpecificationVersion getInstance(Object obj) {
		if (obj == null || obj instanceof TCGSpecificationVersion) {
			return (TCGSpecificationVersion) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new TCGSpecificationVersion((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private TCGSpecificationVersion(ASN1Sequence seq) {
		if (seq.size() != 3) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		if (elements[0] instanceof ASN1Integer) {
			majorVersion = (ASN1Integer) elements[0];
		} else {
			throw new IllegalArgumentException("Expected ASN1Integer, received " + elements[0].getClass().getName());
		}
		if (elements[1] instanceof ASN1Integer) {
			minorVersion = (ASN1Integer) elements[1];
		} else {
			throw new IllegalArgumentException("Expected ASN1Integer, received " + elements[0].getClass().getName());
		}
		if (elements[2] instanceof ASN1Integer) {
			revision = (ASN1Integer) elements[2];
		} else {
			throw new IllegalArgumentException("Expected ASN1Integer, received " + elements[0].getClass().getName());
		}
	}
	
	public TCGSpecificationVersion(ASN1Integer majorVersion, ASN1Integer minorVersion, ASN1Integer revision) {
		super();
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
		this.revision = revision;
	}
	
	public TCGSpecificationVersion(long majorVersion, long minorVersion, long revision) {
	    super();
	    this.majorVersion = new ASN1Integer(majorVersion);
	    this.minorVersion = new ASN1Integer(minorVersion);
	    this.revision = new ASN1Integer(revision);
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(majorVersion);
		vec.add(minorVersion);
		vec.add(revision);
		return new DERSequence(vec);
	}

	public ASN1Integer getMajorVersion() {
		return majorVersion;
	}

	public ASN1Integer getMinorVersion() {
		return minorVersion;
	}

	public ASN1Integer getRevision() {
		return revision;
	}
}
