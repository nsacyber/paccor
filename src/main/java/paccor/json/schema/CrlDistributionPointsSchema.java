package paccor.json.schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x509.DistributionPointName;

public final class CrlDistributionPointsSchema {
    public static final String CRL_DISTRIBUTION_POINTS = "crlDistPoint";
    public static final String CRL_DISTRIBUTION = "crlDistribution";
    public static final String CRL_DISTRIBUTION_POINTS_ALIAS = "crlDistributionPoints";
    public static final String DISTRIBUTION_NAME = "distributionName";
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String CRL_URI = "crlUri";
    public static final String REASON = "reason";
    public static final String ISSUER = "issuer";

    private CrlDistributionPointsSchema() {}

    @AllArgsConstructor
    @Getter
    public enum RootField implements JsonSchemaField {
        CRL_DISTRIBUTION_POINTS_FIELD(CrlDistributionPointsSchema.CRL_DISTRIBUTION_POINTS,
                List.of(CrlDistributionPointsSchema.CRL_DISTRIBUTION, CrlDistributionPointsSchema.CRL_DISTRIBUTION_POINTS_ALIAS));

        private final String jsonName;
        private final List<String> aliases;

        RootField(String jsonName) {
            this(jsonName, List.of());
        }
    }

    @AllArgsConstructor
    @Getter
    public enum DistributionPointField implements JsonSchemaField {
        DISTRIBUTION_NAME_FIELD(CrlDistributionPointsSchema.DISTRIBUTION_NAME),
        REASON_FIELD(CrlDistributionPointsSchema.REASON),
        ISSUER_FIELD(CrlDistributionPointsSchema.ISSUER);

        private final String jsonName;
    }

    @AllArgsConstructor
    @Getter
    public enum DistributionNameField implements JsonSchemaField {
        TYPE_FIELD(CrlDistributionPointsSchema.TYPE),
        NAME_FIELD(CrlDistributionPointsSchema.NAME, JsonSchemaField.aliasList(CrlDistributionPointsSchema.CRL_URI));

        private final String jsonName;
        private final List<String> aliases;

        DistributionNameField(String jsonName) {
            this(jsonName, List.of());
        }
    }

    @Getter
    @RequiredArgsConstructor
    public enum DistributionNameType implements JsonSchemaValue {
        FULL_NAME("fullName", Integer.toString(DistributionPointName.FULL_NAME),
                "GeneralNames form used for a full distribution point name."),
        NAME_RELATIVE_TO_CRL_ISSUER("nameRelativeToCrlIssuer",
                Integer.toString(DistributionPointName.NAME_RELATIVE_TO_CRL_ISSUER),
                "Name relative to the CRL issuer.");

        private final String jsonValue;
        private final String asn1Value;
        private final String description;
    }
}
