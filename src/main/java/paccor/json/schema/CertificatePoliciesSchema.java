package paccor.json.schema;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.PolicyQualifierId;
import paccor.tcg.credential.EnumWithStringValue;

public final class CertificatePoliciesSchema {
    public static final String CERTIFICATE_POLICIES = "certificatePolicies";
    public static final String POLICY_IDENTIFIER = "policyIdentifier";
    public static final String POLICY_QUALIFIERS = "policyQualifiers";
    public static final String POLICY_QUALIFIER_ID = "policyQualifierId";
    public static final String QUALIFIER = "qualifier";

    private CertificatePoliciesSchema() {}

    @Getter
    @RequiredArgsConstructor
    public enum PolicyField implements JsonSchemaField {
        POLICY_IDENTIFIER_FIELD(CertificatePoliciesSchema.POLICY_IDENTIFIER),
        POLICY_QUALIFIERS_FIELD(CertificatePoliciesSchema.POLICY_QUALIFIERS);

        private final String jsonName;
    }

    @Getter
    @RequiredArgsConstructor
    public enum QualifierField implements JsonSchemaField {
        POLICY_QUALIFIER_ID_FIELD(CertificatePoliciesSchema.POLICY_QUALIFIER_ID),
        QUALIFIER_FIELD(CertificatePoliciesSchema.QUALIFIER);

        private final String jsonName;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Qualifier implements EnumWithStringValue, JsonSchemaValue {
        CPS("cps", PolicyQualifierId.id_qt_cps.getId(), "Certification Practice Statement URI."),
        USER_NOTICE("userNotice", PolicyQualifierId.id_qt_unotice.getId(), "User notice text.");

        private final String jsonValue;
        private final String value;
        private final String description;

        public ASN1ObjectIdentifier oid() {
            return new ASN1ObjectIdentifier(value);
        }

        @Override
        public String asn1Value() {
            return value;
        }
    }
}
