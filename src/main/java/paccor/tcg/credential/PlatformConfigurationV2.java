package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.Size;
import java.util.List;
import paccor.json.schema.HardwareManifestSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>{@code
 * platformConfiguration ATTRIBUTE ::= {
 *      WITH SYNTAX PlatformConfiguration
 *      ID tcg-at-platformConfiguration-v2 }
 *
 * PlatformConfiguration ::= SEQUENCE {
 *      componentIdentifiers [0] IMPLICIT SEQUENCE(SIZE(1..MAX)) OF ComponentIdentifier OPTIONAL,
 *      componentIdentifiersUri [1] IMPLICIT URIReference OPTIONAL,
 *      platformProperties [2] IMPLICIT SEQUENCE(SIZE(1..MAX)) OF Properties OPTIONAL,
 *      platformPropertiesUri [3] IMPLICIT URIReference OPTIONAL }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@JsonClassDescription("Platform configuration in the v1.1 JSON form with component identifiers, properties, and optional URI references.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class PlatformConfigurationV2 extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 0;
	private static final int MAX_SEQUENCE_SIZE = 4;

	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonProperty(HardwareManifestSchema.COMPONENTS)
	@JsonPropertyDescription("Platform components in the legacy v1.1 component identifier form.")
	@Singular
	@Size(min = 1)
	private final List<ComponentIdentifierV2> componentIdentifiers; // optional, tagged 0
	@JsonProperty(HardwareManifestSchema.COMPONENTS_URI)
	@JsonPropertyDescription("Optional URI reference for externally hosted component identifiers.")
	private final URIReference componentIdentifiersUri; // optional, tagged 1
	@JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
	@JsonProperty(HardwareManifestSchema.PROPERTIES)
	@JsonPropertyDescription("Platform properties.")
	@Singular
	@Size(min = 1)
	private final List<PlatformPropertiesV2> platformProperties; // optional, tagged 2
	@JsonProperty(HardwareManifestSchema.PROPERTIES_URI)
	@JsonPropertyDescription("Optional URI reference for externally hosted platform properties.")
	private final URIReference platformPropertiesUri; // optional, tagged 3

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return PlatformConfigurationV2
	 */
	public static final PlatformConfigurationV2 getInstance(Object obj) {
		if (obj == null || obj instanceof PlatformConfigurationV2) {
			return (PlatformConfigurationV2) obj;
		}
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
			return PlatformConfigurationV2.fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return PlatformConfigurationV2
	 */
	public static final PlatformConfigurationV2 fromASN1Sequence(@NonNull ASN1Sequence seq) {
		Definitions.warnOnExtraElements(seq, PlatformConfigurationV2.MAX_SEQUENCE_SIZE, PlatformConfigurationV2.class);
		if (seq.size() < PlatformConfigurationV2.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		PlatformConfigurationV2.PlatformConfigurationV2Builder builder = PlatformConfigurationV2.builder();

		ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
			switch (key) {
				case 0 -> builder.componentIdentifiersFromTaggedSequence(value);
				case 1 -> builder.componentIdentifiersUri(URIReference.getInstance(value));
				case 2 -> builder.platformPropertiesFromTaggedSequence(value);
				case 3 -> builder.platformPropertiesUri(URIReference.getInstance(value));
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
		if (componentIdentifiers != null && !componentIdentifiers.isEmpty()) {
			vec.add(new DERTaggedObject(false, 0, new DERSequence(ASN1Utils.toASN1EncodableVector(componentIdentifiers))));
		}
		if (componentIdentifiersUri != null) {
			vec.add(new DERTaggedObject(false, 1, componentIdentifiersUri));
		}
		if (platformProperties != null && !platformProperties.isEmpty()) {
			vec.add(new DERTaggedObject(false, 2, new DERSequence(ASN1Utils.toASN1EncodableVector(platformProperties))));
		}
		if (platformPropertiesUri != null) {
			vec.add(new DERTaggedObject(false, 3, platformPropertiesUri));
		}
		return new DERSequence(vec);
	}

	/**
	 * The rest of this builder is generated by lombok Builder annotation
	 */
	public static class PlatformConfigurationV2Builder {
		/**
		 * Reads the tagged componentIdentifiers field and adds each ComponentIdentifierV2 to the builder.
		 * @param tagged ASN1TaggedObject
		 */
		public final void componentIdentifiersFromTaggedSequence(@NonNull ASN1TaggedObject tagged) {
			ASN1Utils.decodeSequenceOf(tagged, ComponentIdentifierV2::getInstance, "PlatformConfigurationV2 componentIdentifiers")
					.forEach(this::componentIdentifier);
		}

		/**
		 * Reads the tagged platformProperties field and adds each PlatformPropertiesV2 to the builder.
		 * @param tagged ASN1TaggedObject
		 */
		public final void platformPropertiesFromTaggedSequence(@NonNull ASN1TaggedObject tagged) {
			ASN1Utils.decodeSequenceOf(tagged, PlatformPropertiesV2::getInstance, "PlatformConfigurationV2 platformProperties")
					.forEach(this::platformProperty);
		}
	}
}
