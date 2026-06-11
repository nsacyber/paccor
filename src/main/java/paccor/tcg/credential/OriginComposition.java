package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>{@code
 * OriginComposition SEQUENCE ::= Sequence {
 *      location EntityGeoLocation,
 *      hasComponents [0] BOOLEAN DEFAULT TRUE OPTIONAL }
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
@ToString
public class OriginComposition extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 1;
    private static final int MAX_SEQUENCE_SIZE = 2;

    @NonNull
    @NotNull
    private final EntityGeoLocation location;
    @Builder.Default
    @NonNull
    @NotNull
    private final ASN1Boolean hasComponents = ASN1Boolean.TRUE; // default = true, optional, tagged 0

    /**
     * Attempts to cast the provided object.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return OriginComposition
     */
    public static final OriginComposition getInstance(Object obj) {
        if (obj == null || obj instanceof OriginComposition) {
            return (OriginComposition) obj;
        }
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return OriginComposition.fromASN1Sequence(ASN1Utils.getSequence(obj));
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return OriginComposition
     */
    public static final OriginComposition fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < OriginComposition.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

        OriginComposition.OriginCompositionBuilder builder = OriginComposition.builder()
                .location(EntityGeoLocation.getInstance(untaggedElements.getFirst()));

        ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
            if (key == 0) {
                builder.hasComponents(ASN1Utils.safeGetDefaultElement(value, ASN1Boolean.TRUE, ASN1Utils::getBoolean));
            }
        });

        return builder.build();
    }

    /**
     * @return This object as an ASN1Sequence
     */
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(this.location);
        if (!this.hasComponents.isTrue()) {
            vec.add(new DERTaggedObject(false, 0, hasComponents));
        }
        return new DERSequence(vec);
    }
}
