package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import paccor.json.AlgorithmIdentifierDeserializer;
import paccor.json.schema.ComponentSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import paccor.crypto.AlgorithmSupport;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * <pre>{@code
 * HashedCertificateIdentifier ::= SEQUENCE {
 *      hashAlgorithm AlgorithmIdentifier,
 *      hashOverSignatureValue OCTET STRING }
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
public class HashedCertificateIdentifier extends ASN1Object {
    private static final int SEQUENCE_SIZE = 2;

    @JsonDeserialize(using = AlgorithmIdentifierDeserializer.class)
    @JsonProperty(ComponentSchema.HASH_ALGORITHM)
    @JsonAlias(ComponentSchema.HASH_ALG)
    @NonNull
    @NotNull
    private final AlgorithmIdentifier hashAlgorithm;
    @JsonProperty(ComponentSchema.HASH)
    @JsonAlias({ComponentSchema.HASH_OVER_SIGNATURE_VALUE, ComponentSchema.HASH_VALUE})
    @NonNull
    @NotNull
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
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return HashedCertificateIdentifier.fromASN1Sequence(ASN1Utils.getSequence(obj));
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

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

        HashedCertificateIdentifier.HashedCertificateIdentifierBuilder builder = HashedCertificateIdentifier.builder()
                .hashAlgorithm(AlgorithmIdentifier.getInstance(ASN1Utils.getSequence(untaggedElements.get(0))))
                .hashOverSignatureValue(ASN1Utils.getOctetString(untaggedElements.get(1)));

        return builder.build();
    }

    public static final HashedCertificateIdentifier fromAC(X509AttributeCertificateHolder ac) {
        return HashedCertificateIdentifier.builder()
                .hashAlgorithm(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384))
                .hashOverSignatureValue(new DEROctetString(AlgorithmSupport.sha384(ac.getSignature())))
                .build();

    }

    public static final HashedCertificateIdentifier fromPKC(X509CertificateHolder pkc) {
        return HashedCertificateIdentifier.builder()
                .hashAlgorithm(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha384))
                .hashOverSignatureValue(new DEROctetString(AlgorithmSupport.sha384(pkc.getSignature())))
                .build();
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
