package paccor.crypto;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcContentSignerBuilder;

/**
 * Chooses the signing algorithm from the parameters given and the set of supported algorithms
 */
public class PcBcContentSignerBuilder extends BcContentSignerBuilder {
    public PcBcContentSignerBuilder(AlgorithmIdentifier sigAlgId, AlgorithmIdentifier digAlgId) {
        super(sigAlgId, digAlgId);
    }

    @Override
    protected Signer createSigner(AlgorithmIdentifier sigAlgId, AlgorithmIdentifier digAlgId) throws OperatorCreationException {
        return AlgorithmSupport.buildSigner(sigAlgId, digestProvider, digAlgId, null);
    }
}
