package tcg.credential;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
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
public class VirtualPlatformBackupServiceURI extends ASN1Object {
	
	// minimum 1, maximum 2
	ASN1Boolean restoreAllowed = ASN1Boolean.FALSE; // default false
	DERIA5String backupServiceURI;
	
	public static VirtualPlatformBackupServiceURI getInstance(Object obj) {
		if (obj == null || obj instanceof VirtualPlatformBackupServiceURI) {
			return (VirtualPlatformBackupServiceURI) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new VirtualPlatformBackupServiceURI((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private VirtualPlatformBackupServiceURI(ASN1Sequence seq) {
		if (seq.size() < 1 || seq.size() > 2) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		int pos = 0;
		if (elements[pos] instanceof ASN1Boolean) {
			restoreAllowed = (ASN1Boolean) elements[pos];
			pos++;
		} else {
			throw new IllegalArgumentException("Expected ASN1Boolean, received " + elements[pos].getClass().getName());
		}
		if (elements[pos] instanceof DERIA5String) {
			backupServiceURI = (DERIA5String) elements[pos];
			pos++;
		} else {
			throw new IllegalArgumentException("Expected DERIA5String, received " + elements[pos].getClass().getName());
		}
		if ((elements.length - pos) > 0) {
			throw new IllegalArgumentException("Too many elements in VirtualPlatformBackupServiceURI");
		}
	}

	public VirtualPlatformBackupServiceURI(ASN1Boolean restoreAllowed, DERIA5String backupServiceURI) {
		this.restoreAllowed = restoreAllowed;
		this.backupServiceURI = backupServiceURI;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		if (restoreAllowed.isTrue()) {
			vec.add(restoreAllowed);
		}
		vec.add(backupServiceURI);
		return new DERSequence(vec);
	}

	public ASN1Boolean getRestoreAllowed() {
		return restoreAllowed;
	}

	public DERIA5String getBackupServiceURI() {
		return backupServiceURI;
	}
}
