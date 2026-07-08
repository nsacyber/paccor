package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
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
 *      WITH SYNTAX PlatformConfiguration-v3
 *      ID tcg-at-platformConfiguration-v3 }
 *
 * PlatformConfiguration-v3 ::= SEQUENCE {
 *      platformComponents [0] IMPLICIT SEQUENCE(SIZE(1..MAX)) OF ComponentIdentifier OPTIONAL,
 *      platformProperties [1] IMPLICIT SEQUENCE(SIZE(1..MAX)) OF Property OPTIONAL }
 *
 * ComponentIdentifier ::= SEQUENCE(SIZE(1..MAX)) OF Trait
 *
 * Property ::= SEQUENCE {
 *      propertyName UTF8String (SIZE (1..STRMAX)),
 *      propertyValue UTF8String (SIZE (1..STRMAX)),
 *      status [0] IMPLICIT AttributeStatus OPTIONAL }
 *
 * AttributeStatus ::= ENUMERATED {
 *      added (0),
 *      modified (1),
 *      removed (2) }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@JsonClassDescription("Platform configuration in the v3/v2.0 JSON form using trait-based component identifiers.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class PlatformConfigurationV3 extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 0;
	private static final int MAX_SEQUENCE_SIZE = 2;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty(HardwareManifestSchema.COMPONENTS)
	@JsonPropertyDescription("Platform components represented as trait collections.")
	@Singular
    @Size(min = 1)
	private final List<TraitMap> platformComponents; // optional, tagged 0
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    @JsonProperty(HardwareManifestSchema.PROPERTIES)
	@JsonPropertyDescription("Platform properties associated with the configuration.")
	@Singular
    @Size(min = 1)
	private final List<PlatformPropertiesV2> platformProperties; // optional, tagged 1

    /**
     * Attempts to cast the provided object.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return PlatformConfigurationV3
     */
    public static final PlatformConfigurationV3 getInstance(Object obj) {
        if (obj == null || obj instanceof PlatformConfigurationV3) {
            return (PlatformConfigurationV3) obj;
        }
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return PlatformConfigurationV3.fromASN1Sequence(ASN1Utils.getSequence(obj));
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return PlatformConfigurationV3
     */
    public static final PlatformConfigurationV3 fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < PlatformConfigurationV3.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        PlatformConfigurationV3.PlatformConfigurationV3Builder builder = PlatformConfigurationV3.builder();

        ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
            switch (key) {
                case 0 -> builder.platformComponentsFromSequence(ASN1Utils.getSequence(value));
                case 1 -> builder.platformPropertiesFromSequence(ASN1Utils.getSequence(value));
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
        if (this.platformComponents != null) {
            vec.add(new DERTaggedObject(false, 0, new DERSequence(ASN1Utils.toASN1EncodableVector(platformComponents))));
        }
        if (this.platformProperties != null) {
            vec.add(new DERTaggedObject(false, 1, new DERSequence(ASN1Utils.toASN1EncodableVector(platformProperties))));
        }
        return new DERSequence(vec);
    }

    /**
     * The rest of this builder is generated by lombok Builder annotation
     */
    public static class PlatformConfigurationV3Builder {
        /**
         * Reads elements of the given sequence as ComponentIdentifier Trait Sequence and adds them to the builder.
         * @param seq ASN1Sequence
         */
        public final void platformComponentsFromSequence(@NonNull ASN1Sequence seq) {
            Optional.ofNullable(ASN1Utils.safeGetDefaultElement(seq, null, TraitMap::getInstance))
                    .map(List::of)
                    .orElseGet(() -> Stream.of(seq.toArray()).map(TraitMap::getInstance).toList())
                    .forEach(this::platformComponent);
        }
        /**
         * Reads elements of the given sequence as PlatformPropertiesV2 and adds them to the builder.
         * @param seq ASN1Sequence
         */
        public final void platformPropertiesFromSequence(@NonNull ASN1Sequence seq) {
            Optional.ofNullable(ASN1Utils.safeGetDefaultElement(seq, null, PlatformPropertiesV2::getInstance))
                    .map(List::of)
                    .orElseGet(() -> Stream.of(seq.toArray()).map(PlatformPropertiesV2::getInstance).toList())
                    .forEach(this::platformProperty);
        }
    }
}
