package model;

import lombok.Builder;
import org.bouncycastle.asn1.x509.Holder;
import org.bouncycastle.util.encoders.Base64;

@Builder
public record HolderInfo(Holder holder, String holderDerB64) {
    public static HolderInfo fromDerB64(String b64) {
        if (b64 == null) return null;
        try {
            return HolderInfo.builder()
                    .holder(Holder.getInstance(Base64.decode(b64)))
                    .holderDerB64(b64)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    public Holder resolvedHolder() {
        if (holder != null) {
            return holder;
        }
        if (holderDerB64 == null || holderDerB64.isBlank()) {
            return null;
        }
        try {
            return Holder.getInstance(Base64.decode(holderDerB64));
        } catch (Exception e) {
            return null;
        }
    }

    public String describe() {
        Holder resolved = resolvedHolder();
        if (resolved != null) {
            return "Issuer: " + resolved.getBaseCertificateID().getIssuer().toString() + ", Serial: " + resolved.getBaseCertificateID().getSerial().toString();
        }
        return holderDerB64 != null && !holderDerB64.isBlank() ? "present" : "unknown";
    }
}
