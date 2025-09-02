package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
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
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class TCGSpecificationVersion extends ASN1Object {
	private static final int SEQUENCE_SIZE = 3;

	@NonNull
	private final ASN1Integer majorVersion;
	@NonNull
	private final ASN1Integer minorVersion;
	@NonNull
	private final ASN1Integer revision;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return TCGSpecificationVersion
	 */
	public static TCGSpecificationVersion getInstance(Object obj) {
		if (obj == null || obj instanceof TCGSpecificationVersion) {
			return (TCGSpecificationVersion) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return TCGSpecificationVersion
	 */
	public static final TCGSpecificationVersion fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() != TCGSpecificationVersion.SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		ASN1Object[] elements = (ASN1Object[]) seq.toArray();

		TCGSpecificationVersion.TCGSpecificationVersionBuilder builder = TCGSpecificationVersion.builder()
				.majorVersion(ASN1Integer.getInstance(elements[0]))
				.minorVersion(ASN1Integer.getInstance(elements[1]))
				.revision(ASN1Integer.getInstance(elements[2]));

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(this.majorVersion);
		vec.add(this.minorVersion);
		vec.add(this.revision);
		return new DERSequence(vec);
	}
}
