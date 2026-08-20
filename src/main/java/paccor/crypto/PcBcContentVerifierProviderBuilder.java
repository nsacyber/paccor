package paccor.crypto;

import java.io.IOException;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509ContentVerifierProviderBuilder;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcContentVerifierProviderBuilder;

/**
 * Chooses the signing verification algorithm from the parameters given and the set of supported algorithms.
 */
public class PcBcContentVerifierProviderBuilder extends BcContentVerifierProviderBuilder
        implements X509ContentVerifierProviderBuilder {
    final private DigestAlgorithmIdentifierFinder digestAlgorithmFinder;
    
    public PcBcContentVerifierProviderBuilder(DigestAlgorithmIdentifierFinder digestAlgorithmFinder) {
        this.digestAlgorithmFinder = digestAlgorithmFinder;
    }

    @Override
    protected Signer createSigner(AlgorithmIdentifier sigAlgId) throws OperatorCreationException {
        return AlgorithmSupport.buildSigner(sigAlgId, digestProvider, null, digestAlgorithmFinder);
    }

    @Override
    protected AsymmetricKeyParameter extractKeyParameters(SubjectPublicKeyInfo publicKeyInfo) throws IOException {
        return PqcHelper.createKeyFromInfo(publicKeyInfo);
    }

    @Override
    public ContentVerifierProvider build(SubjectPublicKeyInfo publicKeyInfo)
            throws OperatorCreationException {
        try {
            return build(extractKeyParameters(publicKeyInfo));
        } catch (IOException e) {
            throw new OperatorCreationException("Could not create verifier", e);
        }
    }

    @Override
    public ContentVerifierProvider build(X509CertificateHolder certificate)
            throws OperatorCreationException {
        return super.build(certificate);
    }
}
