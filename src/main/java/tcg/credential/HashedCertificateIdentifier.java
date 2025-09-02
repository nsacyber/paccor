package tcg.credential;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import json.ASN1OctetStringFromBase64Deserializer;
import json.AlgorithmIdentifierDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * <pre>
 * HashedCertificateIdentifier ::= SEQUENCE {
 *      hashAlgorithm AlgorithmIdentifier,
 *      hashOverSignatureValue OCTET STRING }
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
public class HashedCertificateIdentifier extends ASN1Object {
    private static final int SEQUENCE_SIZE = 2;

    @JsonDeserialize(using = AlgorithmIdentifierDeserializer.class)
    @NonNull
    private final AlgorithmIdentifier hashAlgorithm;
    @JsonAlias("HASH")
    @JsonDeserialize(using = ASN1OctetStringFromBase64Deserializer.class)
    @NonNull
    private final ASN1OctetString hashOverSignatureValue;

    /**
     * Attempts to cast the provided object.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return HashedCertificateIdentifier
     */
    public static HashedCertificateIdentifier getInstance(Object obj) {
        if (obj == null || obj instanceof HashedCertificateIdentifier) {
            return (HashedCertificateIdentifier) obj;
        }
        if (obj instanceof ASN1Sequence seq) {
            return HashedCertificateIdentifier.fromASN1Sequence(seq);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return HashedCertificateIdentifier
     */
    public static HashedCertificateIdentifier fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() != HashedCertificateIdentifier.SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        ASN1Object[] elements = (ASN1Object[]) seq.toArray();

        HashedCertificateIdentifier.HashedCertificateIdentifierBuilder builder = HashedCertificateIdentifier.builder()
                .hashAlgorithm(AlgorithmIdentifier.getInstance(elements[0]))
                .hashOverSignatureValue(ASN1OctetString.getInstance(elements[1]));

        return builder.build();
    }

    /**
     * @return This object as an ASN1Sequence
     */
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(hashAlgorithm);
        vec.add(hashOverSignatureValue);
        return new DERSequence(vec);
    }
}
