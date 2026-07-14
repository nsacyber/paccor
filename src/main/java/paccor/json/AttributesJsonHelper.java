package paccor.json;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import paccor.exception.JsonException;
import java.io.File;
import paccor.json.schema.AttributesSchema;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.ASN1UTF8String;
import paccor.tcg.credential.TBBSecurityAssertions;
import paccor.tcg.credential.TCPASpecVersion;
import paccor.tcg.credential.TCGPlatformSpecification;
import paccor.tcg.credential.TCGSpecificationVersion;
import paccor.tcg.credential.TPMSecurityAssertions;
import paccor.tcg.credential.TPMSpecification;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.URIReference;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * Top-level JSON input for platform attributes documentation and parsing.
 * @param tCGPlatformSpecification {@link TCGPlatformSpecification}
 * @param tCGCredentialSpecification {@link TCGSpecificationVersion}
 * @param tBBSecurityAssertions {@link TBBSecurityAssertions}
 * @param tCPASpecVersion legacy V1_0 TCPA spec version
 * @param tPMManufacturer legacy V1_0 TPM manufacturer
 * @param tPMModel legacy V1_0 TPM model
 * @param tPMVersion legacy V1_0 TPM version
 * @param tPMSpecification legacy V1_0 TPM specification
 * @param tPMSecurityAssertions legacy V1_0 TPM security assertions
 * @param platformConfigUri {@link URIReference}
 * @param previousPlatformCertificates Traits
 * @param cryptographicAnchors Traits
 */
@Builder(toBuilder = true)
@Jacksonized
@JsonClassDescription("JSON input for platform attributes.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
public record AttributesJsonHelper(
        @JsonProperty(AttributesSchema.TCG_PLATFORM_SPECIFICATION)
        TCGPlatformSpecification tCGPlatformSpecification,
        @JsonProperty(AttributesSchema.TCG_CREDENTIAL_SPECIFICATION)
        TCGSpecificationVersion tCGCredentialSpecification,
        @JsonProperty(AttributesSchema.TBB_SECURITY_ASSERTIONS)
        TBBSecurityAssertions tBBSecurityAssertions,
        @JsonProperty(AttributesSchema.PLATFORM_CONFIG_URI)
        URIReference platformConfigUri,
        @JsonProperty(AttributesSchema.PREVIOUS_PLATFORM_CERTIFICATES)
        @JsonDeserialize(using = PreviousPlatformCertificatesDeserializer.class)
        TraitMap previousPlatformCertificates,
        @JsonProperty(AttributesSchema.CRYPTOGRAPHIC_ANCHORS)
        @JsonDeserialize(using = CryptographicAnchorsDeserializer.class)
        TraitMap cryptographicAnchors,
        @JsonProperty(AttributesSchema.PLATFORM_OWNERSHIP)
        TraitMap platformOwnership,
        @JsonProperty(AttributesSchema.MANUFACTURING_ASSERTIONS)
        TraitMap manufacturingAssertions,
        // uncommon fields, V1.0 fields
        @JsonProperty(AttributesSchema.TCPA_SPEC_VERSION)
        TCPASpecVersion tCPASpecVersion,
        @JsonProperty(AttributesSchema.TPM_MANUFACTURER)
        ASN1UTF8String tPMManufacturer,
        @JsonProperty(AttributesSchema.TPM_MODEL)
        ASN1UTF8String tPMModel,
        @JsonProperty(AttributesSchema.TPM_VERSION)
        ASN1UTF8String tPMVersion,
        @JsonProperty(AttributesSchema.TPM_SPECIFICATION)
        TPMSpecification tPMSpecification,
        @JsonProperty(AttributesSchema.TPM_SECURITY_ASSERTIONS)
        TPMSecurityAssertions tPMSecurityAssertions) {
    /**
     * Read platform attributes JSON from a file.
     * @param jsonFile JSON file containing platform attributes
     * @return AttributesJsonHelper instance
     * @throws JsonException if JSON parsing fails
     */
    public static final AttributesJsonHelper read(@NonNull final File jsonFile) throws JsonException {
        return ObjectMapperFactory.fromJson(jsonFile, AttributesJsonHelper.class);
    }

    /**
     * Read platform attributes JSON from a string.
     * @param json JSON string containing platform attributes
     * @return AttributesJsonHelper instance
     * @throws JsonException if JSON parsing fails
     */
    public static final AttributesJsonHelper read(@NonNull final String json) throws JsonException {
        return ObjectMapperFactory.fromJson(json, AttributesJsonHelper.class);
    }
}
