package factory;

import com.fasterxml.jackson.databind.JsonNode;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import tcg.credential.CommonCriteriaMeasures;
import tcg.credential.EvaluationAssuranceLevel;
import tcg.credential.EvaluationStatus;
import tcg.credential.StrengthOfFunction;
import tcg.credential.URIReference;

/**
 * Functions to help manage the creation of the common criteria measures object.
 * See <a href="https://www.commoncriteriaportal.org/cc/">https://www.commoncriteriaportal.org/cc/</a>
 */
public class CommonCriteriaMeasuresFactory {
    /**
     * fields of the common criteria measures JSON object
     */
    public enum Json {
        VERSION,
        ASSURANCELEVEL,
        EVALUATIONSTATUS,
        PLUS,
        STRENGTHOFFUNCTION,
        PROFILEOID,
        PROFILEURI,
        TARGETOID,
        TARGETURI;
    }
    
    DERIA5String version;
    EvaluationAssuranceLevel assuranceLevel;
    EvaluationStatus evaluationStatus;
    ASN1Boolean plus; // default false
    StrengthOfFunction strengthOfFunction; // optional, tagged 0
    ASN1ObjectIdentifier profileOid; // optional , tagged 1
    URIReference profileUri ; // optional, tagged 2
    ASN1ObjectIdentifier targetOid; // optional, tagged 3
    URIReference targetUri; // optional, tagged 4
    
    private CommonCriteriaMeasuresFactory() {
        version = null;
        assuranceLevel = null;
        evaluationStatus = null;
        plus = ASN1Boolean.FALSE;
        strengthOfFunction = null;
        profileOid = null;
        profileUri = null;
        targetOid = null;
        targetUri = null;
    }
    
    /**
     * Begin creating a new common criteria measures object.
     * @return A new CommonCriteriaMeasuresFactory builder.
     */
    public static final CommonCriteriaMeasuresFactory create() {
        return new CommonCriteriaMeasuresFactory();
    }
    
    /**
     * Set the version. Required field.
     * @param version {@link DERIA5String}
     * @return The CommonCriteriaMeasuresFactory object with the version set.
     */
    public final CommonCriteriaMeasuresFactory version(final DERIA5String version) {
        this.version = version;
        return this;
    }
    
    /**
     * Set the assurance level. Required field. 
     * @param option {@link EvaluationAssuranceLevel}
     * @return The CommonCriteriaMeasuresFactory object with the assurance level set.
     */
    public final CommonCriteriaMeasuresFactory assuranceLevel(final EvaluationAssuranceLevel option) {
        assuranceLevel = option;
        return this;
    }
    
    /**
     * Set the evaluation status. Required field.
     * @param option {@link EvaluationStatus}
     * @return The CommonCriteriaMeasuresFactory object with the evaluation status set.
     */
    public final CommonCriteriaMeasuresFactory evaluationStatus(final EvaluationStatus option) {
        evaluationStatus = option;
        return this;
    }
    
    /**
     * Required field.  Defaults to false.
     * @param setting boolean
     * @return The CommonCriteriaMeasuresFactory object with the plus bit set.
     */
    public final CommonCriteriaMeasuresFactory plus(final ASN1Boolean setting) {
        plus = setting;
        return this;
    }
    
    /**
     * Set the strength of function. Optional field.
     * @param option {@link StrengthOfFunction}
     * @return The CommonCriteriaMeasuresFactory object with the strength of function set.
     */
    public final CommonCriteriaMeasuresFactory strengthOfFunction(final StrengthOfFunction option) {
        strengthOfFunction = option;
        return this;
    }
    
    /**
     * Set the profile OID. Optional field.
     * @param oid String
     * @return The CommonCriteriaMeasuresFactory object with the profile OID set.
     */
    public final CommonCriteriaMeasuresFactory profileOid(final String oid) {
        profileOid = new ASN1ObjectIdentifier(oid);
        return this;
    }
    
    /**
     * Set the profile URI. Optional field.
     * @param uriRef {@link URIReference}
     * @see URIReferenceFactory
     * @return The CommonCriteriaMeasuresFactory object with the profile URI set.
     */
    public final CommonCriteriaMeasuresFactory profileUri(final URIReference uriRef) {
        profileUri = uriRef;
        return this;
    }
    
    /**
     * Set the target oid. Optional field.
     * @param oid String
     * @return The CommonCriteriaMeasuresFactory object with the target OID set.
     */
    public final CommonCriteriaMeasuresFactory targetOid(final String oid) {
        targetOid = new ASN1ObjectIdentifier(oid);
        return this;
    }
    
    /**
     * Set the target URI. Optional field.
     * @param uriRef {@link URIReference}
     * @see URIReferenceFactory
     * @return The CommonCriteriaMeasuresFactory object with the target URI set.
     */
    public final CommonCriteriaMeasuresFactory targetUri(final URIReference uriRef) {
        targetUri = uriRef;
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link CommonCriteriaMeasures}
     */
    public final CommonCriteriaMeasures build() {
        CommonCriteriaMeasures obj = new CommonCriteriaMeasures(version, assuranceLevel, evaluationStatus, plus, strengthOfFunction, profileOid, profileUri, targetOid, targetUri);
        
        return obj;
    }
    
    /**
     * Create a new common criteria measure object from a JSON node.
     * @param refNode JsonNode
     * @return A new CommonCriteriaMeasuresFactory with fields filled out from JSON.
     */
    public static final CommonCriteriaMeasuresFactory fromJsonNode(final JsonNode refNode) {
        CommonCriteriaMeasuresFactory ccmf = null;
        if (refNode.has(Json.VERSION.name()) && refNode.has(Json.ASSURANCELEVEL.name()) && refNode.has(Json.EVALUATIONSTATUS.name())) {
            JsonNode versionNode = refNode.get(Json.VERSION.name());
            JsonNode assuranceLevelNode = refNode.get(Json.ASSURANCELEVEL.name());
            JsonNode evaluationStatusNode = refNode.get(Json.EVALUATIONSTATUS.name());
            JsonNode plusNode = refNode.get(Json.PLUS.name());
            JsonNode sofNode = refNode.get(Json.STRENGTHOFFUNCTION.name());
            JsonNode profileOidNode = refNode.get(Json.PROFILEOID.name());
            JsonNode profileUriNode = refNode.get(Json.PROFILEURI.name());
            JsonNode targetOidNode = refNode.get(Json.TARGETOID.name());
            JsonNode targetUriNode = refNode.get(Json.TARGETURI.name());
            
            ccmf = CommonCriteriaMeasuresFactory.create();
            ccmf.version(new DERIA5String(versionNode.asText()));
            String assuranceLevel = assuranceLevelNode.asText();
            ccmf.assuranceLevel(new EvaluationAssuranceLevel(assuranceLevel));
            ccmf.evaluationStatus(new EvaluationStatus(evaluationStatusNode.asText()));
            
            boolean plus = false;
            if (plusNode != null) {
                if (plusNode.isBoolean()) {
                    plus = plusNode.asBoolean();
                } else if (plusNode.isInt()) {
                    plus = plusNode.asInt() != 0;   
                }
            }
            ccmf.plus(ASN1Boolean.getInstance(plus));
            
            if (sofNode != null) {
                ccmf.strengthOfFunction(new StrengthOfFunction(sofNode.asText()));
            }
            
            if (profileOidNode != null) {
                ccmf.profileOid(profileOidNode.asText());
            }
            
            if (profileUriNode != null) {
                URIReferenceFactory urif = URIReferenceFactory.fromJsonNode(profileUriNode);
                ccmf.profileUri(urif.build());
            }
            
            if (targetOidNode != null) {
                ccmf.targetOid(targetOidNode.asText());
            }
            
            if (targetUriNode != null) {
                URIReferenceFactory urif = URIReferenceFactory.fromJsonNode(targetUriNode);
                ccmf.targetUri(urif.build());
            }
        }
        return ccmf;
    }
}
