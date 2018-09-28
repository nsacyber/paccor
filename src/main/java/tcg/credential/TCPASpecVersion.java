package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * -- tcpa tpm specification attribute (deprecated)
 * 
 * tCPASpecVersion ATTRIBUTE ::= {
 *      WITH SYNTAX TCPASpecVersion
 *      ID tcg-tcpaSpecVersion }
 * 
 * TCPASpecVersion ::= SEQUENCE {
 *      major INTEGER,
 *      minor INTEGER }
 * </pre>
 */
public class TCPASpecVersion extends ASN1Object {
	
	//minimum 2, max 2
	ASN1Integer major;
	ASN1Integer minor;
	
	public static TCPASpecVersion getInstance(Object obj) {
		if (obj == null || obj instanceof TCPASpecVersion) {
			return (TCPASpecVersion) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new TCPASpecVersion((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private TCPASpecVersion(ASN1Sequence seq) {
		if (seq.size() != 3) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		if (elements[0] instanceof ASN1Integer) {
			major = (ASN1Integer) elements[0];
		} else {
			throw new IllegalArgumentException("Expected ASN1Integer, received " + elements[0].getClass().getName());
		}
		if (elements[1] instanceof ASN1Integer) {
			minor = (ASN1Integer) elements[1];
		} else {
			throw new IllegalArgumentException("Expected ASN1Integer, received " + elements[0].getClass().getName());
		}
	}

	public TCPASpecVersion(ASN1Integer major, ASN1Integer minor) {
		this.major = major;
		this.minor = minor;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(major);
		vec.add(minor);
		return new DERSequence(vec);
	}

	public ASN1Integer getMajor() {
		return major;
	}

	public ASN1Integer getMinor() {
		return minor;
	}
}
