package paccor.json.schema;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AccessDescription;
import paccor.tcg.credential.EnumWithStringValue;

public final class AuthorityInformationAccessSchema {
    public static final String AUTHORITY_INFORMATION_ACCESS = "authorityInformationAccess";
    public static final String AUTHORITY_INFO_ACCESS = "authorityInfoAccess";
    public static final String ACCESS_METHOD = "accessMethod";
    public static final String ACCESS_LOCATION = "accessLocation";

    private AuthorityInformationAccessSchema() {}

    @Getter
    @RequiredArgsConstructor
    public enum Field implements JsonSchemaField {
        ACCESS_METHOD_FIELD(AuthorityInformationAccessSchema.ACCESS_METHOD),
        ACCESS_LOCATION_FIELD(AuthorityInformationAccessSchema.ACCESS_LOCATION);

        private final String jsonName;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Method implements EnumWithStringValue, JsonSchemaValue {
        OCSP("ocsp", AccessDescription.id_ad_ocsp.getId(), "Online Certificate Status Protocol responder."),
        CA_ISSUERS("caIssuers", AccessDescription.id_ad_caIssuers.getId(), "Location of issuer certificates.");

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

    @Getter
    public enum RootField implements JsonSchemaField {
        AUTHORITY_INFORMATION_ACCESS_FIELD(AuthorityInformationAccessSchema.AUTHORITY_INFORMATION_ACCESS,
                List.of(AuthorityInformationAccessSchema.AUTHORITY_INFO_ACCESS));

        private final String jsonName;
        private final List<String> aliases;

        RootField(String jsonName, List<String> aliases) {
            this.jsonName = jsonName;
            this.aliases = aliases;
        }
    }
}
