package json.docgen;

import java.util.Map;

/**
 * Maps a {@link JsonSchemaFieldSet} id to the simple name of the backing
 * Java class whose ASN.1 block (extracted by {@code AsnDoclet}) should be
 * embedded in the generated reference page for that fieldset.
 *
 * <p>Doc-only metadata. Lives test-side intentionally so {@code ProjectJsonSchema}
 * and {@code JsonSchemaFieldSet} stay free of doc-only concerns.</p>
 *
 * <p>Only fieldsets with a clean 1:1 mapping to a {@code tcg.credential.*} class
 * are registered here. Fieldsets without a backing class fall back to the
 * legacy table-only rendering.</p>
 */
public final class BackingClassRegistry {

    public static final Map<String, String> BY_FIELDSET_ID = Map.ofEntries(
            Map.entry("component-fields", "ComponentIdentifierV2"),
            Map.entry("component-class-fields", "ComponentClass"),
            Map.entry("component-address-fields", "ComponentAddress"),
            Map.entry("component-property-fields", "PlatformPropertiesV2"),
            Map.entry("uri-reference-fields", "URIReference"),
            Map.entry("certificate-identifier-fields", "CertificateIdentifier"),
            Map.entry("hashed-certificate-fields", "HashedCertificateIdentifier")
    );

    private BackingClassRegistry() {}

    public static String simpleNameFor(String fieldSetId) {
        return BY_FIELDSET_ID.get(fieldSetId);
    }
}
