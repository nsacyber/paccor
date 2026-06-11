package paccor.json;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import paccor.exception.JsonException;
import java.io.File;
import paccor.json.schema.AuthorityInformationAccessSchema;
import paccor.json.schema.CrlDistributionPointsSchema;
import paccor.json.schema.ExtensionsSchema;
import paccor.json.schema.TargetingInformationSchema;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.CRLDistPoint;
import org.bouncycastle.asn1.x509.CertificatePolicies;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.TargetInformation;
import tools.jackson.databind.annotation.JsonDeserialize;

@Builder(toBuilder = true)
@Jacksonized
@JsonClassDescription("Top-level JSON input for X.509 extension values accepted by this project.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
public record ExtensionsJsonHelper(
        @JsonProperty(ExtensionsSchema.CERTIFICATE_POLICIES)
        @JsonPropertyDescription("Certificate policies for the certificate.")
        @JsonDeserialize(using = CertificatePoliciesDeserializer.class)
        CertificatePolicies certificatePolicies,
        @JsonProperty(ExtensionsSchema.AUTHORITY_INFORMATION_ACCESS)
        @JsonAlias(AuthorityInformationAccessSchema.AUTHORITY_INFO_ACCESS)
        @JsonPropertyDescription("Authority Information Access extension.")
        @JsonDeserialize(using = AuthorityInformationAccessDeserializer.class)
        AuthorityInformationAccess authorityInformationAccess,
        @JsonProperty(ExtensionsSchema.CRL_DIST_POINT)
        @JsonAlias({CrlDistributionPointsSchema.CRL_DISTRIBUTION, CrlDistributionPointsSchema.CRL_DISTRIBUTION_POINTS_ALIAS})
        @JsonPropertyDescription("CRL Distribution Points extension.")
        @JsonDeserialize(using = CrlDeserializer.class)
        CRLDistPoint crlDistPoint,
        @JsonProperty(ExtensionsSchema.KEY_USAGE)
        @JsonPropertyDescription("Key Usage extension.")
        @JsonDeserialize(using = KeyUsageDeserializer.class)
        KeyUsage keyUsage,
        @JsonProperty(ExtensionsSchema.TARGETING_INFORMATION)
        @JsonAlias(TargetingInformationSchema.TARGET_INFORMATION)
        @JsonPropertyDescription("Target Information extension.")
        @JsonDeserialize(using = TargetingInformationDeserializer.class)
        TargetInformation targetingInformation) {
    public static final ExtensionsJsonHelper read(@NonNull final File jsonFile) throws JsonException {
        return ObjectMapperFactory.fromJson(jsonFile, ExtensionsJsonHelper.class);
    }

    public static final ExtensionsJsonHelper read(@NonNull final String json) throws JsonException {
        return ObjectMapperFactory.fromJson(json, ExtensionsJsonHelper.class);
    }
}
