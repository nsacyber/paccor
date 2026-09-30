package paccor.model;

import java.io.IOException;
import lombok.Builder;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.encoders.Base64;

@Builder
public record SubjectInfo (NameInfo nameInfo, String subjectPublicKeyInfoDerB64) {
    public static SubjectInfo from(String nameDerB64, String spkiDerB64) {
        X500Name name = (nameDerB64 != null) ? X500Name.getInstance(Base64.decode(nameDerB64)) : null;
        return SubjectInfo.builder()
                .nameInfo(NameInfo.builder()
                        .name(name)
                        .nameDerB64(nameDerB64)
                        .build())
                .subjectPublicKeyInfoDerB64(spkiDerB64)
                .build();
    }

    /**
     * Create a subject.
     * @param subject current subject, or null
     * @param name subject distinguished name
     * @return the subject with its name replaced and any public key kept
     */
    public static SubjectInfo withName(SubjectInfo subject, X500Name name) throws IOException {
        return SubjectInfo.builder()
                .nameInfo(NameInfo.builder()
                        .name(null)
                        .nameDerB64(Base64.toBase64String(name.getEncoded()))
                        .build())
                .subjectPublicKeyInfoDerB64(subject != null ? subject.subjectPublicKeyInfoDerB64() : null)
                .build();
    }

    /**
     * Create a subject.
     * @param publicKey subject public key
     * @return this subject with its public key replaced and its name kept
     */
    public SubjectInfo withPublicKey(SubjectPublicKeyInfo publicKey) throws IOException {
        return SubjectInfo.builder()
                .nameInfo(nameInfo)
                .subjectPublicKeyInfoDerB64(Base64.toBase64String(publicKey.getEncoded()))
                .build();
    }

    public X500Name resolvedSubjectName() {
        if (nameInfo == null) {
            return null;
        }
        return nameInfo.resolvedName();
    }

    public String describe() {
        X500Name resolved = resolvedSubjectName();
        if (resolved != null) {
            return resolved.toString();
        }
        return subjectPublicKeyInfoDerB64 != null && !subjectPublicKeyInfoDerB64.isBlank() ? "present" : "unknown";
    }
}
