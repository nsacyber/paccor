package tcg.credential;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;

/**
 * <pre>
 * VirtualPlatformBackupServiceURI ::= SEQUENCE {
 *      restoreAllowed BOOLEAN DEFAULT FALSE,
 *      backupServiceURI IA5String }
 * </pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@Getter
@NoArgsConstructor(force = true)
public class VirtualPlatformBackupServiceURI extends ASN1Object {
	private static final int MIN_SEQUENCE_SIZE = 1;
	private static final int MAX_SEQUENCE_SIZE = 2;

	@NonNull
	@Builder.Default
	private final ASN1Boolean restoreAllowed = ASN1Boolean.FALSE; // default false
	@NonNull
	private final ASN1IA5String backupServiceURI;

	/**
	 * Attempts to cast the provided object.
	 * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
	 * @param obj the object to parse
	 * @return VirtualPlatformBackupServiceURI
	 */
	public static VirtualPlatformBackupServiceURI getInstance(Object obj) {
		if (obj == null || obj instanceof VirtualPlatformBackupServiceURI) {
			return (VirtualPlatformBackupServiceURI) obj;
		}
		if (obj instanceof ASN1Sequence seq) {
			return VirtualPlatformBackupServiceURI.fromASN1Sequence(seq);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	/**
	 * Attempts to parse the given ASN1Sequence.
	 * @param seq An ASN1Sequence
	 * @return VirtualPlatformBackupServiceURI
	 */
	public static final VirtualPlatformBackupServiceURI fromASN1Sequence(@NonNull ASN1Sequence seq) {
		if (seq.size() < VirtualPlatformBackupServiceURI.MIN_SEQUENCE_SIZE) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}

		VirtualPlatformBackupServiceURI.VirtualPlatformBackupServiceURIBuilder builder = VirtualPlatformBackupServiceURI.builder();
		builder.restoreAllowed(ASN1Utils.safeGetDefaultElementFromSequence(seq, 0, ASN1Boolean.FALSE, ASN1Boolean::getInstance));
		// uri is required
		builder.backupServiceURI(ASN1Utils.safeGetFirstInstanceFromSequenceGivenRange(seq, 0, 1, null, DERIA5String::getInstance));

		return builder.build();
	}

	/**
	 * @return This object as an ASN1Sequence
	 */
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		if (restoreAllowed.isTrue()) {
			vec.add(restoreAllowed);
		}
		vec.add(backupServiceURI);
		return new DERSequence(vec);
	}
}
