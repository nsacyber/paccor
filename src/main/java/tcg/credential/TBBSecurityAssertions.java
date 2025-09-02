package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Optional;
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
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
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
public class TBBSecurityAssertions extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 0;
	private static final int MAX_SEQUENCE_SIZE = 6;
	private static final int ASN1BOOLEAN_MAX_POSITION_IN_SEQUENCE = 4;
	private static final int ASN1STRING_MAX_POSITION_IN_SEQUENCE = 5;

	@Builder.Default
	@NonNull
	private final ASN1Integer version = new ASN1Integer(1); // default = 1
	private final CommonCriteriaMeasures ccInfo; // optional, tagged 0
	private final FIPSLevel fipsLevel; // optional, tagged 1
	private final MeasurementRootType rtmType; // optional, tagged 2
	@Builder.Default
	@JsonDeserialize(using = ASN1BooleanDeserializer.class)
	@NonNull
	private final ASN1Boolean iso9000Certified = ASN1Boolean.FALSE; // default = false
	@JsonDeserialize(as = DERIA5String.class)
	private final ASN1IA5String iso9000Uri; // optional

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return TBBSecurityAssertions
	 */
	public static TBBSecurityAssertions getInstance(Object obj) {
		if (obj == null || obj instanceof TBBSecurityAssertions) {
			return (TBBSecurityAssertions) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return TBBSecurityAssertions.fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return TBBSecurityAssertions
	 */
	public static final TBBSecurityAssertions fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < TBBSecurityAssertions.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		TBBSecurityAssertions.TBBSecurityAssertionsBuilder builder = TBBSecurityAssertions.builder();

		// version can only be in the first position
		builder.version(ASN1Utils.safeGetDefaultElementFromSequence(seq, 0, new ASN1Integer(1), ASN1Integer::getInstance));
		// iso9000Certified could be in any position 0 through 4
		builder.iso9000Certified(ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 0, 4, ASN1Boolean.FALSE, ASN1Boolean::getInstance));
		// iso9000Uri could be in any position 0 through 5, and if not present, don't give anything to the builder
		Optional.ofNullable(ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 0, 5, null, DERIA5String::getInstance))
				.ifPresent(builder::iso9000Uri);

		ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
			switch (key) {
				case 0 -> builder.ccInfo(CommonCriteriaMeasures.getInstance(value));
				case 1 -> builder.fipsLevel(FIPSLevel.getInstance(value));
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
