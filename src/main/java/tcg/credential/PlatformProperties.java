package tcg.credential;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERSequence;

/**
 * The class name is "PlatformProperties" as opposed to TCG given name of "Properties"
 * to limit potential collision issues with any built-in Java class named Properties.
 *
 * <pre>{@code
 * Properties ::= SEQUENCE {
 *      propertyName UTF8String (SIZE (1..STRMAX)),
 *      propertyValue UTF8String (SIZE (1..STRMAX)) }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@NoArgsConstructor(force = true)
public class PlatformProperties extends ASN1Object {
	private static final int SEQUENCE_SIZE = 2;

	@NonNull
	@NotNull
	private final ASN1UTF8String propertyName;
	@NonNull
	@NotNull
	private final ASN1UTF8String propertyValue;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return PlatformProperties
	 */
	public static final PlatformProperties getInstance(Object obj) {
		if (obj == null || obj instanceof PlatformProperties) {
			return (PlatformProperties) obj;
		}
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return PlatformProperties.fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return PlatformProperties
	 */
	public static final PlatformProperties fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() != PlatformProperties.SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		PlatformProperties.PlatformPropertiesBuilder builder = PlatformProperties.builder()
				.propertyName(ASN1UTF8String.getInstance(untaggedElements.get(0)))
				.propertyValue(ASN1UTF8String.getInstance(untaggedElements.get(1)));

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(propertyName);
		vec.add(propertyValue);
		return new DERSequence(vec);
	}
}
