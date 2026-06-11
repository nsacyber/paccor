package paccor.normalization;

import java.util.Locale;
import java.util.Set;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * Translates string synonym values to a canonical empty string for maximum compatibility.
 * Applies to configurable set of trait categories (manufacturer, model, serial, revision, etc.).
 *
 * Synonyms: "unknown", "n/a", "" (empty), whitespace-only strings
 * These all normalize to "" for comparison purposes.
 *
 * Non-synonym values preserve their original case - NO lowercasing applied.
 */
public final class StringSynonymTranslator implements TraitValueTranslator {

    private static final Set<String> SYNONYMS = Set.of("unknown", "n/a", "");

    private final Set<ASN1ObjectIdentifier> targetCategories;

    /**
     * Create a translator that applies to specific trait categories.
     *
     * @param targetCategories Set of trait category OIDs to apply synonym normalization to
     */
    public StringSynonymTranslator(Set<ASN1ObjectIdentifier> targetCategories) {
        this.targetCategories = Set.copyOf(targetCategories);
    }

    @Override
    public boolean supports(ASN1ObjectIdentifier traitId,
                           ASN1ObjectIdentifier traitCategory,
                           ASN1ObjectIdentifier traitRegistry) {
        return targetCategories.contains(traitCategory);
    }

    @Override
    public ASN1Object translate(ASN1ObjectIdentifier traitId,
                               ASN1ObjectIdentifier traitCategory,
                               ASN1ObjectIdentifier traitRegistry,
                               ASN1Object rawValue) {
        if (!(rawValue instanceof ASN1UTF8String utf8)) {
            return rawValue;
        }

        String value = utf8.getString();
        if (value == null) {
            return new DERUTF8String("");
        }

        String trimmed = value.trim();

        // Check if this is a synonym (case-insensitive check)
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        if (SYNONYMS.contains(normalized)) {
            // Normalize to empty string
            return new DERUTF8String("");
        }

        // Not a synonym - return original value with case preserved
        return rawValue;
    }
}
