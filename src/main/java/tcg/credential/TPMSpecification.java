package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
 * tPMSpecification ATTRIBUTE ::= {
 *      WITH SYNTAX TPMSpecification
 *      ID tcg-at-tpmSpecification }
 * 
 * TPMSpecification ::= SEQUENCE {
 *      family UTF8String (SIZE (1..STRMAX)),
 *      level INTEGER,
 *      revision INTEGER }
 * </pre>
 */
public class TPMSpecification extends ASN1Object {
	
	// minimum 3, max 3
	DERUTF8String family;
	ASN1Integer level;
	ASN1Integer revision;
	
	public static TPMSpecification getInstance(Object obj) {
		if (obj == null || obj instanceof TPMSpecification) {
			return (TPMSpecification) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new TPMSpecification((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private TPMSpecification(ASN1Sequence seq) {
		if (seq.size() != 3) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		if (elements[0] instanceof DERUTF8String) {
			family = (DERUTF8String) elements[0];
		} else {
			throw new IllegalArgumentException("Expected DERUTF8String, received " + elements[0].getClass().getName());
		}
		if (elements[1] instanceof ASN1Integer) {
			level = (ASN1Integer) elements[1];
		} else {
			throw new IllegalArgumentException("Expected ASN1Integer, received " + elements[0].getClass().getName());
		}
		if (elements[2] instanceof ASN1Integer) {
			revision = (ASN1Integer) elements[2];
		} else {
			throw new IllegalArgumentException("Expected ASN1Integer, received " + elements[0].getClass().getName());
		}
	}

	public TPMSpecification(DERUTF8String family, ASN1Integer level, ASN1Integer revision) {
		this.family = family;
		this.level = level;
		this.revision = revision;
	}
	
	public TPMSpecification(String family, long level, long revision) {
	    this(new DERUTF8String(family), new ASN1Integer(level), new ASN1Integer(revision));
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(family);
		vec.add(level);
		vec.add(revision);
		return new DERSequence(vec);
	}

	public DERUTF8String getFamily() {
		return family;
	}

	public ASN1Integer getLevel() {
		return level;
	}

	public ASN1Integer getRevision() {
		return revision;
	}
}
