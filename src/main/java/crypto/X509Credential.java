package crypto;

import java.security.PrivateKey;
import lombok.Builder;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;

@Builder
public record X509Credential(PrivateKeyInfo privateKey, PrivateKey jcePrivateKey, X509CertificateHolder certificate) {
}
