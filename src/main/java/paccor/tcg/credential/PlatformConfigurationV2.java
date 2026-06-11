package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
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

	@JsonProperty(HardwareManifestSchema.COMPONENTS)
	@JsonPropertyDescription("Platform components in the legacy v1.1 component identifier form.")
	@Singular
	@Size(min = 1)
	private final List<ComponentIdentifierV2> componentIdentifiers; // optional, tagged 0
	@JsonProperty(HardwareManifestSchema.COMPONENTS_URI)
	@JsonPropertyDescription("Optional URI reference for externally hosted component identifiers.")
	private final URIReference componentIdentifiersUri; // optional, tagged 1
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
		if (seq.size() < PlatformConfigurationV2.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		PlatformConfigurationV2.PlatformConfigurationV2Builder builder = PlatformConfigurationV2.builder();

		ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
			switch (key) {
				case 0 -> builder.componentIdentifiersFromSequence(ASN1Utils.getSequence(value));
				case 1 -> builder.componentIdentifiersUri(URIReference.getInstance(value));
				case 2 -> builder.platformPropertiesFromSequence(ASN1Utils.getSequence(value));
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
		if (componentIdentifiers != null) {
			vec.add(new DERTaggedObject(false, 0, new DERSequence(ASN1Utils.toASN1EncodableVector(componentIdentifiers))));
		}
		if (componentIdentifiersUri != null) {
			vec.add(new DERTaggedObject(false, 1, componentIdentifiersUri));
		}
		if (platformProperties != null) {
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
		 * Reads elements of the given sequence as ComponentIdentifierV2 and adds them to the builder.
		 * @param seq ASN1Sequence
		 */
		public final void componentIdentifiersFromSequence(@NonNull ASN1Sequence seq) {
			Arrays.asList(seq.toArray()).forEach(
					element ->
							this.componentIdentifier(ComponentIdentifierV2.getInstance(element)));
		}

		/**
		 * Reads elements of the given sequence as PlatformPropertiesV2 and adds them to the builder.
		 * @param seq ASN1Sequence
		 */
		public final void platformPropertiesFromSequence(@NonNull ASN1Sequence seq) {
			Arrays.asList(seq.toArray()).forEach(
					element ->
							this.platformProperty(PlatformPropertiesV2.getInstance(element)));
		}
	}
}
