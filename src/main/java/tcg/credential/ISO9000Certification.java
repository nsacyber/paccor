package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * ISO9000Certification ::= SEQUENCE {
 *      iso9000Certified BOOLEAN DEFAULT FALSE,
 *      iso9000Uri IA5STRING (SIZE (1..URIMAX)) OPTIONAL }
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class ISO9000Certification extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 0;
    private static final int MAX_SEQUENCE_SIZE = 2;

    @NonNull
    @Builder.Default
    private final ASN1Boolean iso9000Certified = ASN1Boolean.FALSE; // default false
    private final ASN1IA5String iso9000Uri;

    public static ISO9000Certification getInstance(Object obj) {
        if (obj == null || obj instanceof ISO9000Certification) {
            return (ISO9000Certification) obj;
        }
        if (obj instanceof ASN1Sequence seq) {
            return ISO9000Certification.fromASN1Sequence(seq);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    public static ISO9000Certification fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < ISO9000Certification.MIN_SEQUENCE_SIZE || seq.size() > ISO9000Certification.MAX_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        ISO9000Certification.ISO9000CertificationBuilder builder = ISO9000Certification.builder();

        builder.iso9000Certified(ASN1Utils.safeGetDefaultElementFromSequence(seq, 0, ASN1Boolean.FALSE, ASN1Boolean::getInstance));

        // Sequence could be any combination of class fields
        // String should be the last element if present
        try {
            builder.iso9000Uri(ASN1IA5String.getInstance(seq.getObjectAt(seq.size() - 1)));
        } catch (Exception ignored) {}

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
