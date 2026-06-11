package paccor.model;

import lombok.Builder;
import org.bouncycastle.asn1.x500.X500Name;
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
