package paccor.crypto;

import java.io.IOException;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.crypto.util.PublicKeyFactory;

public class PqcHelper {
    public static final AsymmetricKeyParameter createKeyFromInfo(SubjectPublicKeyInfo pki) throws IOException {
        try {
            return PublicKeyFactory.createKey(pki);
        } catch (Exception ignored) {}
        try { // PQC algorithms use a sample_testgen1 method
            return org.bouncycastle.pqc.crypto.util.PublicKeyFactory.createKey(pki);
        } catch (Exception ignored) {}
        throw new IOException("Could not create key from PublicKeyInfo");
    }

    public static final AsymmetricKeyParameter createKeyFromInfo(PrivateKeyInfo pki) throws IOException {
        try {
            return PrivateKeyFactory.createKey(pki);
        } catch (Exception ignored) {}
        try { // PQC algorithms use a sample_testgen1 method
            return org.bouncycastle.pqc.crypto.util.PrivateKeyFactory.createKey(pki);
        } catch (Exception ignored) {}
        throw new IOException("Could not create key from PrivateKeyInfo");
    }
}
