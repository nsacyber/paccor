package factory;

import java.util.Vector;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.PolicyInformation;

/**
 * Functions to help manage the creation of the certificate policies extension.
 */
public class CertificatePoliciesFactory {
    private Vector<PolicyInformation> policies;
    
    private CertificatePoliciesFactory() {
        policies = new Vector<PolicyInformation>();
    }
    
    /**
     * Begin creating a new certificate policies object.
     */
    public static final CertificatePoliciesFactory create() {
        CertificatePoliciesFactory cpf = new CertificatePoliciesFactory();
        return cpf;
    }
    
    /**
     * Add another policy information object.
     * @param pi {@link PolicyInformation}
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
        PolicyInformation[] pis = policies.toArray(new PolicyInformation[policies.size()]);
        CertificatePolicies cp = new CertificatePolicies(pis);
        return cp;
    }
}
