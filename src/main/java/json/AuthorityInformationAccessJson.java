package json;

import java.util.ArrayList;
import java.util.List;
import org.bouncycastle.asn1.DERIA5String;
import json.schema.AuthorityInformationAccessSchema;
import json.schema.JsonSchemaValue;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.GeneralName;
import tcg.credential.ASN1Utils;
import tools.jackson.databind.JsonNode;

public final class AuthorityInformationAccessJson {
    private AuthorityInformationAccessJson() {}

    public static AuthorityInformationAccess read(JsonNode root) {
        List<AccessDescription> elements = new ArrayList<>();
        if (root == null || !root.isArray()) {
            return AuthorityInformationAccess.getInstance(new DERSequence());
        }

        JsonUtils.asStream(root.spliterator())
                .filter(node -> JsonUtils.has(node, false,
                        AuthorityInformationAccessSchema.Field.ACCESS_METHOD_FIELD,
                        AuthorityInformationAccessSchema.Field.ACCESS_LOCATION_FIELD))
                .forEach(node -> {
                    String methodText = JsonUtils.get(node, false, AuthorityInformationAccessSchema.Field.ACCESS_METHOD_FIELD)
                            .flatMap(JsonUtils::trimmedIfText)
                            .orElse(null);
                    String locationText = JsonUtils.get(node, false, AuthorityInformationAccessSchema.Field.ACCESS_LOCATION_FIELD)
                            .flatMap(JsonUtils::trimmedIfText)
                            .orElse(null);
                    if (methodText == null || locationText == null) {
                        return;
                    }
                    AuthorityInformationAccessSchema.Method method =
                            JsonSchemaValue.lookup(methodText, AuthorityInformationAccessSchema.Method.class);
                    elements.add(new AccessDescription(method.oid(), toGeneralName(locationText)));
                });

        return AuthorityInformationAccess.getInstance(new DERSequence(ASN1Utils.toASN1EncodableVector(elements)));
    }

    private static GeneralName toGeneralName(String locationText) {
        try {
            return new GeneralName(new X500Name(locationText));
        } catch (IllegalArgumentException e) {
            return new GeneralName(GeneralName.uniformResourceIdentifier, new DERIA5String(locationText));
        }
    }
}
