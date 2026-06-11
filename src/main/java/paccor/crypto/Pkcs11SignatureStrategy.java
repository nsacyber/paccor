package paccor.crypto;

import paccor.cert.CertSigEncoding;
import paccor.exception.PaccorException;
import paccor.exception.Pkcs11Exception;
import paccor.exception.UnsupportedAlgorithmException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public record Pkcs11SignatureStrategy(
        File pkcs11Module,
        Integer pkcs11Slot,
        String pkcs11TokenLabel,
        String pkcs11KeyAlias,
        String pkcs11KeyIdHex,
        String pkcs11Pin,
        File pkcs11PinFile) implements SignatureStrategy {
    @Override
    public byte[] sign(byte[] content, AlgorithmIdentifier algId) throws PaccorException {
        algId = AlgorithmSupport.ensureNullParamsForEcdsa(algId);
        String pin = null;
        try {
            pin = resolvePin();
        } catch (Exception ignored) {}
        if (pin == null || pin.isBlank()) {
            throw new Pkcs11Exception("Could not resolve PKCS11 PIN");
        }
        return signWithPkcs11(content, algId, pkcs11Module, pkcs11Slot, pkcs11TokenLabel, pkcs11KeyAlias, pkcs11KeyIdHex, pin);
    }

    @Override
    public boolean isLocal() {
        return false;
    }

    private String resolvePin() throws Exception {
        if (pkcs11Pin != null) {
            return pkcs11Pin;
        }
        if (pkcs11PinFile != null) {
            return Files.readString(pkcs11PinFile.toPath()).trim();
        }
        return System.getenv("PKCS11_PIN");
    }

    // Sign using a PKCS#11 provider (SunPKCS11). Minimal implementation; real HSM support depends on the environment.
    public static final byte[] signWithPkcs11(byte[] tbs, AlgorithmIdentifier algId,
                                              File module, Integer slot, String tokenLabel,
                                              String keyAlias, String keyIdHex, String pin) throws Pkcs11Exception, UnsupportedAlgorithmException {
        Provider prov = buildSunPkcs11Provider(module, slot, tokenLabel);
        try {
            KeyStore ks = KeyStore.getInstance("PKCS11", prov);
            char[] pinChars = pin != null ? pin.toCharArray() : null;
            ks.load(null, pinChars);
            String alias = Pkcs11AliasResolver.selectAlias(ks, keyAlias, keyIdHex);
            Key key = ks.getKey(alias, pinChars);
            if (!(key instanceof PrivateKey)) {
                throw new IllegalStateException("Selected alias does not reference a private key: " + alias);
            }
            String jcaAlg = AlgorithmSupport.jcaSignatureName(algId);
            Signature s = Signature.getInstance(jcaAlg, prov);
            AlgorithmSupport.maybeInitPss(s, algId);
            s.initSign((PrivateKey) key);
            s.update(tbs);
            byte[] out = s.sign();
            return AlgorithmSupport.maybeConvertToDer(out, CertSigEncoding.DER, algId);
        } catch (KeyStoreException | InvalidAlgorithmParameterException | IOException | NoSuchAlgorithmException |
                 CertificateException | UnrecoverableKeyException | InvalidKeyException |
                 SignatureException e) {
            throw new Pkcs11Exception(e);
        } finally {
            Security.removeProvider(prov.getName());
        }
    }

    public static Provider buildSunPkcs11Provider(File module, Integer slot, String tokenLabel) throws Pkcs11Exception {
        StringBuilder cfg = new StringBuilder();
        cfg.append("--name=paccor-pkcs11\n");
        cfg.append("library=").append(module.getAbsolutePath()).append("\n");
        if (slot != null) {
            // Use slotListIndex instead of slot for more predictable behavior
            // slotListIndex=0 means the first initialized token found
            cfg.append("slotListIndex=").append(slot).append("\n");
        }
        if (tokenLabel != null && !tokenLabel.isEmpty()) {
            cfg.append("tokenLabel=").append(tokenLabel).append("\n");
        }
        try {
            Provider base = Security.getProvider("SunPKCS11");
            if (base == null) {
                throw new Pkcs11Exception("SunPKCS11 provider not available");
            }
            return base.configure(cfg.toString());
        } catch (Exception e) {
            throw new Pkcs11Exception("Failed to load SunPKCS11 provider", e);
        }
    }
}
