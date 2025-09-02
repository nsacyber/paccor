package json.schema;

import lombok.Getter;

public final class AttributesSchema {
    public static final String TCG_PLATFORM_SPECIFICATION = "tCGPlatformSpecification";
    public static final String TCG_CREDENTIAL_SPECIFICATION = "tCGCredentialSpecification";
    public static final String TBB_SECURITY_ASSERTIONS = "tBBSecurityAssertions";
    public static final String PLATFORM_CONFIG_URI = "platformConfigUri";
    public static final String PREVIOUS_PLATFORM_CERTIFICATES = "previousPlatformCertificates";
    public static final String CRYPTOGRAPHIC_ANCHORS = "cryptographicAnchors";
    public static final String TCPA_SPEC_VERSION = "tCPASpecVersion";
    public static final String TPM_MANUFACTURER = "tPMManufacturer";
    public static final String TPM_MODEL = "tPMModel";
    public static final String TPM_VERSION = "tPMVersion";
    public static final String TPM_SPECIFICATION = "tPMSpecification";
    public static final String TPM_SECURITY_ASSERTIONS = "tPMSecurityAssertions";

    private AttributesSchema() {}

    @Getter
    public enum Field implements JsonSchemaField {
        TCG_PLATFORM_SPECIFICATION_FIELD(AttributesSchema.TCG_PLATFORM_SPECIFICATION,
                "TCG Platform Specification. Version and platform class."),
        TCG_CREDENTIAL_SPECIFICATION_FIELD(AttributesSchema.TCG_CREDENTIAL_SPECIFICATION,
                "TCG Credential Specification version. Major, Minor, Revision."),
        TBB_SECURITY_ASSERTIONS_FIELD(AttributesSchema.TBB_SECURITY_ASSERTIONS,
                "TBB Security assertions. CC, FIPS, RTM, ISO9000."),
        PLATFORM_CONFIG_URI_FIELD(AttributesSchema.PLATFORM_CONFIG_URI,
                "Platform configuration URI."),
        PREVIOUS_PLATFORM_CERTIFICATES_FIELD(AttributesSchema.PREVIOUS_PLATFORM_CERTIFICATES,
                "List of previous platform certificates. Accepts explicit trait JSON or FILE-backed certificate entries."),
        CRYPTOGRAPHIC_ANCHORS_FIELD(AttributesSchema.CRYPTOGRAPHIC_ANCHORS,
                "Cryptographic anchors for the platform. Accepts explicit trait JSON or FILE-backed certificate entries. FILE entries should use CertificateIdentifierTrait categories such as EK, DICE, or SPDM certificate categories."),
        TCPA_SPEC_VERSION_FIELD(AttributesSchema.TCPA_SPEC_VERSION,
                "Legacy v1.0 TCPA specification version."),
        TPM_MANUFACTURER_FIELD(AttributesSchema.TPM_MANUFACTURER,
                "Legacy v1.0 TPM manufacturer string."),
        TPM_MODEL_FIELD(AttributesSchema.TPM_MODEL,
                "Legacy v1.0 TPM model string."),
        TPM_VERSION_FIELD(AttributesSchema.TPM_VERSION,
                "Legacy v1.0 TPM version string."),
        TPM_SPECIFICATION_FIELD(AttributesSchema.TPM_SPECIFICATION,
                "Legacy v1.0 TPM specification details."),
        TPM_SECURITY_ASSERTIONS_FIELD(AttributesSchema.TPM_SECURITY_ASSERTIONS,
                "Legacy v1.0 TPM security assertions.");

        private final String jsonName;
        private final String descriptionText;

        Field(String jsonName, String descriptionText) {
            this.jsonName = jsonName;
            this.descriptionText = descriptionText;
        }

        @Override
        public String description() {
            return descriptionText;
        }
    }
}
