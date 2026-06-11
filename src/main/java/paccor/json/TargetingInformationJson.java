package paccor.json;

import paccor.cli.CliHelper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import paccor.json.schema.TargetingInformationSchema;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.DERUTF8String;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.Target;
import org.bouncycastle.asn1.x509.TargetInformation;
import org.bouncycastle.cert.X509CertificateHolder;
import paccor.tcg.credential.ASN1Utils;
import tools.jackson.databind.JsonNode;

public final class TargetingInformationJson {
    private TargetingInformationJson() {}

    public static TargetInformation read(JsonNode root) {
        List<Target> elements = new ArrayList<>();
        if (root != null && root.isArray()) {
            JsonUtils.asStream(root.spliterator())
                    .map(TargetingInformationJson::readTarget)
                    .filter(Objects::nonNull)
                    .forEach(elements::add);
        }
        return TargetInformation.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(elements)));
    }

    private static Target readTarget(JsonNode node) {
        String filename = JsonUtils.get(node, false, TargetingInformationSchema.Field.FILE_FIELD)
                .flatMap(JsonUtils::trimmedIfText)
                .orElse(null);
        if (filename == null) {
            return null;
        }
        try {
            X509CertificateHolder cert = CliHelper.loadCert(filename, CliHelper.x509type.CERTIFICATE);
            return fromCertificate(cert);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Target fromCertificate(X509CertificateHolder cert) {
        X500Name subjectName = cert.getSubject();
        if (subjectName == null || cert.getSerialNumber() == null) {
            throw new IllegalArgumentException("The target information extension cannot use the provided certificate. It is missing vital information.");
        }
        if (subjectName.getRDNs(BCStyle.SERIALNUMBER).length == 0) {
            DERUTF8String serialNumber = new DERUTF8String(cert.getSerialNumber().toString());
            AttributeTypeAndValue serialNumberATV = new AttributeTypeAndValue(BCStyle.SERIALNUMBER, serialNumber);
            List<RDN> rdnList = new ArrayList<>(List.of(subjectName.getRDNs()));
            rdnList.add(new RDN(serialNumberATV));
            subjectName = new X500Name(rdnList.toArray(new RDN[0]));
        }
        return new Target(Target.targetName, new GeneralName(subjectName));
    }
}
