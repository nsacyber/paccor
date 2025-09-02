package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
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
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class TCGCredentialType extends ASN1Object {
    private static final int SEQUENCE_SIZE = 1;

    @NonNull
    private final ASN1ObjectIdentifier certificateType;

    /**
     * Attempts to cast the provided object.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return TCGCredentialType
     */
    public static TCGCredentialType getInstance(Object obj) {
        if (obj == null || obj instanceof TCGCredentialType) {
            return (TCGCredentialType) obj;
        }
        if (obj instanceof ASN1Sequence seq) {
            return fromASN1Sequence(seq);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return TCGCredentialType
     */
    public static final TCGCredentialType fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() != TCGCredentialType.SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        ASN1Object[] elements = (ASN1Object[]) seq.toArray();

        TCGCredentialType.TCGCredentialTypeBuilder builder = TCGCredentialType.builder()
                .certificateType(ASN1ObjectIdentifier.getInstance(elements[0]));

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
