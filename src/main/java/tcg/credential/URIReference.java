package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import json.ASN1BitStringFromBase64Deserializer;
import json.AlgorithmIdentifierDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * <pre>
 * URIReference ::= SEQUENCE {
 *      uniformResourceIdentifier IA5String (SIZE (1..URIMAX)),
 *      hashAlgorithm AlgorithmIdentifier OPTIONAL,
 *      hashValue BIT STRING OPTIONAL }
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
@ToString
public class URIReference extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 1;
	private static final int MAX_SEQUENCE_SIZE = 3;

	@JsonDeserialize(as = DERIA5String.class)
	@NonNull
	private final ASN1IA5String uniformResourceIdentifier;
	@JsonDeserialize(using = AlgorithmIdentifierDeserializer.class)
	private final AlgorithmIdentifier hashAlgorithm;
    @JsonDeserialize(using = ASN1BitStringFromBase64Deserializer.class)
	private final ASN1BitString hashValue;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return URIReference
	 */
	public static URIReference getInstance(Object obj) {
		if (obj == null || obj instanceof URIReference) {
			return (URIReference) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return URIReference.fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return URIReference
	 */
	public static final URIReference fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < URIReference.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		ASN1Object[] elements = (ASN1Object[]) seq.toArray();

		URIReference.URIReferenceBuilder builder = URIReference.builder()
				.uniformResourceIdentifier(DERIA5String.getInstance(elements[0]));

		if (elements.length > 1) {
			builder.hashAlgorithm = AlgorithmIdentifier.getInstance(elements[1]);
		}

		if (elements.length > 2) {
			builder.hashValue = ASN1BitString.getInstance(elements[2]);
		}

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(this.uniformResourceIdentifier);
		if (this.hashAlgorithm != null) {
			vec.add(this.hashAlgorithm);
		}
		if (this.hashValue != null) {
			vec.add(this.hashValue);
		}
		return new DERSequence(vec);
	}
}
