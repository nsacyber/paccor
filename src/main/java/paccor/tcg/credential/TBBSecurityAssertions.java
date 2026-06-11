package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.Optional;
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
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>{@code
 * tBBSecurityAssertions ATTRIBUTE ::= {
 *      WITH SYNTAX TBBSecurityAssertions
 *      ID tcg-at-tbbSecurityAssertions }
 *
 * TBBSecurityAssertions ::= SEQUENCE {
 *      version Version DEFAULT v1,
 *      ccInfo [0] IMPLICIT CommonCriteriaMeasures OPTIONAL,
 *      fipsLevel [1] IMPLICIT FIPSLevel OPTIONAL,
 *      rtmType [2] IMPLICIT MeasurementRootType OPTIONAL,
 *      iso9000Certified BOOLEAN DEFAULT FALSE,
 *      iso9000Uri IA5STRING (SIZE (1..URIMAX) OPTIONAL }
 *
 * Version ::= INTEGER { v1(0) }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@JsonClassDescription("Security assertions associated with the trusted building block, including CC, FIPS, RTM, and ISO 9000 statements.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class TBBSecurityAssertions extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 0;
	private static final int MAX_SEQUENCE_SIZE = 6;
	private static final int ASN1BOOLEAN_MAX_POSITION_IN_SEQUENCE = 4;
	private static final int ASN1STRING_MAX_POSITION_IN_SEQUENCE = 5;

	@Builder.Default
	@JsonPropertyDescription("Assertion version. Defaults to 1.")
	@NonNull
	private final ASN1Integer version = new ASN1Integer(1); // default = 1
	@JsonPropertyDescription("Optional Common Criteria assertions.")
	private final CommonCriteriaMeasures ccInfo; // optional, tagged 0
	@JsonPropertyDescription("Optional FIPS level statement.")
	private final FIPSLevel fipsLevel; // optional, tagged 1
	@JsonPropertyDescription("Optional measurement root type.")
	private final MeasurementRootType rtmType; // optional, tagged 2
	@Builder.Default
	@JsonProperty(defaultValue = "false")
	@JsonPropertyDescription("Whether ISO 9000 certification is asserted. Defaults to false.")
	@NonNull
	private final ASN1Boolean iso9000Certified = ASN1Boolean.FALSE; // default = false
	@JsonPropertyDescription("Optional URI providing ISO 9000 certification details.")
	private final ASN1IA5String iso9000Uri; // optional

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return {@link TBBSecurityAssertions}
	 */
	public static final TBBSecurityAssertions getInstance(Object obj) {
		if (obj == null || obj instanceof TBBSecurityAssertions) {
			return (TBBSecurityAssertions) obj;
		}
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return {@link TBBSecurityAssertions}
	 */
	public static final TBBSecurityAssertions fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < TBBSecurityAssertions.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		TBBSecurityAssertions.TBBSecurityAssertionsBuilder builder = TBBSecurityAssertions.builder();

		// version can only be in the first position
		builder.version(ASN1Utils.safeGetDefaultElementFromSequence(seq, 0, new ASN1Integer(1), ASN1Utils::getInteger));
		// iso9000Certified could be in any position 0 through 4
		builder.iso9000Certified(ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 0, 4, ASN1Boolean.FALSE, ASN1Utils::getBoolean));
		// iso9000Uri could be in any position 0 through 5, and if not present, don't give anything to the builder
		Optional.ofNullable(ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 0, 5, null, ASN1Utils::getIA5String))
				.ifPresent(builder::iso9000Uri);

		ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
			switch (key) {
				case 0 -> builder.ccInfo(CommonCriteriaMeasures.getInstance(ASN1Utils.getSequence(value)));
				case 1 -> builder.fipsLevel(FIPSLevel.getInstance(ASN1Utils.getSequence(value)));
				case 2 -> builder.rtmType(MeasurementRootType.getInstance(value));
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
		if (version.getValue().longValue() != 1) {
			vec.add(version);
		}
		if (ccInfo != null) {
			vec.add(new DERTaggedObject(false, 0, ccInfo));
		}
		if (fipsLevel != null) {
			vec.add(new DERTaggedObject(false, 1, fipsLevel));
		}
		if (rtmType != null) {
			vec.add(new DERTaggedObject(false, 2, rtmType));
		}
		if (iso9000Certified.isTrue()) {
			vec.add(iso9000Certified);
		}
		if (iso9000Uri != null) {
			vec.add(iso9000Uri);
		}
		return new DERSequence(vec);
	}
}
