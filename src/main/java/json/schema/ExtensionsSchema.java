package json.schema;

import java.util.List;
import lombok.Getter;

public final class ExtensionsSchema {
    public static final String CERTIFICATE_POLICIES = "certificatePolicies";
    public static final String AUTHORITY_INFORMATION_ACCESS = "authorityInformationAccess";
    public static final String CRL_DIST_POINT = "crlDistPoint";
    public static final String KEY_USAGE = "keyUsage";
    public static final String TARGETING_INFORMATION = "targetingInformation";

    private ExtensionsSchema() {}

    @Getter
    public enum Field implements JsonSchemaField {
        CERTIFICATE_POLICIES_FIELD(ExtensionsSchema.CERTIFICATE_POLICIES),
        AUTHORITY_INFORMATION_ACCESS_FIELD(ExtensionsSchema.AUTHORITY_INFORMATION_ACCESS,
                List.of(AuthorityInformationAccessSchema.AUTHORITY_INFO_ACCESS)),
        CRL_DIST_POINT_FIELD(ExtensionsSchema.CRL_DIST_POINT,
                List.of(CrlDistributionPointsSchema.CRL_DISTRIBUTION, CrlDistributionPointsSchema.CRL_DISTRIBUTION_POINTS_ALIAS)),
        KEY_USAGE_FIELD(ExtensionsSchema.KEY_USAGE),
        TARGETING_INFORMATION_FIELD(ExtensionsSchema.TARGETING_INFORMATION,
                List.of(TargetingInformationSchema.TARGET_INFORMATION));

        private final String jsonName;
        private final List<String> aliases;

        Field(String jsonName) {
            this(jsonName, List.of());
        }

        Field(String jsonName, List<String> aliases) {
            this.jsonName = jsonName;
            this.aliases = aliases;
        }
    }
}
