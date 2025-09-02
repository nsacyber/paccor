package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>{@code
 * tPMSpecification ATTRIBUTE ::= {
 *      WITH SYNTAX TPMSpecification
 *      ID tcg-at-tpmSpecification }
 *
 * TPMSpecification ::= SEQUENCE {
 *      family UTF8String (SIZE (1..STRMAX)),
 *      level INTEGER,
 *      revision INTEGER }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
public class TPMSpecification extends ASN1Object {
	private static final int SEQUENCE_SIZE = 3;

	@NonNull
	@NotNull
	private final ASN1UTF8String family;
	@NonNull
	@NotNull
	private final ASN1Integer level;
	@NonNull
	@NotNull
	private final ASN1Integer revision;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return {@link TPMSpecification}
	 */
	public static final TPMSpecification getInstance(Object obj) {
		if (obj == null || obj instanceof TPMSpecification) {
			return (TPMSpecification) obj;
		}
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return {@link TPMSpecification}
	 */
	public static final TPMSpecification fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() != TPMSpecification.SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		TPMSpecification.TPMSpecificationBuilder builder = TPMSpecification.builder()
				.family(ASN1UTF8String.getInstance(untaggedElements.get(0)))
				.level(ASN1Integer.getInstance(untaggedElements.get(1)))
				.revision(ASN1Integer.getInstance(untaggedElements.get(2)));

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
