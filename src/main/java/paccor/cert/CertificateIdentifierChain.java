package paccor.cert;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import paccor.model.CertificateReference;
import paccor.model.PlatformCertificateInformationModel;
import paccor.tcg.credential.CertificateIdentifier;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitCollection;
import paccor.tcg.credential.TraitMap;

/** Builds and deduplicates chains of CertificateIdentifierTrait entries. */
public final class CertificateIdentifierChain {
    private CertificateIdentifierChain() {}

    /** Append a certificate and optionally its embedded identifier chain. */
    public static void append(
            PlatformCertificateInformationModel model,
            PlatformCertificate certificate,
            boolean includeEmbeddedChain) {
        if (isNotAppendable(model, certificate)) return;

        TraitCollection chain = mergedTraits(model, certificate, includeEmbeddedChain);
        model.setPreviousPlatformCertificates(withCertificate(chain, certificate));
        model.setPreviousPlatformCertificateObjects(withReference(model, certificate));
    }

    private static boolean isNotAppendable(
            PlatformCertificateInformationModel model,
            PlatformCertificate certificate) {
        return model == null
                || certificate == null
                || certificate.getCertificateIdentifier() == null;
    }

    private static TraitCollection mergedTraits(
            PlatformCertificateInformationModel model,
            PlatformCertificate certificate,
            boolean includeEmbeddedChain) {
        TraitCollection chain = TraitCollection.from(model.getPreviousPlatformCertificates());
        return includeEmbeddedChain ? uniqueIdentifiers(chain.appendAll(embeddedTraits(certificate))) : chain;
    }

    private static TraitCollection embeddedTraits(PlatformCertificate certificate) {
        return Optional.ofNullable(certificate.traitMap(TCGObjectIdentifier.tcgAtPreviousPlatformCertificates))
                .map(TraitCollection::from)
                .orElseGet(TraitCollection::empty);
    }

    private static TraitCollection uniqueIdentifiers(TraitCollection traits) {
        Set<CertificateIdentifier> identifiers = new HashSet<>();
        return TraitCollection.fromTraits(traits.stream()
                .filter(trait -> !(trait instanceof CertificateIdentifierTrait certificateTrait)
                        || certificateTrait.getTraitValue() != null
                        && identifiers.add(certificateTrait.getTraitValue()))
                .toList());
    }

    private static TraitMap withCertificate(
            TraitCollection chain,
            PlatformCertificate certificate) {
        CertificateIdentifier id = certificate.getCertificateIdentifier();
        boolean present = chain.stream()
                .filter(CertificateIdentifierTrait.class::isInstance)
                .map(CertificateIdentifierTrait.class::cast)
                .anyMatch(trait -> id.equals(trait.getTraitValue()));
        TraitCollection result = present ? chain : chain.append(CertificateIdentifierTrait.builder()
                    .traitCategory(Optional.ofNullable(CertTypeResolver.toTraitCategory(certificate.getCertType()))
                            .orElse(TCGObjectIdentifier.tcgTrCatPlatformCertificate))
                    .traitValue(id)
                    .build());
        return result.toTraitMap();
    }

    private static List<CertificateReference> withReference(
            PlatformCertificateInformationModel model,
            PlatformCertificate certificate) {
        List<CertificateReference> references = new ArrayList<>(
                Optional.ofNullable(model.getPreviousPlatformCertificateObjects()).orElseGet(List::of));
        boolean present = references.stream()
                .filter(reference -> reference != null)
                .map(CertificateReference::certificateIdentifier)
                .anyMatch(certificate.getCertificateIdentifier()::equals);
        if (!present) {
            references.add(certificate.toReference(CertTypeResolver.toTraitCategory(certificate.getCertType())));
        }
        return references;
    }
}
