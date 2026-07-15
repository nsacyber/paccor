package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>{@code
 * ISO3166CountryCode ::= PrintableString (SIZE(2..3))
 * ISO3166AdminSubdivisionCode ::= PrintableString (SIZE (3..6))
 * OpenLocationCode ::= PrintableString
 * EntityGeoLocation ::= SEQUENCE {
 *      countryCode ISO3166CountryCode,
 *      stateOrProvince [0] IMPLICIT ISO3166AdminSubdivisionCode OPTIONAL,
 *      localityName [1] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      streetAddress [2] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL,
 *      locationCoords [3] IMPLICIT OpenLocationCode OPTIONAL,
 *      postalCode [5] IMPLICIT UTF8String (SIZE (1..STRMAX)) OPTIONAL }
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
public class EntityGeoLocation extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 1;
    private static final int MAX_SEQUENCE_SIZE = 6;

    @NonNull
    @NotNull
    private final ASN1PrintableString countryCode;
    private final ASN1PrintableString stateOrProvince; // optional, tagged 0
    private final ASN1UTF8String localityName; // optional, tagged 1
    private final ASN1UTF8String streetAddress; // optional, tagged 2
    private final ASN1PrintableString locationCoords; // optional, tagged 3
    private final ASN1UTF8String postalCode; // optional, tagged 4

    /**
     * Attempts to cast the provided object.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return EntityGeoLocation
     */
    public static final EntityGeoLocation getInstance(Object obj) {
        if (obj == null || obj instanceof EntityGeoLocation) {
            return (EntityGeoLocation) obj;
        }
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return EntityGeoLocation.fromASN1Sequence(ASN1Utils.getSequence(obj));
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return EntityGeoLocation
     */
    public static final EntityGeoLocation fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < EntityGeoLocation.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

        EntityGeoLocation.EntityGeoLocationBuilder builder = EntityGeoLocation.builder()
                .countryCode(ASN1PrintableString.getInstance(untaggedElements.get(0)));

        ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
            switch (key) {
                case 0 -> builder.stateOrProvince(ASN1Utils.getPrintableString(value));
                case 1 -> builder.localityName(ASN1Utils.getUTF8String(value));
                case 2 -> builder.streetAddress(ASN1Utils.getUTF8String(value));
                case 3 -> builder.locationCoords(ASN1Utils.getPrintableString(value));
                case 4, 5 -> builder.postalCode(ASN1Utils.getUTF8String(value));
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
        vec.add(this.countryCode);
        if (this.stateOrProvince != null) {
            vec.add(new DERTaggedObject(false, 0, stateOrProvince));
        }
        if (this.localityName != null) {
            vec.add(new DERTaggedObject(false, 1, localityName));
        }
        if (this.streetAddress != null) {
            vec.add(new DERTaggedObject(false, 2, streetAddress));
        }
        if (this.locationCoords != null) {
            vec.add(new DERTaggedObject(false, 3, locationCoords));
        }
        if (this.postalCode != null) {
            vec.add(new DERTaggedObject(false, 5, postalCode));
        }
        return new DERSequence(vec);
    }
}
