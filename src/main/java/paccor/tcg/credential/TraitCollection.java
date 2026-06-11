package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;

@JsonClassDescription("Ordered flat collection of Trait entries.")
public final class TraitCollection implements Iterable<Trait<?, ?>> {
    private final List<Trait<?, ?>> traits;

    public TraitCollection(List<? extends Trait<?, ?>> traits) {
        this.traits = List.copyOf(traits != null ? traits : List.of());
    }

    public static TraitCollection empty() {
        return new TraitCollection(List.of());
    }

    public static TraitCollection from(TraitMap map) {
        return map == null ? empty() : new TraitCollection(map.flattenTraits());
    }

    public static TraitCollection fromTraits(List<? extends Trait<?, ?>> traits) {
        return new TraitCollection(traits);
    }

    public List<Trait<?, ?>> asList() {
        return traits;
    }

    public int size() {
        return traits.size();
    }

    public boolean isEmpty() {
        return traits.isEmpty();
    }

    public Trait<?, ?> get(int index) {
        return traits.get(index);
    }

    public Stream<Trait<?, ?>> stream() {
        return traits.stream();
    }

    public <T extends Trait<?, ?>> List<T> traits(Class<T> traitType) {
        return stream()
                .filter(traitType::isInstance)
                .map(traitType::cast)
                .toList();
    }

    public boolean containsCategory(ASN1ObjectIdentifier category) {
        return traits.stream()
                .map(Trait::getTraitCategory)
                .anyMatch(category::equals);
    }

    public Optional<Trait<?, ?>> firstWithCategory(ASN1ObjectIdentifier category) {
        return stream()
                .filter(trait -> category.equals(trait.getTraitCategory()))
                .findFirst();
    }

    public <T extends Trait<?, ?>> Optional<T> firstWithCategory(ASN1ObjectIdentifier category, Class<T> traitType) {
        return firstWithCategory(category)
                .filter(traitType::isInstance)
                .map(traitType::cast);
    }

    public Optional<String> firstStringWithCategory(ASN1ObjectIdentifier category) {
        return firstWithCategory(category)
                .map(Trait::getTraitValue)
                .map(Object::toString);
    }

    public boolean containsAll(TraitCollection other) {
        if(this == other) return true;
        if(other == null) return false;
        List<Trait<?, ?>> copy = new ArrayList<>(traits);
        return other.traits.stream().allMatch(trait -> {
            // Find the first item that matches semantically
            return copy.stream()
                    .filter(trait::equals)
                    .findFirst()
                    // If found, remove it from the copy and return true to allMatch
                    .map(copy::remove)
                    .orElse(false);
        });
    }

    public TraitMap toTraitMap() {
        return TraitMap.fromTraits(traits);
    }

    @NonNull
    @Override
    public Iterator<Trait<?, ?>> iterator() {
        return traits.iterator();
    }

    @Override
    public String toString() {
        return traits.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof TraitCollection other)) return false;
        return traits.equals(other.traits);
    }

    @Override
    public int hashCode() {
        return traits.hashCode();
    }
}
