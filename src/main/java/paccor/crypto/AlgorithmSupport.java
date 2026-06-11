package paccor.crypto;

import paccor.cert.CertSigEncoding;
import paccor.exception.PaccorException;
import paccor.exception.SignatureFailedException;
import paccor.exception.UnsupportedAlgorithmException;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.Arrays;
import java.util.Map;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.edec.EdECObjectIdentifiers;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.pkcs.RSASSAPSSparams;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Signer;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.engines.RSABlindedEngine;
import org.bouncycastle.crypto.signers.DSADigestSigner;
import org.bouncycastle.crypto.signers.ECDSASigner;
import org.bouncycastle.crypto.signers.Ed25519Signer;
import org.bouncycastle.crypto.signers.PSSSigner;
import org.bouncycastle.crypto.signers.RSADigestSigner;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcContentSignerBuilder;
import org.bouncycastle.operator.bc.BcDigestProvider;
import org.bouncycastle.crypto.signers.MLDSASigner;

/**
 * Centralized algorithm support utilities for CLI commands.
 */
public class AlgorithmSupport {
    private static final Map<ASN1ObjectIdentifier, String> OID_TO_JCA_SIGNATURE = Map.ofEntries(
            // ECDSA
            Map.entry(X9ObjectIdentifiers.ecdsa_with_SHA1, "SHA1withECDSA"),
            Map.entry(X9ObjectIdentifiers.ecdsa_with_SHA256, "SHA256withECDSA"),
            Map.entry(X9ObjectIdentifiers.ecdsa_with_SHA384, "SHA384withECDSA"),
            Map.entry(X9ObjectIdentifiers.ecdsa_with_SHA512, "SHA512withECDSA"),
            // RSA PKCS1
            Map.entry(PKCSObjectIdentifiers.sha1WithRSAEncryption, "SHA1withRSA"),
            Map.entry(PKCSObjectIdentifiers.sha256WithRSAEncryption, "SHA256withRSA"),
            Map.entry(PKCSObjectIdentifiers.sha384WithRSAEncryption, "SHA384withRSA"),
            Map.entry(PKCSObjectIdentifiers.sha512WithRSAEncryption, "SHA512withRSA"),
            // RSA PSS
            Map.entry(PKCSObjectIdentifiers.id_RSASSA_PSS, "RSASSA-PSS"),
            // Ed25519
            Map.entry(EdECObjectIdentifiers.id_Ed25519, "Ed25519"),
            // ML-DSA
            Map.entry(NISTObjectIdentifiers.id_ml_dsa_44, "ML-DSA-44"),
            Map.entry(NISTObjectIdentifiers.id_ml_dsa_65, "ML-DSA-65"),
            Map.entry(NISTObjectIdentifiers.id_ml_dsa_87, "ML-DSA-87"),
            // ML-DSA Pre-hashed variants
            Map.entry(NISTObjectIdentifiers.id_hash_ml_dsa_44_with_sha512, "ML-DSA-44-with-SHA512"),
            Map.entry(NISTObjectIdentifiers.id_hash_ml_dsa_65_with_sha512, "ML-DSA-65-with-SHA512"),
            Map.entry(NISTObjectIdentifiers.id_hash_ml_dsa_87_with_sha512, "ML-DSA-87-with-SHA512")
    );
    private static final Map<ASN1ObjectIdentifier, String> OID_TO_JCA_HASH = Map.ofEntries(
            Map.entry(NISTObjectIdentifiers.id_sha256, "SHA-256"),
            Map.entry(NISTObjectIdentifiers.id_sha384, "SHA-384"),
            Map.entry(NISTObjectIdentifiers.id_sha512, "SHA-512"),
            Map.entry(OIWObjectIdentifiers.idSHA1, "SHA-1")
    );

    private AlgorithmSupport() {}

    /**
     * Returns true if the given OID is an ECDSA algorithm.
     * @param oid The OID to check
     * @return True if the OID is an ECDSA algorithm. Otherwise, false.
     */
    public static final boolean isEcdsa(ASN1ObjectIdentifier oid) {
        return oid.equals(X9ObjectIdentifiers.ecdsa_with_SHA1)
            || oid.equals(X9ObjectIdentifiers.ecdsa_with_SHA256)
            || oid.equals(X9ObjectIdentifiers.ecdsa_with_SHA384)
            || oid.equals(X9ObjectIdentifiers.ecdsa_with_SHA512);
    }

    /**
     * Returns true if the given OID is an ML-DSA algorithm.
     * @param oid The OID to check
     * @return True if the OID is an ML-DSA algorithm. Otherwise, false.
     */
    public static final boolean isMlDsa(ASN1ObjectIdentifier oid) {
        return oid.equals(NISTObjectIdentifiers.id_ml_dsa_44)
                || oid.equals(NISTObjectIdentifiers.id_ml_dsa_65)
                || oid.equals(NISTObjectIdentifiers.id_ml_dsa_87);
    }

    /**
     * Returns true if the given OID is an RSA PKCS#1 algorithm.
     * @param oid The OID to check
     * @return True if the OID is an RSA PKCS#1 algorithm. Otherwise, false.
     */
    public static final boolean isRsaPkcs1(ASN1ObjectIdentifier oid) {
        return oid.equals(PKCSObjectIdentifiers.sha1WithRSAEncryption)
            || oid.equals(PKCSObjectIdentifiers.sha256WithRSAEncryption)
            || oid.equals(PKCSObjectIdentifiers.sha384WithRSAEncryption)
            || oid.equals(PKCSObjectIdentifiers.sha512WithRSAEncryption);
    }

    /**
     * Returns true if the given OID is an RSA PSS algorithm.
     * @param oid The OID to check
     * @return True if the OID is an RSA PSS algorithm. Otherwise, false.
     */
    public static final boolean isRsaPss(ASN1ObjectIdentifier oid) {
        return oid.equals(PKCSObjectIdentifiers.id_RSASSA_PSS);
    }

    /**
     * Returns true if the given OID is an Ed25519 algorithm.
     * @param oid The OID to check
     * @return True if the OID is an Ed25519 algorithm. Otherwise, false.
     */
    public static final boolean isEd25519(ASN1ObjectIdentifier oid) {
        return oid.equals(EdECObjectIdentifiers.id_Ed25519);
    }

    /**
     * Unified Signer construction used by both signer and verifier builders.
     * For signing, pass digAlg directly (already chosen). For verification, pass a finder to derive digAlg.
     * @param sigAlgId The signature algorithm identifier
     * @param digestProvider The digest provider
     * @param digAlgOrNull The digest algorithm identifier, or null to derive from the signature algorithm
     * @param finderOrNull The digest algorithm finder, or null to derive from the signature algorithm
     * @return The signer
     * @throws OperatorCreationException To meet BouncyCastle library. If the signer cannot be created
     */
    public static final Signer buildSigner(AlgorithmIdentifier sigAlgId,
                              BcDigestProvider digestProvider,
                              AlgorithmIdentifier digAlgOrNull,
                              DigestAlgorithmIdentifierFinder finderOrNull) throws OperatorCreationException {
        final ASN1ObjectIdentifier oid = sigAlgId.getAlgorithm();
        final Digest dig = getDigestObject(sigAlgId, digestProvider, digAlgOrNull, finderOrNull);

        // ECDSA
        if (isEcdsa(oid)) {
            return new DSADigestSigner(new ECDSASigner(), dig);
        }
        // MLDSA
        if (isMlDsa(oid)) {
            return new MLDSASigner();
        }
        // RSA PKCS#1 v1.5
        if (isRsaPkcs1(oid)) {
            return new RSADigestSigner(dig);
        }
        // RSASSA-PSS
        if (isRsaPss(oid)) {
            return buildPssSigner(sigAlgId, digestProvider);
        }
        // Ed25519
        if (isEd25519(oid)) {
            return new Ed25519Signer();
        }
        throw new OperatorCreationException("Could not build signer", new UnsupportedAlgorithmException(oid));
    }

    private static AlgorithmIdentifier getDigestAlgorithm(AlgorithmIdentifier sigAlgId,
                               AlgorithmIdentifier digAlgOrNull,
                               DigestAlgorithmIdentifierFinder finderOrNull) {
        return digAlgOrNull != null
                ? digAlgOrNull
                : (finderOrNull != null
                    ? finderOrNull.find(sigAlgId)
                    : null);
    }

    private static Digest getDigestObject(AlgorithmIdentifier sigAlgId,
                                          BcDigestProvider digestProvider,
                                          AlgorithmIdentifier digAlgOrNull,
                                          DigestAlgorithmIdentifierFinder finderOrNull) throws OperatorCreationException {
        AlgorithmIdentifier digAlg = getDigestAlgorithm(sigAlgId, digAlgOrNull, finderOrNull);
        return digestProvider.get(digAlg);
    }

    /**
     * Builds a PSS signer from the given parameters.
     * @param sigAlgId The signature algorithm identifier
     * @param digestProvider The digest provider
     * @return The PSS signer
     * @throws OperatorCreationException If the signer cannot be created
     */
    public static final PSSSigner buildPssSigner(AlgorithmIdentifier sigAlgId,
                                                 BcDigestProvider digestProvider) throws OperatorCreationException {
        RSASSAPSSparams p = RSASSAPSSparams.getInstance(sigAlgId.getParameters());
        Digest hash = digestProvider.get(p.getHashAlgorithm());
        AlgorithmIdentifier mgf = AlgorithmIdentifier.getInstance(p.getMaskGenAlgorithm().getParameters());
        Digest mgfHash = digestProvider.get(mgf);
        int saltLen = p.getSaltLength().intValueExact();
        return new PSSSigner(new RSABlindedEngine(), hash, mgfHash, saltLen, (byte)0xBC);
    }

    /**
     * Returns the JCA signature algorithm name for the given AlgorithmIdentifier.
     * @param algId The algorithm identifier
     * @return The JCA signature algorithm name
     * @throws UnsupportedAlgorithmException If the algorithm is not supported
     */
    public static String jcaSignatureName(AlgorithmIdentifier algId) throws UnsupportedAlgorithmException {
        ASN1ObjectIdentifier oid = algId.getAlgorithm();
        String mapped = OID_TO_JCA_SIGNATURE.get(oid);
        if (mapped != null) return mapped;

        if (isEcdsa(oid)) return "SHA384withECDSA";  // default for unknown ECDSA
        if (isRsaPkcs1(oid)) return "SHA384withRSA";  // default for unknown RSA

        throw new UnsupportedAlgorithmException(oid);
    }

    /**
     * Returns the JCA hash algorithm name for the given OID.
     * @param oid The OID
     * @return The JCA hash algorithm name
     */
    public static String jcaHashName(ASN1ObjectIdentifier oid) {
        return OID_TO_JCA_HASH.getOrDefault(oid, "SHA-384");
    }

    /**
     * Returns the MGF1ParameterSpec for the given hash name.
     * @param jcaHashName The JCA hash name
     * @return The MGF1ParameterSpec for the given hash name
     */
    public static MGF1ParameterSpec mgf1ParameterSpec(String jcaHashName) {
        return switch (jcaHashName) {
            case "SHA-1" -> MGF1ParameterSpec.SHA1;
            case "SHA-384" -> MGF1ParameterSpec.SHA384;
            case "SHA-512" -> MGF1ParameterSpec.SHA512;
            default -> MGF1ParameterSpec.SHA256;
        };
    }

    /**
     * Computes the SHA-256 hash of the given byte array.
     * @param objectBytes The byte array to hash
     * @return The SHA-256 hash of the byte array
     */
    public static final byte[] sha256(byte[] objectBytes) {
        SHA256Digest digest = new SHA256Digest();
        byte[] hash = new byte[digest.getDigestSize()];
        digest.update(objectBytes, 0, objectBytes.length);
        digest.doFinal(hash, 0);
        return hash;
    }

    /**
     * Computes the SHA-384 hash of the given byte array.
     * @param objectBytes The byte array to hash
     * @return The SHA-384 hash of the byte array
     */
    public static final byte[] sha384(byte[] objectBytes) {
        SHA384Digest digest = new SHA384Digest();
        byte[] hash = new byte[digest.getDigestSize()];
        digest.update(objectBytes, 0, objectBytes.length);
        digest.doFinal(hash, 0);
        return hash;
    }

    /**
     * Converts a P1363 signature to DER format.
     * @param sig The P1363 signature
     * @param enc The signature encoding
     * @param algId The algorithm identifier
     * @return The DER-encoded signature
     */
    public static byte[] maybeConvertToDer(byte[] sig, CertSigEncoding enc, AlgorithmIdentifier algId) {
        if (enc == CertSigEncoding.P1363 && AlgorithmSupport.isEcdsa(algId.getAlgorithm())) {
            return ecdsaP1363ToDer(sig);
        }
        return sig;
    }

    /**
     * Converts a P1363 signature to DER format.
     * @param sig The P1363 signature
     * @return The DER-encoded signature
     */
    public static byte[] ecdsaP1363ToDer(byte[] sig) {
        int len = sig.length / 2;
        BigInteger r = new BigInteger(1, Arrays.copyOfRange(sig, 0, len));
        BigInteger s = new BigInteger(1, Arrays.copyOfRange(sig, len, sig.length));
        try {
            return new DERSequence(new ASN1Encodable[]{ new ASN1Integer(r), new ASN1Integer(s) }).getEncoded("DER");
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    /**
     * Ensures that the algorithm identifier has null parameters for ECDSA.
     * @param algId The algorithm identifier
     * @return The algorithm identifier with null parameters if applicable
     */
    public static AlgorithmIdentifier ensureNullParamsForEcdsa(AlgorithmIdentifier algId) {
        if (AlgorithmSupport.isEcdsa(algId.getAlgorithm()) && algId.getParameters() != null) {
            return new AlgorithmIdentifier(algId.getAlgorithm(), DERNull.INSTANCE);
        }
        return algId;
    }

    public static final byte[] signWithJca(byte[] tbs, AlgorithmIdentifier algId, PrivateKey key)
            throws GeneralSecurityException, PaccorException {
        Signature signature = Signature.getInstance(AlgorithmSupport.jcaSignatureName(algId));
        AlgorithmSupport.maybeInitPss(signature, algId);
        signature.initSign(key);
        signature.update(tbs);
        return signature.sign();
    }



    public static final byte[] signWithBc(byte[] tbs, AlgorithmIdentifier algId, PrivateKeyInfo pki) throws PaccorException {
        AlgorithmIdentifier normalized = AlgorithmSupport.ensureNullParamsForEcdsa(algId);
        AlgorithmIdentifier digAlg = new DefaultDigestAlgorithmIdentifierFinder().find(normalized);
        BcContentSignerBuilder builder = new PcBcContentSignerBuilder(normalized, digAlg);
        try {
            ContentSigner signer = builder.build(PqcHelper.createKeyFromInfo(pki));
            try (OutputStream os = signer.getOutputStream()) {
                os.write(tbs);
            }
            return signer.getSignature();
        } catch (IOException | OperatorCreationException e) {
            throw new SignatureFailedException(e.getMessage(), e);
        }
    }

    public static final void maybeInitPss(Signature s, AlgorithmIdentifier algId) throws InvalidAlgorithmParameterException {
        if (AlgorithmSupport.isRsaPss(algId.getAlgorithm())) {
            RSASSAPSSparams p = RSASSAPSSparams.getInstance(algId.getParameters());
            String hashName = AlgorithmSupport.jcaHashName(p.getHashAlgorithm().getAlgorithm());
            String mgfHashName = AlgorithmSupport.jcaHashName(AlgorithmIdentifier.getInstance(p.getMaskGenAlgorithm().getParameters()).getAlgorithm());
            MGF1ParameterSpec mgf = AlgorithmSupport.mgf1ParameterSpec(mgfHashName);
            int saltLen = p.getSaltLength().intValueExact();
            int trailerField = p.getTrailerField().intValueExact();
            PSSParameterSpec spec = new PSSParameterSpec(hashName, "MGF1", mgf, saltLen, trailerField);
            s.setParameter(spec);
        }
    }
}
