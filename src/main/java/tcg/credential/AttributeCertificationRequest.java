package tcg.credential;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.crmf.Controls;

/**
 * <pre>
 *     AttrCertRequest ::= SEQUENCE {
 *        attrCertReqID       INTEGER,
 *        attrCertTemplate    AttrCertTemplate,
 *        controls            Controls OPTIONAL }
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class AttributeCertificationRequest extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 2;
    private static final int MAX_SEQUENCE_SIZE = 3;

    @NonNull
    private ASN1Integer attrCertReqID;
    @NonNull
    private AttrCertTemplate attrCertTemplate;
    private Controls controls;

    public static AttributeCertificationRequest getInstance(Object obj) {
        if (obj == null || obj instanceof AttributeCertificationRequest) {
            return (AttributeCertificationRequest) obj;
        }
        if (obj instanceof ASN1Sequence seq) {
            return AttributeCertificationRequest.fromASN1Sequence(seq);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }
    
    public static AttributeCertificationRequest fromASN1Sequence(@NonNull ASN1Sequence seq) {
        List<ASN1Object> elements = ASN1Utils.listUntaggedElements(seq);

        if (elements.size() < AttributeCertificationRequest.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        AttributeCertificationRequest.AttributeCertificationRequestBuilder builder = AttributeCertificationRequest.builder()
                .attrCertReqID(ASN1Integer.getInstance(elements.get(0)))
                .attrCertTemplate(AttrCertTemplate.getInstance(elements.get(1)));

        Optional.ofNullable(Controls.getInstance(elements.size() > 1 ? elements.get(2) : null))
                .ifPresent(builder::controls);

        return builder.build();
    }

    /**
     * @return This object as an ASN1Sequence
     */
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(attrCertReqID);
        vec.add(attrCertTemplate);
        if (controls != null) {
            vec.add(controls);
        }
        return new DERSequence(vec);
    }
}
