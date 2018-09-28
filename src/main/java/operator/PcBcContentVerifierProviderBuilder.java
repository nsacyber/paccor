package operator;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.DSASigner;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PublicKeyFactory;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcContentVerifierProviderBuilder;

/**
 * Automatically chooses the appropriate signing verification algorithm from the set of supported algorithms
 * based on the parameters given- likely defined in a public key certificate
 */
public class PcBcContentVerifierProviderBuilder extends BcContentVerifierProviderBuilder {
    public static final List<ASN1ObjectIdentifier> ECDSA_BASED_SIGALGS =
            Collections.unmodifiableList(new Vector<ASN1ObjectIdentifier>()
            {
                private static final long serialVersionUID = 1L;
            {
                add(X9ObjectIdentifiers.ecdsa_with_SHA1);
                add(X9ObjectIdentifiers.ecdsa_with_SHA256);
                add(X9ObjectIdentifiers.ecdsa_with_SHA384);
                add(X9ObjectIdentifiers.ecdsa_with_SHA512);
            }});
    
    public static final List<ASN1ObjectIdentifier> DSA_BASED_SIGALGS =
            Collections.unmodifiableList(new Vector<ASN1ObjectIdentifier>()
            {
                private static final long serialVersionUID = 1L;
            {
                add(X9ObjectIdentifiers.id_dsa_with_sha1);
                add(NISTObjectIdentifiers.dsa_with_sha256);
                add(NISTObjectIdentifiers.dsa_with_sha384);
                add(NISTObjectIdentifiers.dsa_with_sha512);
            }});
    
    public static final List<ASN1ObjectIdentifier> RSA_BASED_SIGALGS =
            Collections.unmodifiableList(new Vector<ASN1ObjectIdentifier>()
            {
                private static final long serialVersionUID = 1L;
            {
                add(PKCSObjectIdentifiers.sha1WithRSAEncryption);
                add(PKCSObjectIdentifiers.sha256WithRSAEncryption);
                add(PKCSObjectIdentifiers.sha384WithRSAEncryption);
                add(PKCSObjectIdentifiers.sha512WithRSAEncryption);
            }});
    
    private DigestAlgorithmIdentifierFinder digestAlgorithmFinder;
    
    public PcBcContentVerifierProviderBuilder(DigestAlgorithmIdentifierFinder digestAlgorithmFinder) {
        this.digestAlgorithmFinder = digestAlgorithmFinder;
    }

    protected Signer createSigner(AlgorithmIdentifier sigAlgId) throws OperatorCreationException {
        Signer signer = null;
        AlgorithmIdentifier digAlg = digestAlgorithmFinder.find(sigAlgId);
        Digest dig = digestProvider.get(digAlg);
        
        if (ECDSA_BASED_SIGALGS.contains(sigAlgId.getAlgorithm())) {
            signer = new DSADigestSigner(new ECDSASigner(), dig);
        } else if (DSA_BASED_SIGALGS.contains(sigAlgId.getAlgorithm())) {
            signer = new DSADigestSigner(new DSASigner(), dig);
        } else if (RSA_BASED_SIGALGS.contains(sigAlgId.getAlgorithm())) {
            signer = new RSADigestSigner(dig);
        } else {
            throw new IllegalArgumentException("Unsupported algorithm");
        }
        
        return signer;
    }

    protected AsymmetricKeyParameter extractKeyParameters(SubjectPublicKeyInfo publicKeyInfo) throws IOException {
        return PublicKeyFactory.createKey(publicKeyInfo);
    }
}
