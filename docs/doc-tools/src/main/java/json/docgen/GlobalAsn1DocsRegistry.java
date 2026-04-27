package json.docgen;

import java.util.LinkedHashMap;
import java.util.Map;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.KeyUsage;
import tcg.credential.Trait;
import tcg.credential.TraitCollection;
import tcg.credential.TraitId;
import tcg.credential.TraitMap;

/**
 * Documentation registry for ASN.1-backed Java types. Replaces the static
 * {@code ASN1_USAGE_DESCRIPTIONS} map that previously lived in {@code SchemaUtils}.
 * Consumed by the victools description resolver and by the global-types page renderer.
 */
public final class GlobalAsn1DocsRegistry {

    public static final Map<Class<?>, GlobalAsn1Docs> ENTRIES;

    static {
        Map<Class<?>, GlobalAsn1Docs> map = new LinkedHashMap<>();

        map.put(ASN1Integer.class, new GlobalAsn1Docs(
                "ASN1Integer",
                "Accepts standard JSON numbers (int, long, big integer), decimal strings, or specialized binary formats (Hex/Base64)."));

        map.put(ASN1Boolean.class, new GlobalAsn1Docs(
                "ASN1Boolean",
                "Accepts JSON boolean values (true/false) or their string equivalents (\"true\"/\"false\")."));

        map.put(ASN1ObjectIdentifier.class, new GlobalAsn1Docs(
                "ASN1ObjectIdentifier",
                "Supports multiple formats: 1) A simple OID string (e.g., \"1.2.3\"). 2) A JSON object with an \"oid\" property."));

        map.put(ASN1OctetString.class, new GlobalAsn1Docs(
                "ASN1OctetString",
                "Supports multiple formats: 1) A Base64/Hex encoded string. 2) A JSON array of bytes. 3) A JSON object with a \"base64\" or \"hex\" property."));

        map.put(ASN1BitString.class, new GlobalAsn1Docs(
                "ASN1BitString",
                "Supports multiple formats: 1) A Base64/Hex encoded string. 2) A JSON array of bytes. 3) A JSON object with a \"base64\" or \"hex\" property."));

        map.put(ASN1GeneralizedTime.class, new GlobalAsn1Docs(
                "ASN1GeneralizedTime",
                "Accepts a date string format."));

        map.put(AlgorithmIdentifier.class, new GlobalAsn1Docs(
                "AlgorithmIdentifier",
                "Supports multiple formats: 1) A simple OID string (representing the algorithm). 2) A JSON object with \"algorithm\" (OID string) and optional \"parameters\" (Hex/Base64 string)."));

        map.put(KeyUsage.class, new GlobalAsn1Docs(
                "KeyUsage",
                "Supports multiple formats: 1) An integer bitmask. 2) A hex string. 3) A name string (e.g., \"digitalSignature\"). 4) An array of these formats."));

        map.put(ASN1IA5String.class, new GlobalAsn1Docs(
                "ASN1IA5String",
                "Accepts a simple string or a JSON object with a \"string\" property."));

        map.put(ASN1UTF8String.class, new GlobalAsn1Docs(
                "ASN1UTF8String",
                "Accepts a simple string or a JSON object with a \"string\" property."));

        String traitAliases = String.join("`, `", TraitId.getRegisteredAliases());
        map.put(Trait.class, new GlobalAsn1Docs(
                "Trait",
                "**Polymorphic Trait Object**\n\n"
                        + "A Trait is identified and resolved using the following priority:\n"
                        + "1. **By ID**: Matches `traitId` OID against known types.\n"
                        + "2. **By Alias**: Matches any of the supported value property names (see below).\n"
                        + "3. **By Category**: Matches `traitCategory` OID if no ID or Alias match is found.\n\n"
                        + "**Standard Properties:**\n"
                        + "- `traitId`: (Optional) OID string identifying the trait type.\n"
                        + "- `traitCategory`: (Optional) OID string identifying the category.\n"
                        + "- `traitRegistry`: (Optional) OID string for the registry.\n"
                        + "- `description`: (Optional) UTF-8 string description.\n"
                        + "- `descriptionURI`: (Optional) IA5 string URI for the description.\n"
                        + "- `traitValue`: (Fallback) The value of the trait if no alias is used.\n\n"
                        + "**Supported Value Aliases (Property Names):**\n"
                        + "Instead of `traitValue`, you may use one of these property names based on the trait type: "
                        + "`" + traitAliases + "`."));

        map.put(TraitMap.class, new GlobalAsn1Docs(
                "TraitMap",
                "Accepts a JSON array of Trait objects. Runtime deserializers may also accept legacy forms and normalize them into a Trait sequence."));

        map.put(TraitCollection.class, new GlobalAsn1Docs(
                "TraitCollection",
                "Accepts a JSON array of Trait objects. This is a specialised TraitMap used for collections of traits."));

        ENTRIES = Map.copyOf(map);
    }

    private GlobalAsn1DocsRegistry() {}

    /**
     * Convenience: legacy callers that just want the description string keyed by class.
     */
    public static String descriptionFor(Class<?> type) {
        GlobalAsn1Docs docs = ENTRIES.get(type);
        return docs == null ? null : docs.description();
    }
}
