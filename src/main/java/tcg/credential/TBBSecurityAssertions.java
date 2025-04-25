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
 * tBBSecurityAssertions ATTRIBUTE ::= {
 *      WITH SYNTAX TBBSecurityAssertions
 *      ID tcg-at-tbbSecurityAssertions }
 * 
 * TBBSecurityAssertions ::= SEQUENCE {
 *      version Version DEFAULT v1,
 *      ccInfo [0] IMPLICIT CommonCriteriaMeasures OPTIONAL,
 *      fipsLevel [1] IMPLICIT FIPSLevel OPTIONAL,
 *      rtmType [2] IMPLICIT MeasurementRootType OPTIONAL,
 *      iso9000Certified BOOLEAN DEFAULT FALSE,
 *      iso9000Uri IA5STRING (SIZE (1..URIMAX) OPTIONAL }
 *      
 * Version ::= INTEGER { v1(0) }
 * </pre>
 */
public class TBBSecurityAssertions extends ASN1Object {
	// literally, every element of this object is either default or optional
	//  -- it's theoretically possible to have a valid TBBSecurityAssertions that contains nothing
	ASN1Integer version = new ASN1Integer(1); // default = 1
	CommonCriteriaMeasures ccInfo = null; // optional, tagged 0
	FIPSLevel fipsLevel = null; // optional, tagged 1
	MeasurementRootType rtmType = null; // optional, tagged 2
	ASN1Boolean iso9000Certified = ASN1Boolean.FALSE; // default = false
	DERIA5String iso9000Uri = null; // optional
	
	public static TBBSecurityAssertions getInstance(Object obj) {
		if (obj == null || obj instanceof TBBSecurityAssertions) {
			return (TBBSecurityAssertions) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new TBBSecurityAssertions((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	private TBBSecurityAssertions(ASN1Sequence seq) {
		if (seq.size() < 0 || seq.size() > 6) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		int pos = 0;
		if (((elements.length - pos) > 0) && elements[pos] instanceof ASN1Integer) {
			version = (ASN1Integer) elements[pos];
			pos++;
		}
		if (((elements.length - pos) > 0) && elements[pos] instanceof CommonCriteriaMeasures) {
			ccInfo = (CommonCriteriaMeasures) elements[pos];
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 0)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof CommonCriteriaMeasures ccm) {
				ccInfo = ccm;
			} else {
				throw new IllegalArgumentException("Expected CommonCriteriaMeasures object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 1)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof FIPSLevel fl) {
				fipsLevel = fl;
			} else {
				throw new IllegalArgumentException("Expected FIPSLevel object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 2)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof MeasurementRootType mrt) {
				rtmType = mrt;
			} else {
				throw new IllegalArgumentException("Expected MeasurementRootType object, but received " + elements[pos].getClass().getName());
			}
			pos++;
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1Boolean)) {
			iso9000Certified = (ASN1Boolean) elements[pos];
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
			throw new IllegalArgumentException("Too many elements in TBBSecurityAssertions");
		}
	}
	
	public TBBSecurityAssertions (ASN1Integer version, 
			CommonCriteriaMeasures ccInfo, 
			FIPSLevel fipsLevel, 
			MeasurementRootType rtmType, 
			ASN1Boolean iso9000Certified, 
			DERIA5String iso9000Uri) {
		if (iso9000Uri != null && iso9000Uri.toString().length() > Definitions.URIMAX) {
			throw new IllegalArgumentException("Length of iso9000Uri exceeds URIMAX");
		}
		this.ccInfo = ccInfo;
		this.fipsLevel = fipsLevel;
		this.rtmType = rtmType;
		this.iso9000Certified = iso9000Certified;
		this.iso9000Uri = iso9000Uri;
	}
	
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		if (version.getValue().longValue() != 1) {
			vec.add(version);
		}
		if (ccInfo != null) {
			vec.add(new DERTaggedObject(false, 0, ccInfo));
		}
		if (fipsLevel != null) {
			vec.add(new DERTaggedObject(false, 1, fipsLevel));
		}
		if (rtmType != null) {
			vec.add(new DERTaggedObject(false, 2, rtmType));
		}
		if (iso9000Certified.isTrue()) {
			vec.add(iso9000Certified);
		}
		if (iso9000Uri != null) {
			vec.add(iso9000Uri);
		}
		return new DERSequence(vec);
	}

	public ASN1Integer getVersion() {
		return version;
	}

	public CommonCriteriaMeasures getCcInfo() {
		return ccInfo;
	}

	public FIPSLevel getFipsLevel() {
		return fipsLevel;
	}

	public MeasurementRootType getRtmType() {
		return rtmType;
	}

	public ASN1Boolean getIso9000Certified() {
		return iso9000Certified;
	}

	public DERIA5String getIso9000Uri() {
		return iso9000Uri;
	}

}
