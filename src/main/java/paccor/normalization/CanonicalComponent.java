package paccor.normalization;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.tcg.credential.TCGObjectIdentifier;
import paccor.tcg.credential.TraitMap;

/**
 * A component after normalization, split into traits. Ignores trait order.
 * @param source the original component
 * @param hardware translated hardware traits and how often each occurs
 * @param references translated CertificateIdentifierTraits and how often each occurs
 */
public record CanonicalComponent(
        TraitMap source,
        Map<CanonicalTrait, Long> hardware,
        Map<CanonicalTrait, Long> references) {

    private static final Set<ASN1ObjectIdentifier> IDENTITY_CATEGORIES = Set.of(
            TCGObjectIdentifier.tcgTrCatComponentClass,
            TCGObjectIdentifier.tcgTrCatComponentManufacturer,
            TCGObjectIdentifier.tcgTrCatComponentModel,
            TCGObjectIdentifier.tcgTrCatComponentSerial
    );

    private static final Set<ASN1ObjectIdentifier> REQUIRED_IDENTITY_CATEGORIES = Set.of(
            TCGObjectIdentifier.tcgTrCatComponentClass,
            TCGObjectIdentifier.tcgTrCatComponentManufacturer,
            TCGObjectIdentifier.tcgTrCatComponentModel
    );

    public CanonicalComponent {
        hardware = Map.copyOf(hardware);
        references = Map.copyOf(references);
    }

    /**
     * Count traits so that comparison works regardless of order.
     * @param traits translated traits
     * @return each distinct trait with its number of occurrences
     */
    public static Map<CanonicalTrait, Long> count(List<CanonicalTrait> traits) {
        return traits.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    }

    /**
     * @param superset trait counts that should include the other
     * @param subset trait counts that should be included
     * @return true if every trait in {@code subset} occurs at least as often in {@code superset}
     */
    public static boolean includes(Map<CanonicalTrait, Long> superset, Map<CanonicalTrait, Long> subset) {
        return subset.entrySet().stream()
                .allMatch(entry -> superset.getOrDefault(entry.getKey(), 0L) >= entry.getValue());
    }

    /**
     * @return true if the component has the class, manufacturer, and model needed to identify it
     */
    public boolean hasIdentity() {
        return hardware.keySet().stream()
                .map(CanonicalTrait::traitCategory)
                .collect(Collectors.toSet())
                .containsAll(REQUIRED_IDENTITY_CATEGORIES);
    }

    /**
     * @return the class, manufacturer, model, and serial traits that identify the component
     */
    public Map<CanonicalTrait, Long> identity() {
        return hardware.entrySet().stream()
                .filter(entry -> IDENTITY_CATEGORIES.contains(entry.getKey().traitCategory()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * @param other another component
     * @return true if both components describe the same hardware
     */
    public boolean sameHardware(CanonicalComponent other) {
        return hardware.equals(other.hardware());
    }

    /**
     * @param other another component
     * @return true if both components can be identified and their identities are equal
     */
    public boolean sameIdentity(CanonicalComponent other) {
        return hasIdentity() && other.hasIdentity() && identity().equals(other.identity());
    }
}
