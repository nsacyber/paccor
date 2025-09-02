package tcg.credential;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * HashedSubjectInfoURI ::= SEQUENCE {
 *      documentURI IA5String (SIZE (1..URIMAX)),
 *      documentAccessInfo OBJECT IDENTIFIER OPTIONAL,
 *      documentHashInfo HashAlgAndValue OPTIONAL }
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class HashedSubjectInfoURI extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 1;
	private static final int MAX_SEQUENCE_SIZE = 3;

	@NonNull
	private final ASN1IA5String documentURI;
	private final ASN1ObjectIdentifier documentAccessInfo; // optional
	private final HashAlgAndValue documentHashInfo; // optional

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return HashedSubjectInfoURI
	 */
	public static HashedSubjectInfoURI getInstance(Object obj) {
		if (obj == null || obj instanceof HashedSubjectInfoURI) {
			return (HashedSubjectInfoURI) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return HashedSubjectInfoURI.fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return HashedSubjectInfoURI
	 */
	public static final HashedSubjectInfoURI fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < HashedSubjectInfoURI.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		ASN1Object[] elements = (ASN1Object[]) seq.toArray();

		HashedSubjectInfoURI.HashedSubjectInfoURIBuilder builder = HashedSubjectInfoURI.builder()
				.documentURI(DERIA5String.getInstance(elements[0]));

		// documentAccessInfo can only be at position 1 if it exists
		Optional.ofNullable(ASN1Utils.safeGetDefaultElementFromSequence(seq, 1, null, ASN1ObjectIdentifier::getInstance))
				.ifPresent(builder::documentAccessInfo);

		// HashInfo could be at position 1 or 2 if present
		Optional.ofNullable(ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 1, 2, null, HashAlgAndValue::getInstance))
				.ifPresent(builder::documentHashInfo);

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(documentURI);
		if (documentAccessInfo != null) {
			vec.add(documentAccessInfo);
		}
		if (documentHashInfo != null) {
			vec.add(documentHashInfo);
		}
		return new DERSequence(vec);
	}
}
