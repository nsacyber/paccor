package factory;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.function.BiConsumer;
import json.JsonUtils;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.DisplayText;
import org.bouncycastle.asn1.x509.DistributionPoint;
import org.bouncycastle.asn1.x509.DistributionPointName;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.ReasonFlags;

/**
 * Functions to help manage the creation of a distribution point object.
 */
public class DistributionPointFactory {
    public enum CrlJson {
        DISTRIBUTIONNAME,
        TYPE,
        NAME,
        REASON,
        ISSUER;
    }

    private DistributionPointName distributionPoint;
    private ReasonFlags reasons;
    private GeneralNames cRLIssuer;

    private DistributionPointFactory() {
        distributionPoint = null;
        reasons = null;
        cRLIssuer = null;
    }

    /**
     * Begin defining the distribution point object.
     * @return A new DistributionPointFactory builder.
     */
    public static final DistributionPointFactory create() {
        return new DistributionPointFactory();
    }

    /**
     * Set DistributionPointName.
     * @param dpn {@link DistributionPointName}
     * @return The DistributionPointFactory object with a new DistributionPointName set.
     */
    public final DistributionPointFactory distributionPointName(final DistributionPointName dpn) {
        this.distributionPoint = dpn;
        return this;
    }

    /**
     * Set the reason flags.
     * @param rf {@link ReasonFlags}
     * @return The DistributionPointFactory object with the reasons set.
     */
    public final DistributionPointFactory reasons(final ReasonFlags rf) {
        this.reasons = rf;
        return this;
    }

    /**
     * Set the CRL issuer.
     * @param ci {@link GeneralNames}
     * @return The DistributionPointFactory object with the reasons set.
     */
    public final DistributionPointFactory cRLIssuer(final GeneralNames ci) {
        this.cRLIssuer = ci;
        return this;
    }

    /**
     * Compile all of the data given to this factory.
     * @return {@link DistributionPoint}
     */
    public final DistributionPoint build() {
        return new DistributionPoint(distributionPoint, reasons, cRLIssuer);
    }

    /**
     * Create a new distribution point object from a JSON node.
     * @param refNode JsonNode representing a distribution point JSON object
     * @return The DistributionPointFactory object with new information from the JSON data.
     */
    public static final DistributionPointFactory fromJsonNode(final JsonNode refNode) {
        DistributionPointFactory dpf = DistributionPointFactory.create();
        boolean caseSens = false;
        if (JsonUtils.has(refNode, caseSens, DistributionPointFactory.CrlJson.DISTRIBUTIONNAME.name(), DistributionPointFactory.CrlJson.REASON.name(), DistributionPointFactory.CrlJson.ISSUER.name())) {
            final JsonNode distNameNode = JsonUtils.get(refNode, caseSens, DistributionPointFactory.CrlJson.DISTRIBUTIONNAME.name()).orElseThrow();
            final JsonNode reasonNode = JsonUtils.get(refNode, caseSens, DistributionPointFactory.CrlJson.REASON.name()).orElseThrow();
            final JsonNode issuerNode = JsonUtils.get(refNode, caseSens, DistributionPointFactory.CrlJson.ISSUER.name()).orElseThrow();

            DistributionPointName dpn = null;
            if (JsonUtils.has(distNameNode, caseSens, DistributionPointFactory.CrlJson.TYPE.name(), DistributionPointFactory.CrlJson.NAME.name())) {
                final JsonNode typeNode = JsonUtils.get(distNameNode, caseSens, DistributionPointFactory.CrlJson.TYPE.name()).orElseThrow();
                final JsonNode nameNode = JsonUtils.get(distNameNode, caseSens, DistributionPointFactory.CrlJson.NAME.name()).orElseThrow();

                dpn = new DistributionPointName(typeNode.asInt(), new GeneralNames(new GeneralName(new X500Name(nameNode.asText()))));
            }

            dpf.distributionPointName(dpn);
            dpf.reasons(new ReasonFlags(reasonNode.asInt()));
            dpf.cRLIssuer(new GeneralNames(new GeneralName(new X500Name(issuerNode.asText()))));
        }

        return dpf;
    }
}