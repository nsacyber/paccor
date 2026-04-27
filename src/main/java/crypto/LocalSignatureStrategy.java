package crypto;

import exception.InvalidKeyException;
import exception.PaccorException;
import exception.SignatureFailedException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;

public record LocalSignatureStrategy(File localKey, String localKeyPassword, File localKeyPasswordFile) implements SignatureStrategy {
    @Override
    public byte[] sign(byte[] content, AlgorithmIdentifier algId) throws PaccorException {
        algId = AlgorithmSupport.ensureNullParamsForEcdsa(algId);
        if (looksLikePkcs12(localKey)) {
            return signPkcs12(content, algId);
        }
        return SignatureService.sign(content, algId, localKey);
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    private byte[] signPkcs12(byte[] tbs, AlgorithmIdentifier algId) throws PaccorException {
        final X509Credential pkcs12;
        try {
            pkcs12 = Pkcs12Helper.loadPkcs12Key(localKey.getPath(), resolvePkcs12Password());
        } catch (IOException e) {
            throw new InvalidKeyException(localKey);
        }

        PrivateKey jceKey = pkcs12.jcePrivateKey();
        if (jceKey != null) {
            try {
                return AlgorithmSupport.signWithJca(tbs, algId, jceKey);
            } catch (GeneralSecurityException e) {
                if (pkcs12.privateKey() != null) {
                    return AlgorithmSupport.signWithBc(tbs, algId, pkcs12.privateKey());
                }
                throw new SignatureFailedException(e.getMessage(), e);
            }
        }

        PrivateKeyInfo pki = pkcs12.privateKey();
        if (pki != null) {
            return AlgorithmSupport.signWithBc(tbs, algId, pki);
        }

        throw new InvalidKeyException(localKey);
    }

    private char[] resolvePkcs12Password() throws PaccorException {
        if (localKeyPassword != null) {
            return localKeyPassword.toCharArray();
        }
        if (localKeyPasswordFile == null) {
            return null;
        }
        try {
            return Files.readString(localKeyPasswordFile.toPath(), StandardCharsets.UTF_8)
                    .replaceFirst("\\R+\\z", "")
                    .toCharArray();
        } catch (IOException e) {
            throw new SignatureFailedException("Could not read PKCS#12 password file", e);
        }
    }

    private boolean looksLikePkcs12(File keyFile) {
        String name = keyFile.getName().toLowerCase();
        return name.endsWith(".p12") || name.endsWith(".pfx") || name.endsWith(".pkcs12");
    }
}
