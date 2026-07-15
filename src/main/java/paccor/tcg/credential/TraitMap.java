package paccor.tcg.credential;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import paccor.json.ObjectMapperFactory;
import paccor.json.TraitMapDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * <pre>{@code
 * <?> ::= SEQUENCE(SIZE(1..MAX)) OF Trait
 * }</pre>
 */
@AllArgsConstructor
@Builder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
@Getter
@JsonDeserialize(using = TraitMapDeserializer.class)
@JsonClassDescription("Collection of Trait entries. Stored in a map to group traits by type.")
@JsonFormat(with = JsonFormat.Feature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor(force = true)
@ToString
public class TraitMap extends ASN1Object implements Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> {
    public static final int MIN_SEQUENCE_SIZE = 0;

    @JsonPropertyDescription("Traits are indexed by type.")
    @Size(min = 1)
    private final Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> traits;

    public static TraitMap getInstance(Object obj) {
        if (obj == null || obj instanceof TraitMap) {
            return (TraitMap) obj;
        }
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return fromASN1Sequence(ASN1Utils.getSequence(obj));
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    public static final TraitMap fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < TraitMap.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        TraitMapBuilder builder = TraitMap.builder();
        builder.traitsFromSequence(seq);
        return builder.build();
    }

    public static final TraitMap fromTraits(List<? extends Trait<?, ?>> traits) {
        TraitMapBuilder builder = TraitMap.builder();
        if (traits != null) {
            traits.forEach(builder::trait);
        }
        return builder.build();
    }

    public static final void validateTraitMap(
            TraitMap traits,
            String context,
            Set<Class<? extends Trait<?, ?>>> allowedTypes,
            Set<ASN1ObjectIdentifier> requiredCategories,
            List<String> issues) {
        if (traits == null || traits.isEmpty()) return;

        Optional.ofNullable(allowedTypes).ifPresent(allowed ->
                traits.keySet().stream()
                        .filter(type -> !allowed.contains(type))
                        .forEach(type -> issues.add(context + " contains unsupported trait type: " + type.getSimpleName()))
        );

        Optional.ofNullable(requiredCategories).ifPresent(required -> {
            Set<ASN1ObjectIdentifier> presentCategories = traits.flattenTraits().stream()
                    .map(Trait::getTraitCategory)
                    .collect(Collectors.toSet());
            required.stream()
                    .filter(req -> !presentCategories.contains(req))
                    .forEach(req -> issues.add(context + " is missing required trait category: " + req.getId()));
        });
    }

    public final List<Trait<?, ?>> flattenTraits() {
        if (traits == null) return List.of();
        return traits.values().stream().filter(Objects::nonNull).flatMap(Collection::stream).toList();
    }

    public final <TraitValueType extends ASN1Object, TraitType extends Trait<TraitValueType, TraitType>> Optional<TraitValueType> firstTraitValue(Class<TraitType> traitType) {
        return Optional.ofNullable(traits)
                .map(m -> m.get(traitType))
                .flatMap(list -> list.stream().findFirst())
                .map(traitType::cast)
                .map(TraitType::getTraitValue);
    }

    public final <T extends Trait<?, ?>> Optional<T> firstTrait(Class<T> traitType) {
        return Optional.ofNullable(traits)
                .map(m -> m.get(traitType))
                .flatMap(list -> list.stream().findFirst())
                .map(traitType::cast);
    }

    public final <TraitValueType extends ASN1Object, TraitType extends Trait<TraitValueType, TraitType>> TraitValueType firstValueOfType(Class<TraitType> traitType) {
        return firstTraitValue(traitType).orElse(null);
    }

    public final <TraitValueType extends ASN1Object, TraitType extends Trait<TraitValueType, TraitType>> List<TraitType> get(Class<TraitType> traitType) {
        return Optional.ofNullable(traits)
                .map(m -> m.get(traitType))
                .map(list -> list.stream().map(traitType::cast).toList())
                .orElseGet(List::of);
    }

    public final TraitMap filter(Set<Class<? extends Trait<?, ?>>> allowedTypes) {
        TraitMapBuilder builder = TraitMap.builder();
        flattenTraits().stream()
                .filter(t -> allowedTypes.contains(t.getClass()))
                .forEach(builder::trait);
        return builder.build();
    }

    @Override
    public int size() {
        if (traits == null) return 0;
        return traits.values().stream().mapToInt(List::size).sum();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return traits != null && traits.containsKey(key);
    }

    public int typeCount() {
        return keySet().size();
    }

    @Override
    public boolean containsValue(Object value) {
        return traits != null && traits.containsValue(value);
    }

    @Override
    public List<Trait<?, ?>> get(Object key) {
        return traits != null ? traits.get(key) : null;
    }

    @Override
    public List<Trait<?, ?>> put(Class<? extends Trait<?, ?>> key, List<Trait<?, ?>> value) {
        if (traits == null) {
            throw new UnsupportedOperationException("Internal traits map is immutable");
        }
        if (traits.containsKey(key)) {
            List<Trait<?, ?>> existing = traits.get(key);
            if (value != null) {
                for (Trait<?, ?> t : value) {
                    if (!existing.contains(t)) {
                        existing.add(t);
                    }
                }
            }
            return existing;
        } else {
            return traits.put(key, new ArrayList<>(value));
        }
    }

    @Override
    public List<Trait<?, ?>> remove(Object key) {
        return traits != null ? traits.remove(key) : null;
    }

    @Override
    public void putAll(@NonNull Map<? extends Class<? extends Trait<?, ?>>, ? extends List<Trait<?, ?>>> map) {
        if (traits == null) {
            throw new UnsupportedOperationException("Internal traits map is immutable");
        }
        map.forEach(this::put);
    }

    public void setSingleTrait(@NonNull Trait<?, ?> trait) {
        if (traits == null) {
            throw new UnsupportedOperationException("Internal traits map is immutable");
        }
        traits.put(trait.getTraitType(), new ArrayList<>(List.of(trait)));
    }

    @Override
    public void clear() {
        Optional.ofNullable(traits).ifPresent(Map::clear);
    }

    @NonNull
    @Override
    public Set<Class<? extends Trait<?, ?>>> keySet() {
        return traits != null ? traits.keySet() : Collections.emptySet();
    }

    @NonNull
    @Override
    public Collection<List<Trait<?, ?>>> values() {
        return traits != null ? traits.values() : Collections.emptyList();
    }

    @NonNull
    @Override
    public Set<Entry<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>>> entrySet() {
        return traits != null ? traits.entrySet() : Collections.emptySet();
    }

    public ASN1Primitive toASN1Primitive() {
        return new DERSequence(ASN1Utils.toASN1EncodableVector(flattenTraits()));
    }

    public static class TraitMapBuilder {
        private Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> traits = new LinkedHashMap<>();

        public TraitMapBuilder trait(Trait<?, ?> traitObj) {
            traits.computeIfAbsent(traitObj.getTraitType(), _ -> new ArrayList<>()).add(traitObj);
            return this;
        }

        public final void traitsFromSequence(@NonNull ASN1Sequence seq) {
            Arrays.stream(seq.toArray())
                    .map(obj -> (ASN1Object)obj)
                    .forEach(
                    trait -> {
                            ASN1ObjectTrait basic = ASN1ObjectTrait.getInstance(trait);
                            Class<? extends Trait<?, ?>> traitClassFromId = TraitId.getTraitClassForId(basic.getTraitId());
                            Optional.ofNullable(traitClassFromId)
                                    .filter(clazz -> clazz == ASN1ObjectTrait.class)
                                    .ifPresentOrElse(
                                            _ -> this.trait(basic),
                                            () -> this.trait(Trait.getInstance(trait.toASN1Primitive(), TraitId.getTraitClassForId(basic.getTraitId()))));
                    });
        }

        public final void traitsFromJson(@NonNull JsonNode refNode) {
            ObjectMapperFactory.fromJsonNode(refNode, TraitMap.class).flattenTraits()
                    .forEach(this::trait);
        }
    }
}
