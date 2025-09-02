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
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class TPMSpecification extends ASN1Object {
	private static final int SEQUENCE_SIZE = 3;

	@NonNull
	private final ASN1UTF8String family;
	@NonNull
	private final ASN1Integer level;
	@NonNull
	private final ASN1Integer revision;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return TPMSpecification
	 */
	public static TPMSpecification getInstance(Object obj) {
		if (obj == null || obj instanceof TPMSpecification) {
			return (TPMSpecification) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return TPMSpecification.fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return TPMSpecification
	 */
	public static final TPMSpecification fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() != TPMSpecification.SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		ASN1Object[] elements = (ASN1Object[]) seq.toArray();

		TPMSpecification.TPMSpecificationBuilder builder = TPMSpecification.builder()
				.family(DERUTF8String.getInstance(elements[0]))
				.level(ASN1Integer.getInstance(elements[1]))
				.revision(ASN1Integer.getInstance(elements[2]));

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(family);
		vec.add(level);
		vec.add(revision);
		return new DERSequence(vec);
	}
}
