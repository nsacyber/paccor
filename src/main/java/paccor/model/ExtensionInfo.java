package paccor.model;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.bouncycastle.util.encoders.Base64;

@Builder
public record ExtensionInfo (String oid,String valueDerB64,boolean critical, String name) {
    public static final Map<String, ExtensionInfo> extractExtensionInfo(Extensions extensions) {
        if (extensions == null) {
            return Map.of();
        }

        Map<String, ExtensionInfo> result = new HashMap<>();
        Enumeration<?> oids = extensions.oids();
        while (oids.hasMoreElements()) {
            Object next = oids.nextElement();
            if (!(next instanceof ASN1ObjectIdentifier oid)) {
                continue;
            }
            Extension extension = extensions.getExtension(oid);
            if (extension == null || extension.getExtnValue() == null) {
                continue;
            }
            result.put(oid.getId(), ExtensionInfo.builder()
                    .oid(oid.getId())
                    .critical(extension.isCritical())
                    .valueDerB64(Base64.toBase64String(extension.getExtnValue().getOctets()))
                    .name(displayName(oid))
                    .build());
        }
        return result;
    }

    public static String displayName(ASN1ObjectIdentifier oid) {
        if (oid == null) {
            return null;
        }
        return switch (oid.getId()) {
            case "2.5.29.17" -> "Subject Alternative Name";
            case "2.5.29.35" -> "Authority Key Identifier";
            case "2.5.29.32" -> "Certificate Policies";
            case "2.5.29.15" -> "Key Usage";
            case "2.5.29.37" -> "Extended Key Usage";
            case "2.5.29.55" -> "Targeting Information";
            case "2.5.29.14" -> "Subject Key Identifier";
            case "2.5.29.19" -> "Basic Constraints";
            case "2.5.29.31" -> "CRL Distribution Points";
            case "1.3.6.1.5.5.7.1.1" -> "Authority Info Access";
            default -> oid.getId();
        };
    }
}
