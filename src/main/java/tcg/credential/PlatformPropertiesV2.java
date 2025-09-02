package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * Class name is "PlatformProperties" as opposed to TCG given name of "Properties"
 * to limit potential collision issues with built-in Java class named Properties.
 *
 * <pre>
 * Properties ::= SEQUENCE {
 *      propertyName UTF8String (SIZE (1..STRMAX)),
 *      propertyValue UTF8String (SIZE (1..STRMAX)),
 *      status [0] IMPLICIT AttributeStatus OPTIONAL }
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class PlatformPropertiesV2 extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 2;
	private static final int MAX_SEQUENCE_SIZE = 3;

	@NonNull
	private final ASN1UTF8String propertyName;
	@NonNull
	private final ASN1UTF8String propertyValue;
	private final AttributeStatus status; // optional, tagged 0

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return PlatformPropertiesV2
	 */
	public static PlatformPropertiesV2 getInstance(Object obj) {
		if (obj == null || obj instanceof PlatformPropertiesV2) {
			return (PlatformPropertiesV2) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return PlatformPropertiesV2.fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return PlatformPropertiesV2
	 */
	public static final PlatformPropertiesV2 fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < PlatformPropertiesV2.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		ASN1Object[] elements = (ASN1Object[]) seq.toArray();

		PlatformPropertiesV2.PlatformPropertiesV2Builder builder = PlatformPropertiesV2.builder()
				.propertyName(DERUTF8String.getInstance(elements[0]))
				.propertyValue(DERUTF8String.getInstance(elements[1]));

		ASN1Utils.parseTaggedElements(seq).forEach((key, value) -> {
			if (key == 0) {
				builder.status(AttributeStatus.getInstance(value));
			}
		});

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(propertyName);
		vec.add(propertyValue);
		if (status != null) {
			vec.add(new DERTaggedObject(false, 0, status));
		}
		return new DERSequence(vec);
	}
}
