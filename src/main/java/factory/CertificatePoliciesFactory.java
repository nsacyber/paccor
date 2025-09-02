package factory;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.PolicyInformation;
import tcg.credential.ASN1Utils;

/**
 * Functions to help manage the creation of the certificate policies extension.
 */
public class CertificatePoliciesFactory {
    private final List<PolicyInformation> policies;
    
    private CertificatePoliciesFactory() {
        policies = new ArrayList<>();
    }
    
    /**
     * Begin creating a new certificate policies object.
     * @return A new CertificatePoliciesFactory builder.
     */
    public static final CertificatePoliciesFactory create() {
        return new CertificatePoliciesFactory();
    }
    
    /**
     * Add another policy information object.
     * @param pi {@link PolicyInformation}
     * @return The CertificatePoliciesFactory object with new policy information added.
     */
    public final CertificatePoliciesFactory addPolicyInformation(PolicyInformation pi) {
        policies.add(pi);
        return this;
    }
    
    /**
     * Compile all of the data given to this factory.
     * @return {@link CertificatePolicies}
     */
    public final CertificatePolicies build() {
        return CertificatePolicies.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(policies)));
    }



    /**
     * Create a new certificate policies object from a JSON node.
     * @param refNode JsonNode representing a project relevant certificate policies JSON object
     * @return The CertificatePoliciesFactory object with new information from the JSON data.
     */
    public static final CertificatePoliciesFactory fromJsonNode(final JsonNode refNode) {
        CertificatePoliciesFactory cpf = CertificatePoliciesFactory.create();
        if (refNode.isArray()) {
            for (final JsonNode policyInfoNode : refNode) {
                PolicyInformationFactory pif = PolicyInformationFactory.fromJsonNode(policyInfoNode);
                cpf.addPolicyInformation(pif.build());
            }
        }

        return cpf;
    }
}
