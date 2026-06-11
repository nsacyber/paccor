package paccor.crypto;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Enumeration;
import java.util.List;
import paccor.normalization.HexNormalizer;

/**
 * Utility methods for resolving key aliases within a KeyStore when working with PKCS#11 tokens.
 */
public class Pkcs11AliasResolver {
    /**
     * Selects a key alias from the provided KeyStore based on the given parameters.
     * First checks for a specified alias.
     * Next looks for a key matching the hex key ID.
     * Otherwise, it attempts to select the first private key entry in the KeyStore.
     *
     * @param ks       the KeyStore
     * @param keyAlias the alias to use directly
     * @param keyIdHex the hex key ID, used for lookup if no alias is specified
     * @return the resolved alias
     * @throws KeyStoreException if no matching alias is found or if KeyStore operations fail
     */
    public static String selectAlias(KeyStore ks, String keyAlias, String keyIdHex) throws KeyStoreException {
        if (keyAlias != null) {
            return keyAlias;
        }

        if (keyIdHex != null) {
            return Pkcs11AliasResolver.handleCKA_IDLookup(ks, keyIdHex);
        }

        // Fallback: auto-select the first private key
        Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (ks.isKeyEntry(alias)) {
                return alias;
            }
        }
        throw new KeyStoreException("No private key entries on token");
    }

    /**
     * Attempts to look up a key alias using the given CKA_ID.
     * Uses several possible variations of the id to account for format differences with sample_testgen1 PKCS#11 providers.
     *
     * @param ks       the KeyStore
     * @param keyIdHex the hex CKA_ID to look up
     * @return the key alias
     * @throws KeyStoreException if no matching alias is found or if KeyStore operations fail
     */
    public static String handleCKA_IDLookup(KeyStore ks, String keyIdHex) throws KeyStoreException {
        // SunPKCS11 provider formats CKA_ID as a hex alias only when no CKA_LABEL exists.
        // SunPKCS11 requires both the private key and certificate to have matching CKA_ID, otherwise lookup will fail.
        // May fail where only the private key exists without a corresponding certificate object.
        // Trying common variations of hex representation.
        // Could consider a better library for CKA_ID lookup.
        List<String> candidates = HexNormalizer.generateHexVariants(keyIdHex);

        for (String candidate : candidates) {
            if (ks.containsAlias(candidate)) {
                return candidate;
            }
        }

        throw new KeyStoreException("No key found with CKA_ID: " + keyIdHex);
    }
}
