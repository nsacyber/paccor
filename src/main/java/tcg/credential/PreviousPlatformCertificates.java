package tcg.credential;

import java.util.Set;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PreviousPlatformCertificates extends TraitSequence implements TraitSequenceFilter {
    /**
     * Attempts to convert the provided object into an instance of this trait.
     * @param obj the object to convert
     * @return PreviousPlatformCertificates
     */
    public static PreviousPlatformCertificates getInstance(Object obj) {
//        Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> filteredMap = ASN1Utils.mapTraitSequence(TraitSequence.getInstance(obj), List.copyOf(seq.getConversionMethods())).entrySet().stream()
//                .filter(entry -> isSupportedType.test(entry.getKey())) // Use the Predicate explicitly
//                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
//
//        seq.getTraits().clear();

        return null;
    }

    @Override
    public Set<Class<? extends Trait<?, ?>>> getSupportedTraitTypes() {
        return Set.of(CertificateIdentifierTrait.class);
    }
}
