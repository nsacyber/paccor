package tcg.credential;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>{@code
 * HashAlgAndValue ::= SEQUENCE {
 *      hashAlg AlgorithmIdentifier,
 *      hashValue OCTET STRING }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@NoArgsConstructor(force = true)
public class HashAlgAndValue extends ASN1Object {
	private static final int SEQUENCE_SIZE = 2;

	@NonNull
	@NotNull
	private final ASN1ObjectIdentifier hashAlg;
	@NonNull
	@NotNull
	private final ASN1OctetString hashValue;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return HashAlgAndValue
	 */
	public static final HashAlgAndValue getInstance(Object obj) {
		if (obj == null || obj instanceof HashAlgAndValue) {
			return (HashAlgAndValue) obj;
		}
		if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
			return HashAlgAndValue.fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return HashAlgAndValue
	 */
	public static final HashAlgAndValue fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() != SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		HashAlgAndValue.HashAlgAndValueBuilder builder = HashAlgAndValue.builder()
				.hashAlg(ASN1Utils.getOID(untaggedElements.get(0)))
				.hashValue(ASN1Utils.getOctetString(untaggedElements.get(1)));

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(hashAlg);
		vec.add(hashValue);
		return new DERSequence(vec);
	}
}
