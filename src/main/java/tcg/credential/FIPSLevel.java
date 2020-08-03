package tcg.credential;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * FIPSLevel ::= SEQUENCE {
 *      version IA5STRING (SIZE (1..STRMAX)), -- 140-1 or 140-2
 *      level SecurityLevel,
 *      plus BOOLEAN DEFAULT FALSE }
 * </pre>
 */
public class FIPSLevel extends ASN1Object {
	// minimum size 2, max 3
	DERIA5String version;
	SecurityLevel level;
	ASN1Boolean plus = ASN1Boolean.FALSE; // default false

	public static FIPSLevel getInstance(Object obj) {
		if (obj == null || obj instanceof FIPSLevel) {
			return (FIPSLevel) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new FIPSLevel((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private FIPSLevel(ASN1Sequence seq) {
		if (seq.size() < 2 || seq.size() > 3) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		int pos = 0;
		if (elements[pos] instanceof DERIA5String) {
			version = (DERIA5String) elements[pos];
			if (version.toString().length() > Definitions.STRMAX) {
				throw new IllegalArgumentException("Length of version exceeds STRMAX");
			}
			pos++;
		} else {
			throw new IllegalArgumentException("Expected DERIA5String, received " + elements[pos].getClass().getName());
		}
		if (elements[pos] instanceof SecurityLevel) {
			level = (SecurityLevel) elements[pos];
			pos++;
		} else {
			throw new IllegalArgumentException("Expected SecurityLevel, received " + elements[pos].getClass().getName());
		}
		if (((elements.length - pos) > 0) && elements[pos] instanceof ASN1Boolean) {
			plus = (ASN1Boolean)elements[pos];
		}
	}
	
	public FIPSLevel(DERIA5String version, SecurityLevel level, ASN1Boolean plus) {
		if (version.toString().length() > Definitions.STRMAX) {
			throw new IllegalArgumentException("Length of version exceeds STRMAX");
		}
		this.version = version;
		this.level = level;
		this.plus = plus;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(version);
		vec.add(level);
		if (plus.isTrue()) {
			vec.add(plus);
		}
		return new DERSequence(vec);
	}

	public DERIA5String getVersion() {
		return version;
	}

	public SecurityLevel getLevel() {
		return level;
	}

	public ASN1Boolean getPlus() {
		return plus;
	}
	

}
