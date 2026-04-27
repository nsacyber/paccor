package json;

import java.util.ArrayList;
import java.util.List;
import json.schema.CertificatePoliciesSchema;
import json.schema.JsonSchemaValue;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.PolicyInformation;
import org.bouncycastle.asn1.x509.PolicyQualifierInfo;
import org.bouncycastle.asn1.x509.UserNotice;
import tcg.credential.ASN1Utils;
import tools.jackson.databind.JsonNode;

public final class CertificatePoliciesJson {
    private CertificatePoliciesJson() {}

    public static CertificatePolicies read(JsonNode root) {
        List<PolicyInformation> policies = new ArrayList<>();
        if (root != null && root.isArray()) {
            JsonUtils.asStream(root.spliterator())
                    .map(CertificatePoliciesJson::readPolicyInformation)
                    .forEach(policies::add);
        }
        return CertificatePolicies.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(policies)));
    }

    private static PolicyInformation readPolicyInformation(JsonNode node) {
        ASN1ObjectIdentifier oid = JsonUtils.get(node, false, CertificatePoliciesSchema.PolicyField.POLICY_IDENTIFIER_FIELD)
                .flatMap(JsonUtils::trimmedIfText)
                .map(ASN1ObjectIdentifier::new)
                .orElse(null);
        List<PolicyQualifierInfo> qualifiers = new ArrayList<>();
        JsonUtils.get(node, false, CertificatePoliciesSchema.PolicyField.POLICY_QUALIFIERS_FIELD)
                .filter(JsonNode::isArray)
                .ifPresent(array -> JsonUtils.asStream(array.spliterator())
                        .map(CertificatePoliciesJson::readPolicyQualifier)
                        .forEach(qualifiers::add));
        return new PolicyInformation(oid, new DERSequence(ASN1Utils.toASN1EncodableVector(qualifiers)));
    }

    private static PolicyQualifierInfo readPolicyQualifier(JsonNode node) {
        CertificatePoliciesSchema.Qualifier qualifier = JsonUtils.get(node, false, CertificatePoliciesSchema.QualifierField.POLICY_QUALIFIER_ID_FIELD)
                .flatMap(JsonUtils::trimmedIfText)
                .map(value -> JsonSchemaValue.lookup(value, CertificatePoliciesSchema.Qualifier.class))
                .orElseThrow(() -> new IllegalArgumentException("Policy qualifier id is required"));
        String qualifierValue = JsonUtils.get(node, false, CertificatePoliciesSchema.QualifierField.QUALIFIER_FIELD)
                .flatMap(JsonUtils::trimmedIfText)
                .orElse("");
        return switch (qualifier) {
            case CPS -> new PolicyQualifierInfo(qualifier.oid(), new DERIA5String(qualifierValue));
            case USER_NOTICE -> new PolicyQualifierInfo(qualifier.oid(), new UserNotice(null, qualifierValue));
        };
    }
}
