package paccor.normalization;

import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

/**
 * Basic Trait with canonicalized value.
 * @param traitId trait identifier
 * @param traitCategory trait category
 * @param traitRegistry trait registry
 * @param traitValue translated trait value
 */
public record CanonicalTrait(
        ASN1ObjectIdentifier traitId,
        ASN1ObjectIdentifier traitCategory,
        ASN1ObjectIdentifier traitRegistry,
        ASN1Object traitValue) {}
