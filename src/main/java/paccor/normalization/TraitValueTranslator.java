package paccor.normalization;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * SPI for translating/normalizing a trait's value to a canonical representation
 * based on its identifiers. Implementations must be pure (no side-effects)
 * and return a value suitable for equality comparison.
 */
public interface TraitValueTranslator {
    /** Whether this translator applies for the given identifiers. */
    boolean supports(ASN1ObjectIdentifier traitId,
                     ASN1ObjectIdentifier traitCategory,
                     ASN1ObjectIdentifier traitRegistry);

    /** Return a normalized/canonical value. If no change, return the same instance. */
    ASN1Object translate(ASN1ObjectIdentifier traitId,
                         ASN1ObjectIdentifier traitCategory,
                         ASN1ObjectIdentifier traitRegistry,
                         ASN1Object rawValue);
}
