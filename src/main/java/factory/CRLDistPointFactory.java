package factory;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.DistributionPoint;
import tcg.credential.ASN1Utils;

/**
 * Functions to help manage the creation of the CRLDistributionPoints extension.
 */
public class CRLDistPointFactory {
    private final List<DistributionPoint> distPoints;

    private CRLDistPointFactory() {
        distPoints = new ArrayList<>();
    }

    /**
     * Begin creating a new CrlDistPoint object.
     * @return A new CRLDistPointFactory builder.
     */
    public static final CRLDistPointFactory create() {
        return new CRLDistPointFactory();
    }

    /**
     * Add another distribution point object.
     * @param dp {@link DistributionPoint}
     * @return The CRLDistPointFactory object with new distribution point added.
     */
    public final CRLDistPointFactory addDistributionPoint(DistributionPoint dp) {
        distPoints.add(dp);
        return this;
    }

    /**
     * Compile all of the data given to this factory.
     * @return {@link CRLDistPoint}
     */
    public final CRLDistPoint build() {
        return CRLDistPoint.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(distPoints)));
    }



    /**
     * Create a new CRLDistPoint object from a JSON node.
     * @param refNode JsonNode representing a project relevant CRL JSON object
     * @return The CRLDistPointFactory object with new information from the JSON data.
     */
    public static final CRLDistPointFactory fromJsonNode(final JsonNode refNode) {
        CRLDistPointFactory cdp = CRLDistPointFactory.create();
        if (refNode.isArray()) {
            for (final JsonNode destPointNode : refNode) {
                DistributionPointFactory dpf = DistributionPointFactory.fromJsonNode(destPointNode);
                cdp.addDistributionPoint(dpf.build());
            }
        }

        return cdp;
    }
}