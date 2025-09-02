package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import json.ASN1BooleanDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1IA5String;
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
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class FIPSLevel extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 2;
	private static final int MAX_SEQUENCE_SIZE = 3;

	@JsonDeserialize(as = DERIA5String.class)
	@NonNull
	private final ASN1IA5String version;
	@NonNull
	private final SecurityLevel level;
	@Builder.Default
	@JsonDeserialize(using = ASN1BooleanDeserializer.class)
	@NonNull
	private final ASN1Boolean plus = ASN1Boolean.FALSE; // default false

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return FIPSLevel
	 */
	public static FIPSLevel getInstance(Object obj) {
		if (obj == null || obj instanceof FIPSLevel) {
			return (FIPSLevel) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return FIPSLevel.fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return FIPSLevel
	 */
	public static final FIPSLevel fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < FIPSLevel.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		FIPSLevel.FIPSLevelBuilder builder = FIPSLevel.builder()
				.version(DERIA5String.getInstance(untaggedElements.get(0)))
				.level(SecurityLevel.getInstance(untaggedElements.get(1)));

		builder.plus(ASN1Utils.safeGetDefaultElementFromSequence(seq, 3, ASN1Boolean.FALSE, ASN1Boolean::getInstance));

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(version);
		vec.add(level);
		if (plus.isTrue()) {
			vec.add(plus);
		}
		return new DERSequence(vec);
	}
}
