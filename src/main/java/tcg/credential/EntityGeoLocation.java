package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * <pre>
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
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class EntityGeoLocation extends ASN1Object {
    private static final int MIN_SEQUENCE_SIZE = 1;
    private static final int MAX_SEQUENCE_SIZE = 6;

    @NonNull
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
    public static EntityGeoLocation getInstance(Object obj) {
        if (obj == null || obj instanceof EntityGeoLocation) {
            return (EntityGeoLocation) obj;
        }
        if (obj instanceof ASN1Sequence seq) {
            return EntityGeoLocation.fromASN1Sequence(seq);
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return EntityGeoLocation
     */
    public static EntityGeoLocation fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < EntityGeoLocation.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        ASN1Object[] elements = (ASN1Object[]) seq.toArray();

        EntityGeoLocation.EntityGeoLocationBuilder builder = EntityGeoLocation.builder()
                .countryCode(DERPrintableString.getInstance(elements[0]));

        ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
            switch (key) {
                case 0 -> builder.stateOrProvince(DERPrintableString.getInstance(value));
                case 1 -> builder.localityName(DERUTF8String.getInstance(value));
                case 2 -> builder.streetAddress(DERUTF8String.getInstance(value));
                case 3 -> builder.locationCoords(DERPrintableString.getInstance(value));
                case 4, 5 -> builder.postalCode(DERUTF8String.getInstance(value));
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
            vec.add(this.stateOrProvince);
        }
        if (this.localityName != null) {
            vec.add(this.localityName);
        }
        if (this.streetAddress != null) {
            vec.add(this.streetAddress);
        }
        if (this.locationCoords != null) {
            vec.add(this.locationCoords);
        }
        if (this.postalCode != null) {
            vec.add(this.postalCode);
        }
        return new DERSequence(vec);
    }
}
