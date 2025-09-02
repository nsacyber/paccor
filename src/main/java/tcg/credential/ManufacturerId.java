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
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>{@code
 * PlatformManufacturerId ATTRIBUTE ::= {
 *      WITH SYNTAX ManufacturerId
 *      ID tcg-at-platformManufacturerId }
 *
 * ManufacturerId ::= SEQUENCE {
 *      manufacturerIdentifier PrivateEnterpriseNumber }
 *
 * enterprise OBJECT IDENTIFIER :: = {iso(1) identified-organization(3) dod(6) internet(1) private(4) enterprise(1)}
 *
 * PrivateEnterpriseNumber OBJECT IDENTIFIER :: = { enterprise private-enterprise-number }
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@NoArgsConstructor(force = true)
public class ManufacturerId extends ASN1Object {
	private static final int SEQUENCE_SIZE = 1;

	@NonNull
	@NotNull
	private final ASN1ObjectIdentifier manufacturerIdentifier;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return ManufacturerId
	 */
	public static final ManufacturerId getInstance(Object obj) {
		if (obj == null || obj instanceof ManufacturerId) {
			return (ManufacturerId) obj;
		}
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
			return ManufacturerId.fromASN1Sequence(ASN1Utils.getSequence(obj));
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return ManufacturerId
	 */
	public static final ManufacturerId fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() != SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

        List<ASN1Object> untaggedElements = ASN1Utils.listUntaggedElements(seq);

		ManufacturerId.ManufacturerIdBuilder builder = ManufacturerId.builder()
				.manufacturerIdentifier(ASN1Utils.getOID(untaggedElements.getFirst()));

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(manufacturerIdentifier);
		return new DERSequence(vec);
	}
}
