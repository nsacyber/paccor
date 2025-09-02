package factory;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import json.JsonUtils;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.DisplayText;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.PolicyQualifierId;
import org.bouncycastle.asn1.x509.PolicyQualifierInfo;
import org.bouncycastle.asn1.x509.UserNotice;
import tcg.credential.ASN1Utils;

/**
 * Functions to help manage the creation of a policy information object.
 */
public class PolicyInformationFactory {
    private static final Map<QualifierJson, BiConsumer<PolicyInformationFactory, JsonNode>> QUALIFIER_ACTIONS =
            Map.of(
                    PolicyInformationFactory.QualifierJson.CPS,
                    (pif, qualifierNode) -> pif.addQualifier(
                            PolicyInformationFactory.QualifierJson.CPS,
                            new DERIA5String(qualifierNode.asText())
                    ),
                    PolicyInformationFactory.QualifierJson.USERNOTICE,
                    (pif, qualifierNode) -> pif.addQualifier(
                            PolicyInformationFactory.QualifierJson.USERNOTICE,
                            new UserNotice(null, qualifierNode.asText())
                    )
            );
    /**
     * fields within each policy information JSON object
     */
    public enum Json {
        POLICYIDENTIFIER,
        POLICYQUALIFIERS,
        POLICYQUALIFIERID,
        QUALIFIER;
    }

    /**
     * qualifier options
     */
    public enum QualifierJson {
        CPS(PolicyQualifierId.id_qt_cps),
        USERNOTICE(PolicyQualifierId.id_qt_unotice);

        private ASN1ObjectIdentifier oid;

        QualifierJson(ASN1ObjectIdentifier oid) {
            this.oid = oid;
        }

        public ASN1ObjectIdentifier getOid() {
            return oid;
        }
    }

    private ASN1ObjectIdentifier oid;
    private ArrayList<PolicyQualifierInfo> qualifiers;

    private PolicyInformationFactory() {
        oid = null;
        qualifiers = new ArrayList<>();
    }

    /**
     * Begin defining the policy information object.
     * @return A new PolicyInformationFactory builder.
     */
    public static final PolicyInformationFactory create() {
        return new PolicyInformationFactory();
    }

    /**
     * Set the policy OID.
     * @param oid {@link ASN1ObjectIdentifier}
     * @return The PolicyInformationFactory object with the policy identifier OID set.
     */
    public final PolicyInformationFactory policyIdentifier(final ASN1ObjectIdentifier oid) {
        this.oid = oid;
        return this;
    }

    /**
     * Add a policy qualifier.
     * @param qualifier {@link PolicyQualifierInfo}
     * @return The PolicyInformationFactory object with the policy qualifier information set.
     */
    public final PolicyInformationFactory addQualifier(final PolicyQualifierInfo qualifier) {
        qualifiers.add(qualifier);
        return this;
    }

    /**
     * Add a policy qualifier using the enumerated qualifier list.
     * @param type {@link PolicyInformationFactory.QualifierJson}
     * @param value {@link ASN1Encodable}
     * @return The PolicyInformationFactory object with a new qualifier added.
     */
    public final PolicyInformationFactory addQualifier(final QualifierJson type, final ASN1Encodable value) {
        qualifiers.add(new PolicyQualifierInfo(type.getOid(), value));
        return this;
    }

    /**
     * Compile all of the data given to this factory.
     * @return {@link PolicyInformation}
     */
    public final PolicyInformation build() {
        return new PolicyInformation(oid, new DERSequence(ASN1Utils.toASN1EncodableVector(qualifiers)));
    }

    /**
     * Create a new policy information object from a JSON node.
     * @param refNode JsonNode representing a policy information JSON object
     * @return The PolicyInformationFactory object with new information from the JSON data.
     */
    public static final PolicyInformationFactory fromJsonNode(final JsonNode refNode) {
        PolicyInformationFactory pif = PolicyInformationFactory.create();
        boolean caseSens = false;
        if (JsonUtils.has(refNode, caseSens, PolicyInformationFactory.Json.POLICYIDENTIFIER.name(), PolicyInformationFactory.Json.POLICYQUALIFIERS.name())) {
            final JsonNode oidNode = JsonUtils.get(refNode, caseSens, PolicyInformationFactory.Json.POLICYIDENTIFIER.name()).orElseThrow();
            final JsonNode qualifiersArray = JsonUtils.get(refNode, caseSens, PolicyInformationFactory.Json.POLICYQUALIFIERS.name()).orElseThrow();

            pif.policyIdentifier(new ASN1ObjectIdentifier(oidNode.asText()));
            if (qualifiersArray.isArray()) {
                JsonUtils.asStream(qualifiersArray.spliterator())
                        .filter(qualifierNode -> JsonUtils.has(qualifierNode, caseSens, PolicyInformationFactory.Json.POLICYQUALIFIERID.name(), PolicyInformationFactory.Json.QUALIFIER.name()))
                        .forEach(qualifierNode -> {
                            Optional<JsonNode> policyQualifierIdOpt = JsonUtils.get(qualifierNode, caseSens, PolicyInformationFactory.Json.POLICYQUALIFIERID.name());
                            Optional<JsonNode> policyQualifierNodeOpt = JsonUtils.get(qualifierNode, caseSens, PolicyInformationFactory.Json.QUALIFIER.name());

                            policyQualifierIdOpt.ifPresent(policyQualifierId ->
                                    policyQualifierNodeOpt.ifPresent(policyQualifierNode -> {
                                        PolicyInformationFactory.QualifierJson type = PolicyInformationFactory.QualifierJson.valueOf(policyQualifierId.asText());
                                        QUALIFIER_ACTIONS.getOrDefault(type, (factory, node) -> {/*irrelevant qualifier type*/})
                                                .accept(pif, policyQualifierNode);
                                    }
                            )); // if nodes present
                        }); // forEach
            }
        }
        return pif;
    }
}
