package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import paccor.json.schema.ComponentSchema;
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
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

/**
 * <pre>{@code
 * URIReference ::= SEQUENCE {
 *      uniformResourceIdentifier IA5String (SIZE (1..URIMAX)),
 *      hashAlgorithm AlgorithmIdentifier OPTIONAL,
 *      hashValue BIT STRING OPTIONAL }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@JsonClassDescription("Reference to a URI with optional hash metadata for integrity checking.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class URIReference extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 1;
	private static final int MAX_SEQUENCE_SIZE = 3;

	@JsonPropertyDescription("URI being referenced.")
	@JsonProperty(ComponentSchema.UNIFORM_RESOURCE_IDENTIFIER)
	@NonNull
	@NotNull
	private final ASN1IA5String uniformResourceIdentifier;
	@JsonPropertyDescription("Optional hash algorithm for the referenced content.")
	@JsonProperty(ComponentSchema.HASH_ALGORITHM)
	@JsonAlias(ComponentSchema.HASH_ALG)
	private final AlgorithmIdentifier hashAlgorithm;
	@JsonPropertyDescription("Optional hash value for the referenced content.")
	@JsonProperty(ComponentSchema.HASH_VALUE)
	private final ASN1BitString hashValue;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return URIReference
	 */
	public static final URIReference getInstance(Object obj) {
		if (obj == null || obj instanceof URIReference) {
			return (URIReference) obj;
		}
		/*
		 *  URIReference could be encoded as a direct string instead of a sequence due to the way
		 *  some parsers handle ASN.1 SET.
		 *  <pre>{@code
		 *  Attribute ::= SEQUENCE {
		 *            type      AttributeType,
		 *            values SET OF AttributeValue
		 *              -- at least one value is required
		 *           }
		 *  }</pre>
		 */
		if (obj instanceof ASN1TaggedObject t && t.getBaseObject() instanceof ASN1IA5String s) {
			return new URIReference(s, null, null);
		}
		if (obj instanceof ASN1Sequence || ASN1Utils.isSequence(obj)) {
			return fromASN1Sequence(ASN1Utils.getSequence(obj));
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

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		URIReference.URIReferenceBuilder builder = URIReference.builder()
				.uniformResourceIdentifier(ASN1IA5String.getInstance(untaggedElements.get(0)));

		if (untaggedElements.size() > 1) {
			builder.hashAlgorithm = AlgorithmIdentifier.getInstance(untaggedElements.get(1));
		}

		if (untaggedElements.size() > 2) {
			builder.hashValue = ASN1BitString.getInstance(untaggedElements.get(2));
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
