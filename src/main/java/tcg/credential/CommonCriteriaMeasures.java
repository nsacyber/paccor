package tcg.credential;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERTaggedObject;

/**
 * <pre>
 * CommonCriteriaMeasures ::= SEQUENCE {
 *      version IA5STRING (SIZE (1..STRMAX)), -- 2.2 or 3.1; future syntax defined by CC
 *      assuranceLevel EvaluationAssuranceLevel,
 *      evaluationStatus EvaluationStatus,
 *      plus BOOLEAN DEFAULT FALSE,
 *      strengthOfFunction [0] IMPLICIT StrengthOfFunction OPTIONAL,
 *      profileOid [1] IMPLICIT OBJECT IDENTIFIER OPTIONAL,
 *      profileUri [2] IMPLICIT URIReference OPTIONAL,
 *      targetOid [3] IMPLICIT OBJECT IDENTIFIER OPTIONAL,
 *      targetUri [4] IMPLICIT URIReference OPTIONAL }
 * </pre>
 */
public class CommonCriteriaMeasures extends ASN1Object {
	// Must be 3 or more elements
	DERIA5String version;
	EvaluationAssuranceLevel assuranceLevel;
	EvaluationStatus evaluationStatus;
	ASN1Boolean plus = ASN1Boolean.FALSE; // default false
	StrengthOfFunction strengthOfFunction = null; // optional, tagged 0
	ASN1ObjectIdentifier profileOid = null; // optional , tagged 1
	URIReference profileUri = null; // optional, tagged 2
	ASN1ObjectIdentifier targetOid = null; // optional, tagged 3
	URIReference targetUri = null; // optional, tagged 4

	public static CommonCriteriaMeasures getInstance(Object obj) {
		if (obj == null || obj instanceof TBBSecurityAssertions) {
			return (CommonCriteriaMeasures) obj;
		}
		if (obj instanceof ASN1Sequence) {
			return new CommonCriteriaMeasures((ASN1Sequence)obj);
		}
		throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
	}

	private CommonCriteriaMeasures(ASN1Sequence seq) {
		if (seq.size() < 3 || seq.size() > 9) {
			throw new IllegalArgumentException("Bad sequence size: " + seq.size());
		}
		ASN1Object[] elements = (ASN1Object[]) seq.toArray();
		int pos = 0;
		if (elements[pos] instanceof DERIA5String) {
			version = (DERIA5String) elements[pos];
			if (version.toString().length() > Definitions.STRMAX) {
				throw new IllegalArgumentException("Length of version exceeds STRMAX");
			}
			pos++;
		} else {
			throw new IllegalArgumentException("Expected DERIA5String, received " + elements[pos].getClass().getName());
		}
		if (elements[pos] instanceof EvaluationAssuranceLevel) {
			assuranceLevel = (EvaluationAssuranceLevel)elements[pos];
			pos++;
		} else {
			throw new IllegalArgumentException("Expected EvaluationAssuranceLevel, received " + elements[pos].getClass().getName());
		}
		if (elements[pos] instanceof EvaluationStatus) {
			evaluationStatus = (EvaluationStatus)elements[pos];
			pos++;
		} else {
			throw new IllegalArgumentException("Expected EvaluationStatus, received " + elements[pos].getClass().getName());
		}
		if (((elements.length - pos) > 0) && elements[pos] instanceof ASN1Boolean) {
			plus = (ASN1Boolean)elements[pos];
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 0)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof StrengthOfFunction sof) {
				strengthOfFunction = sof;
			} else {
				throw new IllegalArgumentException("Expected StrengthOfFunction, received " + elementObject.getClass().getName());
			}
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 1)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof ASN1ObjectIdentifier oid) {
				profileOid = oid;
			} else {
				throw new IllegalArgumentException("Expected ASN1ObjectIdentifier, received " + elementObject.getClass().getName());
			}
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 2)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof URIReference uriRef) {
				profileUri = uriRef;
			} else {
				throw new IllegalArgumentException("Expected URIReference, received " + elementObject.getClass().getName());
			}
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 3)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof ASN1ObjectIdentifier oid) {
				targetOid = oid;
			} else {
				throw new IllegalArgumentException("Expected ASN1ObjectIdentifier, received " + elementObject.getClass().getName());
			}
		}
		if (((elements.length - pos) > 0) && (elements[pos] instanceof ASN1TaggedObject taggedElement) && (taggedElement.getTagNo() == 4)) {
			ASN1Object elementObject = taggedElement.getBaseUniversal(taggedElement.isExplicit(), taggedElement.getTagNo());
			if (elementObject instanceof URIReference uriRef) {
				targetUri = uriRef;
			} else {
				throw new IllegalArgumentException("Expected URIReference, received " + elementObject.getClass().getName());
			}
		}
	}
	
	public CommonCriteriaMeasures(DERIA5String version, EvaluationAssuranceLevel assurancelevel,
			EvaluationStatus evaluationStatus, ASN1Boolean plus, StrengthOfFunction strengthOfFunction,
			ASN1ObjectIdentifier profileOid, URIReference profileUri, ASN1ObjectIdentifier targetOid,
			URIReference targetUri) {
		if (version.toString().length() > Definitions.STRMAX) {
			throw new IllegalArgumentException("Length of version exceeds STRMAX");
		}
		this.version = version;
		this.assuranceLevel = assurancelevel;
		this.evaluationStatus = evaluationStatus;
		this.plus = plus;
		this.strengthOfFunction = strengthOfFunction;
		this.profileOid = profileOid;
		this.profileUri = profileUri;
		this.targetOid = targetOid;
		this.targetUri = targetUri;
	}
	
	public ASN1Primitive toASN1Primitive() {
		ASN1EncodableVector vec = new ASN1EncodableVector();
		vec.add(version);
		vec.add(assuranceLevel);
		vec.add(evaluationStatus);
		if (plus.isTrue()) {
			vec.add(plus);
		}
		if (strengthOfFunction != null) {
			vec.add(new DERTaggedObject(false, 0, strengthOfFunction));
		}
		if (profileOid != null) {
			vec.add(new DERTaggedObject(false, 1, profileOid));
		}
		if (profileUri != null) {
			vec.add(new DERTaggedObject(false, 2, profileUri));
		}
		if (targetOid != null) {
			vec.add(new DERTaggedObject(false, 3, targetOid));
		}
		if (targetUri != null) {
			vec.add(new DERTaggedObject(false, 4, targetUri));
		}
		return new DERSequence(vec);
	}
	
	public DERIA5String getVersion() {
		return version;
	}

	public EvaluationAssuranceLevel getAssuranceLevel() {
		return assuranceLevel;
	}

	public EvaluationStatus getEvaluationStatus() {
		return evaluationStatus;
	}

	public ASN1Boolean getPlus() {
		return plus;
	}

	public StrengthOfFunction getStrengthOfFunction() {
		return strengthOfFunction;
	}

	public ASN1ObjectIdentifier getProfileOid() {
		return profileOid;
	}

	public URIReference getProfileUri() {
		return profileUri;
	}

	public ASN1ObjectIdentifier getTargetOid() {
		return targetOid;
	}

	public URIReference getTargetUri() {
		return targetUri;
	}
}
