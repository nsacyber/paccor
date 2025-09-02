package tcg.credential;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Arrays;
import java.util.List;
import json.ASN1BooleanDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
 * ComponentIdentifier ::= SEQUENCE {
 *      componentClass ComponentClass,
 *      componentManufacturer UTF8String (SIZE (1..STRMAX)),
 *      componentModel UTF8String (SIZE (1..STRMAX)),
 *      componentSerial [0] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      componentRevision [1] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      componentManufacturerId [2] IMPLICIT PrivateEnterpriseNumber OPTIONAL,
 *      fieldReplaceable [3] IMPLICIT BOOLEAN OPTIONAL,
 *      componentAddresses [4] IMPLICIT SEQUENCE(SIZE(1..MAX)) OF ComponentAddress OPTIONAL,
 *      componentPlatformCert [5] IMPLICIT CertificateIdentifier OPTIONAL,
 *      componentPlatformCertUri [6] IMPLICIT URIReference OPTIONAL,
 *      status [7] IMPLICIT AttributeStatus OPTIONAL }
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
public class ComponentIdentifierV2 extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 3;
    private static final int MAX_SEQUENCE_SIZE = 11;

    @NonNull
    private final ComponentClass componentClass;
    @JsonAlias("MANUFACTURER")
    @JsonDeserialize(as = DERUTF8String.class)
    @NonNull
    private final ASN1UTF8String componentManufacturer;
    @JsonAlias("MODEL")
    @JsonDeserialize(as = DERUTF8String.class)
    @NonNull
    private final ASN1UTF8String componentModel;
    @JsonAlias("SERIAL")
    @JsonDeserialize(as = DERUTF8String.class)
    private final ASN1UTF8String componentSerial; // optional, tagged 0
    @JsonAlias("REVISION")
    @JsonDeserialize(as = DERUTF8String.class)
    private final ASN1UTF8String componentRevision; // optional, tagged 1
    @JsonAlias("MANUFACTURERID")
    private final ASN1ObjectIdentifier componentManufacturerId; // optional, tagged 2
    @JsonDeserialize(using = ASN1BooleanDeserializer.class)
    private final ASN1Boolean fieldReplaceable; // optional, tagged 3
    @JsonAlias("ADDRESSES")
    @Singular
    private final List<ComponentAddress> componentAddresses; // optional, tagged 4
    @JsonAlias("PLATFORMCERT")
    private final CertificateIdentifier componentPlatformCert; // optional, tagged 5
    @JsonAlias("PLATFORMCERTURI")
    private final URIReference componentPlatformCertUri; // optional, tagged 6
    @JsonAlias("STATUS")
    private final AttributeStatus status; // optional, tagged 7

    /**
     * Attempts to cast the provided object.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return ComponentIdentifierV2
     */
    public static ComponentIdentifierV2 getInstance(Object obj) {
        if (obj == null || obj instanceof ComponentIdentifierV2) {
            return (ComponentIdentifierV2) obj;
        }
        if (obj instanceof ASN1Sequence seq) {
            return ComponentIdentifierV2.getInstance(seq);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return ComponentIdentifierV2
     */
    public static ComponentIdentifierV2 fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < ComponentIdentifierV2.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        ASN1Object[] elements = (ASN1Object[]) seq.toArray();

        ComponentIdentifierV2.ComponentIdentifierV2Builder builder = ComponentIdentifierV2.builder()
                .componentClass(ComponentClass.getInstance(elements[0]))
                .componentManufacturer(DERUTF8String.getInstance(elements[1]))
                .componentModel(DERUTF8String.getInstance(elements[2]));

        ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
            switch (key) {
                case 0 -> builder.componentSerial(DERUTF8String.getInstance(value));
                case 1 -> builder.componentRevision(DERUTF8String.getInstance(value));
                case 2 -> builder.componentManufacturerId(ASN1ObjectIdentifier.getInstance(value));
                case 3 -> builder.fieldReplaceable(ASN1Boolean.getInstance(value));
                case 4 -> builder.componentAddressesFromSequence(ASN1Sequence.getInstance(value));
                case 5 -> builder.componentPlatformCert(CertificateIdentifier.getInstance(value));
                case 6 -> builder.componentPlatformCertUri(URIReference.getInstance(value));
                case 7 -> builder.status(AttributeStatus.getInstance(value));
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
        vec.add(this.componentClass);
        vec.add(this.componentManufacturer);
        vec.add(this.componentModel);
        if (this.componentSerial != null) {
            vec.add(new DERTaggedObject(false, 0, this.componentSerial));
        }
        if (this.componentRevision != null) {
            vec.add(new DERTaggedObject(false, 1, this.componentRevision));
        }
        if (this.componentManufacturerId != null) {
            vec.add(new DERTaggedObject(false, 2, this.componentManufacturerId));
        }
        if (this.fieldReplaceable != null) {
            vec.add(new DERTaggedObject(false, 3, this.fieldReplaceable));
        }
        if (this.componentAddresses != null) {
            vec.add(new DERTaggedObject(false, 4, new DERSequence(ASN1Utils.toASN1EncodableVector(this.componentAddresses))));
        }
        if (this.componentPlatformCert != null) {
            vec.add(new DERTaggedObject(false, 5, this.componentPlatformCert));
        }
        if (this.componentPlatformCertUri != null) {
            vec.add(new DERTaggedObject(false, 6, this.componentPlatformCertUri));
        }
        if (this.status != null) {
            vec.add(new DERTaggedObject(false, 7, this.status));
        }
        return new DERSequence(vec);
    }

    /**
     * The rest of this builder is generated by lombok Builder annotation
     */
    public static class ComponentIdentifierV2Builder {
        /**
         * Reads elements of the given sequence as ComponentAddresses and adds them to the builder.
         * @param seq ASN1Sequence
         */
        public final void componentAddressesFromSequence(@NonNull ASN1Sequence seq) {
            Arrays.asList(seq.toArray()).forEach(
                    element ->
                            this.componentAddress(ComponentAddress.getInstance(element)));
        }
    }
}
