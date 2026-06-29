package paccor.json;

import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.asn1.DERIA5String;
import paccor.json.schema.CrlDistributionPointsSchema;
import paccor.json.schema.JsonSchemaValue;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.ReasonFlags;
import paccor.tcg.credential.ASN1Utils;
import tools.jackson.databind.JsonNode;

public final class CrlDistributionPointsJson {
    private CrlDistributionPointsJson() {}

    public static CRLDistPoint read(JsonNode root) {
        List<DistributionPoint> points = new ArrayList<>();
        if (root != null && root.isArray()) {
            JsonUtils.asStream(root.spliterator())
                    .map(CrlDistributionPointsJson::readDistributionPoint)
                    .forEach(points::add);
        } else if (root != null && root.isObject()) {
            points.add(readDistributionPoint(root));
        }
        return CRLDistPoint.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(points)));
    }

    private static DistributionPoint readDistributionPoint(JsonNode node) {
        JsonNode distNameNode = JsonUtils.get(node, false, CrlDistributionPointsSchema.DistributionPointField.DISTRIBUTION_NAME_FIELD)
                .orElse(null);
        DistributionPointName distributionPointName = null;
        if (JsonUtils.has(distNameNode, false,
                CrlDistributionPointsSchema.DistributionNameField.TYPE_FIELD,
                CrlDistributionPointsSchema.DistributionNameField.NAME_FIELD)) {
            int type = JsonUtils.get(distNameNode, false, CrlDistributionPointsSchema.DistributionNameField.TYPE_FIELD)
                    .map(typeNode -> typeNode.isString()
                            ? Integer.parseInt(JsonSchemaValue.lookup(
                                    typeNode.asString(),
                                    CrlDistributionPointsSchema.DistributionNameType.class).asn1Value())
                            : typeNode.asInt())
                    .orElse(0);
            String name = JsonUtils.get(distNameNode, false, CrlDistributionPointsSchema.DistributionNameField.NAME_FIELD)
                    .flatMap(JsonUtils::trimmedIfText)
                    .orElse("");
            distributionPointName = new DistributionPointName(type, new GeneralNames(toDistributionPointName(type, name)));
        }

        int reason = JsonUtils.get(node, false, CrlDistributionPointsSchema.DistributionPointField.REASON_FIELD)
                .map(JsonNode::asInt)
                .orElse(0);
        String issuer = JsonUtils.get(node, false, CrlDistributionPointsSchema.DistributionPointField.ISSUER_FIELD)
                .flatMap(JsonUtils::trimmedIfText)
                .orElse("");

        return new DistributionPoint(
                distributionPointName,
                new ReasonFlags(reason),
                new GeneralNames(new GeneralName(new X500Name(issuer))));
    }

    private static GeneralName toDistributionPointName(int type, String name) {
        if (type == DistributionPointName.FULL_NAME) {
            return toGeneralName(name);
        }
        return new GeneralName(new X500Name(name));
    }

    private static GeneralName toGeneralName(String locationText) {
        try {
            return new GeneralName(new X500Name(locationText));
        } catch (IllegalArgumentException e) {
            return new GeneralName(GeneralName.uniformResourceIdentifier, new DERIA5String(locationText));
        }
    }
}
