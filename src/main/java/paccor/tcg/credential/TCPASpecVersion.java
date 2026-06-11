package paccor.tcg.credential;

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
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>{@code
 * -- tcpa tpm specification attribute (deprecated)
 *
 * tCPASpecVersion ATTRIBUTE ::= {
 *      WITH SYNTAX TCPASpecVersion
 *      ID tcg-tcpaSpecVersion }
 *
 * TCPASpecVersion ::= SEQUENCE {
 *      major INTEGER,
 *      minor INTEGER }
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
public class TCPASpecVersion extends ASN1Object {
	private static final int SEQUENCE_SIZE = 2;

	@NonNull
	@NotNull
	private final ASN1Integer major;
	@NonNull
	@NotNull
	private final ASN1Integer minor;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return TCPASpecVersion
	 */
	public static final TCPASpecVersion getInstance(Object obj) {
		if (obj == null || obj instanceof TCPASpecVersion) {
			return (TCPASpecVersion) obj;
		}
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return fromASN1Sequence(ASN1Utils.getSequence(obj));
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

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		TCPASpecVersion.TCPASpecVersionBuilder builder = TCPASpecVersion.builder()
				.major(ASN1Integer.getInstance(untaggedElements.get(0)))
				.minor(ASN1Integer.getInstance(untaggedElements.get(1)));

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
