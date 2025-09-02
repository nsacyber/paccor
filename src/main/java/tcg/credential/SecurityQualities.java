package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTF8String;
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
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class SecurityQualities extends ASN1Object {
	private static final int SEQUENCE_SIZE = 2;

	@NonNull
	private final ASN1Integer version;
	@NonNull
	private final ASN1UTF8String statement;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return SecurityQualities
	 */
	public static SecurityQualities getInstance(Object obj) {
		if (obj == null || obj instanceof SecurityQualities) {
			return (SecurityQualities) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return SecurityQualities.fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return SecurityQualities
	 */
	public static final SecurityQualities fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() != SecurityQualities.SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		ASN1Object[] elements = (ASN1Object[]) seq.toArray();

		SecurityQualities.SecurityQualitiesBuilder builder = SecurityQualities.builder()
				.version(ASN1Integer.getInstance(elements[0]))
				.statement(DERUTF8String.getInstance(elements[1]));

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(version);
		vec.add(statement);
		return new DERSequence(vec);
	}
}
