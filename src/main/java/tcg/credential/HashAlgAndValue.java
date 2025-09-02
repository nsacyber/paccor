package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
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
 * HashAlgAndValue ::= SEQUENCE {
 *      hashAlg AlgorithmIdentifier,
 *      hashValue OCTET STRING }
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class HashAlgAndValue extends ASN1Object {
	private static final int SEQUENCE_SIZE = 2;

	@NonNull
	private final ASN1ObjectIdentifier hashAlg;
	@NonNull
	private final ASN1OctetString hashValue;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return HashAlgAndValue
	 */
	public static HashAlgAndValue getInstance(Object obj) {
		if (obj == null || obj instanceof HashAlgAndValue) {
			return (HashAlgAndValue) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return HashAlgAndValue.fromASN1Sequence(seq);
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

		ASN1Object[] elements = (ASN1Object[]) seq.toArray();

		HashAlgAndValue.HashAlgAndValueBuilder builder = HashAlgAndValue.builder()
				.hashAlg(ASN1ObjectIdentifier.getInstance(elements[0]))
				.hashValue(DEROctetString.getInstance(elements[1]));

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
