package model;

import lombok.Builder;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.util.encoders.Base64;

@Builder
public record NameInfo(X500Name name, String nameDerB64) {
    public static NameInfo fromDerB64(String b64) {
        if (b64 == null) return null;
        try {
            return NameInfo.builder()
                    .name(X500Name.getInstance(Base64.decode(b64)))
                    .nameDerB64(b64)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    public X500Name resolvedName() {
        if (name != null) {
            return name;
        }
        if (nameDerB64 == null || nameDerB64.isBlank()) {
            return null;
        }
        try {
            return X500Name.getInstance(Base64.decode(nameDerB64));
        } catch (Exception e) {
            return null;
        }
    }

    public String describe() {
        X500Name resolved = resolvedName();
        if (resolved != null) {
            return resolved.toString();
        }
        return nameDerB64 != null && !nameDerB64.isBlank() ? "present" : "unknown";
    }
}
