package key;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;

public class SupportedAlgorithms {
	public static final ASN1ObjectIdentifier RSA = PKCSObjectIdentifiers.rsaEncryption;
	public static final ASN1ObjectIdentifier EC = X9ObjectIdentifiers.id_ecPublicKey;
	public static final ASN1ObjectIdentifier DSA = X9ObjectIdentifiers.id_dsa;
	
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
}
