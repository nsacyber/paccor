package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import json.IssuerSerialDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.IssuerSerial;

/**
 * <pre>
 * CertificateIdentifier ::= SEQUENCE {
 *      hashedCertIdentifier [0] IMPLICIT HashedCertificateIdentifier OPTIONAL,
 *      genericCertIdentifier [1] IMPLICIT IssuerSerial OPTIONAL }
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Getter
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class CertificateIdentifier extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 0;
    private static final int MAX_SEQUENCE_SIZE = 2;

    private final HashedCertificateIdentifier hashedCertIdentifier; // optional, tagged 0
    @JsonDeserialize(using = IssuerSerialDeserializer.class)
    private final IssuerSerial genericCertIdentifier; // optional, tagged 1

    /**
     * Attempts to cast the provided object to CertificateIdentifier.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return CertificateIdentifier
     */
    public static CertificateIdentifier getInstance(Object obj) {
        if (obj == null || obj instanceof CertificateIdentifier) {
            return (CertificateIdentifier) obj;
        }
        if (obj instanceof ASN1Sequence seq) {
            return CertificateIdentifier.fromASN1Sequence(seq);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to read a CertificateIdentifier sequence from the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return CertificateIdentifier
     */
    public static CertificateIdentifier fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < CertificateIdentifier.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        CertificateIdentifier.CertificateIdentifierBuilder builder = CertificateIdentifier.builder();

        ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
            switch (key) {
                case 0 -> builder.hashedCertIdentifier(HashedCertificateIdentifier.getInstance(value));
                case 1 -> builder.genericCertIdentifier(IssuerSerial.getInstance(value));
                default -> {}
            }
        });

        return builder.build();
    }

    /**
     * @return this object as an ASN1Sequence
     */
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        if (hashedCertIdentifier != null) {
            vec.add(new DERTaggedObject(false, 0, hashedCertIdentifier));
        }
        if (genericCertIdentifier != null) {
            vec.add(new DERTaggedObject(false, 1, genericCertIdentifier));
        }
        return new DERSequence(vec);
    }
}
