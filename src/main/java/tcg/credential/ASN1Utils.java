package tcg.credential;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DEROctetString;

/**
 * Common methods to transform ASN1 objects.
 */
public class ASN1Utils {
    /**
     * Given a list of ASN1Objects, put all elements into an ASN1 vector.
     * @param list List of ASN1Encodable objects, usually ASN1Objects
     * @return ASN1EncodableVector containing all elements from the list. Empty list if the input list was null.
     * @param <T> Anything that extends or implements ASN1Encodable
     * @param <U> List of T
     */
    public static final <T extends ASN1Object, U extends Collection<T>> ASN1EncodableVector toASN1EncodableVector(U list) {
        ASN1EncodableVector vec = new ASN1EncodableVector();

        if (list == null) {
            return vec;
        }

        list.stream()
                .map(obj -> (ASN1Object)obj)
                .forEach(vec::add);
        return vec;
    }

    /**
     * Pull out all ASN1TaggedObjects from the sequence and put them into a map where
     * the keys are tag numbers of ASN1TaggedObjects
     * and the values are the ASN1Object they reference.
     * @param seq ASN1Sequence of any size
     * @return Map of the tagged objects indexed by tag number. Empty map if no tagged objects are found.
     */
    public static final Map<Integer, ASN1Object> parseTaggedElements(ASN1Sequence seq) {
        Map<Integer, ASN1Object> map = new HashMap<>();

        if (seq == null) {
            return map;
        }

        Arrays.stream(seq.toArray())
                .map(obj -> (ASN1Object)obj)
                .map(opt -> safeGetDefaultElement(opt, null, ASN1TaggedObject::getInstance))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(ASN1TaggedObject::getTagNo))
                .forEach(taggedElement -> map.put(taggedElement.getTagNo(), taggedElement.getBaseObject()));

        map.entrySet().removeIf(entry -> Objects.isNull(entry.getValue()));

        return map;
    }

    /**
     * List all untagged elements from the sequence in order. Null elements are not included in the list.
     * @param seq ASN1Sequence
     * @return List of ASN1Objects that are not ASN1TaggedObjects in order
     */
    public static final List<ASN1Object> listUntaggedElements(ASN1Sequence seq) {
        List<ASN1Object> list = new ArrayList<>();

        if (seq == null) {
            return list;
        }

        Arrays.stream(seq.toArray())
                .filter(obj -> obj instanceof ASN1Object)
                .map(obj -> (ASN1Object)obj)
                .map(obj -> safeGetDefaultElement(obj, obj, ASN1TaggedObject::getInstance))
                .filter(obj -> !(obj instanceof ASN1TaggedObject))
                .forEach(list::add);

        return list;
    }

    /**
     * Attempt to transform the ASN1Object at the requested position within the ASN1Sequence into T using the method supplied.
     * @param seq If null, the default answer will be returned
     * @param zeroBasedIndex If this is out of bounds of the seq, the default answer will be returned
     * @param defaultAnswer Can be null if the default answer is null
     * @param getInstanceMethod The method to transform the object found at the index within the sequence
     * @return The transformed object
     * @param <T> The return type
     */
    public static final <T> T safeGetDefaultElementFromSequence(ASN1Sequence seq, int zeroBasedIndex, T defaultAnswer, @NonNull Function<Object, T> getInstanceMethod) {
        T result = defaultAnswer;
        if (seq != null && seq.size() > zeroBasedIndex && 0 < seq.size() && seq.getObjectAt(zeroBasedIndex) instanceof ASN1Object obj) {
            result = safeGetDefaultElement(obj, defaultAnswer, getInstanceMethod);
        }
        return result;
    }

    /**
     * Attempt to transform the ASN1Object into T using the method supplied.
     * @param obj ASN1Object
     * @param defaultAnswer Can be null if the default answer is null
     * @param getInstanceMethod The method to transform the object found at the index within the sequence
     * @return The transformed object
     * @param <T> The return type
     */
    public static final <T> T safeGetDefaultElement(ASN1Object obj, T defaultAnswer, @NonNull Function<Object, T> getInstanceMethod) {
        if (obj == null) {
            return defaultAnswer;
        }

        T result = defaultAnswer;
        try {
            result = getInstanceMethod.apply(obj);
        } catch (Exception ignored) {}
        return result;
    }

    /**
     * Look for the first element, within the supplied range of the ASN1Sequence, that can be transformed into T using the method supplied.
     * If no qualified elements are found, the default answer will be returned.
     * @param seq If null, the default answer will be returned
     * @param zeroBasedStartIndex If this is out of bounds of the seq, the default answer will be returned
     * @param zeroBasedMaxIndex If this is out of bounds of the seq, the default answer will be returned
     * @param defaultAnswer Can be null if the default answer is null
     * @param getInstanceMethod The method to transform the object found at the index within the sequence
     * @return The transformed object
     * @param <T> The return type
     */
    public static final <T> T safeGetFirstInstanceFromSequenceGivenRange(ASN1Sequence seq, int zeroBasedStartIndex, int zeroBasedMaxIndex, T defaultAnswer, @NonNull Function<Object, T> getInstanceMethod) {
        T result = defaultAnswer;

        for (int i = zeroBasedStartIndex; i <= zeroBasedMaxIndex; i++) {
            T temp = safeGetDefaultElementFromSequence(seq, i, null, getInstanceMethod);

            if (temp != null) {
                result = temp;
                break;
            }
        }

        return result;
    }

    /**
     * TraitSequences can have any kind of trait. Map arbitrary traits using their trait type class as keys.
     * @param seq TraitSequence of arbitrary traits.
     * @param expectedConversionMethods A list of methods to convert arbitrary ASN1Objects into trait value types.
     * @return Map&lt;Class&lt;Trait&gt;, List&lt;Trait&gt;&gt;
     */
    public static final Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> mapTraitSequence(TraitSequence seq, List<Function<Object, ? extends Trait<?, ?>>> expectedConversionMethods) {
        Map<Class<? extends Trait<?, ?>>, List<Trait<?, ?>>> map = new HashMap<>();
        if (seq == null || expectedConversionMethods == null) {
            return map;
        }

        seq.getTraits().values().stream()
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .flatMap(trait -> tryConvertTrait(trait, expectedConversionMethods).stream())
                .forEach(trait -> {
                    if (!map.containsKey(trait.getTraitType())) {
                        map.put(trait.getTraitType(), new ArrayList<>());
                    }
                    List<Trait<?, ?>> list = map.get(trait.getTraitType());
                    list.add(trait);
                    map.put(trait.getTraitType(), list);
                });

        return map;
    }

    /**
     * Try a selection of methods to convert the trait value of the given trait into a different trait type.
     * @param inTrait an arbitrary trait
     * @param conversionMethods A selection of functions to convert the traitValue of the arbitrary trait into a desired type.
     * @return An Optional container object that will contain a Trait converted using one of the supplied methods.
     * If none of the supplied methods were successful, an ASN1ObjectTrait will be returned.
     * The container will be empty if either parameter is null.
     */
    public static final Optional<Trait<?, ?>> tryConvertTrait(Trait<?, ?> inTrait, List<Function<Object, ? extends Trait<?, ?>>> conversionMethods) {
        if (inTrait == null || conversionMethods == null) {
            return Optional.empty();
        }

        Optional<Trait<?, ?>> opt = conversionMethods.stream()
                .filter(Objects::nonNull)
                .flatMap(method -> tryConvertTrait(inTrait, method).stream())
                .findFirst();

        return opt.or(() -> Optional.of(ASN1ObjectTrait.getInstance(inTrait.toASN1Primitive())));
    }

    /**
     * Try to use the supplied method to convert the trait value of the given trait into a different trait type.
     * @param inTrait an arbitrary trait
     * @param conversionMethod Function to convert the traitValue of the arbitrary trait into a desired type.
     * @return An Optional container object that will either be empty or contain the converted Trait.
     */
    public static final Optional<Trait<?, ?>> tryConvertTrait(Trait<?, ?> inTrait, Function<Object, ? extends Trait<?, ?>> conversionMethod) {
        if (inTrait == null || conversionMethod == null) {
            return Optional.empty();
        }

        Optional<Trait<?, ?>> out = Optional.empty();
        try {
            out = Optional.of(conversionMethod.apply(inTrait));
        } catch (Exception ignored) {}

        return out;
    }

    /**
     * Resize the given ASN1OctetString to the given sequenceSize.
     * If the value already meets the requested sequenceSize, or if the value is null, the value will pass through.
     * If sequenceSize is smaller than value size, octets will be truncated from MSB.
     * If sequenceSize is larger than value size, zero bytes will be prepended.
     * @param sequenceSize New size
     * @param value object to resize
     * @return ASN1OctetString of requested size.
     */
    public static ASN1OctetString resizeOctets(int sequenceSize, ASN1OctetString value) {
        return new DEROctetString(resizeOctets(sequenceSize, value == null ? null : value.getOctets()));
    }

    /**
     * Resize the given byte array to the given sequenceSize.
     * If the value already meets the requested sequenceSize, or if the value is null, the value will pass through.
     * If sequenceSize is smaller than value size, octets will be truncated from MSB.
     * If sequenceSize is larger than value size, zero bytes will be prepended.
     * @param sequenceSize New size
     * @param value object to resize
     * @return byte array of the requested size.
     */
    public static byte[] resizeOctets(int sequenceSize, byte[] value) {
        // Example: Component class value is 4 bytes long
        byte[] newValue = new byte[sequenceSize];
        Arrays.fill(newValue, (byte)0);

        if (value != null && value.length > 0) {
            // If value is <= 4 octets, prepend with zeroes
            // If value is > 4 octets, save last 4 octets
            System.arraycopy(value, Math.max(0, value.length - sequenceSize), newValue, Math.max(sequenceSize - value.length, 0), Math.min(value.length, sequenceSize));
        }
        return newValue;
    }

    /**
     * Convert the given hex string into a byte array, then resize it to the given sequenceSize.
     * If the value already meets the requested sequenceSize, the value will pass through.
     * If sequenceSize is smaller than value size, octets will be truncated from MSB.
     * If sequenceSize is larger than value size, zero bytes will be prepended.
     * @param sequenceSize New size
     * @param valueHex hex data to read
     * @return ASN1OctetString of requested size.
     */
    public static ASN1OctetString resizeOctets(int sequenceSize, @NonNull String valueHex) {
        BigInteger value = new BigInteger(valueHex, 16);
        return new DEROctetString(resizeOctets(sequenceSize, value.toByteArray()));
    }
}
