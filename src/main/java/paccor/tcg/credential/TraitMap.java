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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import paccor.json.ObjectMapperFactory;
import paccor.json.TraitMapDeserializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.annotation.JsonDeserialize;

/**
 * <pre>{@code
 * &lt;?&gt; ::= SEQUENCE(SIZE(1..MAX)) OF Trait
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

    /**
     * Attempts to cast the provided object to TraitMap.
     * If the object is an ASN1Sequence, the object is parsed by fromASN1Sequence.
     * @param obj the object to parse
     * @return TraitMap
     */
    public static TraitMap getInstance(Object obj) {
        if (obj == null || obj instanceof TraitMap) {
            return (TraitMap) obj;
        }
        if (obj instanceof ASN1Sequence || obj instanceof ASN1TaggedObject) {
            return fromASN1Sequence(ASN1Utils.getSequence(obj));
        }
        throw new IllegalArgumentException("Illegal argument in getInstance: " + obj.getClass().getName());
    }

    /**
     * Attempts to parse the given ASN1Sequence.
     * @param seq An ASN1Sequence
     * @return TraitMap
     */
    public static final TraitMap fromASN1Sequence(@NonNull ASN1Sequence seq) {
        if (seq.size() < TraitMap.MIN_SEQUENCE_SIZE) {
            throw new IllegalArgumentException("Bad sequence size: " + seq.size());
        }

        TraitMapBuilder builder = TraitMap.builder();
        builder.traitsFromSequence(seq);
        return builder.build();
    }

    public static TraitMap fromTraits(List<? extends Trait<?, ?>> traits) {
        TraitMapBuilder builder = TraitMap.builder();
        if (traits != null) {
            traits.forEach(builder::trait);
        }
        return builder.build();
    }

    /**
     * Flatten the trait map into a single list.
     * @return List of Traits
     */
    public final List<Trait<?, ?>> flattenTraits() {
        if (getTraits() == null) return List.of();
        return getTraits().values().stream().filter(Objects::nonNull).flatMap(Collection::stream).toList();
    }

    public final <TraitValueType extends ASN1Object, TraitType extends Trait<TraitValueType, TraitType>> TraitValueType firstValueOfType(Class<TraitType> traitType) {
        return Optional.ofNullable(traits)
                .map(list -> traits.get(traitType))
                .flatMap(list -> list.stream().findFirst())
                .map(traitType::cast)
                .map(TraitType::getTraitValue)
                .orElse(null);
    }

    /**
     * Flatten the trait map into a single list of the specified type.
     * @param traitType The type of Trait to focus on
     * @return List of Traits of the specified type
     * @param <TraitType> The type of Trait to focus on
     */
    public final <TraitValueType extends ASN1Object, TraitType extends Trait<TraitValueType, TraitType>> List<TraitType> get(Class<TraitType> traitType) {
        return flattenTraits().stream()
                .filter(traitType::isInstance)
                .map(traitType::cast)
                .toList();
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
        for (Entry<? extends Class<? extends Trait<?, ?>>, ? extends List<Trait<?, ?>>> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        if (traits != null) {
            traits.clear();
        }
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

    /**
     * @return This object as an ASN1Sequence
     */
    public ASN1Primitive toASN1Primitive() {
        ASN1EncodableVector vec = new ASN1EncodableVector();
        if (traits != null) {
            vec = ASN1Utils.toASN1EncodableVector(
                    traits.values().stream()
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .toList());
        }
        return new DERSequence(vec);
    }

    /**
     * The rest of this builder is generated by lombok Builder annotation
     */
    public static class TraitMapBuilder {
        public TraitMapBuilder trait(Trait<?, ?> traitObj) {
            if (traits == null) {
                traits = new HashMap<>();
            }
            if (!traits.containsKey(traitObj.getTraitType())) {
                traits.put(traitObj.getTraitType(), new ArrayList<>());
            }
            List<Trait<?, ?>> list = traits.get(traitObj.getTraitType());
            list.add(traitObj);
            traits.put(traitObj.getTraitType(), list);
            return this;
        }

        /**
         * Attempts to recover Traits from the ASN1Sequence and then read their TraitID and process them into appropriate types.
         * @param seq ASN1Sequence
         */
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

        /**
         * Read traits from the JsonNode into the TraitMap.
         * @param refNode JsonNode
         */
        public final void traitsFromJson(@NonNull JsonNode refNode) {
            ObjectMapperFactory.fromJsonNode(refNode, TraitMap.class).flattenTraits()
                    .forEach(this::trait);
        }
    }
}
