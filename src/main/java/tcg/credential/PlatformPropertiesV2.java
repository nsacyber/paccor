package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import json.schema.ComponentSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * The class name is "PlatformProperties" as opposed to TCG given name of "Properties"
 * to limit potential collision issues with any built-in Java class named Properties.
 *
 * <pre>{@code
 * Properties ::= SEQUENCE {
 *      propertyName UTF8String (SIZE (1..STRMAX)),
 *      propertyValue UTF8String (SIZE (1..STRMAX)),
 *      status [0] IMPLICIT AttributeStatus OPTIONAL }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@JsonClassDescription("Single platform property entry with name, value, and optional change status.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class PlatformPropertiesV2 extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 2;
	private static final int MAX_SEQUENCE_SIZE = 3;

    @JsonDeserialize(as = DERUTF8String.class)
    @JsonProperty(ComponentSchema.PROPERTY_NAME)
	@JsonPropertyDescription("Property name.")
	@NonNull
	@NotNull
	private final ASN1UTF8String propertyName;
    @JsonDeserialize(as = DERUTF8String.class)
    @JsonProperty(ComponentSchema.PROPERTY_VALUE)
	@JsonPropertyDescription("Property value.")
	@NonNull
	@NotNull
	private final ASN1UTF8String propertyValue;
    @JsonProperty(ComponentSchema.PROPERTY_STATUS)
	@JsonPropertyDescription("Optional attribute status indicating whether the property was added, modified, or removed.")
	private final AttributeStatus status; // optional, tagged 0

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return PlatformPropertiesV2
	 */
	public static final PlatformPropertiesV2 getInstance(Object obj) {
		if (obj == null || obj instanceof PlatformPropertiesV2) {
			return (PlatformPropertiesV2) obj;
		}
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return PlatformPropertiesV2.fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return PlatformPropertiesV2
	 */
	public static final PlatformPropertiesV2 fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < PlatformPropertiesV2.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		PlatformPropertiesV2.PlatformPropertiesV2Builder builder = PlatformPropertiesV2.builder()
				.propertyName(ASN1UTF8String.getInstance(untaggedElements.get(0)))
				.propertyValue(ASN1UTF8String.getInstance(untaggedElements.get(1)));

		ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
			if (key == 0) {
				builder.status(AttributeStatus.getInstance(value));
			}
		});

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(propertyName);
		vec.add(propertyValue);
		if (status != null) {
			vec.add(new DERTaggedObject(false, 0, status));
		}
		return new DERSequence(vec);
	}
}
