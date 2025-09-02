package tcg.credential;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import json.schema.ComponentSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>{@code
 * ComponentIdentifier ::= SEQUENCE {
 *      componentManufacturer UTF8String (SIZE (1..STRMAX)),
 *      componentModel UTF8String (SIZE (1..STRMAX)),
 *      componentSerial[0] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      componentRevision [1] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      componentManufacturerId [2] IMPLICIT PrivateEnterpriseNumber OPTIONAL,
 *      fieldReplaceable [3] IMPLICIT BOOLEAN OPTIONAL,
 *      componentAddress [4] IMPLICIT SEQUENCE(SIZE(1..CONFIGMAX)) OF ComponentAddress OPTIONAL }
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
public class ComponentIdentifier extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 2;
	private static final int MAX_SEQUENCE_SIZE = 7;

	@JsonProperty(ComponentSchema.MANUFACTURER)
    @JsonAlias(ComponentSchema.COMPONENT_MANUFACTURER)
	@NonNull
	@NotNull
	private final ASN1UTF8String componentManufacturer;
	@JsonProperty(ComponentSchema.MODEL)
    @JsonAlias(ComponentSchema.COMPONENT_MODEL)
	@NonNull
	@NotNull
	private final ASN1UTF8String componentModel;
	@JsonProperty(ComponentSchema.SERIAL)
    @JsonAlias(ComponentSchema.COMPONENT_SERIAL)
	private final ASN1UTF8String componentSerial; // optional, tagged 0
	@JsonProperty(ComponentSchema.REVISION)
    @JsonAlias(ComponentSchema.COMPONENT_REVISION)
	private final ASN1UTF8String componentRevision; // optional, tagged 1
	@JsonProperty(ComponentSchema.MANUFACTURER_ID)
    @JsonAlias(ComponentSchema.COMPONENT_MANUFACTURER_ID)
	private final ASN1ObjectIdentifier componentManufacturerId; // optional, tagged 2
	private final ASN1Boolean fieldReplaceable; // optional, tagged 3
	@JsonProperty(ComponentSchema.ADDRESSES)
    @JsonAlias(ComponentSchema.COMPONENT_ADDRESSES)
	@Singular
	@Size(min = 1)
	private final List<ComponentAddress> componentAddresses; // optional, tagged 4

	/**
	 * Attempts to cast the provided object.
	 * @param obj the object to parse
	 * @return ComponentIdentifier
	 */
	public static ComponentIdentifier getInstance(Object obj) {
		if (obj == null || obj instanceof ComponentIdentifier) {
			return (ComponentIdentifier) obj;
		}
		if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
			return ComponentIdentifier.fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return ComponentIdentifier
	 */
	public static ComponentIdentifier fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < ComponentIdentifier.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		ComponentIdentifier.ComponentIdentifierBuilder builder = ComponentIdentifier.builder()
				.componentManufacturer(ASN1UTF8String.getInstance(untaggedElements.get(0)))
				.componentModel(ASN1UTF8String.getInstance(untaggedElements.get(1)));

		ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
			switch (key) {
				case 0 -> builder.componentSerial(ASN1Utils.getUTF8String(value));
				case 1 -> builder.componentRevision(ASN1Utils.getUTF8String(value));
				case 2 -> builder.componentManufacturerId(ASN1Utils.getOID(value));
				case 3 -> builder.fieldReplaceable(ASN1Utils.getBoolean(value));
				case 4 -> builder.componentAddressesFromSequence(ASN1Utils.getSequence(value));
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
		vec.add(this.componentManufacturer);
		vec.add(this.componentModel);
		if (this.componentSerial != null) {
			vec.add(new DERTaggedObject(false, 0, this.componentSerial));
		}
		if (this.componentRevision != null) {
			vec.add(new DERTaggedObject(false, 1, this.componentRevision));
		}
		if (this.componentManufacturerId != null) {
			vec.add(new DERTaggedObject(false, 2, this.componentManufacturerId));
		}
		if (this.fieldReplaceable != null) {
			vec.add(new DERTaggedObject(false, 3, this.fieldReplaceable));
		}
		if (this.componentAddresses != null) {
			vec.add(new DERTaggedObject(false, 4, new DERSequence(ASN1Utils.toASN1EncodableVector(this.componentAddresses))));
		}
		return new DERSequence(vec);
	}

	/**
	 * The rest of this builder is generated by lombok Builder annotation
	 */
	public static class ComponentIdentifierBuilder {
		/**
		 * Reads elements of the given sequence as ComponentAddresses and adds them to the builder.
		 * @param seq ASN1Sequence
		 */
		public final void componentAddressesFromSequence(@NonNull ASN1Sequence seq) {
			Arrays.asList(seq.toArray()).forEach(
					element ->
							this.componentAddress(ComponentAddress.getInstance(element)));
		}
	}
}
