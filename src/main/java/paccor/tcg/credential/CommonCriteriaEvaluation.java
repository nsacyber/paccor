package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>{@code
 * CommonCriteriaEvaluation ::= SEQUENCE {
 *      cCMeasures CommonCriteriaMeasures,
 *      cCCertificateNumber UTF8String (SIZE (1..STRMAX)),
 *      cCCertificateAuthority UTF8String (SIZE (1..STRMAX)),
 *      evaluationScheme [0] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      cCCertificateIssuanceDate [1] IMPLICIT GeneralizedTime OPTIONAL,
 *      cCCertificateExpiryDate [2] IMPLICIT GeneralizedTime OPTIONAL
 * }
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
public class CommonCriteriaEvaluation extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 3;
    private static final int MAX_SEQUENCE_SIZE = 6;

    @JsonProperty("commonCriteriaMeasures")
    @NonNull
    @NotNull
    private final CommonCriteriaMeasures cCMeasures;
    @NonNull
    @NotNull
    private final ASN1UTF8String cCCertificateNumber;
    @NonNull
    @NotNull
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
    public static final CommonCriteriaEvaluation getInstance(Object obj) {
        if (obj == null || obj instanceof CommonCriteriaEvaluation) {
            return (CommonCriteriaEvaluation) obj;
        }
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return fromASN1Sequence(ASN1Utils.getSequence(obj));
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

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

        CommonCriteriaEvaluation.CommonCriteriaEvaluationBuilder builder = CommonCriteriaEvaluation.builder()
                .cCMeasures(CommonCriteriaMeasures.getInstance(untaggedElements.get(0)))
                .cCCertificateNumber(ASN1UTF8String.getInstance(untaggedElements.get(1)))
                .cCCertificateAuthority(ASN1UTF8String.getInstance(untaggedElements.get(2)));

        ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
            switch (key) {
                case 0 -> builder.evaluationScheme(ASN1Utils.getUTF8String(value));
                case 1 -> builder.cCCertificateIssuanceDate(ASN1Utils.getGeneralizedTime(value));
                case 2 -> builder.cCCertificateExpiryDate(ASN1Utils.getGeneralizedTime(value));
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
            vec.add(new DERTaggedObject(false, 1, this.cCCertificateIssuanceDate));
        }
        if (this.cCCertificateExpiryDate != null) {
            vec.add(new DERTaggedObject(false, 2, this.cCCertificateExpiryDate));
        }
        return new DERSequence(vec);
    }
}
