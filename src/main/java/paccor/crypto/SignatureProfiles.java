package paccor.crypto;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.cert.X509CertificateHolder;

@UtilityClass
public class SignatureProfiles {
    private static final Map<String, AlgorithmIdentifier> PROFILES;
    static {
        Map<String, AlgorithmIdentifier> m = new LinkedHashMap<>();
        m.put("ecdsa-p256-sha256", new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256));
        m.put("ecdsa-p384-sha384", new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA384));
        m.put("ecdsa-p521-sha512", new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA512));
        m.put("ed25519", new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519));
        m.put("rsa-pss-sha256-32", rsaPss(NISTObjectIdentifiers.id_sha256, 32));
        m.put("rsa-pss-sha384-48", rsaPss(NISTObjectIdentifiers.id_sha384, 48));
        m.put("rsa-pss-sha512-64", rsaPss(NISTObjectIdentifiers.id_sha512, 64));
        m.put("rsa-sha256", new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption));
        m.put("rsa-sha384", new AlgorithmIdentifier(PKCSObjectIdentifiers.sha384WithRSAEncryption));
        m.put("rsa-sha512", new AlgorithmIdentifier(PKCSObjectIdentifiers.sha512WithRSAEncryption));
        m.put("ml-dsa-44", new AlgorithmIdentifier(NISTObjectIdentifiers.id_ml_dsa_44));
        m.put("ml-dsa-65", new AlgorithmIdentifier(NISTObjectIdentifiers.id_ml_dsa_65));
        m.put("ml-dsa-87", new AlgorithmIdentifier(NISTObjectIdentifiers.id_ml_dsa_87));
        PROFILES = Collections.unmodifiableMap(m);
    }

    public Map<String, AlgorithmIdentifier> profiles() {
        return PROFILES;
    }

    public AlgorithmIdentifier algIdFor(String profile) {
        AlgorithmIdentifier alg = PROFILES.get(profile.toLowerCase());
        if (alg == null) throw new IllegalArgumentException("Unknown sig profile: " + profile);
        return alg;
    }

    private AlgorithmIdentifier rsaPss(ASN1ObjectIdentifier hashOid, int saltLen) { // package-private
        var hashAlg = new AlgorithmIdentifier(hashOid, DERNull.INSTANCE);
        var mgf1 = new AlgorithmIdentifier(PKCSObjectIdentifiers.id_mgf1, hashAlg);
        var params = new RSASSAPSSparams(hashAlg, mgf1, new ASN1Integer(saltLen), new ASN1Integer(1));
        return new AlgorithmIdentifier(PKCSObjectIdentifiers.id_RSASSA_PSS, params);
    }

    public AlgorithmIdentifier inferAlgIdFromIssuer(X509CertificateHolder issuer) {
        if (issuer == null) return null;
        // Prefer signature algorithm OID if recognized; else infer from SPKI algorithm
        String sigOid = issuer.getSignatureAlgorithm().getAlgorithm().getId();
        try {
            // Map common OIDs to default profiles
            if (sigOid.startsWith("1.2.840.113549.1.1")) { // RSA
                return new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption);
            }
            if (sigOid.startsWith("1.2.840.10045.4.3.2")) { // ecdsa-with-SHA256
                return new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256);
            }
        } catch (Exception ignored) {
        }
        var spkiAlg = issuer.getSubjectPublicKeyInfo().getAlgorithm().getAlgorithm();
        if (spkiAlg.getId().startsWith("1.2.840.10045")) {
            return new AlgorithmIdentifier(X9ObjectIdentifiers.ecdsa_with_SHA256);
        }
        if (spkiAlg.equals(EdECObjectIdentifiers.id_Ed25519)) {
            return new AlgorithmIdentifier(EdECObjectIdentifiers.id_Ed25519);
        }
        // Default to RSA SHA-256 if unknown
        return new AlgorithmIdentifier(PKCSObjectIdentifiers.sha256WithRSAEncryption);
    }

}
