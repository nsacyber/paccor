package tcg.credential;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>
 * tPMSecurityAssertions ATTRIBUTE ::= {
 *      WITH SYNTAX TPMSecurityAssertions
 *      ID tcg-at-tpmSecurityAssertions }
 * 
 * TPMSecurityAssertions ::= SEQUENCE {
 *      version Version DEFAULT v1,
 *      fieldUpgradable BOOLEAN DEFAULT FALSE,
 *      ekGenerationType [0] IMPLICIT EKGenerationType OPTIONAL,
 *      ekGenerationLocation [1] IMPLICIT EKGenerationLocation OPTIONAL,
 *      ekCertificateGenerationLocation [2] IMPLICIT EKCertificateGenerationLocation OPTIONAL,
 *      ccInfo [3] IMPLICIT CommonCriteriaMeasures OPTIONAL,
 *      fipsLevel [4] IMPLICIT FIPSLevel OPTIONAL,
 *      iso9000Certified [5] IMPLICIT BOOLEAN DEFAULT FALSE,
 *      iso9000Uri IA5STRING (SIZE (1..URIMAX)) OPTIONAL }
 * 
 * Version ::= INTEGER { v1(0) }
 * </pre>
 */
public class TPMSecurityAssertions extends ASN1Object {
	
	// minimum 0, max 9
	ASN1Integer version = new ASN1Integer(0); // default 0
	ASN1Boolean fieldUpgradable = ASN1Boolean.FALSE; // default false
	EKGenerationType ekGenerationType = null; // optional, tagged 0
	EKGenerationLocation ekGenerationLocation = null; // optional, tagged 1
	EKCertificateGenerationLocation ekCertificateGenerationLocation = null; // optional, tagged 2
	CommonCriteriaMeasures ccInfo = null; // optional, tagged 3
	FIPSLevel fipsLevel = null; // optional, tagged 4
	ASN1Boolean iso9000Certified = ASN1Boolean.FALSE; // default false
	DERIA5String iso9000Uri = null; // optional, not tagged
	
	public static TPMSecurityAssertions getInstance(Object obj) {
		if (obj == null || obj instanceof TPMSecurityAssertions) {
			return (TPMSecurityAssertions) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new TPMSecurityAssertions((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}
	
	private TPMSecurityAssertions(ASN1Sequence seq) {
		if (seq.size() < 0 || seq.size() > 9) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		int pos = 0;
		if (((elements.length - pos) > 0) && elements[pos] instanceof ASN1Integer) {
			version = (ASN1Integer) elements[pos];
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1Boolean)) {
			fieldUpgradable = (ASN1Boolean) elements[pos];
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 0)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof EKGenerationType ekgt) {
				ekGenerationType = ekgt;
			} else {
				throw new IllegalArgumentException("Expected EKGenerationType object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 1)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof EKGenerationLocation ekgl) {
				ekGenerationLocation = ekgl;
			} else {
				throw new IllegalArgumentException("Expected EKGenerationLocation object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 2)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof EKCertificateGenerationLocation ekcgl) {
				ekCertificateGenerationLocation = ekcgl;
			} else {
				throw new IllegalArgumentException("Expected EKCertificateGenerationLocation object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 3)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof CommonCriteriaMeasures ccm) {
				ccInfo = ccm;
			} else {
				throw new IllegalArgumentException("Expected CommonCriteriaMeasures object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 4)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof FIPSLevel fl) {
				fipsLevel = fl;
			} else {
				throw new IllegalArgumentException("Expected FIPSLevel object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 5)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof ASN1Boolean bool) {
				iso9000Certified = bool;
			} else {
				throw new IllegalArgumentException("Expected ASN1Boolean object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof DERIA5String)) {
			iso9000Uri = (DERIA5String) elements[pos];
			if (iso9000Uri.toString().length() > Definitions.URIMAX) {
				throw new IllegalArgumentException("Length of iso9000Uri exceeds URIMAX");
			}
			pos++;
		}
		if ((elements.length - pos) > 0) {
			throw new IllegalArgumentException("Too many elements in TPMSecurityAssertions");
		}
	}

	public TPMSecurityAssertions(ASN1Integer version, ASN1Boolean fieldUpgradable, EKGenerationType ekGenerationType,
			EKGenerationLocation ekGenerationLocation, EKCertificateGenerationLocation ekCertificateGenerationLocation,
			CommonCriteriaMeasures ccInfo, FIPSLevel fipsLevel, ASN1Boolean iso9000Certified, DERIA5String iso9000Uri) {
		if (iso9000Uri.toString().length() > Definitions.URIMAX) {
			throw new IllegalArgumentException("Length of iso9000Uri exceeds URIMAX");
		}
		this.version = version;
		this.fieldUpgradable = fieldUpgradable;
		this.ekGenerationType = ekGenerationType;
		this.ekGenerationLocation = ekGenerationLocation;
		this.ekCertificateGenerationLocation = ekCertificateGenerationLocation;
		this.ccInfo = ccInfo;
		this.fipsLevel = fipsLevel;
		this.iso9000Certified = iso9000Certified;
		this.iso9000Uri = iso9000Uri;
	}

	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		if (version.getValue().intValue() != 0) {
			vec.add(version);
		}
		if (fieldUpgradable.isTrue()) {
			vec.add(fieldUpgradable);
		}
		if (ekGenerationType != null) {
			vec.add(new DERTaggedObject(false, 0, ekGenerationType));
		}
		if (ekGenerationLocation != null) {
			vec.add(new DERTaggedObject(false, 1, ekGenerationLocation));
		}
		if (ekCertificateGenerationLocation != null) {
			vec.add(new DERTaggedObject(false, 2, ekCertificateGenerationLocation));
		}
		if (ccInfo != null) {
			vec.add(new DERTaggedObject(false, 3, ccInfo));
		}
		if (fipsLevel != null) {
			vec.add(new DERTaggedObject(false, 4, fipsLevel));
		}
		if (iso9000Certified.isTrue()) {
			vec.add(new DERTaggedObject(false, 5, iso9000Certified));
		}
		if (iso9000Uri != null) {
			vec.add(iso9000Uri);
		}
		return new DERSequence(vec);
	}

	public ASN1Integer getVersion() {
		return version;
	}

	public ASN1Boolean getFieldUpgradable() {
		return fieldUpgradable;
	}

	public EKGenerationType getEkGenerationType() {
		return ekGenerationType;
	}

	public EKGenerationLocation getEkGenerationLocation() {
		return ekGenerationLocation;
	}

	public EKCertificateGenerationLocation getEkCertificateGenerationLocation() {
		return ekCertificateGenerationLocation;
	}

	public CommonCriteriaMeasures getCcInfo() {
		return ccInfo;
	}

	public FIPSLevel getFipsLevel() {
		return fipsLevel;
	}

	public ASN1Boolean getIso9000Certified() {
		return iso9000Certified;
	}

	public DERIA5String getIso9000Uri() {
		return iso9000Uri;
	}
}
