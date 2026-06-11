package paccor.tcg.credential;

import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>{@code
 * ISO9000Certification ::= SEQUENCE {
 *      iso9000Certified BOOLEAN DEFAULT FALSE,
 *      iso9000Uri IA5STRING (SIZE (1..URIMAX)) OPTIONAL }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@NoArgsConstructor(force = true)
public class ISO9000Certification extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 0;
    private static final int MAX_SEQUENCE_SIZE = 2;

    @Builder.Default
    @NonNull
    @NotNull
    private final ASN1Boolean iso9000Certified = ASN1Boolean.FALSE; // default false
    private final ASN1IA5String iso9000Uri;

    public static ISO9000Certification getInstance(Object obj) {
        if (obj == null || obj instanceof ISO9000Certification) {
            return (ISO9000Certification) obj;
        }
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return ISO9000Certification.fromASN1Sequence(ASN1Utils.getSequence(obj));
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    public static ISO9000Certification fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < ISO9000Certification.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        ISO9000Certification.ISO9000CertificationBuilder builder = ISO9000Certification.builder();

        builder.iso9000Certified(ASN1Utils.safeGetDefaultElementFromSequence(seq, 0, ASN1Boolean.FALSE, ASN1Utils::getBoolean));

        // iso9000Uri can only be at position 1 if it exists
        Optional.ofNullable(ASN1Utils.safeGetDefaultElementFromSequence(seq, 1, null, ASN1Utils::getIA5String))
                .ifPresent(builder::iso9000Uri);

        return builder.build();
    }

    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        if (iso9000Certified.isTrue()) {
            vec.add(iso9000Certified);
        }
        if (iso9000Uri != null) {
            vec.add(iso9000Uri);
        }
        return new DERSequence(vec);
    }
}
