package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import json.ComponentClassValueDeserializer;
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
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * ComponentClass ::= SEQUENCE {
 *      componentClassRegistry ComponentClassRegistry,
 *      componentClassValue OCTET STRING SIZE(4) }
 *
 * ComponentClassRegistry ::= OBJECT IDENTIFIER ( tcg-registry-componentClass-tcg | tcg-registry-componentClass-ietf | tcg-registry-componentClass-dmtf )
 * </pre>
 */
// Not using @AllArgsConstructor, Needed custom
@Builder(toBuilder = true)
@Getter
@Jacksonized
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
public class ComponentClass extends ASN1Object {
    private static final int SEQUENCE_SIZE = 2;
    /**
     * The number of octets that should be in componentClassValue.
     */
    public static final int VALUE_SIZE = 4;

    @NonNull
    private final ASN1ObjectIdentifier componentClassRegistry;
    @JsonDeserialize(using = ComponentClassValueDeserializer.class)
    @NonNull
    private final ASN1OctetString componentClassValue;

    /**
     * Attempts to cast the provided object.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return ComponentClass
     */
    public static ComponentClass getInstance(Object obj) {
        if (obj == null || obj instanceof ComponentClass) {
            return (ComponentClass) obj;
        }
        if (obj instanceof ASN1Sequence seq) {
            return ComponentClass.fromASN1Sequence(seq);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return ComponentClass
     */
    public static final ComponentClass fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() != ComponentClass.SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        ASN1Object[] elements = (ASN1Object[]) seq.toArray();

        ComponentClass.ComponentClassBuilder builder = ComponentClass.builder()
                .componentClassRegistry(ASN1ObjectIdentifier.getInstance(elements[0]))
                .componentClassValue(DEROctetString.getInstance(elements[1]));

        return builder.build();
    }

    /**
     * Create a new object. Resize the value to fit the required octet length.
     * @param componentClassRegistry OID
     * @param componentClassValue octets
     */
    public ComponentClass(@NonNull ASN1ObjectIdentifier componentClassRegistry, @NonNull ASN1OctetString componentClassValue) {
        this.componentClassRegistry = componentClassRegistry;
        this.componentClassValue = ASN1Utils.resizeOctets(VALUE_SIZE, componentClassValue); // should affect builder as well
    }

    /**
     * @return This object as an ASN1Sequence
     */
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        vec.add(this.componentClassRegistry);
        vec.add(this.componentClassValue);
        return new DERSequence(vec);
    }
}
