package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>{@code
 * CommonCriteriaMeasures ::= SEQUENCE {
 *      version IA5STRING (SIZE (1..STRMAX)), -- 2.2 or 3.1; future syntax defined by CC
 *      assuranceLevel EvaluationAssuranceLevel,
 *      evaluationStatus EvaluationStatus,
 *      plus BOOLEAN DEFAULT FALSE,
 *      strengthOfFunction [0] IMPLICIT StrengthOfFunction OPTIONAL,
 *      profileOid [1] IMPLICIT OBJECT IDENTIFIER OPTIONAL,
 *      profileUri [2] IMPLICIT URIReference OPTIONAL,
 *      targetOid [3] IMPLICIT OBJECT IDENTIFIER OPTIONAL,
 *      targetUri [4] IMPLICIT URIReference OPTIONAL }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@JsonClassDescription("Common Criteria evaluation details, including assurance level, evaluation status, and optional profile or target references.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class CommonCriteriaMeasures extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 3;
	private static final int MAX_SEQUENCE_SIZE = 9;

    @JsonPropertyDescription("Common Criteria version string, for example 2.2 or 3.1.")
	@NonNull
	@NotNull
	private final ASN1IA5String version;
	@JsonPropertyDescription("Evaluation assurance level.")
	@NonNull
	@NotNull
	private final EvaluationAssuranceLevel assuranceLevel;
	@JsonPropertyDescription("Overall evaluation status.")
	@NonNull
	@NotNull
	private final EvaluationStatus evaluationStatus;
	@Builder.Default
	@JsonProperty(defaultValue = "false")
	@JsonPropertyDescription("Whether the assurance level includes a '+' augmentation.")
	@NonNull
	private final ASN1Boolean plus = ASN1Boolean.FALSE; // default false
	@JsonPropertyDescription("Optional strength of function statement.")
	private final StrengthOfFunction strengthOfFunction; // optional, tagged 0
	@JsonPropertyDescription("Optional protection profile OID.")
	private final ASN1ObjectIdentifier profileOid; // optional, tagged 1
	@JsonPropertyDescription("Optional URI for the protection profile.")
	private final URIReference profileUri; // optional, tagged 2
	@JsonPropertyDescription("Optional target OID.")
	private final ASN1ObjectIdentifier targetOid; // optional, tagged 3
	@JsonPropertyDescription("Optional URI for the evaluation target.")
	private final URIReference targetUri; // optional, tagged 4

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return CommonCriteriaMeasures
	 */
	public static CommonCriteriaMeasures getInstance(Object obj) {
		if (obj == null || obj instanceof CommonCriteriaMeasures) {
			return (CommonCriteriaMeasures) obj;
		}
		if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
			return CommonCriteriaMeasures.fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return CommonCriteriaMeasures
	 */
	public static final CommonCriteriaMeasures fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < CommonCriteriaMeasures.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		CommonCriteriaMeasures.CommonCriteriaMeasuresBuilder builder = CommonCriteriaMeasures.builder()
				.version(ASN1IA5String.getInstance(untaggedElements.get(0)))
				.assuranceLevel(EvaluationAssuranceLevel.getInstance(untaggedElements.get(1)))
				.evaluationStatus(EvaluationStatus.getInstance(untaggedElements.get(2)));

		builder.plus(ASN1Utils.safeGetDefaultElementFromSequence(seq, 3, ASN1Boolean.FALSE, ASN1Utils::getBoolean));

		ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
			switch (key) {
				case 0 -> builder.strengthOfFunction(StrengthOfFunction.getInstance(value));
				case 1 -> builder.profileOid(ASN1Utils.getOID(value));
				case 2 -> builder.profileUri(URIReference.getInstance(value));
				case 3 -> builder.targetOid(ASN1Utils.getOID(value));
				case 4 -> builder.targetUri(URIReference.getInstance(value));
				default -> {}
			}
		});

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(this.version);
		vec.add(this.assuranceLevel);
		vec.add(this.evaluationStatus);
		if (this.plus.isTrue()) {
			vec.add(this.plus);
		}
		if (this.strengthOfFunction != null) {
			vec.add(new DERTaggedObject(false, 0, this.strengthOfFunction));
		}
		if (this.profileOid != null) {
			vec.add(new DERTaggedObject(false, 1, this.profileOid));
		}
		if (this.profileUri != null) {
			vec.add(new DERTaggedObject(false, 2, this.profileUri));
		}
		if (this.targetOid != null) {
			vec.add(new DERTaggedObject(false, 3, this.targetOid));
		}
		if (this.targetUri != null) {
			vec.add(new DERTaggedObject(false, 4, this.targetUri));
		}
		return new DERSequence(vec);
	}
}
