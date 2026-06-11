package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.x509.AttCertIssuer;
import org.bouncycastle.asn1.x509.AttributeCertificate;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.IssuerSerial;
import org.bouncycastle.asn1.x509.V2Form;
import org.bouncycastle.cert.X509AttributeCertificateHolder;
import org.bouncycastle.cert.X509CertificateHolder;
import paccor.json.schema.ComponentSchema;

/**
 * <pre>{@code
 * CertificateIdentifier ::= SEQUENCE {
 *      hashedCertIdentifier [0] IMPLICIT HashedCertificateIdentifier OPTIONAL,
 *      genericCertIdentifier [1] IMPLICIT IssuerSerial OPTIONAL }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@JsonClassDescription("Certificate identifier containing either a hashed certificate reference, an issuer/serial reference, or both.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class CertificateIdentifier extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 0;
    private static final int MAX_SEQUENCE_SIZE = 2;

    @JsonProperty(ComponentSchema.HASHED_CERT_IDENTIFIER)
    @JsonAlias(ComponentSchema.ATTRIBUTE_CERT_IDENTIFIER)
    @JsonPropertyDescription("Hashed certificate identifier form.")
    private final HashedCertificateIdentifier hashedCertIdentifier; // optional, tagged 0
    @JsonProperty(ComponentSchema.GENERIC_CERT_IDENTIFIER)
    @JsonPropertyDescription("Issuer/serial form of certificate identification.")
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
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return CertificateIdentifier.fromASN1Sequence(ASN1Utils.getSequence(obj));
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
                case 1 -> builder.genericCertIdentifier(IssuerSerial.getInstance(ASN1Utils.getSequence(value)));
                default -> {}
            }
        });

        return builder.build();
    }

    /**
     * Create a CertificateIdentifier from an X509AttributeCertificateHolder.
     * @param ac X509AttributeCertificateHolder
     * @return CertificateIdentifier
     */
    public static final CertificateIdentifier fromAC(X509AttributeCertificateHolder ac) {
        AttributeCertificate structure = ac.toASN1Structure();
        AttCertIssuer issuer = structure.getAcinfo().getIssuer();
        GeneralNames issuerNames;
        try {
            issuerNames = ((V2Form)issuer.getIssuer()).getIssuerName();
        } catch (Exception e) {
            issuerNames = GeneralNames.getInstance(issuer.getIssuer());
        }

        return CertificateIdentifier.builder()
                .hashedCertIdentifier(HashedCertificateIdentifier.fromAC(ac))
                .genericCertIdentifier(new IssuerSerial(issuerNames, ac.getSerialNumber()))
                .build();
    }

    /**
     * Create a CertificateIdentifier from an X509CertificateHolder.
     * @param pkc X509CertificateHolder
     * @return CertificateIdentifier
     */
    public static final CertificateIdentifier fromPKC(X509CertificateHolder pkc) {
        return CertificateIdentifier.builder()
                .hashedCertIdentifier(HashedCertificateIdentifier.fromPKC(pkc))
                .genericCertIdentifier(new IssuerSerial(pkc.getIssuer(), pkc.getSerialNumber()))
                .build();
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
