package tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>{@code
 * tCGPlatformSpecification ATTRIBUTE ::= {
 *      WITH SYNTAX TCGPlatformSpecification
 *      ID tcg-at-tcgPlatformSpecification }
 *
 * TCGPlatformSpecification ::= SEQUENCE {
 *      Version TCGSpecificationVersion,
 *      platformClass OCTET STRING SIZE(4) }
 * }</pre>
 */
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@JsonClassDescription("TCG platform specification identifier with version and four-octet platform class.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class TCGPlatformSpecification extends ASN1Object {
	private static final int SEQUENCE_SIZE = 2;
	private static final int PLATFORMCLASS_OCTET_SIZE = 4;

	@JsonPropertyDescription("TCG specification version.")
	@NonNull
	@NotNull
	private final TCGSpecificationVersion version;
	@JsonPropertyDescription("Four-octet platform class value.")
	@NonNull
	@NotNull
	private final ASN1OctetString platformClass;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return {@link TCGPlatformSpecification}
	 */
	public static final TCGPlatformSpecification getInstance(Object obj) {
		if (obj == null || obj instanceof TCGPlatformSpecification) {
			return (TCGPlatformSpecification) obj;
		}
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return {@link TCGPlatformSpecification}
	 */
	public static final TCGPlatformSpecification fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() != TCGPlatformSpecification.SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		TCGPlatformSpecification.TCGPlatformSpecificationBuilder builder = TCGPlatformSpecification.builder()
				.version(TCGSpecificationVersion.getInstance(untaggedElements.get(0)))
				.platformClass(ASN1OctetString.getInstance(untaggedElements.get(1)));

		return builder.build();
	}

	/**
	 * Create a new object. Resize the platformClass to fit the required octet length.
	 * @param version OID
	 * @param platformClass octets
	 */
	public TCGPlatformSpecification(@NonNull TCGSpecificationVersion version, @NonNull ASN1OctetString platformClass) {
		this.version = version;
		this.platformClass = ASN1Utils.resizeOctets(PLATFORMCLASS_OCTET_SIZE, platformClass); // should affect builder as well
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(this.version);
		vec.add(this.platformClass);
		return new DERSequence(vec);
	}
}
