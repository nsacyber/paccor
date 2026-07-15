package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>{@code
 *
 * CertSpecVersion V1_0 and V1_1
 *
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
 *
 * CertSpecVersion V2_0
 *
 * tBBSecurityAssertions ATTRIBUTE ::= {
 *  WITH SYNTAX TBBSecurityAssertions-v3
 *  ID tcg-at-tbbSecurityAssertions-v3 }
 *
 * TBBSecurityAssertions-v3 ::= SEQUENCE (SIZE(1..MAX)) OF Trait
 *
 * The tBBSecurityAssertions attribute MAY include:
 * CommonCriteriaTrait, FIPSLevelTrait, ISO9000Trait,
 * PlatformFirmwareCapabilitiesTrait, PlatformFirmwareSignatureVerificationTrait,
 * PlatformFirmwareUpdateComplianceTrait, PlatformHardwareCapabilitiesTrait, RTMTrait,
 * and URITrait.
 *
 * Any other Traits SHALL NOT be included in the tBBSecurityAssertions attribute.
 *
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

	@Builder.Default
	@JsonPropertyDescription("Assertion version. Optional, omitted in V2.0.")
	private final ASN1Integer version = new ASN1Integer(0);

	@JsonPropertyDescription("Collection of traits for assertions.")
	@NonNull
	private final TraitMap traits;

	public static final Set<Class<? extends Trait<?, ?>>> ALLOWED_TRAIT_TYPES = Set.of(
			CommonCriteriaTrait.class,
			FIPSLevelTrait.class,
			ISO9000Trait.class,
			PlatformFirmwareCapabilitiesTrait.class,
			PlatformFirmwareSignatureVerificationTrait.class,
			PlatformFirmwareUpdateComplianceTrait.class,
			PlatformHardwareCapabilitiesTrait.class,
			RTMTrait.class,
			URITrait.class
	);

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

		// Heuristic to check if it's a TraitMap (V3) or legacy.
		// TraitMap is SEQUENCE OF Trait. Trait is SEQUENCE { traitId OID, ... }
		if (seq.size() > 0 && seq.getObjectAt(0) instanceof ASN1Sequence subSeq) {
			if (subSeq.size() > 0 && subSeq.getObjectAt(0) instanceof ASN1ObjectIdentifier) {
				return new TBBSecurityAssertions(null, TraitMap.fromASN1Sequence(seq));
			}
		}

		TBBSecurityAssertions.TBBSecurityAssertionsBuilder builder = TBBSecurityAssertions.builder();

		List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);
		ASN1Sequence untaggedSequence = new DERSequence(ASN1Utils.toASN1EncodableVector(untaggedElements));

		// version can only be in the first position. We don't store it but we parse it to consume it.
		builder.version(ASN1Utils.safeGetDefaultElementFromSequence(untaggedSequence, 0, new ASN1Integer(0), ASN1Utils::getInteger));

		// iso9000Certified could be in any position 0 through 4, 0 to 1 in the untaggedSequence
		builder.iso9000Certified(ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(untaggedSequence, 0, 4, ASN1Boolean.FALSE, ASN1Utils::getBoolean));
		// iso9000Uri could be in any position 0 through 5, 0 to 2 in the untaggedSequence. If not present, don't give anything to the builder
		Optional.ofNullable(ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(untaggedSequence, 0, 5, null, ASN1Utils::getIA5String))
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
	 * @return This object as an ASN1Sequence in legacy format (V1.x)
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		if (version != null && version.getValue().longValue() != 0) {
			vec.add(version);
		}

		getCcInfo().ifPresent(ccInfo -> vec.add(new DERTaggedObject(false, 0, ccInfo)));
		getFipsLevel().ifPresent(fipsLevel -> vec.add(new DERTaggedObject(false, 1, fipsLevel)));
		getRtmType().ifPresent(rtmType -> vec.add(new DERTaggedObject(false, 2, rtmType)));

		ISO9000Certification iso = traits.firstTraitValue(ISO9000Trait.class).orElse(null);
		if (iso != null) {
			if (iso.getIso9000Certified().isTrue()) {
				vec.add(iso.getIso9000Certified());
			}
			if (iso.getIso9000Uri() != null) {
				vec.add(iso.getIso9000Uri());
			}
		}
		return new DERSequence(vec);
	}

	/**
	 * Convert to a TraitMap representation, filtering for only allowed traits.
	 * @return {@link TraitMap}
	 */
	public TraitMap toTraitMap() {
		return traits.filter(ALLOWED_TRAIT_TYPES);
	}

	@JsonPropertyDescription("Optional Common Criteria assertions.")
	public Optional<CommonCriteriaMeasures> getCcInfo() {
		return traits.firstTraitValue(CommonCriteriaTrait.class)
				.map(CommonCriteriaEvaluation::getCCMeasures);
	}

	@JsonPropertyDescription("Optional FIPS level statement.")
	public Optional<FIPSLevel> getFipsLevel() {
		return traits.firstTraitValue(FIPSLevelTrait.class);
	}

	@JsonPropertyDescription("Optional measurement root type.")
	public Optional<MeasurementRootType> getRtmType() {
		return traits.firstTraitValue(RTMTrait.class)
				.map(types -> new MeasurementRootType(types.getValue()));
	}

	@JsonPropertyDescription("Whether ISO 9000 certification is asserted. Defaults to false.")
	public ASN1Boolean getIso9000Certified() {
		return traits.firstTraitValue(ISO9000Trait.class)
				.map(ISO9000Certification::getIso9000Certified)
				.orElse(ASN1Boolean.FALSE);
	}

	@JsonPropertyDescription("Optional URI providing ISO 9000 certification details.")
	public Optional<ASN1IA5String> getIso9000Uri() {
		return traits.firstTraitValue(ISO9000Trait.class)
				.map(ISO9000Certification::getIso9000Uri);
	}

	public Optional<ASN1Integer> getVersion() {
		return Optional.ofNullable(version);
	}

	public static class TBBSecurityAssertionsBuilder {
		private ASN1Integer version = new ASN1Integer(0);
		private final TraitMap traits = new TraitMap(new LinkedHashMap<>());

		public TBBSecurityAssertionsBuilder version(ASN1Integer version) {
			if (version != null) {
				this.version = version;
			}
			return this;
		}

		public TBBSecurityAssertionsBuilder traits(TraitMap traits) {
			if (traits != null) {
				this.traits.putAll(traits);
			}
			return this;
		}

		public TBBSecurityAssertionsBuilder ccInfo(CommonCriteriaMeasures ccInfo) {
			Optional.ofNullable(ccInfo).ifPresent(info ->
					traits.setSingleTrait(CommonCriteriaTrait.builder()
							.traitValue(CommonCriteriaEvaluation.builder()
									.cCMeasures(info)
									.cCCertificateNumber(ASN1Utils.getUTF8String(""))
									.cCCertificateAuthority(ASN1Utils.getUTF8String(""))
									.build())
							.build())
			);
			return this;
		}

		public TBBSecurityAssertionsBuilder fipsLevel(FIPSLevel fipsLevel) {
			Optional.ofNullable(fipsLevel).ifPresent(level ->
					traits.setSingleTrait(FIPSLevelTrait.builder().traitValue(level).build())
			);
			return this;
		}

		public TBBSecurityAssertionsBuilder rtmType(MeasurementRootType rtmType) {
			Optional.ofNullable(rtmType).ifPresent(rtm ->
					traits.setSingleTrait(RTMTrait.builder().traitValue(new RTMTypes(rtm.getValue())).build())
			);
			return this;
		}

		public TBBSecurityAssertionsBuilder iso9000Certified(ASN1Boolean certified) {
			ISO9000Certification updated = getIso9000Certification()
					.map(current -> current.toBuilder().iso9000Certified(certified).build())
					.orElseGet(() -> ISO9000Certification.builder().iso9000Certified(certified).build());
			return iso9000Certification(updated);
		}

		public TBBSecurityAssertionsBuilder iso9000Uri(ASN1IA5String uri) {
			ISO9000Certification updated = getIso9000Certification()
					.map(current -> current.toBuilder().iso9000Uri(uri).build())
					.orElseGet(() -> ISO9000Certification.builder().iso9000Uri(uri).build());
			return iso9000Certification(updated);
		}

		private TBBSecurityAssertionsBuilder iso9000Certification(ISO9000Certification certification) {
			traits.setSingleTrait(ISO9000Trait.builder().traitValue(certification).build());
			return this;
		}

		private Optional<ISO9000Certification> getIso9000Certification() {
			return traits.firstTrait(ISO9000Trait.class).map(Trait::getTraitValue);
		}

		public TBBSecurityAssertions build() {
			return new TBBSecurityAssertions(version, traits);
		}
	}
}
