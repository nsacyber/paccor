package tcg.credential;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

/**
 *<pre>
 * securityQualities ATTRIBUTE ::= {
 *    WITH SYNTAX SecurityQualities
 *    ID tcg-at-tpmSecurityQualities }
 *
 * SecurityQualities ::= SEQUENCE {
 *    version INTEGER,
 *    -- version 0 defined by TCPA 1.1b
 *    statement UTF8String }
 *</pre>
 */
public class SecurityQualities extends ASN1Object {
	
	ASN1Integer version;
	DERUTF8String statement;
	
	public static SecurityQualities getInstance(Object obj) {
		if (obj == null || obj instanceof SecurityQualities) {
			return (SecurityQualities) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new SecurityQualities((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private SecurityQualities(ASN1Sequence seq) {
		if (seq.size() != 2) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		if (elements[0] instanceof ASN1Integer) {
			version = (ASN1Integer) elements[0];
		} else {
			throw new IllegalArgumentException("Array object not instance of ASN1Integer: " + elements[0].getClass().getName());
		}
		if (elements[1] instanceof DERUTF8String) {
			statement = (DERUTF8String) elements[1];
		} else {
			throw new IllegalArgumentException("Array object not instance of DERUTF8String: " + elements[1].getClass().getName());
		}
	}
	
	public SecurityQualities(ASN1Integer version, DERUTF8String statement) {
		this.version = version;
		this.statement = statement;
	}
		

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(version);
		vec.add(statement);
		return new DERSequence(vec);
	}
	
	public ASN1Integer getVersion() {
		return version;
	}
	
	public DERUTF8String getStatement() {
		return statement;
	}
}
