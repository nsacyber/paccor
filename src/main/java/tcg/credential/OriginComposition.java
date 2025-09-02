package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>
 * OriginComposition SEQUENCE ::= Sequence {
 *      location EntityGeoLocation,
 *      hasComponents [0] BOOLEAN DEFAULT TRUE OPTIONAL }
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class OriginComposition extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 1;
    private static final int MAX_SEQUENCE_SIZE = 2;

    @NonNull
    private final EntityGeoLocation location;
    @NonNull
    @Builder.Default
    private final ASN1Boolean hasComponents = ASN1Boolean.TRUE; // default = true, optional, tagged 0

    /**
     * Attempts to cast the provided object.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return OriginComposition
     */
    public static OriginComposition getInstance(Object obj) {
        if (obj == null || obj instanceof OriginComposition) {
            return (OriginComposition) obj;
        }
        if (obj instanceof ASN1Sequence seq) {
            return OriginComposition.fromASN1Sequence(seq);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return OriginComposition
     */
    public static OriginComposition fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < OriginComposition.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        ASN1Object[] elements = (ASN1Object[]) seq.toArray();

        OriginComposition.OriginCompositionBuilder builder = OriginComposition.builder()
                .location(EntityGeoLocation.getInstance(elements[0]));

        ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
            if (key == 0) {
                builder.hasComponents(ASN1Utils.safeGetDefaultElement(value, ASN1Boolean.TRUE, ASN1Boolean::getInstance));
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
