package json.docgen;

import java.util.List;
import paccor.json.schema.AttributesSchema;
import paccor.json.schema.AuthorityInformationAccessSchema;
import paccor.json.schema.CertificatePoliciesSchema;
import paccor.json.schema.ComponentSchema;
import paccor.json.schema.CrlDistributionPointsSchema;
import paccor.json.schema.ExtensionsSchema;
import paccor.json.schema.HardwareManifestSchema;
import paccor.json.schema.SubjectAlternativeNameSchema;
import paccor.json.schema.TargetingInformationSchema;

/**
 * Central registry for project-owned JSON schema field and value metadata used by doc generation.
 */
public final class ProjectJsonSchema {
    private ProjectJsonSchema() {}

    public static final List<JsonSchemaFieldSet> FIELD_SETS = List.of(
            new JsonSchemaFieldSet(
                    "attributes-fields",
                    "Attributes Fields",
                    "$",
                    "Top-level fields accepted by AttributesJsonHelper.",
                    List.of(AttributesSchema.Field.values())),
            new JsonSchemaFieldSet(
                    "extensions-fields",
                    "Extensions Fields",
                    "$",
                    "Top-level fields accepted by ExtensionsJsonHelper.",
                    List.of(ExtensionsSchema.Field.values())),
            new JsonSchemaFieldSet(
                    "hardware-manifest-fields",
                    "Hardware Manifest Fields",
                    "$",
                    "Top-level fields accepted by HardwareManifestJsonHelper when reconstructing platform configuration.",
                    List.of(HardwareManifestSchema.Field.values())),
            new JsonSchemaFieldSet(
                    "authority-information-access-fields",
                    "Authority Information Access Fields",
                    "authorityInformationAccess[]",
                    "Fields used to deserialize each Authority Information Access entry.",
                    List.of(AuthorityInformationAccessSchema.Field.values())),
            new JsonSchemaFieldSet(
                    "certificate-policies-fields",
                    "Certificate Policies Fields",
                    "certificatePolicies[]",
                    "Fields used to deserialize each certificate policy entry.",
                    List.of(CertificatePoliciesSchema.PolicyField.values())),
            new JsonSchemaFieldSet(
                    "certificate-policy-qualifier-fields",
                    "Certificate Policy Qualifier Fields",
                    "certificatePolicies[].policyQualifiers[]",
                    "Fields used to deserialize each certificate policy qualifier.",
                    List.of(CertificatePoliciesSchema.QualifierField.values())),
            new JsonSchemaFieldSet(
                    "crl-distribution-point-fields",
                    "CRL Distribution Point Fields",
                    "crlDistPoint[]",
                    "Fields used to deserialize each CRL distribution point entry.",
                    List.of(CrlDistributionPointsSchema.DistributionPointField.values())),
            new JsonSchemaFieldSet(
                    "crl-distribution-name-fields",
                    "CRL Distribution Name Fields",
                    "crlDistPoint[].distributionName",
                    "Fields used to deserialize a CRL distribution point name.",
                    List.of(CrlDistributionPointsSchema.DistributionNameField.values())),
            new JsonSchemaFieldSet(
                    "component-fields",
                    "Component Fields",
                    "components[]",
                    "Canonical and compatibility field names accepted for component identifiers.",
                    List.of(ComponentSchema.Field.values())),
            new JsonSchemaFieldSet(
                    "component-class-fields",
                    "Component Class Fields",
                    "components[].componentClass",
                    "Fields used inside a componentClass object.",
                    List.of(ComponentSchema.ComponentClassField.values())),
            new JsonSchemaFieldSet(
                    "component-address-fields",
                    "Component Address Fields",
                    "components[].addresses[]",
                    "Fields used inside a canonical component address object.",
                    List.of(ComponentSchema.AddressField.values())),
            new JsonSchemaFieldSet(
                    "component-property-fields",
                    "Component Property Fields",
                    "properties[]",
                    "Fields used for platform properties.",
                    List.of(ComponentSchema.PropertyField.values())),
            new JsonSchemaFieldSet(
                    "uri-reference-fields",
                    "URI Reference Fields",
                    "*.uriReference",
                    "Fields used inside URIReference JSON objects.",
                    List.of(ComponentSchema.UriReferenceField.values())),
            new JsonSchemaFieldSet(
                    "certificate-identifier-fields",
                    "Certificate Identifier Fields",
                    "*.certificateIdentifier",
                    "Fields used inside CertificateIdentifier JSON objects.",
                    List.of(ComponentSchema.CertificateIdentifierField.values())),
            new JsonSchemaFieldSet(
                    "hashed-certificate-fields",
                    "Hashed Certificate Identifier Fields",
                    "*.hashedCertIdentifier",
                    "Fields used inside HashedCertificateIdentifier JSON objects.",
                    List.of(ComponentSchema.HashedCertificateField.values())),
            new JsonSchemaFieldSet(
                    "issuer-serial-fields",
                    "Issuer Serial Fields",
                    "*.genericCertIdentifier",
                    "Fields used inside issuer/serial certificate references.",
                    List.of(ComponentSchema.IssuerSerialField.values())),
            new JsonSchemaFieldSet(
                    "subject-alternative-name-platform-fields",
                    "Subject Alternative Name Platform Fields",
                    "platform",
                    "Platform sub-fields that bridge JSON platform data to trait/OID mappings.",
                    List.of(SubjectAlternativeNameSchema.PlatformField.values())),
            new JsonSchemaFieldSet(
                    "targeting-information-fields",
                    "Targeting Information Fields",
                    "targetingInformation[]",
                    "Fields used to derive targeting information entries from certificate files.",
                    List.of(TargetingInformationSchema.Field.values())));

    public static final List<JsonSchemaVocabulary> VOCABULARIES = List.of(
            new JsonSchemaVocabulary(
                    "authority-information-access-methods",
                    "Authority Information Access Methods",
                    "authorityInformationAccess[].accessMethod",
                    "Allowed symbolic values for the accessMethod field.",
                    AuthorityInformationAccessSchema.Field.ACCESS_METHOD_FIELD,
                    List.of(AuthorityInformationAccessSchema.Method.values())),
            new JsonSchemaVocabulary(
                    "certificate-policy-qualifier-ids",
                    "Certificate Policy Qualifier IDs",
                    "certificatePolicies[].policyQualifiers[].policyQualifierId",
                    "Allowed symbolic values for certificate policy qualifier identifiers.",
                    CertificatePoliciesSchema.QualifierField.POLICY_QUALIFIER_ID_FIELD,
                    List.of(CertificatePoliciesSchema.Qualifier.values())),
            new JsonSchemaVocabulary(
                    "component-address-types",
                    "Component Address Types",
                    "components[].addresses[].addressType",
                    "Allowed symbolic values for component address types. The runtime also accepts the ASN.1 OID string.",
                    ComponentSchema.AddressField.ADDRESS_TYPE_FIELD,
                    List.of(ComponentSchema.AddressTypeValue.values())),
            new JsonSchemaVocabulary(
                    "crl-distribution-name-types",
                    "CRL Distribution Name Types",
                    "crlDistPoint[].distributionName.type",
                    "Allowed symbolic values for distribution name type tags. The runtime also accepts the numeric tag.",
                    CrlDistributionPointsSchema.DistributionNameField.TYPE_FIELD,
                    List.of(CrlDistributionPointsSchema.DistributionNameType.values())));
}
