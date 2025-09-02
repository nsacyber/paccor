package tcg.credential;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>{@code
 * tCGCredentialType ATTRIBUTE ::= {
 *      WITH SYNTAX TCGCredentialType
 *      ID tcg-at-tcgCredentialType }
 *
 * TCGCredentialType ::= SEQUENCE {
 *      certificateType CredentialType }
 *
 * CredentialType ::= OBJECT IDENTIFIER (tcg-kp-PlatformAttributeCertificate | tcg-kp-PlatformKeyCertificate |
 *                          tcg-kp-AdditionalPlatformAttributeCertificate | tcg-kp-AdditionalPlatformKeyCertificate |
 *                          tcg-kp-DeltaPlatformAttributeCertificate | tcg-kp-DeltaPlatformKeyCertificate)
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@NoArgsConstructor(force = true)
public class TCGCredentialType extends ASN1Object {
    private static final int SEQUENCE_SIZE = 1;

    @NonNull
    @NotNull
    private final ASN1ObjectIdentifier certificateType;

    /**
     * Attempts to cast the provided object.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return {@link TCGCredentialType}
     */
    public static final TCGCredentialType getInstance(Object obj) {
        if (obj == null || obj instanceof TCGCredentialType) {
            return (TCGCredentialType) obj;
        }
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return fromASN1Sequence(ASN1Utils.getSequence(obj));
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return {@link TCGCredentialType}
     */
    public static final TCGCredentialType fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() != TCGCredentialType.SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

        TCGCredentialType.TCGCredentialTypeBuilder builder = TCGCredentialType.builder()
                .certificateType(ASN1ObjectIdentifier.getInstance(untaggedElements.getFirst()));

        return builder.build();
    }

    /**
     * @return This object as an ASN1Sequence
     */
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(this.certificateType);
        return new DERSequence(vec);
    }
}
