package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.NotNull;
import java.math.BigInteger;
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
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>{@code
 * tCGCredentialSpecification ATTRIBUTE ::= {
 *      WITH SYNTAX TCGSpecificationVersion
 *      ID tcg-at-tcgCredentialSpecification }
 *
 * TCGSpecificationVersion ::= SEQUENCE {
 *      majorVersion INTEGER,
 *      minorVersion INTEGER,
 *      revision INTEGER }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@Jacksonized
@JsonClassDescription("Three-part version number consisting of major, minor, and revision integers.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class TCGSpecificationVersion extends ASN1Object {
	private static final int SEQUENCE_SIZE = 3;

	@JsonPropertyDescription("Major version number.")
	@NotNull
	@NonNull
	private final ASN1Integer majorVersion;
	@JsonPropertyDescription("Minor version number.")
	@NotNull
	@NonNull
	private final ASN1Integer minorVersion;
	@JsonPropertyDescription("Revision number.")
	@NotNull
	@NonNull
	private final ASN1Integer revision;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return {@link TCGSpecificationVersion}
	 */
	public static final TCGSpecificationVersion getInstance(Object obj) {
		if (obj == null || obj instanceof TCGSpecificationVersion) {
			return (TCGSpecificationVersion) obj;
		}
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return {@link TCGSpecificationVersion}
	 */
	public static final TCGSpecificationVersion fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() != TCGSpecificationVersion.SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		TCGSpecificationVersion.TCGSpecificationVersionBuilder builder = TCGSpecificationVersion.builder()
				.majorVersion(ASN1Integer.getInstance(untaggedElements.get(0)))
				.minorVersion(ASN1Integer.getInstance(untaggedElements.get(1)))
				.revision(ASN1Integer.getInstance(untaggedElements.get(2)));

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(this.majorVersion);
		vec.add(this.minorVersion);
		vec.add(this.revision);
		return new DERSequence(vec);
	}

	public String describe() {
		BigInteger major = this.majorVersion.getValue();
		BigInteger minor = this.minorVersion.getValue();
		BigInteger revision = this.revision.getValue();
		return major + "." + minor + "." + revision;
	}
}
