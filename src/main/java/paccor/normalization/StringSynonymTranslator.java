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
 * Non-synonym values are trimmed and runs of whitespace are collapsed to a single space.
 * Case is preserved unless the category was configured as case-insensitive.
 */
public final class StringSynonymTranslator implements TraitValueTranslator {

    private static final Set<String> SYNONYMS = Set.of("unknown", "n/a", "");

    private final Set<ASN1ObjectIdentifier> targetCategories;
    private final Set<ASN1ObjectIdentifier> caseInsensitiveCategories;

    /**
     * Create a translator that applies to specific trait categories.
     *
     * @param targetCategories Set of trait category OIDs to apply synonym normalization to
     */
    public StringSynonymTranslator(Set<ASN1ObjectIdentifier> targetCategories) {
        this(targetCategories, Set.of());
    }

    /**
     * Create a translator that applies to specific trait categories.
     *
     * @param targetCategories Set of trait category OIDs to apply synonym normalization to
     * @param caseInsensitiveCategories Subset of categories whose values are also lower-cased
     */
    public StringSynonymTranslator(Set<ASN1ObjectIdentifier> targetCategories,
                                   Set<ASN1ObjectIdentifier> caseInsensitiveCategories) {
        this.targetCategories = Set.copyOf(targetCategories);
        this.caseInsensitiveCategories = Set.copyOf(caseInsensitiveCategories);
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

        String canonical = trimmed.replaceAll("\\s+", " ");
        if (caseInsensitiveCategories.contains(traitCategory)) {
            canonical = canonical.toLowerCase(Locale.ROOT);
        }
        return canonical.equals(value) ? rawValue : new DERUTF8String(canonical);
    }
}
