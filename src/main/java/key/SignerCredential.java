package key;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.DSASigner;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.crypto.util.PrivateKeyInfoFactory;
import org.bouncycastle.jcajce.provider.asymmetric.dsa.DSAUtil;
import org.bouncycastle.jcajce.provider.asymmetric.util.ECUtil;

import cli.CliHelper.x509type;

public class SignerCredential {
	private PrivateKeyInfo key;
	private X509CertificateHolder cert;
	private x509type privateKeyType;
	
	public static final SignerCredential createCredential(String alg) {
		x509type type = findType(alg);
		return new SignerCredential(type);
	}
	
	private SignerCredential(x509type type) {
		this.privateKeyType = type;
	}
	
	public final void loadCertificate(X509Certificate c) throws IOException {
		if (c == null) {
			throw new IllegalArgumentException("Input was null.");
		} else if (findType(c.getSigAlgOID()) != privateKeyType) {
			throw new IllegalArgumentException("Expected certificate key algorithm to be " + privateKeyType.getPemObjectType());
		}
		
		try {
			cert = new X509CertificateHolder(c.getEncoded());
		} catch (CertificateEncodingException e) {
			throw new IOException(e);
		}
	}
	
	public final void loadCertificate(X509CertificateHolder c) {
		if (c == null) {
			throw new IllegalArgumentException("Input was null.");
		} else if (findType(c.getSubjectPublicKeyInfo().getAlgorithm().getAlgorithm()) != privateKeyType) {
			throw new IllegalArgumentException("Expected certificate key algorithm to be " + privateKeyType.getPemObjectType());
		}
		
		cert = c;
	}
	
	public final void loadPrivateKey(PrivateKey pk) throws IOException {
		if (pk == null) {
			throw new IllegalArgumentException("Input was null.");
		} else if (findType(pk.getAlgorithm()) != privateKeyType) {
			throw new IllegalArgumentException("Expected private key algorithm to be " + privateKeyType.getPemObjectType());
		}
		
		AsymmetricKeyParameter param = null;
        try {
	        switch(privateKeyType) {
	        	case RSA_PRIVATE_KEY: // Copy of BouncyCastle RSAUtil#generatePrivateKeyParameter because it is protected whereas EC and DSA versions are public
	        		if (pk instanceof RSAPrivateCrtKey) {
	                    RSAPrivateCrtKey k = (RSAPrivateCrtKey)pk;
	
	                    param = new RSAPrivateCrtKeyParameters(k.getModulus(),
	                        k.getPublicExponent(), k.getPrivateExponent(),
	                        k.getPrimeP(), k.getPrimeQ(), k.getPrimeExponentP(), k.getPrimeExponentQ(), k.getCrtCoefficient());
	                } else if (pk instanceof RSAPrivateKey ){
	                    RSAPrivateKey k = (RSAPrivateKey)pk;
	
	                    param = new RSAKeyParameters(true, k.getModulus(), k.getPrivateExponent());
	                }
	        		break;
	        	case EC_PRIVATE_KEY:
					param = ECUtil.generatePrivateKeyParameter(pk);
	        		break;
	        	case DSA_PRIVATE_KEY:
					param = DSAUtil.generatePrivateKeyParameter(pk);
	        		break;
	    		default:
	        }
        } catch (ClassCastException | InvalidKeyException e) {
        	throw new IOException(e);
        }
        if (param == null) {
        	throw new IOException("Private key type not supported.");
        }
        
        key = PrivateKeyInfoFactory.createPrivateKeyInfo(param);
	}
	
	public final void loadPrivateKey(PrivateKeyInfo pk) {
		if (pk == null) {
			throw new IllegalArgumentException("Input was null.");
		} else if (findType(pk.getPrivateKeyAlgorithm().getAlgorithm()) != privateKeyType) {
			throw new IllegalArgumentException("Expected private key algorithm to be " + privateKeyType.getPemObjectType());
		}
		
		key = pk;
	}
	
	public static final x509type findType(final String algStr) {
        x509type keyType = null;
        String keyAlg = algStr.toLowerCase();
        switch (keyAlg) {
            case "rsa": keyType = x509type.RSA_PRIVATE_KEY;
                        break;
            case "dsa": keyType = x509type.DSA_PRIVATE_KEY;
                        break;
            case "ec":  keyType = x509type.EC_PRIVATE_KEY;
                        break;
            default:
            	try {
            	    keyType = findType(new ASN1ObjectIdentifier(algStr));
            	} catch (IllegalArgumentException e) {
            		// null check will report error
            	}
        }
        
        if (keyType == null) {
            throw new IllegalArgumentException("Unsupported key pair algorithm (" + keyType + ").  See the Supported Signing Algorithms section of the User Guide.");
        }
        
        return keyType;
	}
	
	public static final x509type findType(final ASN1ObjectIdentifier oid) {
		x509type keyType = null;
		
		if (SupportedAlgorithms.ECDSA_BASED_SIGALGS.contains(oid) || SupportedAlgorithms.EC.equals(oid)) {
            keyType = x509type.EC_PRIVATE_KEY;
        } else if (SupportedAlgorithms.DSA_BASED_SIGALGS.contains(oid) || SupportedAlgorithms.DSA.equals(oid)) {
            keyType = x509type.DSA_PRIVATE_KEY;
        } else if (SupportedAlgorithms.RSA_BASED_SIGALGS.contains(oid) || SupportedAlgorithms.RSA.equals(oid)) {
            keyType = x509type.RSA_PRIVATE_KEY;
        } else {
            throw new IllegalArgumentException("Unsupported algorithm");
        }
		
		return keyType;
	}
	
	public final PrivateKeyInfo getPrivateKey() {
		return key;
	}
	
	public final X509CertificateHolder getCertificate() {
		return cert;
	}
	
	public final x509type getKeyType() {
		return privateKeyType;
	}
	
	public final boolean hasKey() {
		return key != null;
	}
	
	public final boolean hasCertificate() {
		return cert != null;
	}
}
