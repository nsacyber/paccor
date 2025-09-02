package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
 * CommonCriteriaEvaluation ::= SEQUENCE {
 *      cCMeasures CommonCriteriaMeasures,
 *      cCCertificateNumber UTF8String (SIZE (1..STRMAX)),
 *      cCCertificateAuthority UTF8String (SIZE (1..STRMAX)),
 *      evaluationScheme [0] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      cCCertificateIssuanceDate [1] IMPLICIT GeneralizedTime OPTIONAL,
 *      cCCertificateExpiryDate [2] IMPLICIT GeneralizedTime OPTIONAL
 * }
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class CommonCriteriaEvaluation extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 3;
    private static final int MAX_SEQUENCE_SIZE = 6;

    @NonNull
    private final CommonCriteriaMeasures cCMeasures;
    @NonNull
    private final ASN1UTF8String cCCertificateNumber;
    @NonNull
    private final ASN1UTF8String cCCertificateAuthority;
    private final ASN1UTF8String evaluationScheme; // optional, tagged 0
    private final ASN1GeneralizedTime cCCertificateIssuanceDate; // optional, tagged 1
    private final ASN1GeneralizedTime cCCertificateExpiryDate; // optional, tagged 2

    /**
     * Attempts to cast the provided object.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return CommonCriteriaEvaluation
     */
    public static CommonCriteriaEvaluation getInstance(Object obj) {
        if (obj == null || obj instanceof CommonCriteriaEvaluation) {
            return (CommonCriteriaEvaluation) obj;
        }
        if (obj instanceof ASN1Sequence seq) {
            return fromASN1Sequence(seq);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return CommonCriteriaEvaluation
     */
    public static final CommonCriteriaEvaluation fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < CommonCriteriaEvaluation.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        ASN1Object[] elements = (ASN1Object[]) seq.toArray();

        CommonCriteriaEvaluation.CommonCriteriaEvaluationBuilder builder = CommonCriteriaEvaluation.builder()
                .cCMeasures(CommonCriteriaMeasures.getInstance(elements[0]))
                .cCCertificateNumber(DERUTF8String.getInstance(elements[1]))
                .cCCertificateAuthority(DERUTF8String.getInstance(elements[2]));

        ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
            switch (key) {
                case 0 -> builder.evaluationScheme(DERUTF8String.getInstance(value));
                case 1 -> builder.cCCertificateIssuanceDate(ASN1GeneralizedTime.getInstance(value));
                case 2 -> builder.cCCertificateExpiryDate(ASN1GeneralizedTime.getInstance(value));
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
        vec.add(this.cCMeasures);
        vec.add(this.cCCertificateNumber);
        vec.add(this.cCCertificateAuthority);
        if (this.evaluationScheme != null) {
            vec.add(new DERTaggedObject(false, 0, this.evaluationScheme));
        }
        if (this.cCCertificateIssuanceDate != null) {
            vec.add(new DERTaggedObject(false, 0, this.cCCertificateIssuanceDate));
        }
        if (this.cCCertificateExpiryDate != null) {
            vec.add(new DERTaggedObject(false, 0, this.cCCertificateExpiryDate));
        }
        return new DERSequence(vec);
    }
}
