package tcg.credential;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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

/**
 * <pre>
 * tPMSecurityAssertions ATTRIBUTE ::= {
 *      WITH SYNTAX TPMSecurityAssertions
 *      ID tcg-at-tpmSecurityAssertions }
 *
 * TPMSecurityAssertions ::= SEQUENCE {
 *      version Version DEFAULT v1,
 *      fieldUpgradable BOOLEAN DEFAULT FALSE,
 *      ekGenerationType [0] IMPLICIT EKGenerationType OPTIONAL,
 *      ekGenerationLocation [1] IMPLICIT EKGenerationLocation OPTIONAL,
 *      ekCertificateGenerationLocation [2] IMPLICIT EKCertificateGenerationLocation OPTIONAL,
 *      ccInfo [3] IMPLICIT CommonCriteriaMeasures OPTIONAL,
 *      fipsLevel [4] IMPLICIT FIPSLevel OPTIONAL,
 *      iso9000Certified [5] IMPLICIT BOOLEAN DEFAULT FALSE,
 *      iso9000Uri IA5STRING (SIZE (1..URIMAX)) OPTIONAL }
 *
 * Version ::= INTEGER { v1(0) }
 * </pre>
 */
@AllArgsConstructor
@Jacksonized
@Builder(toBuilder = true)
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
public class TPMSecurityAssertions extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 0;
	private static final int MAX_SEQUENCE_SIZE = 9;

	@NonNull
	@Builder.Default
	private final ASN1Integer version = new ASN1Integer(0); // default 0
	@NonNull
	@Builder.Default
	private final ASN1Boolean fieldUpgradable = ASN1Boolean.FALSE; // default false
	private final EKGenerationType ekGenerationType; // optional, tagged 0
	private final EKGenerationLocation ekGenerationLocation; // optional, tagged 1
	private final EKCertificateGenerationLocation ekCertificateGenerationLocation; // optional, tagged 2
	private final CommonCriteriaMeasures ccInfo; // optional, tagged 3
	private final FIPSLevel fipsLevel; // optional, tagged 4
	@NonNull
	@Builder.Default
	private final ASN1Boolean iso9000Certified = ASN1Boolean.FALSE; // default false
	private final ASN1IA5String iso9000Uri; // optional, not tagged

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return TPMSecurityAssertions
	 */
	public static TPMSecurityAssertions getInstance(Object obj) {
		if (obj == null || obj instanceof TPMSecurityAssertions) {
			return (TPMSecurityAssertions) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return TPMSecurityAssertions.fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return TPMSecurityAssertions
	 */
	public static final TPMSecurityAssertions fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < TPMSecurityAssertions.MIN_SEQUENCE_SIZE || seq.size() > TPMSecurityAssertions.MAX_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		TPMSecurityAssertions.TPMSecurityAssertionsBuilder builder = TPMSecurityAssertions.builder();

		// version can only be in the first position
		builder.version(ASN1Utils.safeGetDefaultElementFromSequence(seq, 0, new ASN1Integer(1), ASN1Integer::getInstance));
		// fieldUpgradable could be in any position 0 through 1
		builder.fieldUpgradable(ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 0, 1, ASN1Boolean.FALSE, ASN1Boolean::getInstance));

		Optional.ofNullable(ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 0, 8, null, DERIA5String::getInstance))
				.ifPresent(builder::iso9000Uri);

		ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
			switch (key) {
				case 0 -> builder.ekGenerationType(EKGenerationType.getInstance(value));
				case 1 -> builder.ekGenerationLocation(EKGenerationLocation.getInstance(value));
				case 2 -> builder.ekCertificateGenerationLocation(EKCertificateGenerationLocation.getInstance(value));
				case 3 -> builder.ccInfo(CommonCriteriaMeasures.getInstance(value));
				case 4 -> builder.fipsLevel(FIPSLevel.getInstance(value));
				case 5 -> builder.iso9000Certified(ASN1Boolean.getInstance(value));
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
		if (version.getValue().intValue() != 0) {
			vec.add(version);
		}
		if (fieldUpgradable.isTrue()) {
			vec.add(fieldUpgradable);
		}
		if (ekGenerationType != null) {
			vec.add(new DERTaggedObject(false, 0, ekGenerationType));
		}
		if (ekGenerationLocation != null) {
			vec.add(new DERTaggedObject(false, 1, ekGenerationLocation));
		}
		if (ekCertificateGenerationLocation != null) {
			vec.add(new DERTaggedObject(false, 2, ekCertificateGenerationLocation));
		}
		if (ccInfo != null) {
			vec.add(new DERTaggedObject(false, 3, ccInfo));
		}
		if (fipsLevel != null) {
			vec.add(new DERTaggedObject(false, 4, fipsLevel));
		}
		if (iso9000Certified.isTrue()) {
			vec.add(new DERTaggedObject(false, 5, iso9000Certified));
		}
		if (iso9000Uri != null) {
			vec.add(iso9000Uri);
		}
		return new DERSequence(vec);
	}
}
