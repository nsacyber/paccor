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
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class TCPASpecVersion extends ASN1Object {
	private static final int SEQUENCE_SIZE = 2;

	@NonNull
	private final ASN1Integer major;
	@NonNull
	private final ASN1Integer minor;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return TCPASpecVersion
	 */
	public static TCPASpecVersion getInstance(Object obj) {
		if (obj == null || obj instanceof TCPASpecVersion) {
			return (TCPASpecVersion) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return TCPASpecVersion.fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return TCPASpecVersion
	 */
	public static final TCPASpecVersion fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() != TCPASpecVersion.SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		ASN1Object[] elements = (ASN1Object[]) seq.toArray();

		TCPASpecVersion.TCPASpecVersionBuilder builder = TCPASpecVersion.builder()
				.major(ASN1Integer.getInstance(elements[0]))
				.minor(ASN1Integer.getInstance(elements[1]));

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(major);
		vec.add(minor);
		return new DERSequence(vec);
	}
}
