package crypto;

import cli.CliHelper;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Optional;
import javax.security.auth.DestroyFailedException;
import lombok.NonNull;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;

public class Pkcs12Helper {
    /**
     * Preferred API for loading a single private key (and optional certificate) from a PKCS#12 file.
     * Prompts for password.
     * @param filename File name including the path.
     * @return X509Credential
     */
    public static final X509Credential loadPkcs12Key(final String filename) throws IOException {
        return loadPkcs12Key(filename, null);
    }

    /**
     * Preferred API for loading a single private key (and optional certificate) from a PKCS#12 file.
     * Uses the provided password when present, otherwise prompts interactively.
     * @param filename File name including the path.
     * @param password PKCS#12 password, or null to prompt.
     * @return X509Credential
     */
    public static final X509Credential loadPkcs12Key(final String filename, final char[] password) throws IOException {
        KeyStore.PasswordProtection pw = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filename);
            pw = password != null
                    ? CliHelper.passwordProtection(password)
                    : CliHelper.getPassword("Enter password for " + filename + ":");
            KeyStore p12 = KeyStore.getInstance("pkcs12");
            p12.load(fis, pw.getPassword());

            String alias = getKeyAlias(p12);
            PrivateKey jceKey = (PrivateKey) p12.getKey(alias, pw.getPassword());
            byte[] encoded = jceKey.getEncoded();
            PrivateKeyInfo keyInfo = encoded != null ? PrivateKeyInfo.getInstance(encoded) : null;
            X509CertificateHolder holder = getFirstCertForAlias(p12, alias);

            return new X509Credential(keyInfo, jceKey, holder);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        } finally {
            if (fis != null) {
                fis.close();
            }
            try {
                if (pw != null) {
                    pw.destroy();
                }
            } catch (DestroyFailedException ignored) {}
        }
    }

    private static String getKeyAlias(KeyStore p12) throws GeneralSecurityException, IOException {
        Optional<String> aliasOpt =
            Collections.list(p12.aliases()).stream()
                    .filter(alias -> isKeyEntryUnchecked(p12, alias))
                    .reduce((_, _) -> {
                        throw new RuntimeException("The key store is too complex. Looking for one key. See user guide or github for discussion.");
                    })
                ;

        if (aliasOpt.isEmpty()) {
            throw new IOException("No private key found in PKCS12 file provided.");
        }

        return aliasOpt.get();
    }

    private static boolean isKeyEntryUnchecked(KeyStore ks, String alias) {
        try {
            return ks.isKeyEntry(alias);
        } catch (KeyStoreException e) {
            return false;
        }
    }

    private static X509CertificateHolder getFirstCertForAlias(@NonNull KeyStore p12, @NonNull String alias) throws GeneralSecurityException, IOException {
        Certificate[] chain = p12.getCertificateChain(alias);
        if (chain == null || chain.length == 0 || !(chain[0] instanceof X509Certificate x509)) {
            return null;
        }
        return new X509CertificateHolder(x509.getEncoded());
    }
}
