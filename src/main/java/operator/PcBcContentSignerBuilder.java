package operator;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.DSASigner;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcContentSignerBuilder;

/**
 * Automatically chooses the appropriate signing algorithm from the set of supported algorithms
 * based on the parameters given- likely defined in a public key certificate
 */
public class PcBcContentSignerBuilder extends BcContentSignerBuilder
{
    public PcBcContentSignerBuilder(AlgorithmIdentifier sigAlgId, AlgorithmIdentifier digAlgId)
    {
        super(sigAlgId, digAlgId);
    }

    protected Signer createSigner(AlgorithmIdentifier sigAlgId, AlgorithmIdentifier digAlgId)
        throws OperatorCreationException
    {
        Signer signer = null;
        Digest dig = digestProvider.get(digAlgId);
        
        if (PcBcContentVerifierProviderBuilder.ECDSA_BASED_SIGALGS.contains(sigAlgId.getAlgorithm())) {
            signer = new DSADigestSigner(new ECDSASigner(), dig);
        } else if (PcBcContentVerifierProviderBuilder.DSA_BASED_SIGALGS.contains(sigAlgId.getAlgorithm())) {
            signer = new DSADigestSigner(new DSASigner(), dig);
        } else if (PcBcContentVerifierProviderBuilder.RSA_BASED_SIGALGS.contains(sigAlgId.getAlgorithm())) {
            signer = new RSADigestSigner(dig);
        } else {
            throw new IllegalArgumentException("Unsupported algorithm");
        }
        
        return signer;
    }
}
