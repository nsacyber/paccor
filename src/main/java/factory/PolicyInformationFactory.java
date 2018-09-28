package factory;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x509.DisplayText;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.PolicyQualifierId;
import org.bouncycastle.asn1.x509.PolicyQualifierInfo;

/**
 * Functions to help manage the creation of a policy information object.
 */
public class PolicyInformationFactory {
    /**
     * fields within each policy information JSON object
     */
    public enum Json {
        POLICYIDENTIFIER,
        POLICYQUALIFIERS,
        POLICYQUALIFIERID,
        QUALIFIER;
    }
    
    /**
     * qualifier options
     */
    public enum QualifierJson {
        CPS(PolicyQualifierId.id_qt_cps),
        USERNOTICE(PolicyQualifierId.id_qt_unotice);
        
        private ASN1ObjectIdentifier oid;
        
        private QualifierJson(ASN1ObjectIdentifier oid) {
            this.oid = oid;
        }
        
        public ASN1ObjectIdentifier getOid() {
            return oid;
        }
    }
    
    private ASN1ObjectIdentifier oid;
    private Vector<PolicyQualifierInfo> qualifiers;
    
    private PolicyInformationFactory() {
        oid = null;
        qualifiers = new Vector<PolicyQualifierInfo>();
    }
    
    /**
     * Begin defining the policy information object.
     */
    public static final PolicyInformationFactory create() {
        PolicyInformationFactory pif = new PolicyInformationFactory();
        return pif;
    }
    
    /**
     * Set the policy OID.
     * @param oid {@link ASN1ObjectIdentifier}
     */
    public final PolicyInformationFactory policyIdentifier(final ASN1ObjectIdentifier oid) {
        this.oid = oid;
        return this;
    }
    
    /**
     * Add a policy qualifier.
     * @param qualifier {@link PolicyQualifierInfo}
     */
    public final PolicyInformationFactory addQualifier(final PolicyQualifierInfo qualifier) {
        qualifiers.add(qualifier);
        return this;
    }
    
    /**
     * Add a policy qualifier using the enumerated qualifier list.
     * @param type {@link PolicyInformationFactory.QualifierJson}
     * @param value {@link ASN1Encodable}
     */
    public final PolicyInformationFactory addQualifier(final QualifierJson type, final ASN1Encodable value) {
        qualifiers.add(new PolicyQualifierInfo(type.getOid(), value));
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link PolicyInformation}
     */
    public final PolicyInformation build() {
        PolicyQualifierInfo[] pqi = qualifiers.toArray(new PolicyQualifierInfo[qualifiers.size()]);
        ASN1Sequence seq = new DERSequence(pqi);
        PolicyInformation pi = new PolicyInformation(oid, seq);
        return pi;
    }
    
    /**
     * Create a new policy information object from a JSON node.
     * @param refNode JsonNode representing a policy information JSON object
     */
    public static final PolicyInformationFactory fromJsonNode(final JsonNode refNode) {
        PolicyInformationFactory pif = create();
        if (refNode.has(Json.POLICYIDENTIFIER.name()) && refNode.has(Json.POLICYQUALIFIERS.name())) {
            final JsonNode oidNode = refNode.get(Json.POLICYIDENTIFIER.name());
            final JsonNode qualifiersArray = refNode.get(Json.POLICYQUALIFIERS.name());
            
            pif.policyIdentifier(new ASN1ObjectIdentifier(oidNode.asText()));
            if (qualifiersArray.isArray()) {
                for (final JsonNode qualifierNode : qualifiersArray) {
                    if (qualifierNode.has(Json.POLICYQUALIFIERID.name()) && qualifierNode.has(Json.QUALIFIER.name())) {
                        final JsonNode policyQualifierId = qualifierNode.get(Json.POLICYQUALIFIERID.name());
                        final JsonNode policyQualiferNode = qualifierNode.get(Json.QUALIFIER.name());
                        
                        QualifierJson type = QualifierJson.valueOf(policyQualifierId.asText());
                        if (type == QualifierJson.CPS) {
                            pif.addQualifier(type, new DisplayText(0, policyQualiferNode.asText()));
                        } else if (type == QualifierJson.USERNOTICE) {
                            pif.addQualifier(type, new DERSequence(new DERUTF8String(policyQualiferNode.asText())));
                        }
                        
                    }
                }
            }
        }
            
        return pif;
    }
}
