package paccor.model;

import paccor.cert.CertKind;
import paccor.cert.CertType;
import paccor.cert.CertTypeResolver;
import paccor.cert.CertSpecVersion;
import lombok.Builder;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.tcg.credential.CertificateIdentifier;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.TCGCredentialType;
import paccor.tcg.credential.TCGObjectIdentifier;

/**
 * Serializable summary of a certificate referenced from JSON.
 * Carries enough metadata to re-emit the corresponding CertificateIdentifierTrait.
 * @param file the file path of the certificate
 * @param certKind CertKind AC or PKC
 * @param certSpecVersion CertSpecVersion 1.1 or 2.0
 * @param certType CertType Base, Delta, Rebase
 * @param tcgCredentialType {@link TCGCredentialType}
 * @param certificateIdentifier {@link CertificateIdentifier}
 * @param traitCategory {@link ASN1ObjectIdentifier}
 */
@Builder(toBuilder = true)
public record CertificateReference(
        String file,
        CertKind certKind,
        CertSpecVersion certSpecVersion,
        CertType certType,
        TCGCredentialType tcgCredentialType,
        CertificateIdentifier certificateIdentifier,
        ASN1ObjectIdentifier traitCategory) {

    /**
     * Resolve the trait category.
     * @return Trait category OID if resolvable. Otherwise, generic OID.
     */
    public ASN1ObjectIdentifier resolvedTraitCategory() {
        if (traitCategory != null) {
            return traitCategory;
        }
        ASN1ObjectIdentifier derived = CertTypeResolver.toTraitCategory(certType);
        return derived != null ? derived : TCGObjectIdentifier.tcgTrCatGenericCertificate;
    }

    /**
     * Convert to a {@link CertificateIdentifierTrait}.
     * @return {@link CertificateIdentifierTrait}
     */
    public CertificateIdentifierTrait toTrait() {
        return CertificateIdentifierTrait.builder()
                .traitCategory(resolvedTraitCategory())
                .traitValue(certificateIdentifier)
                .build();
    }
}
